const TOKEN_KEY = "lantext_token";

const pairView = document.getElementById("pair-view");
const appView = document.getElementById("app-view");
const pairForm = document.getElementById("pair-form");
const pairStatus = document.getElementById("pair-status");
const fpLine = document.getElementById("fp-line");
const convoList = document.getElementById("convo-list");
const searchInput = document.getElementById("search");
const threadPanel = document.getElementById("thread-panel");
const emptyThread = document.getElementById("empty-thread");
const messagesEl = document.getElementById("messages");
const composeForm = document.getElementById("composer");
const composeText = document.getElementById("compose-text");
const fileInput = document.getElementById("file-input");
const pendingPreview = document.getElementById("pending-preview");
const newBtn = document.getElementById("new-btn");
const newModal = document.getElementById("new-modal");
const contactSearch = document.getElementById("contact-search");
const contactList = document.getElementById("contact-list");
const newForm = document.getElementById("new-form");
const backBtn = document.getElementById("back-btn");
const connLabel = document.getElementById("conn-label");
const newBody = document.getElementById("new-body");
const helpModal = document.getElementById("help-modal");
const convoMenu = document.getElementById("convo-menu");
const menuDelete = document.getElementById("menu-delete");
const notifyBanner = document.getElementById("notify-banner");
const threadEl = document.getElementById("thread");
const newFileInput = document.getElementById("new-file-input");
const newPendingPreview = document.getElementById("new-pending-preview");

let token = localStorage.getItem(TOKEN_KEY) || "";
let conversations = [];
let selectedId = null;
let pendingImage = null;
let pendingNewImage = null;
let selectedContact = null;
let socket = null;
let recentNoticeKeys = [];

function authHeaders(extra = {}) {
  const headers = { ...extra };
  if (token) headers.Authorization = "Bearer " + token;
  return headers;
}

async function api(path, options = {}) {
  const res = await fetch(path, {
    ...options,
    headers: authHeaders(options.headers),
    credentials: "include",
  });
  if (res.status === 401) {
    token = "";
    localStorage.removeItem(TOKEN_KEY);
    showPair();
    throw new Error("unpaired");
  }
  if (!res.ok) {
    let msg = res.statusText;
    try { msg = (await res.json()).error || msg; } catch (_) {}
    throw new Error(msg);
  }
  const ct = res.headers.get("content-type") || "";
  if (ct.includes("application/json")) return res.json();
  return res;
}

function showPair() {
  pairView.hidden = false;
  appView.hidden = true;
  closeNewModal();
  closeHelp();
}

function showApp() {
  pairView.hidden = true;
  appView.hidden = false;
  closeNewModal();
  closeHelp();
}

function openNewModal() {
  newModal.hidden = false;
  contactSearch.value = "";
  contactList.innerHTML = "";
  document.getElementById("new-body").value = "";
  selectedContact = null;
  pendingNewImage = null;
  newPendingPreview.hidden = true;
  newPendingPreview.innerHTML = "";
  contactSearch.focus();
  loadContacts("");
}

function closeNewModal() {
  newModal.hidden = true;
}

function openHelp() { helpModal.hidden = false; }
function closeHelp() { helpModal.hidden = true; }

function isTypingTarget(el) {
  const tag = (el?.tagName || "").toLowerCase();
  return tag === "textarea" || tag === "input" || el?.isContentEditable;
}

function moveConversation(delta) {
  const ids = [...convoList.querySelectorAll(".convo")].map((row) => row.dataset.id).filter(Boolean);
  if (!ids.length) return;
  const idx = ids.indexOf(selectedId);
  const nextIdx = idx < 0 ? 0 : Math.min(ids.length - 1, Math.max(0, idx + delta));
  const nextId = ids[nextIdx];
  if (nextId) openThread(nextId, true).then(() => composeText.focus());
}

async function boot() {
  try {
    const meta = await fetch("/api/v1/meta", { credentials: "include" }).then((r) => r.json());
    if (meta.fingerprint) {
      fpLine.textContent = "Certificate fingerprint: " + meta.fingerprint.match(/.{1,4}/g).join(" ");
    }
    if (token || meta.paired) {
      await api("/api/v1/session");
      showApp();
      await loadInbox();
      connectEvents();
      setupNotifications();
      return;
    }
  } catch (_) {}
  showPair();
}

