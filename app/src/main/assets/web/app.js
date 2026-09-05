const TOKEN_KEY = "lantext_token";
const THEME_PALETTE_KEY = "lantext_palette";
const THEME_MODE_KEY = "lantext_mode";
const INBOX_KEY = "lantext_inbox_cache";

const PALETTES = [
  { id: "fern", name: "Fern", swatch: ["#156b57", "#1f8a70", "#f3f6f4"] },
  { id: "ocean", name: "Ocean", swatch: ["#0b6e99", "#38bdf8", "#f0f6fa"] },
  { id: "dusk", name: "Dusk", swatch: ["#5b3cc4", "#a78bfa", "#f6f3fb"] },
  { id: "ember", name: "Ember", swatch: ["#c2410c", "#fb923c", "#fbf6f1"] },
  { id: "slate", name: "Slate", swatch: ["#334155", "#94a3b8", "#f4f5f7"] },
  { id: "sakura", name: "Sakura", swatch: ["#be185d", "#f472b6", "#fdf6f8"] },
  { id: "meadow", name: "Meadow", swatch: ["#3f7d20", "#86efac", "#f4faf2"] },
  { id: "nord", name: "Nord", swatch: ["#5e81ac", "#88c0d0", "#eceff4"] },
  { id: "contrast", name: "Contrast", swatch: ["#000000", "#ffffff", "#e6e6e6"] },
];

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
const themeModal = document.getElementById("theme-modal");
const contactModal = document.getElementById("contact-modal");
const saveContactBtn = document.getElementById("save-contact-btn");
const notifyBanner = document.getElementById("notify-banner");
const threadEl = document.getElementById("thread");
const newFileInput = document.getElementById("new-file-input");
const newPendingPreview = document.getElementById("new-pending-preview");
const disconnectOverlay = document.getElementById("disconnect-overlay");
const disconnectStatus = document.getElementById("disconnect-status");
const reconnectBtn = document.getElementById("reconnect-btn");
const emojiPanel = document.getElementById("emoji-panel");
const emojiBtn = document.getElementById("emoji-btn");
const pdfBtn = document.getElementById("pdf-btn");
const gifBtn = document.getElementById("gif-btn");
const gifPanel = document.getElementById("gif-panel");
const gifSearch = document.getElementById("gif-search");
const gifGrid = document.getElementById("gif-grid");
const gifStatus = document.getElementById("gif-status");
const voiceBtn = document.getElementById("voice-btn");
const EMOJI = [
  { glyph: "😂", names: ["lol", "joy", "laugh"] },
  { glyph: "🤣", names: ["rofl", "rolling"] },
  { glyph: "😀", names: ["grinning", "grin"] },
  { glyph: "😁", names: ["beaming", "grin2"] },
  { glyph: "😊", names: ["blush", "smile"] },
  { glyph: "😇", names: ["innocent", "halo"] },
  { glyph: "🙂", names: ["slight_smile"] },
  { glyph: "😉", names: ["wink"] },
  { glyph: "😍", names: ["heart_eyes", "love"] },
  { glyph: "😘", names: ["kiss"] },
  { glyph: "😜", names: ["stuck_out_tongue", "winky"] },
  { glyph: "🤔", names: ["thinking", "think"] },
  { glyph: "🙄", names: ["eyeroll", "rolling_eyes"] },
  { glyph: "😏", names: ["smirk"] },
  { glyph: "😢", names: ["cry", "sad"] },
  { glyph: "😭", names: ["sob", "bawling"] },
  { glyph: "😤", names: ["huff", "triumph"] },
  { glyph: "😡", names: ["rage", "angry"] },
  { glyph: "🤯", names: ["exploding_head", "mindblown"] },
  { glyph: "😱", names: ["scream", "shocked"] },
  { glyph: "😴", names: ["sleep", "zzz"] },
  { glyph: "🤗", names: ["hug", "hugging"] },
  { glyph: "🙌", names: ["raised_hands", "hooray"] },
  { glyph: "👍", names: ["thumbsup", "yes", "+1"] },
  { glyph: "👎", names: ["thumbsdown", "no", "-1"] },
  { glyph: "👏", names: ["clap", "applause"] },
  { glyph: "🙏", names: ["pray", "thanks", "please"] },
  { glyph: "💪", names: ["muscle", "flex"] },
  { glyph: "🔥", names: ["fire", "lit"] },
  { glyph: "❤️", names: ["heart", "red_heart", "love_heart"] },
  { glyph: "💯", names: ["100", "hundred"] },
  { glyph: "✨", names: ["sparkles", "stars"] },
  { glyph: "🎉", names: ["tada", "party", "celebrate"] },
  { glyph: "✅", names: ["check", "done", "white_check_mark"] },
  { glyph: "❌", names: ["x", "cross"] },
  { glyph: "⭐", names: ["star"] },
  { glyph: "👋", names: ["wave", "hello", "hi"] },
  { glyph: "🤝", names: ["handshake", "deal"] },
  { glyph: "👀", names: ["eyes", "look"] },
  { glyph: "💬", names: ["speech", "comment"] },
  { glyph: "📱", names: ["iphone", "phone"] },
  { glyph: "💻", names: ["computer", "laptop"] },
  { glyph: "🏠", names: ["house", "home"] },
  { glyph: "☀️", names: ["sun", "sunny"] },
  { glyph: "🌙", names: ["moon"] },
  { glyph: "🌧️", names: ["rain", "cloud_rain"] },
  { glyph: "☕", names: ["coffee"] },
  { glyph: "🍕", names: ["pizza"] },
  { glyph: "🍰", names: ["cake"] },
  { glyph: "🎵", names: ["music"] },
  { glyph: "📸", names: ["camera"] },
  { glyph: "💡", names: ["bulb", "idea"] },
  { glyph: "🔒", names: ["lock"] },
  { glyph: "📍", names: ["pin", "location"] },
  { glyph: "🚗", names: ["car"] },
  { glyph: "✈️", names: ["airplane", "plane"] },
];