pairForm.addEventListener("submit", async (e) => {
  e.preventDefault();
  pairStatus.textContent = "Waiting for approval on your phone…";
  try {
    const pin = document.getElementById("pin").value.trim();
    const started = await fetch("/api/v1/pair", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ pin, clientName: navigator.userAgent.slice(0, 60) }),
    });
    if (!started.ok) {
      pairStatus.textContent = "PIN rejected. Check the app and try again.";
      return;
    }
    const { requestId } = await started.json();
    const tokenFound = await pollPair(requestId);
    if (!tokenFound) {
      pairStatus.textContent = "Pairing was denied or expired.";
      return;
    }
    token = tokenFound;
    localStorage.setItem(TOKEN_KEY, token);
    showApp();
    await loadInbox();
    connectEvents();
    setupNotifications();
  } catch (err) {
    pairStatus.textContent = err.message;
  }
});

async function pollPair(requestId) {
  for (let i = 0; i < 60; i++) {
    const res = await fetch("/api/v1/pair/" + requestId, { credentials: "include" });
    if (res.ok) {
      const data = await res.json();
      if (data.status === "approved") return data.token;
      if (data.status === "denied") return null;
    } else if (res.status === 404) {
      await sleep(1000);
      continue;
    }
    await sleep(1000);
  }
  return null;
}

function sleep(ms) { return new Promise((r) => setTimeout(r, ms)); }

async function loadInbox(opts = {}) {
  const previous = conversations;
  conversations = await api("/api/v1/conversations");
  if (opts.notify) notifyFromInbox(previous, conversations);
  updateTitle();
  renderConversations(searchInput.value);
  if (selectedId) await openThread(selectedId, false);
}

function updateTitle() {
  const unread = conversations.reduce((n, c) => n + (c.unread || 0), 0);
  document.title = unread > 0 ? `(${unread}) LanText` : "LanText";
}

function notifyFromInbox(prev, next) {
  const prevMap = new Map((prev || []).map((c) => [c.id, c]));
  for (const c of next) {
    const old = prevMap.get(c.id);
    const arrived = !old ? (c.unread || 0) > 0 : (c.unread || 0) > (old.unread || 0) && c.timestamp >= (old.timestamp || 0);
    if (arrived) {
      showDesktopNotice({
        title: c.displayName || c.address,
        body: c.snippet || "New message",
        threadId: c.id,
      });
    }
  }
}

function setupNotifications() {
  if (!("Notification" in window)) return;
  if (Notification.permission === "default") {
    notifyBanner.hidden = false;
  } else {
    notifyBanner.hidden = true;
  }
}

notifyBanner.addEventListener("click", async () => {
  if (!("Notification" in window)) return;
  await Notification.requestPermission();
  setupNotifications();
});

function showDesktopNotice({ title, body, threadId }) {
  if (!("Notification" in window) || Notification.permission !== "granted") return;
  const looking = document.visibilityState === "visible" && document.hasFocus() && selectedId && selectedId === threadId;
  if (looking) return;
  const key = `${title}|${body}`;
  if (recentNoticeKeys.includes(key)) return;
  recentNoticeKeys.push(key);
  if (recentNoticeKeys.length > 30) recentNoticeKeys.shift();
  try {
    const note = new Notification(title || "LanText", {
      body: body || "New message",
      tag: threadId || title || "lantext",
      silent: false,
    });
    note.onclick = () => {
      window.focus();
      if (threadId) openThread(threadId, true);
      note.close();
    };
  } catch (_) {}
}