let token = localStorage.getItem(TOKEN_KEY) || "";
let conversations = [];
let selectedId = null;
let pendingImage = null;
let pendingNewImage = null;
let pendingGifUrl = null;
let pendingNewGifUrl = null;
let voiceRec = null;
let selectedContact = null;
let selectedExistingContact = null;
let contactSaveMode = "new";
let contactModalNumber = "";
let socket = null;
let recentNoticeKeys = [];
let eventGen = 0;
let reconnectTimer = null;
let reconnectAttempt = 0;
let reconnecting = false;
let wantEvents = false;
let phoneReachable = true;
let overlayTimer = null;
const OVERLAY_GRACE_MS = 20000;

function currentPalette() {
  return localStorage.getItem(THEME_PALETTE_KEY) || "fern";
}
function currentMode() {
  return localStorage.getItem(THEME_MODE_KEY) || "auto";
}
function resolvedMode(mode) {
  if (mode === "light" || mode === "dark") return mode;
  return window.matchMedia("(prefers-color-scheme: dark)").matches ? "dark" : "light";
}
function applyTheme() {
  const palette = currentPalette();
  const mode = currentMode();
  const root = document.documentElement;
  root.dataset.palette = palette;
  root.dataset.mode = mode;
  root.dataset.resolved = resolvedMode(mode);
  syncThemeControls();
  if (appearanceReady) syncAppearance();
}
let appearanceReady = false;
function syncAppearance() {
  fetch("/api/v1/appearance", {
    method: "POST",
    headers: authHeaders({ "Content-Type": "application/json" }),
    credentials: "include",
    body: JSON.stringify({ palette: currentPalette(), mode: currentMode() }),
  }).catch(() => {});
}
function syncThemeControls() {
  const palette = currentPalette();
  const mode = currentMode();
  document.querySelectorAll("#theme-mode button").forEach((btn) => {
    btn.classList.toggle("active", btn.dataset.mode === mode);
  });
  document.querySelectorAll(".theme-choice").forEach((btn) => {
    btn.classList.toggle("active", btn.dataset.palette === palette);
  });
}
function renderThemeGrid() {
  const grid = document.getElementById("theme-grid");
  if (!grid || grid.childElementCount) return;
  for (const p of PALETTES) {
    const btn = document.createElement("button");
    btn.type = "button";
    btn.className = "theme-choice";
    btn.dataset.palette = p.id;
    btn.innerHTML = `<div class="theme-swatch">${p.swatch.map((c) => `<span style="background:${c}"></span>`).join("")}</div><div class="theme-choice-name">${escapeHtml(p.name)}</div>`;
    btn.addEventListener("click", () => {
      localStorage.setItem(THEME_PALETTE_KEY, p.id);
      applyTheme();
    });
    grid.appendChild(btn);
  }
}
function openTheme() {
  renderThemeGrid();
  themeModal.hidden = false;
  syncThemeControls();
}
function closeTheme() { themeModal.hidden = true; }

applyTheme();
window.matchMedia("(prefers-color-scheme: dark)").addEventListener("change", () => {
  if (currentMode() === "auto") applyTheme();
});
document.getElementById("theme-mode").addEventListener("click", (e) => {
  const btn = e.target.closest("button[data-mode]");
  if (!btn) return;
  localStorage.setItem(THEME_MODE_KEY, btn.dataset.mode);
  applyTheme();
});
document.getElementById("refresh-btn").addEventListener("click", (e) => {
  refreshInbox(e.shiftKey);
});
document.getElementById("theme-btn").addEventListener("click", openTheme);
document.getElementById("pair-theme-btn").addEventListener("click", openTheme);
document.getElementById("close-theme").addEventListener("click", closeTheme);
themeModal.addEventListener("click", (e) => {
  if (e.target === themeModal) closeTheme();
});

function authHeaders(extra = {}) {
  const headers = { ...extra };
  if (token) headers.Authorization = "Bearer " + token;
  return headers;
}