function renderConversations(filter) {
  const q = (filter || "").trim().toLowerCase();
  convoList.innerHTML = "";
  const list = q
    ? conversations.filter((c) => (c.displayName + " " + c.address + " " + c.snippet).toLowerCase().includes(q))
    : conversations;
  if (!list.length) {
    convoList.innerHTML = "<p class='muted' style='padding:16px'>No conversations</p>";
    return;
  }
  for (const c of list) {
    const el = document.createElement("div");
    el.className = "convo" + (c.id === selectedId ? " active" : "") + (c.unread ? " unread" : "");
    el.dataset.id = c.id;
    el.innerHTML = `
      ${avatarHtml(c.displayName, c.avatarColor, c.photoUrl)}
      <div>
        <div class="name">${escapeHtml(c.displayName)}${c.unread ? '<span class="dot"></span>' : ""}</div>
        <div class="snippet">${escapeHtml(c.snippet || "")}</div>
      </div>
      <div class="when">${formatTime(c.timestamp)}</div>`;
    el.addEventListener("click", () => openThread(c.id, true));
    el.addEventListener("contextmenu", (e) => {
      e.preventDefault();
      showConvoMenu(c.id, e.clientX, e.clientY);
    });
    let pressTimer = null;
    el.addEventListener("pointerdown", (e) => {
      if (e.pointerType === "mouse") return;
      pressTimer = setTimeout(() => showConvoMenu(c.id, e.clientX, e.clientY), 550);
    });
    ["pointerup", "pointercancel", "pointerleave"].forEach((name) => {
      el.addEventListener(name, () => { if (pressTimer) { clearTimeout(pressTimer); pressTimer = null; } });
    });
    convoList.appendChild(el);
  }
}

searchInput.addEventListener("input", () => {
  const q = searchInput.value.trim();
  if (q.length >= 2) {
    api("/api/v1/search?q=" + encodeURIComponent(q)).then((hits) => {
      const seen = new Set();
      const list = [];
      for (const hit of hits) {
        if (!seen.has(hit.conversation.id)) {
          seen.add(hit.conversation.id);
          list.push(hit.conversation);
        }
      }
      conversations = mergeById(conversations, list);
      renderConversations(q);
    }).catch(() => renderConversations(q));
  } else {
    renderConversations(q);
  }
});

async function openThread(id, mark) {
  selectedId = id;
  const convo = conversations.find((c) => c.id === id);
  if (!convo) return;
  appView.classList.add("thread-open");
  emptyThread.hidden = true;
  threadPanel.hidden = false;
  document.getElementById("thread-name").textContent = convo.displayName;
  document.getElementById("thread-number").textContent = convo.address;
  document.getElementById("thread-avatar").outerHTML = avatarHtml(convo.displayName, convo.avatarColor, convo.photoUrl).replace("class=\"avatar\"", "class=\"avatar\" id=\"thread-avatar\"");
  renderConversations(searchInput.value);
  const msgs = await api(`/api/v1/conversations/${id}/messages?limit=50`);
  renderMessages(msgs.slice().reverse());
  if (mark) api(`/api/v1/conversations/${id}/read`, { method: "POST" }).catch(() => {});
  composeText.focus();
}

function renderMessages(msgs) {
  messagesEl.innerHTML = "";
  let lastDay = "";
  for (const m of msgs) {
    const day = new Date(m.timestamp).toDateString();
    if (day !== lastDay) {
      lastDay = day;
      const d = document.createElement("div");
      d.className = "day";
      d.textContent = formatDay(m.timestamp);
      messagesEl.appendChild(d);
    }
    const b = document.createElement("div");
    b.className = "bubble " + (m.incoming ? "in" : "out");
    let html = "";
    for (const att of m.attachments || []) {
      if ((att.mimeType || "").startsWith("image/")) {
        html += `<img alt="" src="${att.url}" />`;
      }
    }
    html += linkify(m.body || "");
    html += `<div class="meta">${formatTime(m.timestamp)}</div>`;
    b.innerHTML = html;
    messagesEl.appendChild(b);
  }
  messagesEl.scrollTop = messagesEl.scrollHeight;
}

composeText.addEventListener("input", () => {
  composeText.style.height = "auto";
  composeText.style.height = Math.min(composeText.scrollHeight, 160) + "px";
});

function imageFromClipboard(e) {
  const items = [...(e.clipboardData?.items || []), ...(e.clipboardData?.files || [])];
  const item = items.find((i) => (i.type || "").startsWith("image/"));
  if (!item) return null;
  return typeof item.getAsFile === "function" ? item.getAsFile() : item;
}

function imageFromDrop(e) {
  const file = [...(e.dataTransfer?.files || [])].find((f) => (f.type || "").startsWith("image/"));
  return file || null;
}

composeText.addEventListener("paste", (e) => {
  const file = imageFromClipboard(e);
  if (!file) return;
  e.preventDefault();
  setPendingImage(file);
});

document.addEventListener("paste", (e) => {
  if (appView.hidden) return;
  const file = imageFromClipboard(e);
  if (!file) return;
  if (!newModal.hidden) {
    e.preventDefault();
    setPendingNewImage(file);
    return;
  }
  if (threadPanel.hidden) return;
  if (isTypingTarget(e.target) && e.target !== composeText) return;
  e.preventDefault();
  setPendingImage(file);
});

["dragenter", "dragover"].forEach((name) => {
  threadEl.addEventListener(name, (e) => {
    if (![...e.dataTransfer.types].includes("Files")) return;
    e.preventDefault();
    threadEl.classList.add("drop-target");
  });
  newModal.addEventListener(name, (e) => {
    if (![...e.dataTransfer.types].includes("Files")) return;
    e.preventDefault();
  });
});
threadEl.addEventListener("dragleave", () => threadEl.classList.remove("drop-target"));
threadEl.addEventListener("drop", (e) => {
  e.preventDefault();
  threadEl.classList.remove("drop-target");
  const file = imageFromDrop(e);
  if (file) setPendingImage(file);
});
newModal.addEventListener("drop", (e) => {
  e.preventDefault();
  const file = imageFromDrop(e);
  if (file) setPendingNewImage(file);
});

document.getElementById("attach-btn").addEventListener("click", () => fileInput.click());
fileInput.addEventListener("change", () => {
  if (fileInput.files[0]) setPendingImage(fileInput.files[0]);
});
document.getElementById("new-attach-btn").addEventListener("click", () => newFileInput.click());
newFileInput.addEventListener("change", () => {
  if (newFileInput.files[0]) setPendingNewImage(newFileInput.files[0]);
});

function renderPreview(container, file, onClear) {
  container.hidden = false;
  container.innerHTML = `<img alt="attachment" /><span class="muted">${escapeHtml(file.name || "image")}</span><button type="button">Remove</button>`;
  container.querySelector("img").src = URL.createObjectURL(file);
  container.querySelector("button").onclick = onClear;
}

function setPendingImage(file) {
  pendingImage = file;
  renderPreview(pendingPreview, file, () => {
    pendingImage = null;
    pendingPreview.hidden = true;
    pendingPreview.innerHTML = "";
    fileInput.value = "";
  });
}

function setPendingNewImage(file) {
  pendingNewImage = file;
  renderPreview(newPendingPreview, file, () => {
    pendingNewImage = null;
    newPendingPreview.hidden = true;
    newPendingPreview.innerHTML = "";
    newFileInput.value = "";
  });
}

async function fileToBase64(file) {
  const buf = await file.arrayBuffer();
  const bytes = new Uint8Array(buf);
  let binary = "";
  const chunk = 0x8000;
  for (let i = 0; i < bytes.length; i += chunk) {
    binary += String.fromCharCode(...bytes.subarray(i, i + chunk));
  }
  return btoa(binary);
}

function wantsSend(e) {
  return e.key === "Enter" && !e.shiftKey && !e.isComposing && e.keyCode !== 229;
}

composeText.addEventListener("keydown", (e) => {
  if (wantsSend(e)) {
    e.preventDefault();
    composeForm.requestSubmit();
  }
});

newBody.addEventListener("keydown", (e) => {
  if (wantsSend(e)) {
    e.preventDefault();
    newForm.requestSubmit();
  }
});

let sending = false;
composeForm.addEventListener("submit", async (e) => {
  e.preventDefault();
  if (!selectedId || sending) return;
  const body = composeText.value.trim();
  if (!body && !pendingImage) return;
  const convo = conversations.find((c) => c.id === selectedId);
  sending = true;
  const sendBtn = document.getElementById("send-btn");
  const previousLabel = sendBtn.textContent;
  sendBtn.textContent = pendingImage ? "Sending picture…" : "Sending…";
  try {
    const dest = (convo?.recipients || []).filter(Boolean);
    const recipients = dest.length ? dest : [convo?.address].filter(Boolean);
    const payload = { body, recipients };
    if (pendingImage) {
      payload.imageBase64 = await fileToBase64(pendingImage);
      payload.imageMime = pendingImage.type || "image/jpeg";
    }
    await api(`/api/v1/conversations/${selectedId}/messages`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    });
    pendingImage = null;
    pendingPreview.hidden = true;
    pendingPreview.innerHTML = "";
    composeText.value = "";
    composeText.style.height = "auto";
    await loadInbox();
    composeText.focus();
  } catch (err) {
    alert(err.message || "Could not send");
  } finally {
    sendBtn.textContent = previousLabel;
    sending = false;
  }
});