async function api(path, options = {}) {
  let res;
  try {
    res = await fetch(path, {
      ...options,
      headers: authHeaders(options.headers),
      credentials: "include",
      cache: "no-store",
    });
  } catch (_) {
    noteUnreachable("Can't reach the phone.");
    scheduleReconnect();
    throw new Error("Can't reach the phone");
  }
  if (res.status === 401) {
    token = "";
    localStorage.removeItem(TOKEN_KEY);
    showPair();
    markLive();
    throw new Error("unpaired");
  }
  if (res.status === 502 || res.status === 503 || res.status === 504) {
    noteUnreachable("Can't reach the phone.");
    scheduleReconnect();
    throw new Error("Can't reach the phone");
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

function registerServiceWorker() {
  if (!("serviceWorker" in navigator)) return;
  navigator.serviceWorker.register("/sw.js", { updateViaCache: "none" }).catch(() => {});
}

function saveInboxSnapshot() {
  try {
    sessionStorage.setItem(INBOX_KEY, JSON.stringify({ selectedId, conversations }));
  } catch (_) {}
}

function restoreInboxSnapshot() {
  try {
    const raw = sessionStorage.getItem(INBOX_KEY);
    if (!raw) return;
    const data = JSON.parse(raw);
    if (Array.isArray(data.conversations)) conversations = data.conversations;
    if (data.selectedId) selectedId = data.selectedId;
  } catch (_) {}
}

function showDisconnectOverlay(detail) {
  document.body.classList.add("disconnected");
  disconnectOverlay.hidden = false;
  connLabel.textContent = reconnecting ? "Reconnecting…" : "Offline";
  if (detail) disconnectStatus.textContent = detail;
  else if (!disconnectStatus.textContent) {
    disconnectStatus.textContent = "The phone did not answer. It may be off the Wi-Fi, or this computer just woke.";
  }
}

function noteUnreachable(detail, { showNow = false } = {}) {
  if (phoneReachable) {
    phoneReachable = false;
    connLabel.textContent = "Reconnecting…";
  }
  if (showNow || disconnectOverlay.hidden === false) {
    showDisconnectOverlay(detail);
    return;
  }
  if (overlayTimer == null) {
    overlayTimer = setTimeout(() => {
      overlayTimer = null;
      if (!phoneReachable) showDisconnectOverlay(detail);
    }, OVERLAY_GRACE_MS);
  }
}

function markLive() {
  phoneReachable = true;
  reconnectAttempt = 0;
  reconnecting = false;
  if (reconnectTimer != null) {
    clearTimeout(reconnectTimer);
    reconnectTimer = null;
  }
  if (overlayTimer != null) {
    clearTimeout(overlayTimer);
    overlayTimer = null;
  }
  document.body.classList.remove("disconnected");
  disconnectOverlay.hidden = true;
  disconnectStatus.textContent = "";
  reconnectBtn.disabled = false;
  if (wantEvents && socket && socket.readyState === WebSocket.OPEN) {
    connLabel.textContent = "Live";
  } else {
    connLabel.textContent = "Connected";
  }
}

function scheduleReconnect() {
  if (reconnecting || reconnectTimer != null) return;
  const overlayUp = disconnectOverlay.hidden === false;
  const delay = overlayUp
    ? Math.min(20000, 1500 * Math.pow(1.4, reconnectAttempt++))
    : 2000;
  reconnectTimer = setTimeout(() => {
    reconnectTimer = null;
    if (disconnectOverlay.hidden === false) reconnectToPhone();
    else if (wantEvents) connectEvents();
    else reconnectToPhone();
  }, delay);
}

async function enterApp() {
  showApp();
  await api("/api/v1/session");
  await loadInbox();
  wantEvents = true;
  connectEvents();
  appearanceReady = true;
  syncAppearance();
  setupNotifications();
  markLive();
}

async function reconnectToPhone() {
  if (reconnecting) return;
  reconnecting = true;
  reconnectBtn.disabled = true;
  connLabel.textContent = "Reconnecting…";
  disconnectStatus.textContent = "Trying again…";
  try {
    const res = await fetch("/api/v1/meta", { credentials: "include", cache: "no-store" });
    if (!res.ok) throw new Error("unreachable");
    const meta = await res.json();
    if (token || meta.paired) {
      await enterApp();
      return;
    }
    showPair();
    markLive();
  } catch (_) {
    reconnecting = false;
    reconnectBtn.disabled = false;
    noteUnreachable("Still can't reach the phone.", { showNow: true });
    scheduleReconnect();
  }
}

function showPair() {
  wantEvents = false;
  eventGen += 1;
  if (socket) {
    try { socket.close(); } catch (_) {}
    socket = null;
  }
  pairView.hidden = false;
  appView.hidden = true;
  closeNewModal();
  closeHelp();
  closeTheme();
  closeContactModal();
}

function showApp() {
  pairView.hidden = true;
  appView.hidden = false;
  closeNewModal();
  closeHelp();
  closeTheme();
  closeContactModal();
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
  hideEmojiPanel();
  if (gifPanel) gifPanel.hidden = true;
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
  registerServiceWorker();
  restoreInboxSnapshot();
  if (token) {
    showApp();
    renderConversations(searchInput.value);
  } else {
    showPair();
  }
  setupNotifications();
  try {
    const res = await fetch("/api/v1/meta", {
      credentials: "include",
      cache: "no-store",
      signal: AbortSignal.timeout(8000),
    });
    if (!res.ok) throw new Error("unreachable");
    const meta = await res.json();
    if (meta.fingerprint) {
      fpLine.textContent = "Certificate fingerprint: " + meta.fingerprint.match(/.{1,4}/g).join(" ");
    }
    if (token || meta.paired) {
      await enterApp();
      return;
    }
    showPair();
    markLive();
    return;
  } catch (_) {
    if (token) {
      showApp();
      renderConversations(searchInput.value);
    } else {
      showPair();
    }
    noteUnreachable("Can't reach the phone.", { showNow: true });
    scheduleReconnect();
  }
}

async function refreshInbox(fullReload) {
  if (fullReload) {
    location.reload();
    return;
  }
  if (disconnectOverlay && disconnectOverlay.hidden === false) {
    await reconnectToPhone();
    return;
  }
  try {
    connLabel.textContent = "Refreshing…";
    await loadInbox();
    if (wantEvents) connectEvents();
    markLive();
  } catch (_) {
    noteUnreachable("Can't reach the phone.");
    scheduleReconnect();
  }
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
    await enterApp();
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
  saveInboxSnapshot();
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

async function setupNotifications() {
  if (!("Notification" in window)) return;
  if (Notification.permission === "default") {
    try {
      await Notification.requestPermission();
    } catch (_) {}
  }
  notifyBanner.hidden = Notification.permission !== "default";
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

let searchMatchIds = null;
function renderConversations(filter) {
  const q = (filter || "").trim().toLowerCase();
  convoList.innerHTML = "";
  const list = !q
    ? conversations
    : conversations.filter((c) => {
        if (searchMatchIds && searchMatchIds.has(c.id)) return true;
        return (c.displayName + " " + c.address + " " + c.snippet).toLowerCase().includes(q);
      });
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
    convoList.appendChild(el);
  }
}

searchInput.addEventListener("input", () => {
  const q = searchInput.value.trim();
  if (q.length >= 2) {
    api("/api/v1/search?q=" + encodeURIComponent(q)).then((hits) => {
      searchMatchIds = new Set();
      const list = [];
      const seen = new Set();
      for (const hit of hits) {
        const convo = hit.conversation;
        if (hit.message && hit.message.body) convo.snippet = hit.message.body;
        searchMatchIds.add(convo.id);
        if (!seen.has(convo.id)) {
          seen.add(convo.id);
          list.push(convo);
        }
      }
      conversations = mergeById(conversations, list);
      renderConversations(q);
    }).catch(() => {
      searchMatchIds = null;
      renderConversations(q);
    });
  } else {
    searchMatchIds = null;
    renderConversations("");
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
  saveContactBtn.hidden = !canSaveContact(convo);
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
        html += `<img alt="" src="${escapeHtml(att.url)}" />`;
      } else if ((att.mimeType || "").startsWith("audio/")) {
        html += `<audio controls preload="none" src="${escapeHtml(att.url)}"></audio>`;
      }
    }
    html += linkify(m.body || "");
    html += `<div class="meta">${formatTime(m.timestamp)}</div>`;
    b.innerHTML = html;
    messagesEl.appendChild(b);
  }
  messagesEl.scrollTop = messagesEl.scrollHeight;
}

const lightbox = document.getElementById("lightbox");
const lightboxImg = document.getElementById("lightbox-img");
function openLightbox(src) {
  lightboxImg.src = src;
  lightbox.hidden = false;
}
function closeLightbox() {
  lightbox.hidden = true;
  lightboxImg.removeAttribute("src");
}
messagesEl.addEventListener("click", (e) => {
  const img = e.target.closest("img");
  if (!img) return;
  e.preventDefault();
  openLightbox(img.currentSrc || img.src);
});
lightbox.addEventListener("click", closeLightbox);

composeText.addEventListener("input", () => {
  composeText.style.height = "auto";
  composeText.style.height = Math.min(composeText.scrollHeight, 160) + "px";
  updateEmojiSuggest(composeText);
});
document.getElementById("new-body").addEventListener("input", () => {
  updateEmojiSuggest(document.getElementById("new-body"));
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
gifBtn.addEventListener("click", () => {
  hideEmojiPanel();
  gifPanel.hidden = !gifPanel.hidden;
  if (!gifPanel.hidden) {
    gifSearch.focus();
    if (!gifGrid.childElementCount) searchGifs("reaction");
  }
});
document.getElementById("new-gif-btn").addEventListener("click", () => {
  hideEmojiPanel();
  const form = document.getElementById("new-form");
  form.insertBefore(gifPanel, form.querySelector(".new-actions"));
  gifPanel.hidden = false;
  gifSearch.focus();
  if (!gifGrid.childElementCount) searchGifs("reaction");
});
let gifTimer = null;
gifSearch.addEventListener("input", () => {
  clearTimeout(gifTimer);
  gifTimer = setTimeout(() => searchGifs(gifSearch.value.trim() || "reaction"), 300);
});
async function searchGifs(q) {
  gifStatus.hidden = false;
  gifStatus.textContent = "Searching…";
  gifGrid.innerHTML = "";
  try {
    const hits = await api("/api/v1/gifs?q=" + encodeURIComponent(q || "reaction"));
    if (!hits.length) {
      gifStatus.textContent = "No GIFs small enough for MMS. Try another word, or attach a .gif file.";
      return;
    }
    gifStatus.hidden = true;
    for (const hit of hits) {
      const btn = document.createElement("button");
      btn.type = "button";
      btn.title = hit.title || "";
      btn.innerHTML = `<img alt="" src="${escapeHtml(hit.url)}" />`;
      btn.addEventListener("click", () => pickGif(hit));
      gifGrid.appendChild(btn);
    }
    bindGifHover();
  } catch (err) {
  } catch (err) {
    gifStatus.textContent = err.message || "Could not search GIFs";
  }
}
function bindGifHover() {
  const hover = document.getElementById("gif-hover");
  const hoverImg = document.getElementById("gif-hover-img");
  if (!hover || !gifGrid || gifGrid.dataset.hoverBound) return;
  gifGrid.dataset.hoverBound = "1";
  gifGrid.addEventListener("mouseover", (e) => {
    const img = e.target.closest("img");
    if (!img) return;
    hoverImg.src = img.currentSrc || img.src;
    hover.hidden = false;
    const rect = img.getBoundingClientRect();
    hover.style.left = (rect.right + 12) + "px";
    hover.style.top = rect.top + "px";
    requestAnimationFrame(() => {
      const box = hover.getBoundingClientRect();
      let left = rect.right + 12;
      let top = rect.top;
      if (box.right > window.innerWidth - 8) left = rect.left - box.width - 12;
      if (box.bottom > window.innerHeight - 8) top = Math.max(8, window.innerHeight - box.height - 8);
      hover.style.left = Math.max(8, left) + "px";
      hover.style.top = Math.max(8, top) + "px";
    });
  });
  gifGrid.addEventListener("mouseleave", () => {
    hover.hidden = true;
    hoverImg.removeAttribute("src");
  });
}
function pickGif(hit) {
  const fileish = { name: (hit.title || "gif") + ".gif", type: "image/gif" };
  if (!newModal.hidden) {
    pendingNewGifUrl = hit.url;
    pendingNewImage = null;
    renderPreview(newPendingPreview, fileish, () => {
      pendingNewGifUrl = null;
      newPendingPreview.hidden = true;
      newPendingPreview.innerHTML = "";
    }, hit.url);
  } else {
    pendingGifUrl = hit.url;
    pendingImage = null;
    renderPreview(pendingPreview, fileish, () => {
      pendingGifUrl = null;
      pendingPreview.hidden = true;
      pendingPreview.innerHTML = "";
    }, hit.url);
  }
  gifPanel.hidden = true;
}
voiceBtn.addEventListener("click", () => toggleVoice(false));
document.getElementById("new-voice-btn").addEventListener("click", () => toggleVoice(true));
async function toggleVoice(forNew) {
  if (voiceRec) {
    await voiceRec.stop();
    return;
  }
  if (!navigator.mediaDevices?.getUserMedia) {
    alert("This browser cannot record audio.");
    return;
  }
  const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
  voiceBtn.classList.add("recording");
  voiceBtn.title = "Stop recording";
  const chunks = [];
  const ctx = new AudioContext();
  const src = ctx.createMediaStreamSource(stream);
  const proc = ctx.createScriptProcessor(4096, 1, 1);
  const rate = ctx.sampleRate;
  proc.onaudioprocess = (ev) => {
    chunks.push(new Float32Array(ev.inputBuffer.getChannelData(0)));
  };
  const mute = ctx.createGain();
  mute.gain.value = 0;
  src.connect(proc);
  proc.connect(mute);
  mute.connect(ctx.destination);
  const started = Date.now();
  const limit = setTimeout(() => voiceRec && voiceRec.stop(), 45000);
  voiceRec = {
    stop: async () => {
      clearTimeout(limit);
      voiceRec = null;
      proc.disconnect();
      src.disconnect();
      stream.getTracks().forEach((t) => t.stop());
      await ctx.close();
      voiceBtn.classList.remove("recording");
      voiceBtn.title = "Record a voice message";
      const samples = mergeFloat32(chunks);
      if (samples.length < rate / 5) return;
      const wav = encodeWav(downsample(samples, rate, 8000), 8000);
      const file = new File([wav], "voice.wav", { type: "audio/wav" });
      if (forNew) setPendingNewImage(file);
      else setPendingImage(file);
    },
  };
}
function mergeFloat32(chunks) {
  let n = 0;
  for (const c of chunks) n += c.length;
  const out = new Float32Array(n);
  let o = 0;
  for (const c of chunks) {
    out.set(c, o);
    o += c.length;
  }
  return out;
}
function downsample(input, fromRate, toRate) {
  if (fromRate === toRate) {
    const out = new Int16Array(input.length);
    for (let i = 0; i < input.length; i++) {
      const s = Math.max(-1, Math.min(1, input[i]));
      out[i] = s < 0 ? s * 0x8000 : s * 0x7fff;
    }
    return out;
  }
  const outLen = Math.max(1, Math.floor(input.length * toRate / fromRate));
  const out = new Int16Array(outLen);
  for (let i = 0; i < outLen; i++) {
    const src = i * fromRate / toRate;
    const i0 = Math.min(input.length - 1, Math.floor(src));
    const s = Math.max(-1, Math.min(1, input[i0]));
    out[i] = s < 0 ? s * 0x8000 : s * 0x7fff;
  }
  return out;
}
function encodeWav(samples, sampleRate) {
  const n = samples.length * 2;
  const buf = new ArrayBuffer(44 + n);
  const v = new DataView(buf);
  const w = (off, s) => { for (let i = 0; i < s.length; i++) v.setUint8(off + i, s.charCodeAt(i)); };
  w(0, "RIFF");
  v.setUint32(4, 36 + n, true);
  w(8, "WAVE");
  w(12, "fmt ");
  v.setUint32(16, 16, true);
  v.setUint16(20, 1, true);
  v.setUint16(22, 1, true);
  v.setUint32(24, sampleRate, true);
  v.setUint32(28, sampleRate * 2, true);
  v.setUint16(32, 2, true);
  v.setUint16(34, 16, true);
  w(36, "data");
  v.setUint32(40, n, true);
  for (let i = 0; i < samples.length; i++) v.setInt16(44 + i * 2, samples[i], true);
  return buf;
}
let emojiMode = "";
let emojiSel = 0;
let emojiHits = [];
let emojiToken = null;

function emojiCommand(item) {
  return ":" + item.names[0] + ":";
}
function insertEmoji(textarea, glyph, from, to) {
  const start = from ?? textarea.selectionStart ?? textarea.value.length;
  const end = to ?? textarea.selectionEnd ?? start;
  textarea.value = textarea.value.slice(0, start) + glyph + textarea.value.slice(end);
  const pos = start + glyph.length;
  textarea.selectionStart = textarea.selectionEnd = pos;
  textarea.focus();
  hideEmojiPanel();
  textarea.dispatchEvent(new Event("input"));
}
function hideEmojiPanel() {
  if (!emojiPanel) return;
  emojiPanel.hidden = true;
  emojiMode = "";
  emojiToken = null;
}
function colonToken(textarea) {
  const pos = textarea.selectionStart;
  if (pos !== textarea.selectionEnd) return null;
  const before = textarea.value.slice(0, pos);
  const m = before.match(/(^|[\s\n]):([a-zA-Z+][a-zA-Z0-9_+]*)(:)?$/);
  if (!m) return null;
  const query = m[2];
  const closed = !!m[3];
  const start = pos - 1 - query.length - (closed ? 1 : 0);
  return { start, end: pos, query: query.toLowerCase(), closed };
}
function matchEmoji(query) {
  const q = query.toLowerCase();
  const exact = [];
  const prefix = [];
  const inner = [];
  for (const item of EMOJI) {
    if (item.names.some((n) => n === q)) exact.push(item);
    else if (item.names.some((n) => n.startsWith(q))) prefix.push(item);
    else if (item.names.some((n) => n.includes(q))) inner.push(item);
  }
  return exact.concat(prefix, inner).slice(0, 12);
}
function renderEmojiPanel(items, mode) {
  emojiPanel.innerHTML = "";
  emojiPanel.classList.toggle("suggest", mode === "suggest");
  emojiMode = mode;
  emojiHits = items;
  items.forEach((item, i) => {
    const btn = document.createElement("button");
    btn.type = "button";
    const cmd = emojiCommand(item);
    btn.dataset.cmd = item.names.map((n) => ":" + n + ":").join("  ");
    btn.title = btn.dataset.cmd;
    if (mode === "suggest") {
      btn.innerHTML = `<span class="emoji-glyph">${item.glyph}</span><span class="emoji-cmd">${escapeHtml(cmd)}</span>`;
      btn.classList.toggle("active", i === emojiSel);
    } else {
      btn.textContent = item.glyph;
    }
    btn.addEventListener("click", () => {
      const target = emojiToken?.textarea || (!newModal.hidden ? document.getElementById("new-body") : composeText);
      if (emojiToken && emojiToken.textarea === target) {
        insertEmoji(target, item.glyph, emojiToken.start, emojiToken.end);
      } else {
        insertEmoji(target, item.glyph);
      }
    });
    emojiPanel.appendChild(btn);
  });
}
function setEmojiSel(index) {
  if (!emojiHits.length) return;
  emojiSel = (index + emojiHits.length) % emojiHits.length;
  [...emojiPanel.children].forEach((btn, i) => btn.classList.toggle("active", i === emojiSel));
  emojiPanel.children[emojiSel]?.scrollIntoView({ block: "nearest" });
}
function commitEmojiSuggest(textarea) {
  const item = emojiHits[emojiSel];
  if (!item || !emojiToken) return;
  insertEmoji(textarea, item.glyph, emojiToken.start, emojiToken.end);
}
function updateEmojiSuggest(textarea) {
  const token = colonToken(textarea);
  if (!token) {
    if (emojiMode === "suggest") hideEmojiPanel();
    return;
  }
  const hits = matchEmoji(token.query);
  if (!hits.length) {
    if (emojiMode === "suggest") hideEmojiPanel();
    return;
  }
  if (token.closed) {
    const exact = hits.find((item) => item.names.includes(token.query));
    if (exact) {
      insertEmoji(textarea, exact.glyph, token.start, token.end);
      return;
    }
  }
  emojiToken = { ...token, textarea };
  emojiSel = 0;
  const host = textarea.closest(".compose-main") || textarea.closest("form");
  if (host) host.insertBefore(emojiPanel, textarea.nextSibling);
  renderEmojiPanel(hits, "suggest");
  emojiPanel.hidden = false;
}
function handleEmojiKeys(e, textarea) {
  if (emojiPanel.hidden || emojiMode !== "suggest") return false;
  if (e.key === "ArrowDown") {
    e.preventDefault();
    setEmojiSel(emojiSel + 1);
    return true;
  }
  if (e.key === "ArrowUp") {
    e.preventDefault();
    setEmojiSel(emojiSel - 1);
    return true;
  }
  if (e.key === "Tab" || e.key === "Enter") {
    e.preventDefault();
    e.stopPropagation();
    commitEmojiSuggest(textarea);
    return true;
  }
  if (e.key === "Escape") {
    e.preventDefault();
    hideEmojiPanel();
    return true;
  }
  return false;
}
function fillEmojiPanel() {
  emojiToken = null;
  renderEmojiPanel(EMOJI, "browse");
}
emojiBtn.addEventListener("click", () => {
  if (!emojiPanel.hidden && emojiMode === "browse") {
    hideEmojiPanel();
    return;
  }
  fillEmojiPanel();
  composeText.closest(".compose-main").appendChild(emojiPanel);
  emojiPanel.hidden = false;
});
document.getElementById("new-emoji-btn").addEventListener("click", () => {
  fillEmojiPanel();
  const form = document.getElementById("new-form");
  form.insertBefore(emojiPanel, form.querySelector(".new-actions"));
  emojiPanel.hidden = false;
  document.getElementById("new-body").focus();
});
pdfBtn.addEventListener("click", downloadThreadPdf);
document.getElementById("copy-number-btn").addEventListener("click", async () => {
  const convo = conversations.find((c) => c.id === selectedId);
  const text = ((convo?.recipients || []).filter(Boolean).join("\n") || convo?.address || "").trim();
  if (!text) return;
  try {
    await navigator.clipboard.writeText(text);
  } catch (_) {
    const ta = document.createElement("textarea");
    ta.value = text;
    document.body.appendChild(ta);
    ta.select();
    document.execCommand("copy");
    ta.remove();
  }
  const btn = document.getElementById("copy-number-btn");
  const prev = btn.textContent;
  btn.textContent = "Copied";
  setTimeout(() => { btn.textContent = prev; }, 1200);
});
const peopleModal = document.getElementById("people-modal");
const peopleList = document.getElementById("people-list");
document.getElementById("people-btn").addEventListener("click", showPeople);
document.getElementById("close-people").addEventListener("click", () => { peopleModal.hidden = true; });
peopleModal.addEventListener("click", (e) => { if (e.target === peopleModal) peopleModal.hidden = true; });
async function showPeople() {
  if (!selectedId) return;
  peopleList.innerHTML = "<p class='muted'>Loading…</p>";
  peopleModal.hidden = false;
  try {
    const people = await api("/api/v1/conversations/" + encodeURIComponent(selectedId) + "/people");
    peopleList.innerHTML = "";
    if (!people.length) {
      peopleList.innerHTML = "<p class='muted'>No numbers on this thread.</p>";
      return;
    }
    for (const p of people) {
      const el = document.createElement("div");
      el.className = "people-card";
      let rows = "";
      for (const phone of p.phones || []) {
        rows += `<div class="people-row"><span class="muted">${escapeHtml(phone.label)}</span><span>${escapeHtml(phone.value)}</span><button type="button" class="text-btn" data-copy="${escapeHtml(phone.value)}">Copy</button></div>`;
      }
      for (const email of p.emails || []) {
        rows += `<div class="people-row"><span class="muted">${escapeHtml(email.label)}</span><span>${escapeHtml(email.value)}</span><button type="button" class="text-btn" data-copy="${escapeHtml(email.value)}">Copy</button></div>`;
      }
      if (p.org) rows += `<div class="people-row"><span class="muted">Org</span><span>${escapeHtml(p.org)}${p.title ? " · " + escapeHtml(p.title) : ""}</span></div>`;
      if (p.postal) rows += `<div class="people-row"><span class="muted">Address</span><span>${escapeHtml(p.postal)}</span></div>`;
      el.innerHTML = `${avatarHtml(p.name, p.avatarColor, p.photoUrl)}<div class="people-meta"><h3>${escapeHtml(p.name)}</h3>${rows || "<p class='muted'>No extra details</p>"}</div>`;
      el.querySelectorAll("[data-copy]").forEach((btn) => {
        btn.addEventListener("click", async () => {
          const value = btn.getAttribute("data-copy") || "";
          try { await navigator.clipboard.writeText(value); } catch (_) {}
          btn.textContent = "Copied";
          setTimeout(() => { btn.textContent = "Copy"; }, 1200);
        });
      });
      peopleList.appendChild(el);
    }
  } catch (err) {
    peopleList.innerHTML = "<p class='muted'>" + escapeHtml(err.message || "Could not load details") + "</p>";
  }
}
async function downloadThreadPdf() {
  if (!selectedId) return;
  try {
    const res = await fetch("/api/v1/conversations/" + encodeURIComponent(selectedId) + "/pdf", {
      headers: authHeaders(),
      credentials: "include",
    });
    if (res.status === 401) {
      token = "";
      localStorage.removeItem(TOKEN_KEY);
      showPair();
      throw new Error("unpaired");
    }
    if (!res.ok) throw new Error("export failed");
    const blob = await res.blob();
    const header = res.headers.get("content-disposition") || "";
    const match = /filename=\"([^\"]+)\"/.exec(header);
    const fallback = "LanText-" + (document.getElementById("thread-name").textContent || "thread").replace(/[^\w.-]+/g, "-") + ".pdf";
    const a = document.createElement("a");
    const href = URL.createObjectURL(blob);
    a.href = href;
    a.download = match ? match[1] : fallback;
    a.click();
    URL.revokeObjectURL(href);
  } catch (err) {
    alert(err.message || "Could not download this thread.");
  }
}
fileInput.addEventListener("change", () => {
  if (fileInput.files[0]) setPendingImage(fileInput.files[0]);
});
document.getElementById("new-attach-btn").addEventListener("click", () => newFileInput.click());
newFileInput.addEventListener("change", () => {
  if (newFileInput.files[0]) setPendingNewImage(newFileInput.files[0]);
});

function renderPreview(container, file, onClear, previewUrl) {
  container.hidden = false;
  const audio = (file.type || "").startsWith("audio/");
  if (audio) {
    container.innerHTML = `<audio controls></audio><span class="muted">Voice message</span><button type="button">Remove</button>`;
    container.querySelector("audio").src = previewUrl || URL.createObjectURL(file);
  } else {
    container.innerHTML = `<img alt="attachment" /><span class="muted">${escapeHtml(file.name || "image")}</span><button type="button">Remove</button>`;
    container.querySelector("img").src = previewUrl || URL.createObjectURL(file);
  }
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
  if (handleEmojiKeys(e, composeText)) return;
  if (wantsSend(e)) {
    e.preventDefault();
    composeForm.requestSubmit();
  }
});

newBody.addEventListener("keydown", (e) => {
  if (handleEmojiKeys(e, newBody)) return;
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
  if (!body && !pendingImage && !pendingGifUrl) return;
  const convo = conversations.find((c) => c.id === selectedId);
  sending = true;
  const sendBtn = document.getElementById("send-btn");
  const previousLabel = sendBtn.textContent;
  sendBtn.textContent = pendingGifUrl ? "Sending GIF…" : pendingImage ? ((pendingImage.type || "").startsWith("audio/") ? "Sending voice…" : "Sending picture…") : "Sending…";
  try {
    const dest = (convo?.recipients || []).filter(Boolean);
    const recipients = dest.length ? dest : [convo?.address].filter(Boolean);
    const payload = { body, recipients };
    if (pendingGifUrl) {
      payload.mediaUrl = pendingGifUrl;
    } else if (pendingImage) {
      payload.imageBase64 = await fileToBase64(pendingImage);
      payload.imageMime = pendingImage.type || "image/jpeg";
    }
    await api(`/api/v1/conversations/${selectedId}/messages`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    });
    pendingImage = null;
    pendingGifUrl = null;
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
    if (!lightbox.hidden) { closeLightbox(); e.preventDefault(); return; }
    if (emojiPanel && !emojiPanel.hidden) { hideEmojiPanel(); e.preventDefault(); return; }
    if (gifPanel && !gifPanel.hidden) { gifPanel.hidden = true; e.preventDefault(); return; }
    if (!helpModal.hidden) { closeHelp(); e.preventDefault(); return; }
    if (!themeModal.hidden) { closeTheme(); e.preventDefault(); return; }
    if (peopleModal && !peopleModal.hidden) { peopleModal.hidden = true; e.preventDefault(); return; }
    if (!contactModal.hidden) { closeContactModal(); e.preventDefault(); return; }
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
  if (!number || (!body && !pendingNewImage && !pendingNewGifUrl)) return;
  try {
    const payload = { recipients: [number], body };
    if (pendingNewGifUrl) {
      payload.mediaUrl = pendingNewGifUrl;
    } else if (pendingNewImage) {
      payload.imageBase64 = await fileToBase64(pendingNewImage);
      payload.imageMime = pendingNewImage.type || "image/jpeg";
    }
    await api("/api/v1/conversations", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    });
    pendingNewImage = null;
    pendingNewGifUrl = null;
    closeNewModal();
    document.getElementById("new-body").value = "";
    await loadInbox();
  } catch (err) {
    alert(err.message || "Could not send");
  }
});

function connectEvents() {
  const gen = ++eventGen;
  if (socket) try { socket.close(); } catch (_) {}
  const proto = location.protocol === "https:" ? "wss:" : "ws:";
  const q = token ? ("?token=" + encodeURIComponent(token)) : "";
  socket = new WebSocket(proto + "//" + location.host + "/api/v1/events" + q);
  socket.onopen = () => {
    if (gen !== eventGen) return;
    const recovered = !phoneReachable;
    connLabel.textContent = "Live";
    if (recovered) markLive();
    if (recovered) loadInbox().catch(() => {});
  };
  socket.onclose = () => {
    if (gen !== eventGen) return;
    noteUnreachable("The live link to the phone closed.");
    scheduleReconnect();
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

function canSaveContact(convo) {
  if (!convo || convo.isGroup) return false;
  if (convo.contactId) return false;
  const number = threadNumber(convo);
  return !!number;
}

function threadNumber(convo) {
  const fromRecipients = (convo?.recipients || []).map((n) => n.trim()).filter(Boolean);
  if (fromRecipients.length === 1) return fromRecipients[0];
  const address = (convo?.address || "").trim();
  if (!address || address.includes(",")) return "";
  return address;
}

function setContactMode(mode) {
  contactSaveMode = mode;
  document.querySelectorAll("#contact-mode button").forEach((btn) => {
    btn.classList.toggle("active", btn.dataset.contactMode === mode);
  });
  document.getElementById("contact-new-fields").hidden = mode !== "new";
  document.getElementById("contact-existing-fields").hidden = mode !== "existing";
  const nameInput = document.getElementById("contact-name");
  if (mode === "new") {
    nameInput.required = true;
    nameInput.focus();
  } else {
    nameInput.required = false;
    document.getElementById("contact-existing-search").focus();
    loadExistingContacts(document.getElementById("contact-existing-search").value);
  }
}

function openContactModal() {
  const convo = conversations.find((c) => c.id === selectedId);
  const number = threadNumber(convo);
  if (!number) return;
  contactModalNumber = number;
  selectedExistingContact = null;
  document.getElementById("contact-number-label").textContent = number;
  const nameGuess = convo.displayName && !looksLikeNumber(convo.displayName) ? convo.displayName : "";
  document.getElementById("contact-name").value = nameGuess;
  document.getElementById("contact-existing-search").value = "";
  document.getElementById("contact-existing-list").innerHTML = "";
  setContactMode("new");
  contactModal.hidden = false;
}

function closeContactModal() {
  contactModal.hidden = true;
  selectedExistingContact = null;
}

async function loadExistingContacts(q) {
  const list = await api("/api/v1/contacts?q=" + encodeURIComponent(q || ""));
  const box = document.getElementById("contact-existing-list");
  box.innerHTML = "";
  if (!list.length) {
    box.innerHTML = "<p class='muted' style='padding:8px'>No matching contacts</p>";
    return;
  }
  for (const c of list) {
    const el = document.createElement("div");
    el.className = "contact" + (selectedExistingContact?.id === c.id ? " selected" : "");
    el.innerHTML = `${avatarHtml(c.name, c.avatarColor, c.photoUrl)}<div><strong>${escapeHtml(c.name)}</strong><div class="muted">${escapeHtml(c.number)}</div></div>`;
    el.addEventListener("click", () => {
      selectedExistingContact = c;
      [...box.children].forEach((n) => n.classList.remove("selected"));
      el.classList.add("selected");
    });
    box.appendChild(el);
  }
}

saveContactBtn.addEventListener("click", openContactModal);
document.getElementById("close-contact").addEventListener("click", closeContactModal);
contactModal.addEventListener("click", (e) => {
  if (e.target === contactModal) closeContactModal();
});
document.getElementById("contact-mode").addEventListener("click", (e) => {
  const btn = e.target.closest("button[data-contact-mode]");
  if (!btn) return;
  setContactMode(btn.dataset.contactMode);
});
document.getElementById("contact-existing-search").addEventListener("input", () => {
  loadExistingContacts(document.getElementById("contact-existing-search").value);
});
document.getElementById("save-contact-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  const number = contactModalNumber;
  if (!number) return;
  try {
    if (contactSaveMode === "new") {
      const name = document.getElementById("contact-name").value.trim();
      if (!name) {
        alert("Enter a name for this contact.");
        return;
      }
      await api("/api/v1/contacts", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ name, number }),
      });
    } else {
      if (!selectedExistingContact) {
        alert("Pick an existing contact to add this number to.");
        return;
      }
      await api("/api/v1/contacts/" + encodeURIComponent(selectedExistingContact.id) + "/phones", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ number }),
      });
    }
    closeContactModal();
    await loadInbox();
    if (selectedId) await openThread(selectedId, false);
  } catch (err) {
    alert(err.message || "Could not save that contact.");
  }
});

function linkify(text) {
  const escaped = escapeHtml(text);
  return escaped.replace(/((?:https?:\/\/|www\.)[^\s<]+)/gi, (url) => {
    const hrefRaw = url.startsWith("http") ? url : "https://" + url;
    const href = hrefRaw.replace(/&amp;/g, "&");
    if (!/^https?:\/\//i.test(href)) return url;
    return `<a href="${href}" target="_blank" rel="noopener noreferrer">${url}</a>`;
  });
}

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

reconnectBtn.addEventListener("click", () => {
  if (reconnectTimer != null) {
    clearTimeout(reconnectTimer);
    reconnectTimer = null;
  }
  reconnectToPhone();
});
window.addEventListener("online", () => {
  if (disconnectOverlay.hidden === false) reconnectToPhone();
  else if (wantEvents) connectEvents();
});
window.addEventListener("offline", () => {
  noteUnreachable("This computer is offline.");
});
document.addEventListener("visibilitychange", () => {
  if (document.visibilityState === "visible" && disconnectOverlay.hidden === false) {
    reconnectToPhone();
  }
});
window.addEventListener("pageshow", (e) => {
  if (e.persisted && disconnectOverlay.hidden === false) reconnectToPhone();
});

boot();