newBtn.addEventListener("click", openNewModal);
document.getElementById("close-new").addEventListener("click", closeNewModal);
document.getElementById("close-help").addEventListener("click", closeHelp);
newModal.addEventListener("click", (e) => {
  if (e.target === newModal) closeNewModal();
});
helpModal.addEventListener("click", (e) => {
  if (e.target === helpModal) closeHelp();
});
document.addEventListener("keydown", (e) => {
  const mod = e.ctrlKey || e.metaKey;
  if (e.key === "Escape") {
    if (!helpModal.hidden) { closeHelp(); e.preventDefault(); return; }
    if (!convoMenu.hidden) { hideConvoMenu(); e.preventDefault(); return; }
    if (!newModal.hidden) { closeNewModal(); e.preventDefault(); return; }
    if (document.activeElement === composeText) { composeText.blur(); return; }
    if (document.activeElement === searchInput) { searchInput.blur(); return; }
    return;
  }
  if (appView.hidden) return;
  if (mod && e.key.toLowerCase() === "n") {
    e.preventDefault();
    openNewModal();
    return;
  }
  if (mod && e.key.toLowerCase() === "k") {
    e.preventDefault();
    closeNewModal();
    searchInput.focus();
    searchInput.select();
    return;
  }
  if (e.key === "?" && !isTypingTarget(e.target) && !mod) {
    e.preventDefault();
    if (helpModal.hidden) openHelp(); else closeHelp();
    return;
  }
  if (e.key === "/" && !isTypingTarget(e.target) && !mod) {
    e.preventDefault();
    searchInput.focus();
    return;
  }
  if (!isTypingTarget(e.target) && !mod && !e.altKey) {
    if (e.key === "ArrowDown" || e.key === "j") {
      e.preventDefault();
      moveConversation(1);
    } else if (e.key === "ArrowUp" || e.key === "k") {
      e.preventDefault();
      moveConversation(-1);
    }
  }
});
backBtn.addEventListener("click", () => {
  appView.classList.remove("thread-open");
  selectedId = null;
});

contactSearch.addEventListener("input", () => loadContacts(contactSearch.value));

async function loadContacts(q) {
  const list = await api("/api/v1/contacts?q=" + encodeURIComponent(q || ""));
  contactList.innerHTML = "";
  for (const c of list) {
    const el = document.createElement("div");
    el.className = "contact";
    el.innerHTML = `${avatarHtml(c.name, c.avatarColor, c.photoUrl)}<div><strong>${escapeHtml(c.name)}</strong><div class="muted">${escapeHtml(c.number)}</div></div>`;
    el.addEventListener("click", () => {
      selectedContact = c;
      [...contactList.children].forEach((n) => n.classList.remove("selected"));
      el.classList.add("selected");
    });
    contactList.appendChild(el);
  }
  if (!list.length && looksLikeNumber(q)) {
    selectedContact = { name: q, number: q, recipients: [q] };
  }
}

newForm.addEventListener("submit", async (e) => {
  e.preventDefault();
  const body = document.getElementById("new-body").value.trim();
  const number = selectedContact?.number || contactSearch.value.trim();
  if (!number || (!body && !pendingNewImage)) return;
  try {
    const payload = { recipients: [number], body };
    if (pendingNewImage) {
      payload.imageBase64 = await fileToBase64(pendingNewImage);
      payload.imageMime = pendingNewImage.type || "image/jpeg";
    }
    await api("/api/v1/conversations", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    });
    pendingNewImage = null;
    closeNewModal();
    document.getElementById("new-body").value = "";
    await loadInbox();
  } catch (err) {
    alert(err.message || "Could not send");
  }
});

function connectEvents() {
  if (socket) try { socket.close(); } catch (_) {}
  const proto = location.protocol === "https:" ? "wss:" : "ws:";
  const q = token ? ("?token=" + encodeURIComponent(token)) : "";
  socket = new WebSocket(proto + "//" + location.host + "/api/v1/events" + q);
  socket.onopen = () => { connLabel.textContent = "Live"; };
  socket.onclose = () => {
    connLabel.textContent = "Reconnecting…";
    setTimeout(connectEvents, 3000);
  };
  socket.onmessage = (ev) => {
    let payload = {};
    try { payload = JSON.parse(ev.data); } catch (_) {}
    if (payload.type === "incoming") {
      const thread = conversations.find((c) =>
        c.address === payload.address || (c.recipients || []).includes(payload.address),
      );
      showDesktopNotice({
        title: payload.displayName || payload.address || "New message",
        body: payload.body || "New message",
        threadId: thread?.id,
      });
    }
    loadInbox({ notify: payload.type !== "incoming" }).catch(() => {});
  };
}

function avatarHtml(name, color, photoUrl) {
  const initial = (name || "?").trim().charAt(0).toUpperCase();
  const img = photoUrl ? `<img alt="" src="${photoUrl}" />` : escapeHtml(initial);
  return `<div class="avatar" style="background:${color || "#156b57"}">${img}</div>`;
}

function escapeHtml(s) {
  return String(s).replace(/[&<>"']/g, (c) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));
}

function linkify(text) {
  const escaped = escapeHtml(text);
  return escaped.replace(/((?:https?:\/\/|www\.)[^\s<]+)/gi, (url) => {
    const hrefRaw = url.startsWith("http") ? url : "https://" + url;
    const href = hrefRaw.replace(/&amp;/g, "&");
    if (!/^https?:\/\//i.test(href)) return url;
    return `<a href="${href}" target="_blank" rel="noopener noreferrer">${url}</a>`;
  });
}

let menuThreadId = null;
function showConvoMenu(id, x, y) {
  menuThreadId = id;
  convoMenu.hidden = false;
  const pad = 8;
  const w = convoMenu.offsetWidth || 200;
  const h = convoMenu.offsetHeight || 48;
  convoMenu.style.left = Math.min(x, window.innerWidth - w - pad) + "px";
  convoMenu.style.top = Math.min(y, window.innerHeight - h - pad) + "px";
}
function hideConvoMenu() {
  convoMenu.hidden = true;
  menuThreadId = null;
}
document.addEventListener("click", () => hideConvoMenu());
window.addEventListener("blur", hideConvoMenu);
menuDelete.addEventListener("click", async (e) => {
  e.stopPropagation();
  const id = menuThreadId;
  hideConvoMenu();
  const convo = conversations.find((c) => c.id === id);
  const name = convo?.displayName || "this conversation";
  if (!id || !confirm(`Delete the conversation with ${name}? This removes it from the phone too.`)) return;
  try {
    await api("/api/v1/conversations/" + encodeURIComponent(id), { method: "DELETE" });
    if (selectedId === id) {
      selectedId = null;
      threadPanel.hidden = true;
      emptyThread.hidden = false;
      appView.classList.remove("thread-open");
    }
    await loadInbox();
  } catch (err) {
    alert(err.message || "Could not delete that conversation.");
  }
});

function formatTime(ts) {
  if (!ts) return "";
  const d = new Date(ts);
  const now = new Date();
  if (d.toDateString() === now.toDateString()) {
    return d.toLocaleTimeString([], { hour: "numeric", minute: "2-digit" });
  }
  return d.toLocaleDateString([], { month: "short", day: "numeric" });
}

function formatDay(ts) {
  return new Date(ts).toLocaleDateString([], { weekday: "short", month: "short", day: "numeric" });
}

function looksLikeNumber(q) {
  return /^[+0-9()\-\s]{5,}$/.test(q || "");
}

function mergeById(base, extra) {
  const map = new Map(base.map((c) => [c.id, c]));
  for (const c of extra) map.set(c.id, c);
  return [...map.values()];
}

boot();
