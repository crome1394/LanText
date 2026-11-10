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
const pairBtn = document.getElementById("pair-btn");
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
let threadLoadGen = 0;
let threadHasMore = false;
let threadPaging = false;
let threadUserMoved = false;
let pendingImage = null;
let pendingNewImage = null;
let pendingGifUrl = null;
let pendingNewGifUrl = null;
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
let pairingInFlight = false;
let gifEnabled = true;
let enterSends = true;
let phoneSubId = -1;
let inboxGroup = "none";
const phoneDetails = {
  direction: false,
  party: false,
  time: true,
  type: false,
  sim: false,
  showSim: false,
};
let folder = "inbox";
let folders = { archivedIds: [], trash: [], blocked: [], scheduled: [] };

function setPairStatus(msg) {
  if (pairStatus) pairStatus.textContent = msg || "";
}

function sleep(ms) { return new Promise((r) => setTimeout(r, ms)); }

async function pollPair(requestId) {
  for (let i = 0; i < 60; i++) {
    try {
      const res = await fetch("/api/v1/pair/" + requestId, {
        credentials: "include",
        signal: AbortSignal.timeout(8000),
      });
      if (res.ok) {
        const data = await res.json();
        if (data.status === "approved") return data.token;
        if (data.status === "denied") return null;
      }
    } catch (_) {}
    await sleep(1000);
  }
  return null;
}

function guessDeviceName(ua) {
  const raw = ua || "";
  let browser = "Browser";
  if (/Edg\/|Edge\//.test(raw)) browser = "Edge";
  else if (/OPR\/|Opera/.test(raw)) browser = "Opera";
  else if (/Firefox\//.test(raw)) browser = "Firefox";
  else if (/CriOS\/|Chrome\//.test(raw)) browser = "Chrome";
  else if (/Safari\//.test(raw)) browser = "Safari";
  let os = "";
  if (/Windows/.test(raw)) os = "Windows";
  else if (/Android/.test(raw)) os = "Android";
  else if (/CrOS/.test(raw)) os = "ChromeOS";
  else if (/iPad/.test(raw)) os = "iPad";
  else if (/iPhone/.test(raw)) os = "iPhone";
  else if (/Mac OS|Macintosh/.test(raw)) os = "Mac";
  else if (/Linux/.test(raw)) os = "Linux";
  return (os ? browser + " on " + os : browser).slice(0, 80);
}

const deviceNameInput = document.getElementById("device-name");
if (deviceNameInput && !deviceNameInput.value.trim()) {
  deviceNameInput.value = guessDeviceName(navigator.userAgent);
}

function deviceName() {
  const typed = deviceNameInput && deviceNameInput.value ? deviceNameInput.value.trim() : "";
  return (typed || guessDeviceName(navigator.userAgent)).slice(0, 80);
}

async function startPair(e) {
  if (e) e.preventDefault();
  if (pairingInFlight) return;
  const pinEl = document.getElementById("pin");
  const pin = (pinEl && pinEl.value ? pinEl.value : "").trim();
  if (!/^\d{8}$/.test(pin)) {
    setPairStatus("Enter the 8-digit PIN from the LanText app.");
    return;
  }
  pairingInFlight = true;
  if (pairBtn) pairBtn.disabled = true;
  setPairStatus("Waiting for approval on your phone…");
  try {
    const started = await fetch("/api/v1/pair", {
      method: "POST",
      headers: { "Content-Type": "application/json; charset=UTF-8" },
      body: JSON.stringify({ pin, clientName: deviceName() }),
      credentials: "include",
      signal: AbortSignal.timeout(15000),
    });
    if (!started.ok) {
      let err = "PIN rejected";
      try { err = (await started.json()).error || err; } catch (_) {}
      setPairStatus(
        err === "expired"
          ? "That PIN expired. Use the PIN currently shown in the app."
          : err === "locked"
            ? "Too many attempts. Wait a moment and try again."
            : "PIN rejected. Check the PIN on the phone and try again.",
      );
      return;
    }
    const { requestId } = await started.json();
    if (!requestId) {
      setPairStatus("The phone did not start pairing. Try again with a fresh PIN.");
      return;
    }
    const tokenFound = await pollPair(requestId);
    if (!tokenFound) {
      setPairStatus("Pairing was denied or expired. Approve on the phone, then try again.");
      return;
    }
    token = tokenFound;
    localStorage.setItem(TOKEN_KEY, token);
    await enterApp();
  } catch (err) {
    const timedOut = err && (err.name === "TimeoutError" || err.name === "AbortError");
    setPairStatus(timedOut
      ? "The phone did not respond. Check Wi-Fi and try again."
      : ((err && err.message) || "Could not reach the phone."));
  } finally {
    pairingInFlight = false;
    if (pairBtn) pairBtn.disabled = false;
  }
}

if (pairForm) pairForm.addEventListener("submit", startPair);

function applyExperimental(src) {
  if (!src) return;
  if (typeof src.gifEnabled === "boolean") gifEnabled = src.gifEnabled;
  if (gifBtn) gifBtn.hidden = !gifEnabled;
  const newGif = document.getElementById("new-gif-btn");
  if (newGif) newGif.hidden = !gifEnabled;
  if (!gifEnabled && gifPanel) gifPanel.hidden = true;
  if (!gifEnabled) {
    pendingGifUrl = null;
    pendingNewGifUrl = null;
  }
  if (pendingImage && isAudioFile(pendingImage)) {
    pendingImage = null;
    if (pendingPreview) { pendingPreview.hidden = true; pendingPreview.innerHTML = ""; }
  }
  if (pendingNewImage && isAudioFile(pendingNewImage)) {
    pendingNewImage = null;
    if (newPendingPreview) { newPendingPreview.hidden = true; newPendingPreview.innerHTML = ""; }
  }
  const accept = gifEnabled
    ? ["image/*", ".gif"]
    : ["image/jpeg", "image/png", "image/webp", "image/heic", "image/heif"];
  const acceptStr = accept.join(",");
  if (fileInput) fileInput.accept = acceptStr;
  if (newFileInput) newFileInput.accept = acceptStr;
  const attach = document.getElementById("attach-btn");
  if (attach) attach.title = gifEnabled ? "Attach a picture or GIF" : "Attach a picture";
  if (composeText) composeText.placeholder = gifEnabled ? "Message or GIF" : "Message";
  applyPhoneSettings(src);
  updateComposeHint();
}
function updateComposeHint() {
  const hint = document.querySelector(".compose-hint");
  if (!hint) return;
  const extra = gifEnabled ? "GIF for MMS · " : "";
  const enter = enterSends ? "Enter to send · " : "Ctrl+Enter to send · ";
  const line = enterSends ? "Shift+Enter for a new line" : "Enter for a new line";
  hint.textContent = enter + ":lol then Tab · " + extra + line;
  if (composeText) {
    composeText.title = enterSends
      ? "Enter to send, Shift+Enter for a new line"
      : "Enter for a new line, Ctrl+Enter to send";
  }
  if (newBody) {
    newBody.placeholder = enterSends
      ? "Message (Enter to send, paste a picture or GIF)"
      : "Message (Ctrl+Enter to send, paste a picture or GIF)";
  }
  const sendBtn = document.getElementById("send-btn");
  if (sendBtn) sendBtn.title = enterSends ? "Send (Enter)" : "Send (Ctrl+Enter)";
  const helpRows = document.querySelectorAll("#help-modal .keys tr td:last-child");
  if (helpRows.length >= 3) {
    helpRows[0].textContent = enterSends ? "Send message" : "New line";
    helpRows[1].textContent = "New line";
    helpRows[2].textContent = enterSends ? "Send (same as Enter)" : "Send message";
  }
}

function applyPhoneSettings(src) {
  if (!src) return;
  if (typeof src.launcherIcon === "string" && src.launcherIcon) applyLauncherIcon(src.launcherIcon);
  if (typeof src.inboxGroup === "string") {
    const next = inboxGroupMode(src.inboxGroup);
    const changed = next !== inboxGroup;
    inboxGroup = next;
    if (changed && conversations.length) renderConversations(searchInput ? searchInput.value : "");
  }
  if (typeof src.palette !== "string" || !src.palette) return;
  if (typeof src.enterSends === "boolean") enterSends = src.enterSends;
  if (typeof src.defaultSubId === "number") phoneSubId = src.defaultSubId;
  phoneDetails.direction = !!src.detailDirection;
  phoneDetails.party = !!src.detailParty;
  phoneDetails.time = src.detailTime !== false;
  phoneDetails.type = !!src.detailType;
  phoneDetails.sim = !!src.detailSim;
  phoneDetails.showSim = !!src.showSim;
  const scale = typeof src.fontScale === "number" ? Math.min(1.4, Math.max(0.85, src.fontScale)) : 1;
  document.documentElement.style.fontSize = (scale * 100) + "%";
  document.body.classList.toggle("striped", !!src.striped);
  document.body.classList.toggle("search-bottom", !!src.searchAtBottom);
  const readBtn = document.getElementById("read-btn");
  const deleteBtn = document.getElementById("delete-thread-btn");
  const menuRead = document.getElementById("convo-menu-read");
  const menuDelete = document.getElementById("convo-menu-delete");
  const menuPin = document.getElementById("convo-menu-pin");
  if (typeof src.ribbonRead === "boolean" && readBtn && menuRead) {
    readBtn.hidden = !src.ribbonRead;
    menuRead.hidden = !src.ribbonRead;
  }
  if (typeof src.ribbonDelete === "boolean" && deleteBtn && menuDelete) {
    deleteBtn.hidden = !src.ribbonDelete;
    menuDelete.hidden = !src.ribbonDelete;
  }
  if (typeof src.ribbonPin === "boolean") {
    document.body.classList.toggle("hide-row-pin", !src.ribbonPin);
    if (menuPin) menuPin.hidden = !src.ribbonPin;
  }
  const note = document.querySelector("#theme-modal .muted");
  if (note) note.textContent = "This follows Appearance on the phone. A change here updates the phone.";
  applyingPhone = true;
  localStorage.setItem(THEME_PALETTE_KEY, src.palette);
  if (src.mode === "light" || src.mode === "dark" || src.mode === "auto") {
    localStorage.setItem(THEME_MODE_KEY, src.mode);
  }
  applyTheme();
  applyingPhone = false;
  if (openMessages.length) renderMessages(openMessages, { keepScroll: true });
}
const LAUNCHER_ICONS = {
  cyan: ["#75FBFD", "#1A1A1A"],
  black: ["#000000", "#F4F4F4"],
  dark_blue: ["#2854C5", "#1A1A1A"],
  dark_purple: ["#8C1AF6", "#1A1A1A"],
  dark_red: ["#8B1A10", "#1A1A1A"],
  green: ["#75FB4C", "#1A1A1A"],
  light_blue: ["#5985E1", "#1A1A1A"],
  orange: ["#F19E39", "#1A1A1A"],
  purple: ["#EA33F7", "#1A1A1A"],
  red: ["#EA3323", "#1A1A1A"],
  white: ["#FFFFFF", "#1A1A1A"],
  yellow: ["#FFFF55", "#1A1A1A"],
};
const LAUNCHER_PATH = "M348.5-531.5Q360-543 360-560t-11.5-28.5Q337-600 320-600t-28.5 11.5Q280-577 280-560t11.5 28.5Q303-520 320-520t28.5-11.5Zm160 0Q520-543 520-560t-11.5-28.5Q497-600 480-600t-28.5 11.5Q440-577 440-560t11.5 28.5Q463-520 480-520t28.5-11.5Zm160 0Q680-543 680-560t-11.5-28.5Q657-600 640-600t-28.5 11.5Q600-577 600-560t11.5 28.5Q623-520 640-520t28.5-11.5ZM80-80v-720q0-33 23.5-56.5T160-880h640q33 0 56.5 23.5T880-800v480q0 33-23.5 56.5T800-240H240L80-80Zm126-240h594v-480H160v525l46-45Zm-46 0v-480 480Z";
function launcherSvg(id) {
  const spec = LAUNCHER_ICONS[id] || LAUNCHER_ICONS.cyan;
  return `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 960 960"><rect width="960" height="960" rx="210" fill="${spec[1]}"/><g transform="translate(480 480) scale(0.56) translate(-480 -480) translate(0 960)"><path fill="${spec[0]}" d="${LAUNCHER_PATH}"/></g></svg>`;
}
function applyLauncherIcon(id) {
  const key = LAUNCHER_ICONS[id] ? id : "cyan";
  const href = "data:image/svg+xml," + encodeURIComponent(launcherSvg(key));
  let link = document.querySelector('link[rel="icon"]');
  if (!link) {
    link = document.createElement("link");
    link.rel = "icon";
    document.head.appendChild(link);
  }
  link.href = href;
  const mark = document.getElementById("brand-mark");
  if (mark) mark.src = href;
}
applyLauncherIcon("cyan");
let applyingPhone = false;
let openMessages = [];

function isGifFile(file) {
  return !!file && ((file.type || "") === "image/gif" || /\.gif$/i.test(file.name || ""));
}
function isAudioFile(file) {
  return !!file && (file.type || "").startsWith("audio/");
}
function rejectDisabledMedia(file) {
  if (isGifFile(file) && !gifEnabled) {
    alert("GIF sending is turned off on the phone.");
    return true;
  }
  if (isAudioFile(file)) {
    alert("Voice notes are not available on this computer.");
    return true;
  }
  return false;
}

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
  if (appearanceReady && !applyingPhone) syncAppearance();
}
let appearanceReady = false;
function syncAppearance() {
  fetch("/api/v1/appearance", {
    method: "POST",
    headers: authHeaders({ "Content-Type": "application/json; charset=UTF-8" }),
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
function on(id, ev, fn) {
  const node = typeof id === "string" ? document.getElementById(id) : id;
  if (!node) return;
  node.addEventListener(ev, fn);
}
on("refresh-btn", "click", (e) => {
  refreshInbox(e.shiftKey);
});
on("logout-btn", "click", logoutComputer);
on("theme-btn", "click", openTheme);
on("pair-theme-btn", "click", openTheme);
on("close-theme", "click", closeTheme);
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
  // Register once so a worker left by an older version is replaced by the
  // copy that unregisters itself. Do not cache the shell.
  if (!("serviceWorker" in navigator)) return;
  if (window.caches) {
    caches.keys().then((keys) => {
      keys.forEach((key) => {
        if (key.indexOf("lantext-shell") === 0) caches.delete(key);
      });
    }).catch(() => {});
  }
  navigator.serviceWorker.getRegistrations().then((list) => {
    if (!list.length) return;
    navigator.serviceWorker.register("/sw.js", { updateViaCache: "none" }).catch(() => {});
  }).catch(() => {});
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

function fillDisconnectHint() {
  const hint = document.getElementById("disconnect-hint");
  if (!hint) return;
  hint.textContent = "Stay on " + location.origin + ". Open that same address again and accept the certificate if the browser asks.";
}

async function logoutComputer() {
  if (!confirm("Log out this computer? It will be removed from the phone and will need to pair again.")) return;
  const btn = document.getElementById("logout-btn");
  if (btn) btn.disabled = true;
  try {
    await api("/api/v1/session", { method: "DELETE" });
    token = "";
    localStorage.removeItem(TOKEN_KEY);
    showPair();
    setPairStatus("This computer is logged out. Pair it again from the phone.");
  } catch (err) {
    if (err && err.message === "unpaired") return;
    alert("Could not log out. The phone did not answer, so this computer is still paired.");
  } finally {
    if (btn) btn.disabled = false;
  }
}

function showDisconnectOverlay(detail) {
  fillDisconnectHint();
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
  const session = await api("/api/v1/session");
  applyExperimental(session);
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
    applyExperimental(meta);
    if (token || meta.paired) {
      await enterApp();
      return;
    }
    showPair();
    markLive();
  } catch (err) {
    reconnecting = false;
    reconnectBtn.disabled = false;
    if (err && err.message === "unpaired") {
      showPair();
      setPairStatus("This computer is no longer paired. Enter the PIN shown on the phone.");
      markLive();
      return;
    }
    noteUnreachable("Still can't reach the phone. Reload this tab if the phone is on Wi-Fi.", { showNow: true });
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
  if (disconnectOverlay) disconnectOverlay.hidden = true;
  document.body.classList.remove("disconnected");
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
  fillDisconnectHint();
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
    applyExperimental(meta);
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
      noteUnreachable("Can't reach the phone.", { showNow: true });
      scheduleReconnect();
    } else {
      showPair();
      pairStatus.textContent = "Could not reach the phone. Check the PIN on the app and try again.";
    }
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

async function loadInbox(opts = {}) {
  const previous = conversations;
  const loaded = await Promise.all([
    api("/api/v1/conversations"),
    api("/api/v1/folders").catch(() => folders),
  ]);
  conversations = loaded[0];
  if (loaded[1] && Array.isArray(loaded[1].archivedIds)) folders = loaded[1];
  sortConversations();
  if (opts.notify) notifyFromInbox(previous, conversations);
  updateTitle();
  renderConversations(searchInput.value);
  saveInboxSnapshot();
  if (selectedId && !conversations.some((c) => c.id === selectedId)) {
    closeOpenThread();
  } else if (selectedId) {
    await openThread(selectedId, false);
  }
}

function updateTitle() {
  const unread = inboxConversations().reduce((n, c) => n + (c.unread || 0), 0);
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
function idSet(values) {
  return new Set((values || []).map((id) => String(id)));
}
function trashIds() {
  return idSet((folders.trash || []).map((row) => row.threadId));
}
function archivedIds() {
  return idSet(folders.archivedIds);
}
function inboxConversations() {
  const archived = archivedIds();
  const trash = trashIds();
  return conversations.filter((c) => !archived.has(String(c.id)) && !trash.has(String(c.id)));
}
function conversationsForFolder(query) {
  const q = (query || "").trim().toLowerCase();
  const archived = archivedIds();
  const trash = trashIds();
  if (folder === "trash") {
    return conversations.filter((c) => trash.has(String(c.id)));
  }
  let list = conversations.filter((c) => !trash.has(String(c.id)));
  if (!q && folder === "inbox") list = list.filter((c) => !archived.has(String(c.id)));
  if (!q && folder === "archived") list = list.filter((c) => archived.has(String(c.id)));
  if (q) {
    list = list.filter((c) => {
      if (searchMatchIds && searchMatchIds.has(c.id)) return true;
      return (c.displayName + " " + c.address + " " + c.snippet).toLowerCase().includes(q);
    });
  }
  return list;
}
function setFolder(next) {
  folder = next;
  const heading = document.getElementById("inbox-heading");
  const titles = {
    inbox: "Inbox",
    archived: "Archived",
    trash: "Trash",
    blocked: "Blocked",
    scheduled: "Scheduled",
  };
  if (heading) heading.textContent = titles[next] || "Inbox";
  const back = document.getElementById("folder-back");
  if (back) back.hidden = next === "inbox";
  renderConversations(searchInput ? searchInput.value : "");
}
function folderEmpty(text) {
  convoList.innerHTML = `<p class='muted' style='padding:16px'>${text}</p>`;
}
function inboxGroupMode(raw) {
  return raw === "read" || raw === "date" || raw === "type" || raw === "pinned" ? raw : "none";
}
function weekStartMs(today) {
  let first = 0;
  try {
    const info = new Intl.Locale(navigator.language || "en").weekInfo;
    if (info && typeof info.firstDay === "number") first = info.firstDay % 7;
  } catch (_) {}
  const day = new Date(today.getFullYear(), today.getMonth(), today.getDate());
  const delta = (day.getDay() - first + 7) % 7;
  day.setDate(day.getDate() - delta);
  day.setHours(0, 0, 0, 0);
  return day.getTime();
}
function receivedBucket(timestamp, now) {
  const today = new Date(now);
  today.setHours(0, 0, 0, 0);
  const day = new Date(Math.max(0, Number(timestamp) || 0));
  day.setHours(0, 0, 0, 0);
  if (day.getTime() >= today.getTime()) return "Today";
  const yesterday = new Date(today);
  yesterday.setDate(yesterday.getDate() - 1);
  if (day.getTime() === yesterday.getTime()) return "Yesterday";
  if (day.getTime() >= weekStartMs(today)) return "This week";
  const monthStart = new Date(today.getFullYear(), today.getMonth(), 1);
  if (day.getTime() >= monthStart.getTime()) return "This month";
  return "Older";
}
function groupInbox(rows, mode, now) {
  const normalized = inboxGroupMode(mode);
  if (!rows.length || normalized === "none") return [{ title: "", rows }];
  const order = {
    read: ["Unread", "Read"],
    date: ["Today", "Yesterday", "This week", "This month", "Older"],
    type: ["SMS", "MMS"],
    pinned: ["Pinned", "Other"],
  }[normalized];
  const buckets = new Map(order.map((title) => [title, []]));
  for (const row of rows) {
    let title = "";
    if (normalized === "read") title = row.unread > 0 ? "Unread" : "Read";
    else if (normalized === "date") title = receivedBucket(row.timestamp, now);
    else if (normalized === "type") title = row.latestMms ? "MMS" : "SMS";
    else if (normalized === "pinned") title = row.pinned ? "Pinned" : "Other";
    const bucket = buckets.get(title);
    if (bucket) bucket.push(row);
  }
  return order.filter((title) => buckets.get(title).length).map((title) => ({ title, rows: buckets.get(title) }));
}
function renderConversations(filter) {
  const q = (filter || "").trim().toLowerCase();
  convoList.innerHTML = "";
  if (!q && folder !== "blocked" && folder !== "scheduled") sortConversations();
  if (folder === "blocked") {
    renderBlocked(q);
    return;
  }
  if (folder === "scheduled") {
    renderScheduled(q);
    return;
  }
  const list = conversationsForFolder(q);
  if (folder === "trash") {
    const bar = document.createElement("div");
    bar.className = "folder-bar";
    bar.innerHTML = `<button type="button" class="text-btn danger" id="trash-delete-all"${list.length ? "" : " disabled"}>Delete all</button><button type="button" class="text-btn" id="trash-restore-all"${list.length ? "" : " disabled"}>Restore all</button>`;
    convoList.appendChild(bar);
    const del = bar.querySelector("#trash-delete-all");
    const restore = bar.querySelector("#trash-restore-all");
    if (del) del.addEventListener("click", () => deleteTrashIds(list.map((c) => c.id), true));
    if (restore) restore.addEventListener("click", () => restoreTrashIds(list.map((c) => c.id)));
  }
  if (!list.length) {
    const note = document.createElement("p");
    note.className = "muted";
    note.style.padding = "16px";
    note.textContent = folder === "archived" ? "No archived conversations"
      : folder === "trash" ? "Trash is empty"
        : "No conversations";
    convoList.appendChild(note);
    return;
  }
  const grouped = folder === "inbox" && !q;
  const sections = grouped ? groupInbox(list, inboxGroup, Date.now()) : [{ title: "", rows: list }];
  let stripe = 0;
  for (const section of sections) {
    if (section.title) {
      const head = document.createElement("p");
      head.className = "inbox-section";
      head.textContent = section.title;
      convoList.appendChild(head);
    }
    for (const c of section.rows) {
    const el = document.createElement("div");
    el.className = "convo" + (c.id === selectedId ? " active" : "") + (c.unread ? " unread" : "") + (c.pinned ? " pinned" : "") + (stripe % 2 === 1 ? " alt" : "");
    stripe += 1;
    el.dataset.id = c.id;
    const action = folder === "archived"
      ? `<button type="button" class="text-btn row-action" data-unarchive="${escapeHtml(c.id)}">Unarchive</button>`
      : folder === "trash"
        ? `<button type="button" class="text-btn row-action" data-restore="${escapeHtml(c.id)}">Restore</button><button type="button" class="text-btn danger row-action" data-trash-delete="${escapeHtml(c.id)}">Delete</button>`
        : "";
    const pinLabel = c.pinned ? "Unpin" : "Pin";
    const pinPath = c.pinned
      ? "M16 9V4h1c.55 0 1-.45 1-1s-.45-1-1-1H7c-.55 0-1 .45-1 1s.45 1 1 1h1v5c0 1.66-1.34 3-3 3v2h5.97v7l1 1 1-1v-7H19v-2c-1.66 0-3-1.34-3-3z"
      : "M14 4v5c0 1.12.37 2.16 1 3H9c.65-.86 1-1.9 1-3V4h4m3-2H7c-.55 0-1 .45-1 1s.45 1 1 1h1v5c0 1.66-1.34 3-3 3v2h5.97v7l1 1 1-1v-7H19v-2c-1.66 0-3-1.34-3-3V4h1c.55 0 1-.45 1-1s-.45-1-1-1z";
    const pinButton = folder === "inbox"
      ? `<button type="button" class="row-pin${c.pinned ? " on" : ""}" data-pin="${escapeHtml(c.id)}" data-pinned="${c.pinned ? "1" : "0"}" aria-label="${pinLabel}" title="${pinLabel}"><svg viewBox="0 0 24 24" aria-hidden="true"><path fill="currentColor" d="${pinPath}"/></svg></button>`
      : "";
    el.innerHTML = `
      ${avatarHtml(c.displayName, c.avatarColor, c.photoUrl)}
      <div>
        <div class="name">${escapeHtml(c.displayName)}${c.unread ? '<span class="dot"></span>' : ""}</div>
        <div class="snippet">${escapeHtml(c.snippet || "")}</div>
      </div>
      <div class="when-col"><div class="when">${action || formatTime(c.timestamp)}</div>${pinButton}</div>`;
    el.addEventListener("click", (e) => {
      const pin = e.target.closest("[data-pin]");
      const unarchive = e.target.closest("[data-unarchive]");
      const restore = e.target.closest("[data-restore]");
      const remove = e.target.closest("[data-trash-delete]");
      if (pin) {
        e.stopPropagation();
        setPinned(pin.dataset.pin, pin.dataset.pinned !== "1");
        return;
      }
      if (unarchive) { e.stopPropagation(); unarchiveThread(unarchive.dataset.unarchive); return; }
      if (restore) { e.stopPropagation(); restoreTrashIds([restore.dataset.restore]); return; }
      if (remove) { e.stopPropagation(); deleteTrashIds([remove.dataset.trashDelete], false); return; }
      openThread(c.id, folder !== "trash");
    });
    if (folder === "inbox") el.addEventListener("contextmenu", (e) => showConvoMenu(e, c.id));
    convoList.appendChild(el);
    }
  }
}
function renderBlocked(q) {
  const form = document.createElement("form");
  form.className = "folder-bar";
  form.innerHTML = `<input id="block-entry" type="text" placeholder="Phone number" aria-label="Phone number" /><button type="submit" class="text-btn">Block</button>`;
  form.addEventListener("submit", (e) => {
    e.preventDefault();
    const input = form.querySelector("input");
    const number = (input && input.value || "").trim();
    if (number) blockNumber(number);
  });
  convoList.appendChild(form);
  const rows = (folders.blocked || []).filter((number) => !q || String(number).toLowerCase().includes(q));
  if (!rows.length) {
    folderEmpty("No blocked numbers");
    convoList.insertBefore(form, convoList.firstChild);
    return;
  }
  for (const number of rows) {
    const el = document.createElement("div");
    el.className = "convo plain";
    el.innerHTML = `<div><div class="name">${escapeHtml(number)}</div></div><button type="button" class="text-btn row-action">Unblock</button>`;
    el.querySelector("button").addEventListener("click", () => unblockNumber(number));
    convoList.appendChild(el);
  }
}
function renderScheduled(q) {
  const rows = (folders.scheduled || []).filter((item) => {
    if (!q) return true;
    return (item.address + " " + item.body).toLowerCase().includes(q);
  });
  if (!rows.length) {
    folderEmpty("No scheduled texts");
    return;
  }
  for (const item of rows) {
    const el = document.createElement("div");
    el.className = "convo plain";
    const when = item.sendAt ? formatFullStamp(item.sendAt) : "";
    const body = item.body ? `<div class="snippet schedule-body">${escapeHtml(item.body)}</div>` : "";
    el.innerHTML = `<div><div class="name">${escapeHtml(item.address || "")}</div><div class="snippet">${escapeHtml(when)}</div>${body}</div><button type="button" class="text-btn row-action">Cancel</button>`;
    el.querySelector("button").addEventListener("click", () => cancelScheduled(item.id));
    convoList.appendChild(el);
  }
}
async function unarchiveThread(id) {
  if (!id) return;
  try {
    await api("/api/v1/conversations/" + encodeURIComponent(id) + "/unarchive", { method: "POST" });
    folders.archivedIds = (folders.archivedIds || []).filter((row) => String(row) !== String(id));
    renderConversations(searchInput.value);
  } catch (err) {
    alert(err.message || "Could not unarchive that conversation.");
  }
}
async function restoreTrashIds(ids) {
  const clean = (ids || []).map((id) => String(id)).filter(Boolean);
  if (!clean.length) return;
  try {
    for (const id of clean) {
      await api("/api/v1/trash/" + encodeURIComponent(id) + "/restore", { method: "POST" });
    }
    const gone = new Set(clean);
    folders.trash = (folders.trash || []).filter((row) => !gone.has(String(row.threadId)));
    renderConversations(searchInput.value);
    updateTitle();
  } catch (err) {
    alert(err.message || "Could not restore that conversation.");
  }
}
async function deleteTrashIds(ids, all) {
  const clean = (ids || []).map((id) => String(id)).filter(Boolean);
  if (!clean.length) return;
  const prompt = all && clean.length > 1 ? "Delete all?" : "Delete permanently?";
  if (!window.confirm(prompt)) return;
  try {
    for (const id of clean) {
      await api("/api/v1/trash/" + encodeURIComponent(id), { method: "DELETE" });
    }
    const gone = new Set(clean);
    folders.trash = (folders.trash || []).filter((row) => !gone.has(String(row.threadId)));
    conversations = conversations.filter((c) => !gone.has(String(c.id)));
    if (selectedId && gone.has(String(selectedId))) closeOpenThread();
    renderConversations(searchInput.value);
    updateTitle();
  } catch (err) {
    alert(err.message || "Could not delete that conversation.");
  }
}
async function blockNumber(number) {
  try {
    await api("/api/v1/blocked", {
      method: "POST",
      headers: { "Content-Type": "application/json; charset=UTF-8" },
      body: JSON.stringify({ number }),
    });
    folders.blocked = (folders.blocked || []).concat([number]);
    renderConversations("");
  } catch (err) {
    alert(err.message || "Could not block that number.");
  }
}
async function unblockNumber(number) {
  try {
    await api("/api/v1/blocked?number=" + encodeURIComponent(number), { method: "DELETE" });
    folders.blocked = (folders.blocked || []).filter((row) => row !== number);
    renderConversations(searchInput.value);
  } catch (err) {
    alert(err.message || "Could not unblock that number.");
  }
}
async function cancelScheduled(id) {
  if (!id) return;
  try {
    await api("/api/v1/scheduled/" + encodeURIComponent(id), { method: "DELETE" });
    folders.scheduled = (folders.scheduled || []).filter((row) => row.id !== id);
    renderConversations(searchInput.value);
  } catch (err) {
    alert(err.message || "Could not cancel that text.");
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
  const gen = ++threadLoadGen;
  threadHasMore = false;
  threadUserMoved = false;
  selectedId = id;
  const convo = conversations.find((c) => c.id === id);
  if (!convo) return;
  appView.classList.add("thread-open");
  emptyThread.hidden = true;
  threadPanel.hidden = false;
  document.getElementById("thread-name").textContent = convo.displayName;
  document.getElementById("thread-number").textContent = convo.address;
  const avWrap = document.createElement("div");
  avWrap.innerHTML = avatarHtml(convo.displayName, convo.avatarColor, convo.photoUrl);
  const av = avWrap.firstElementChild;
  av.id = "thread-avatar";
  av.title = "Contact details";
  av.setAttribute("role", "button");
  av.tabIndex = 0;
  av.addEventListener("click", showPeople);
  av.addEventListener("keydown", (e) => {
    if (e.key === "Enter" || e.key === " ") { e.preventDefault(); showPeople(); }
  });
  document.getElementById("thread-avatar").replaceWith(av);
  saveContactBtn.hidden = !canSaveContact(convo);
  syncPinButton();
  renderConversations(searchInput.value);
  const msgs = await api(`/api/v1/conversations/${id}/messages?limit=50`);
  if (gen !== threadLoadGen || selectedId !== id) return;
  threadHasMore = msgs.length >= 50;
  threadUserMoved = false;
  renderMessages(msgs.slice().reverse());
  if (mark) api(`/api/v1/conversations/${id}/read`, { method: "POST" }).catch(() => {});
  composeText.focus();
}

function renderMessages(msgs, opts) {
  openMessages = msgs || [];
  const laid = reactionLayout(openMessages);
  const keepScroll = !!(opts && opts.keepScroll);
  const prevScroll = messagesEl.scrollTop;
  messagesEl.innerHTML = "";
  let lastDay = "";
  openMessages.forEach((m, index) => {
    const key = messageKey(m, index);
    if (laid.hidden.has(key)) return;
    const day = new Date(m.timestamp).toDateString();
    if (day !== lastDay) {
      lastDay = day;
      const d = document.createElement("div");
      d.className = "day";
      d.textContent = formatDay(m.timestamp);
      messagesEl.appendChild(d);
    }
    const wrap = document.createElement("div");
    wrap.className = "bubble-wrap " + (m.incoming ? "in" : "out");
    const b = document.createElement("div");
    b.className = "bubble " + (m.incoming ? "in" : "out");
    let media = "";
    for (const att of m.attachments || []) {
      const mime = att.mimeType || "";
      const src = escapeHtml(att.url || "");
      if (!src) continue;
      if (mime.startsWith("image/")) {
        media += `<img alt="" src="${src}" />`;
      } else if (mime.startsWith("video/")) {
        media += `<video controls preload="metadata" src="${src}"></video>`;
      } else if (mime.startsWith("audio/")) {
        media += `<audio controls preload="none" src="${src}"></audio>`;
      }
    }
    let html = media;
    if ((m.body || "").trim()) html += linkify(m.body);
    else if (!media) html += "Media message";
    const meta = messageMeta(m);
    if (meta) html += `<div class="meta">${escapeHtml(meta)}</div>`;
    b.innerHTML = html;
    if (m.id) b.dataset.id = m.id;
    wrap.appendChild(b);
    const marks = laid.byKey.get(key) || [];
    if (marks.length) {
      wrap.classList.add("has-react");
      const chip = document.createElement("div");
      chip.className = "react-chip";
      chip.textContent = reactionChipText(marks);
      chip.title = reactionDescription(marks);
      wrap.appendChild(chip);
    }
    messagesEl.appendChild(wrap);
  });
  if (opts && opts.anchor) {
    messagesEl.scrollTop = Math.max(0, messagesEl.scrollHeight - opts.prevHeight + opts.prevTop);
  } else {
    messagesEl.scrollTop = keepScroll ? prevScroll : messagesEl.scrollHeight;
  }
}

async function loadOlderMessages() {
  if (threadPaging || !threadHasMore || !selectedId || !openMessages.length) return;
  const id = selectedId;
  const gen = threadLoadGen;
  const oldest = Number(openMessages[0] && openMessages[0].timestamp);
  if (!Number.isFinite(oldest) || oldest <= 0) {
    threadHasMore = false;
    return;
  }
  threadPaging = true;
  const prevHeight = messagesEl.scrollHeight;
  const prevTop = messagesEl.scrollTop;
  try {
    const older = await api(`/api/v1/conversations/${encodeURIComponent(id)}/messages?limit=50&before=${oldest + 1}`);
    if (gen !== threadLoadGen || selectedId !== id) return;
    const seen = new Set(openMessages.map((m) => m.id));
    const fresh = [];
    for (const m of older.slice().reverse()) {
      if (m.id && seen.has(m.id)) continue;
      fresh.push(m);
    }
    if (!fresh.length) {
      threadHasMore = false;
      return;
    }
    threadHasMore = older.length >= 50;
    renderMessages(fresh.concat(openMessages), { anchor: true, prevHeight, prevTop });
  } catch (_) {
  } finally {
    if (gen === threadLoadGen) threadPaging = false;
  }
}
function noteThreadUserMove() { threadUserMoved = true; }
messagesEl.addEventListener("wheel", noteThreadUserMove, { passive: true });
messagesEl.addEventListener("touchstart", noteThreadUserMove, { passive: true });
messagesEl.addEventListener("pointerdown", noteThreadUserMove);
messagesEl.addEventListener("scroll", () => {
  if (!threadUserMoved || messagesEl.scrollTop >= 48) return;
  loadOlderMessages();
});
function messageMeta(m) {
  const parts = [];
  if (phoneDetails.direction) parts.push(m.incoming ? "Received" : "Sent");
  if (phoneDetails.party) parts.push(m.incoming ? (m.displayName || "") : "You");
  if (phoneDetails.time) parts.push(formatMessageStamp(m.timestamp));
  if (phoneDetails.type) parts.push((m.type || "sms").toUpperCase());
  const slot = Number(m.simSlot);
  if ((phoneDetails.showSim || phoneDetails.sim) && Number.isInteger(slot) && slot >= 0) {
    parts.push("SIM " + (slot + 1));
  }
  return parts.filter(Boolean).join(" · ");
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
  if (!img || !messagesEl.contains(img)) return;
  e.preventDefault();
  openLightbox(img.currentSrc || img.src);
});
messagesEl.addEventListener("contextmenu", (e) => {
  const bubble = e.target.closest && e.target.closest(".bubble");
  if (!bubble || !messagesEl.contains(bubble) || !bubble.dataset.id) return;
  showMessageMenu(e, bubble.dataset.id);
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
  if (!gifEnabled) return;
  hideEmojiPanel();
  gifPanel.hidden = !gifPanel.hidden;
  if (!gifPanel.hidden) {
    gifSearch.focus();
    if (!gifGrid.childElementCount) searchGifs("reaction");
  }
});
document.getElementById("new-gif-btn").addEventListener("click", () => {
  if (!gifEnabled) return;
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
      btn.innerHTML = `<img alt="" src="${escapeHtml(gifFileSrc(hit.url))}" />`;
      btn.dataset.send = hit.sendUrl || hit.url;
      btn.addEventListener("click", () => pickGif(hit));
      gifGrid.appendChild(btn);
    }
    bindGifHover();
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
function gifSendUrl(hit) {
  return (hit && (hit.sendUrl || hit.url)) || "";
}
function gifFileSrc(url) {
  if (!url) return "";
  if (url.charAt(0) === "/") return url;
  return "/api/v1/gif-file?u=" + encodeURIComponent(url);
}
function pickGif(hit) {
  if (!gifEnabled) return;
  const send = gifSendUrl(hit);
  const preview = gifFileSrc(hit.url || send);
  const fileish = { name: (hit.title || "gif") + ".gif", type: "image/gif" };
  if (!newModal.hidden) {
    pendingNewGifUrl = send;
    pendingNewImage = null;
    renderPreview(newPendingPreview, fileish, () => {
      pendingNewGifUrl = null;
      newPendingPreview.hidden = true;
      newPendingPreview.innerHTML = "";
    }, preview);
  } else {
    pendingGifUrl = send;
    pendingImage = null;
    renderPreview(pendingPreview, fileish, () => {
      pendingGifUrl = null;
      pendingPreview.hidden = true;
      pendingPreview.innerHTML = "";
    }, preview);
  }
  gifPanel.hidden = true;
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
  btn.title = "Copied";
  btn.setAttribute("aria-label", "Copied");
  setTimeout(() => {
    btn.title = "Copy Number";
    btn.setAttribute("aria-label", "Copy Number");
  }, 1200);
});
function sortConversations() {
  conversations.sort((a, b) => Number(!!b.pinned) - Number(!!a.pinned) || (b.timestamp || 0) - (a.timestamp || 0));
}
function syncPinButton() {
  const btn = document.getElementById("pin-btn");
  if (!btn) return;
  const convo = conversations.find((c) => c.id === selectedId);
  const pinned = !!(convo && convo.pinned);
  btn.classList.toggle("on", pinned);
  btn.title = pinned ? "Unpin" : "Pin";
  btn.setAttribute("aria-label", pinned ? "Unpin" : "Pin");
  const off = btn.querySelector(".pin-off");
  const on = btn.querySelector(".pin-on");
  if (off) off.hidden = pinned;
  if (on) on.hidden = !pinned;
}
async function setPinned(id, pinned) {
  const convo = conversations.find((c) => c.id === id);
  if (!convo) return;
  const previous = !!convo.pinned;
  convo.pinned = pinned;
  sortConversations();
  syncPinButton();
  renderConversations(searchInput.value);
  hideConvoMenu();
  try {
    await api("/api/v1/conversations/" + encodeURIComponent(id) + "/pin", {
      method: "POST",
      headers: { "Content-Type": "application/json; charset=UTF-8" },
      body: JSON.stringify({ pinned }),
    });
  } catch (err) {
    convo.pinned = previous;
    sortConversations();
    syncPinButton();
    renderConversations(searchInput.value);
    alert(err.message || "Could not pin this conversation.");
  }
}
const convoMenu = document.getElementById("convo-menu");
let menuThreadId = null;
function hideConvoMenu() {
  if (convoMenu) convoMenu.hidden = true;
  menuThreadId = null;
}
function showConvoMenu(e, id) {
  e.preventDefault();
  e.stopPropagation();
  hideMessageMenu();
  hideNavMenu();
  if (!convoMenu) return;
  menuThreadId = id;
  const convo = conversations.find((c) => c.id === id);
  const pinBtn = document.getElementById("convo-menu-pin");
  if (pinBtn) pinBtn.textContent = convo && convo.pinned ? "Unpin conversation" : "Pin conversation";
  convoMenu.hidden = false;
  const x = Math.min(e.clientX, window.innerWidth - 220);
  const y = Math.min(e.clientY, window.innerHeight - 148);
  convoMenu.style.left = x + "px";
  convoMenu.style.top = y + "px";
}
function closeOpenThread() {
  threadLoadGen += 1;
  threadHasMore = false;
  appView.classList.remove("thread-open");
  selectedId = null;
  if (emptyThread) emptyThread.hidden = false;
  if (threadPanel) threadPanel.hidden = true;
  if (messagesEl) messagesEl.innerHTML = "";
}
async function markConversationRead(id) {
  if (!id) return;
  hideConvoMenu();
  const convo = conversations.find((c) => c.id === id);
  if (convo) convo.unread = 0;
  renderConversations(searchInput.value);
  updateTitle();
  try {
    await api("/api/v1/conversations/" + encodeURIComponent(id) + "/read", { method: "POST" });
  } catch (err) {
    alert(err.message || "Could not mark as read.");
    loadInbox().catch(() => {});
  }
}
async function deleteConversation(id) {
  if (!id) return;
  hideConvoMenu();
  if (!window.confirm("Delete this conversation and its messages from the phone?")) return;
  try {
    await api("/api/v1/conversations/" + encodeURIComponent(id), { method: "DELETE" });
  } catch (err) {
    alert(err.message || "Could not delete that conversation.");
    return;
  }
  conversations = conversations.filter((c) => c.id !== id);
  sortConversations();
  renderConversations(searchInput.value);
  updateTitle();
  if (selectedId === id) closeOpenThread();
}
const messageMenu = document.getElementById("message-menu");
let menuMessageId = null;
let menuImageUrl = "";
let menuCopyImage = false;
function hideMessageMenu() {
  if (messageMenu) messageMenu.hidden = true;
  menuMessageId = null;
  menuImageUrl = "";
  menuCopyImage = false;
}
function showMessageMenu(e, id) {
  e.preventDefault();
  e.stopPropagation();
  hideConvoMenu();
  hideNavMenu();
  if (!messageMenu || !id) return;
  const message = openMessages.find((m) => m.id === id);
  if (!message) return;
  menuMessageId = id;
  const image = messageImage(message, e);
  menuImageUrl = image ? image.url : "";
  const bodyText = (message.body || "").trim();
  const hitImage = !!(e.target.closest && e.target.closest("img"));
  menuCopyImage = !!menuImageUrl && (hitImage || !bodyText);
  const download = document.getElementById("message-menu-download");
  if (download) {
    download.hidden = !menuImageUrl;
    download.dataset.name = image ? image.name : "";
  }
  const title = document.getElementById("message-menu-title");
  if (title) title.textContent = message.incoming ? (message.displayName || "Message") : "You";
  const stamp = document.getElementById("message-menu-stamp");
  if (stamp) stamp.textContent = formatFullStamp(message.timestamp);
  const status = document.getElementById("message-menu-status");
  if (status) {
    status.hidden = !message.status;
    status.textContent = message.status || "";
  }
  const address = document.getElementById("message-menu-address");
  if (address) {
    address.hidden = !message.address;
    address.textContent = message.address || "";
  }
  const react = document.getElementById("message-menu-react");
  if (react) {
    react.innerHTML = "";
    const applied = youReactions(message);
    for (const spec of TAPBACKS) {
      const button = document.createElement("button");
      button.type = "button";
      button.textContent = spec.symbol;
      button.title = spec.label;
      button.setAttribute("aria-label", spec.label);
      if (applied.has(spec.symbol)) button.classList.add("on");
      button.addEventListener("click", (ev) => {
        ev.stopPropagation();
        hideMessageMenu();
        sendTapback(message, spec, applied.has(spec.symbol));
      });
      react.appendChild(button);
    }
  }
  const codeBtn = document.getElementById("message-menu-code");
  const code = verificationCode(message.body || "");
  if (codeBtn) {
    codeBtn.hidden = !code;
    codeBtn.onclick = (ev) => {
      ev.stopPropagation();
      hideMessageMenu();
      if (code) copyText(code);
    };
  }
  const links = document.getElementById("message-menu-links");
  if (links) {
    links.innerHTML = "";
    const found = messageLinks(message.body || "");
    found.slice(0, 4).forEach((url, index) => {
      const button = document.createElement("button");
      button.type = "button";
      button.textContent = found.length === 1 ? "Open link" : "Open " + linkHost(url);
      button.addEventListener("click", (ev) => {
        ev.stopPropagation();
        hideMessageMenu();
        const href = /^www\./i.test(url) ? "https://" + url : url;
        window.open(href, "_blank", "noopener");
      });
      if (index === 0 && found.length === 1) button.id = "message-menu-open-link";
      links.appendChild(button);
    });
  }
  messageMenu.hidden = false;
  const rect = messageMenu.getBoundingClientRect();
  const x = Math.max(8, Math.min(e.clientX, window.innerWidth - rect.width - 8));
  const y = Math.max(8, Math.min(e.clientY, window.innerHeight - rect.height - 8));
  messageMenu.style.left = x + "px";
  messageMenu.style.top = y + "px";
}
async function deleteMessage(id) {
  if (!selectedId || !id) return;
  if (!window.confirm("Delete this message from the phone?")) return;
  try {
    await api(
      "/api/v1/conversations/" + encodeURIComponent(selectedId) + "/messages/" + encodeURIComponent(id),
      { method: "DELETE" },
    );
  } catch (err) {
    alert(err.message || "Could not delete that message.");
    return;
  }
  await openThread(selectedId, false);
}
if (document.getElementById("convo-menu-pin")) {
  document.getElementById("convo-menu-pin").addEventListener("click", (e) => {
    e.stopPropagation();
    const id = menuThreadId;
    const convo = conversations.find((c) => c.id === id);
    if (id) setPinned(id, !(convo && convo.pinned));
  });
}
on("convo-menu-read", "click", (e) => {
  e.stopPropagation();
  const id = menuThreadId;
  if (id) markConversationRead(id);
});
on("convo-menu-delete", "click", (e) => {
  e.stopPropagation();
  const id = menuThreadId;
  if (id) deleteConversation(id);
});
on("message-menu-delete", "click", (e) => {
  e.stopPropagation();
  const id = menuMessageId;
  hideMessageMenu();
  if (id) deleteMessage(id);
});
on("message-menu-copy", "click", async (e) => {
  e.stopPropagation();
  const message = openMessages.find((m) => m.id === menuMessageId);
  const imageUrl = menuImageUrl;
  const asImage = menuCopyImage;
  hideMessageMenu();
  if (!message) return;
  if (asImage && imageUrl) {
    try {
      await copyImage(imageUrl);
    } catch (err) {
      alert(err.message || "Could not copy that image");
    }
    return;
  }
  copyText(message.body || "");
});
on("message-menu-download", "click", (e) => {
  e.stopPropagation();
  const url = menuImageUrl;
  const name = e.currentTarget && e.currentTarget.dataset ? e.currentTarget.dataset.name : "";
  hideMessageMenu();
  if (url) downloadFile(url, name || "image");
});
on("message-menu-close", "click", (e) => {
  e.stopPropagation();
  hideMessageMenu();
});
on("message-menu-unread", "click", async (e) => {
  e.stopPropagation();
  const thread = selectedId;
  hideMessageMenu();
  if (!thread) return;
  try {
    const res = await api("/api/v1/conversations/" + encodeURIComponent(thread) + "/unread", { method: "POST" });
    if (res && res.ok === false) {
      alert("No incoming message to mark unread");
      return;
    }
    const convo = conversations.find((c) => c.id === thread);
    if (convo) convo.unread = Math.max(1, convo.unread || 0);
    closeOpenThread();
    renderConversations(searchInput.value);
    updateTitle();
  } catch (err) {
    alert(err.message || "Could not mark as unread.");
  }
});
on("read-btn", "click", () => {
  if (selectedId) markConversationRead(selectedId);
});
on("delete-thread-btn", "click", () => {
  if (selectedId) deleteConversation(selectedId);
});
document.addEventListener("click", () => {
  hideConvoMenu();
  hideMessageMenu();
  hideNavMenu();
});
document.addEventListener("scroll", () => {
  hideConvoMenu();
  hideMessageMenu();
  hideNavMenu();
}, true);

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
  if (rejectDisabledMedia(file)) return;
  pendingImage = file;
  renderPreview(pendingPreview, file, () => {
    pendingImage = null;
    pendingPreview.hidden = true;
    pendingPreview.innerHTML = "";
    fileInput.value = "";
  });
}

function setPendingNewImage(file) {
  if (rejectDisabledMedia(file)) return;
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
  if (e.key !== "Enter" || e.isComposing || e.keyCode === 229) return false;
  if (enterSends) return !e.shiftKey;
  return e.ctrlKey || e.metaKey;
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
  if (pendingGifUrl && !gifEnabled) { alert("GIF sending is turned off on the phone."); return; }
  if (pendingImage && isAudioFile(pendingImage)) { alert("Voice notes are not available on this computer."); return; }
  if (!body && !pendingImage && !pendingGifUrl) return;
  const convo = conversations.find((c) => c.id === selectedId);
  sending = true;
  const sendBtn = document.getElementById("send-btn");
  const previousLabel = sendBtn.textContent;
  sendBtn.textContent = pendingGifUrl ? "Sending GIF…" : pendingImage ? "Sending picture…" : "Sending…";
  try {
    const dest = (convo?.recipients || []).filter(Boolean);
    const recipients = dest.length ? dest : [convo?.address].filter(Boolean);
    const payload = { body, recipients };
    if (phoneSubId >= 0) payload.subscriptionId = phoneSubId;
    if (pendingGifUrl) {
      payload.mediaUrl = pendingGifUrl;
    } else if (pendingImage) {
      payload.imageBase64 = await fileToBase64(pendingImage);
      payload.imageMime = pendingImage.type || "image/jpeg";
    }
    await api(`/api/v1/conversations/${selectedId}/messages`, {
      method: "POST",
      headers: { "Content-Type": "application/json; charset=UTF-8" },
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
  if (pendingNewGifUrl && !gifEnabled) { alert("GIF sending is turned off on the phone."); return; }
  if (pendingNewImage && isAudioFile(pendingNewImage)) { alert("Voice notes are not available on this computer."); return; }
  if (!number || (!body && !pendingNewImage && !pendingNewGifUrl)) return;
  try {
    const payload = { recipients: [number], body };
    if (phoneSubId >= 0) payload.subscriptionId = phoneSubId;
    if (pendingNewGifUrl) {
      payload.mediaUrl = pendingNewGifUrl;
    } else if (pendingNewImage) {
      payload.imageBase64 = await fileToBase64(pendingNewImage);
      payload.imageMime = pendingNewImage.type || "image/jpeg";
    }
    await api("/api/v1/conversations", {
      method: "POST",
      headers: { "Content-Type": "application/json; charset=UTF-8" },
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
  socket = new WebSocket(proto + "//" + location.host + "/api/v1/events");
  socket.onopen = () => {
    if (gen !== eventGen) return;
    const recovered = !phoneReachable;
    connLabel.textContent = "Live";
    if (recovered) markLive();
    if (recovered) loadInbox().catch(() => {});
  };
  socket.onclose = (ev) => {
    if (gen !== eventGen) return;
    if (ev && ev.reason === "unpaired") {
      token = "";
      localStorage.removeItem(TOKEN_KEY);
      showPair();
      setPairStatus("This computer is no longer paired. Enter the PIN shown on the phone.");
      markLive();
      return;
    }
    noteUnreachable("The live link to the phone closed.");
    scheduleReconnect();
  };
  socket.onmessage = (ev) => {
    let payload = {};
    try { payload = JSON.parse(ev.data); } catch (_) {}
    if (payload.type === "settings") {
      applyExperimental(payload);
      return;
    }
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
  const initial = escapeHtml(((name || "?").trim().charAt(0) || "?").toUpperCase());
  const img = photoUrl ? `<img alt="" src="${escapeHtml(photoUrl)}" />` : "";
  return `<div class="avatar" style="background:${escapeHtml(color || "#156b57")}">${initial}${img}</div>`;
}

document.addEventListener("error", (e) => {
  const img = e.target;
  if (!img || img.tagName !== "IMG" || !img.closest) return;
  if (img.closest(".avatar")) img.remove();
}, true);

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
        headers: { "Content-Type": "application/json; charset=UTF-8" },
        body: JSON.stringify({ name, number }),
      });
    } else {
      if (!selectedExistingContact) {
        alert("Pick an existing contact to add this number to.");
        return;
      }
      await api("/api/v1/contacts/" + encodeURIComponent(selectedExistingContact.id) + "/phones", {
        method: "POST",
        headers: { "Content-Type": "application/json; charset=UTF-8" },
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

function formatFullStamp(ts) {
  const n = Number(ts);
  if (!Number.isFinite(n) || n <= 0) return "";
  return new Date(n).toLocaleString(undefined, {
    weekday: "short",
    month: "short",
    day: "numeric",
    year: "numeric",
    hour: "numeric",
    minute: "2-digit",
    second: "2-digit",
  });
}
const TAPBACKS = [
  { symbol: "👍", verb: "Liked", removal: "Removed a like from", label: "Like" },
  { symbol: "❤️", verb: "Loved", removal: "Removed a heart from", label: "Love" },
  { symbol: "👎", verb: "Disliked", removal: "Removed a dislike from", label: "Dislike" },
  { symbol: "😂", verb: "Laughed at", removal: "Removed a laugh from", label: "Laugh" },
  { symbol: "‼️", verb: "Emphasized", removal: "Removed an exclamation from", label: "Emphasize" },
  { symbol: "❓", verb: "Questioned", removal: "Removed a question from", label: "Question" },
];
function tapbackQuote(body) {
  const trimmed = String(body || "").trim().replace(/[“”"]/g, "").trim();
  if (!trimmed) return null;
  if (trimmed.length <= 80) return trimmed;
  return trimmed.slice(0, 80).trimEnd() + "…";
}
function tapbackText(message, spec, remove) {
  const lead = remove ? spec.removal : spec.verb;
  const quote = tapbackQuote(message.body || "");
  if (quote) return lead + " “" + quote + "”";
  const atts = message.attachments || [];
  if (atts.some((a) => (a.mimeType || "").startsWith("image/"))) return lead + " an image";
  if (atts.some((a) => (a.mimeType || "").startsWith("video/"))) return lead + " a video";
  if (atts.some((a) => (a.mimeType || "").startsWith("audio/"))) return lead + " an audio message";
  return null;
}
function youReactions(message) {
  const laid = reactionLayout(openMessages);
  const index = openMessages.indexOf(message);
  const marks = laid.byKey.get(messageKey(message, index)) || [];
  const on = new Set();
  for (const mark of marks) {
    if (mark.names.indexOf("You") >= 0) on.add(mark.symbol);
  }
  return on;
}
const ECHO_WINDOW_MS = 180000;
const QUOTED_TAPBACK = /^(Removed a heart from|Removed a like from|Removed a dislike from|Removed a laugh from|Removed an exclamation from|Removed a question from|Laughed at|Emphasized|Questioned|Disliked|Loved|Liked)\s+[“"](.+)[”"]\s*\.?\s*$/s;
const EMOJI_TAPBACK = /^Reacted (\S+) to [“"](.+)[”"]\s*\.?\s*$/s;
const BARE_TAPBACK = /^(Removed a heart from|Removed a like from|Removed a dislike from|Removed a laugh from|Removed an exclamation from|Removed a question from|Laughed at|Emphasized|Questioned|Disliked|Loved|Liked)\s+(an image|a photo|a picture|a gif|a video|a movie|an audio message|a voice message|an attachment)\s*\.?\s*$/i;
function messageKey(message, index) {
  return message && message.id ? String(message.id) : "m-" + index;
}
function tapbackKind(verb) {
  const v = String(verb || "").toLowerCase();
  if (v.includes("heart") || v === "loved") return { kind: "love", symbol: "❤️" };
  if (v.includes("dislike") || v === "disliked") return { kind: "dislike", symbol: "👎" };
  if (v.includes("laugh") || v.startsWith("laughed")) return { kind: "laugh", symbol: "😂" };
  if (v.includes("exclamation") || v === "emphasized") return { kind: "emphasis", symbol: "‼️" };
  if (v.includes("question") || v === "questioned") return { kind: "question", symbol: "❓" };
  return { kind: "like", symbol: "👍" };
}
function mediaHint(phrase) {
  const p = String(phrase || "").trim().toLowerCase().replace(/^(?:…|\.\.\.)/, "").trim();
  if (p === "an image" || p === "a photo" || p === "a picture" || p === "a gif") return "image";
  if (p === "a video" || p === "a movie") return "video";
  if (p === "an audio message" || p === "a voice message") return "audio";
  if (p === "an attachment") return "any";
  return null;
}
function parseTapback(body) {
  const text = String(body || "").trim();
  const quoted = text.match(QUOTED_TAPBACK);
  if (quoted) {
    const verb = quoted[1];
    const quote = quoted[2].trim();
    const kind = tapbackKind(verb);
    return { kind: kind.kind, symbol: kind.symbol, removal: /^removed/i.test(verb), quote, media: mediaHint(quote) };
  }
  const emoji = text.match(EMOJI_TAPBACK);
  if (emoji) {
    const quote = emoji[2].trim();
    return { kind: "custom", symbol: emoji[1], removal: false, quote, media: mediaHint(quote) };
  }
  const bare = text.match(BARE_TAPBACK);
  if (!bare) return null;
  const verb = bare[1];
  const phrase = bare[2];
  const kind = tapbackKind(verb);
  return { kind: kind.kind, symbol: kind.symbol, removal: /^removed/i.test(verb), quote: phrase, media: mediaHint(phrase) || "any" };
}
function normalizeQuote(value) {
  return String(value || "").replace(/\u2019/g, "'").replace(/\u2018/g, "'").replace(/\s+/g, " ").trim();
}
function quoteMatches(body, rawQuote) {
  const trimmed = String(rawQuote || "").trim();
  const lead = trimmed.startsWith("…") || trimmed.startsWith("...");
  const tail = trimmed.endsWith("…") || trimmed.endsWith("...");
  let core = trimmed;
  if (lead) core = core.replace(/^(?:…|\.\.\.)/, "").trim();
  if (tail) core = core.replace(/(?:…|\.\.\.)$/, "").trim();
  if (core.length < 2) return false;
  const hay = normalizeQuote(body);
  const needle = normalizeQuote(core);
  if (needle.length < 2) return false;
  if (hay === needle || hay === normalizeQuote(trimmed)) return true;
  if (lead && !tail) return hay.endsWith(needle);
  if (tail && !lead) return hay.startsWith(needle);
  if (lead && tail) return hay.includes(needle);
  return needle.length >= 12 && hay.includes(needle);
}
function attachmentMime(att) {
  return (att && (att.mimeType || att.mime)) || "";
}
function matchesMedia(message, hint) {
  const atts = (message && message.attachments) || [];
  if (hint === "image") return atts.some((a) => attachmentMime(a).startsWith("image/"));
  if (hint === "video") return atts.some((a) => attachmentMime(a).startsWith("video/"));
  if (hint === "audio") return atts.some((a) => attachmentMime(a).startsWith("audio/"));
  if (hint === "any") return atts.length > 0;
  return false;
}
function findTapbackTarget(messages, index, tap) {
  let found = -1;
  for (let i = 0; i < index; i++) {
    if (parseTapback(messages[i].body)) continue;
    const matches = tap.media ? matchesMedia(messages[i], tap.media) : quoteMatches(messages[i].body || "", tap.quote);
    if (matches) found = i;
  }
  return found;
}
function reactorName(message) {
  if (!message.incoming) return "You";
  const name = String(message.displayName || "").trim();
  if (name) return name;
  const address = String(message.address || "").trim();
  return address || "Someone";
}
function isOwnEcho(messages, index) {
  const message = messages[index];
  if (!message || !message.incoming) return false;
  const text = String(message.body || "").trim();
  if (!text) return false;
  const at = Number(message.timestamp);
  return messages.some((other, i) => {
    if (i === index || other.incoming) return false;
    if (message.threadId && other.threadId && String(other.threadId) !== String(message.threadId)) return false;
    if (String(other.body || "").trim() !== text) return false;
    const delta = Math.abs(Number(other.timestamp) - at);
    return Number.isFinite(delta) && delta <= ECHO_WINDOW_MS;
  });
}
function reactionLayout(messages) {
  const hidden = new Set();
  const marks = new Map();
  (messages || []).forEach((message, index) => {
    const tap = parseTapback(message.body);
    if (!tap) return;
    const target = findTapbackTarget(messages, index, tap);
    if (target < 0) return;
    hidden.add(messageKey(message, index));
    if (isOwnEcho(messages, index)) return;
    const key = messageKey(messages[target], target);
    let kinds = marks.get(key);
    if (!kinds) {
      kinds = new Map();
      marks.set(key, kinds);
    }
    let people = kinds.get(tap.symbol);
    if (!people) {
      people = [];
      kinds.set(tap.symbol, people);
    }
    const name = reactorName(message);
    if (tap.removal) {
      for (let i = people.length - 1; i >= 0; i--) {
        if (people[i] === name) people.splice(i, 1);
      }
    } else if (people.indexOf(name) < 0) {
      people.push(name);
    }
  });
  const byKey = new Map();
  marks.forEach((kinds, key) => {
    const list = [];
    kinds.forEach((names, symbol) => {
      if (names.length) list.push({ symbol, names: names.slice() });
    });
    if (list.length) byKey.set(key, list);
  });
  return { hidden, byKey };
}
function reactionChipText(marks) {
  return marks.map((mark) => (mark.names.length > 1 ? mark.symbol + " " + mark.names.length : mark.symbol)).join("  ");
}
function reactionDescription(marks) {
  return marks.map((mark) => {
    const who = [...new Set(mark.names)].join(" and ") || "Someone";
    const verb = {
      "❤️": "loved",
      "👎": "disliked",
      "😂": "laughed at",
      "‼️": "emphasized",
      "❓": "questioned",
      "👍": "liked",
    }[mark.symbol] || ("reacted " + mark.symbol + " to");
    return who + " " + verb + " this";
  }).join(". ");
}
async function sendTapback(message, spec, remove) {
  if (!selectedId) return;
  const body = tapbackText(message, spec, remove);
  if (!body) {
    alert("Nothing to react to");
    return;
  }
  try {
    await api("/api/v1/conversations/" + encodeURIComponent(selectedId) + "/messages", {
      method: "POST",
      headers: { "Content-Type": "application/json; charset=UTF-8" },
      body: JSON.stringify({ body, recipients: [] }),
    });
    await openThread(selectedId, false);
  } catch (err) {
    alert(err.message || "Could not send that reaction.");
  }
}
function verificationCode(text) {
  if (!text || !String(text).trim()) return null;
  const keywords = /(?<![A-Za-z0-9])(?:verification|verify|passcodes?|passwords?|codes?|otps?|pins?|log[\s-]?in|one[\s-]?time|2[\s-]?fa|two[\s-]?factor|security|auth)(?![A-Za-z0-9])/gi;
  const keys = [];
  let match;
  while ((match = keywords.exec(text))) keys.push([match.index, match.index + match[0].length]);
  if (!keys.length) return null;
  const hits = [];
  const prefixed = /(?<![A-Za-z0-9])[A-Za-z]-(\d{4,8})(?!\d)/g;
  const plain = /(?<!\d)(\d{4,8})(?!\d)/g;
  const mixed = /(?<![A-Za-z0-9])(?=[A-Za-z0-9]*\d)(?=[A-Za-z0-9]*[A-Za-z])[A-Za-z0-9]{6,8}(?![A-Za-z0-9])/g;
  while ((match = prefixed.exec(text))) hits.push({ code: match[1], start: match.index, end: match.index + match[0].length });
  while ((match = plain.exec(text))) {
    const start = match.index;
    const end = start + match[0].length;
    if (start > 0 && text[start - 1] === "-") continue;
    if (end < text.length && text[end] === "-") continue;
    hits.push({ code: match[1], start, end });
  }
  while ((match = mixed.exec(text))) hits.push({ code: match[0], start: match.index, end: match.index + match[0].length });
  let best = null;
  for (const key of keys) {
    for (const hit of hits) {
      const gap = hit.end <= key[0] ? key[0] - hit.end : hit.start >= key[1] ? hit.start - key[1] : 0;
      if (gap > 60) continue;
      const score = gap * 1000 + Math.abs(hit.code.length - 6);
      if (!best || score < best.score) best = { code: hit.code, score };
    }
  }
  return best ? best.code : null;
}
function messageLinks(text) {
  if (!text) return [];
  const re = /(?<![\w@])((?:https?:\/\/|www\.)[^\s<>"']+)/gi;
  const trailing = /[.,;:!?)]+$/;
  const out = [];
  let match;
  while ((match = re.exec(text))) {
    const raw = match[1].replace(trailing, "");
    if (raw.length >= 8) out.push(raw);
  }
  return out;
}
function linkHost(url) {
  const href = /^www\./i.test(url) ? "https://" + url : url;
  const rest = href.split("://")[1] || href;
  return (rest.split("/")[0].split("?")[0] || "link").replace(/^www\./i, "") || "link";
}
function messageImage(message, event) {
  const attachments = message.attachments || [];
  const hit = event && event.target && event.target.closest && event.target.closest("img");
  if (hit && (hit.currentSrc || hit.src)) {
    const url = hit.currentSrc || hit.src;
    const matched = attachments.find((item) => item.url && url.indexOf(item.url) >= 0);
    return { url, name: fileNameFromUrl(url, (matched && matched.mimeType) || "") };
  }
  const att = attachments.find((item) => (item.mimeType || "").startsWith("image/") && item.url);
  if (!att) return null;
  return { url: att.url, name: fileNameFromUrl(att.url, att.mimeType || "") };
}
function fileNameFromUrl(url, mime) {
  try {
    const path = new URL(url, location.href).pathname;
    const base = decodeURIComponent(path.split("/").pop() || "");
    if (base && base.includes(".")) return base;
  } catch (_) {}
  const ext = mime === "image/png" ? "png" : mime === "image/gif" ? "gif" : mime === "image/webp" ? "webp" : "jpg";
  return "image." + ext;
}
function blobToPng(blob) {
  return new Promise((resolve, reject) => {
    const img = new Image();
    const objectUrl = URL.createObjectURL(blob);
    img.onload = () => {
      const canvas = document.createElement("canvas");
      canvas.width = img.naturalWidth || img.width || 1;
      canvas.height = img.naturalHeight || img.height || 1;
      const ctx = canvas.getContext("2d");
      if (!ctx) {
        URL.revokeObjectURL(objectUrl);
        reject(new Error("Could not copy that image"));
        return;
      }
      ctx.drawImage(img, 0, 0);
      canvas.toBlob((out) => {
        URL.revokeObjectURL(objectUrl);
        if (out) resolve(out);
        else reject(new Error("Could not copy that image"));
      }, "image/png");
    };
    img.onerror = () => {
      URL.revokeObjectURL(objectUrl);
      reject(new Error("Could not copy that image"));
    };
    img.src = objectUrl;
  });
}
async function copyImage(url) {
  if (!navigator.clipboard || typeof ClipboardItem === "undefined") {
    throw new Error("This browser cannot copy images");
  }
  const png = fetch(url, { credentials: "same-origin", headers: authHeaders() }).then(async (res) => {
    if (!res.ok) throw new Error("Could not copy that image");
    const blob = await res.blob();
    if (blob.type === "image/png") return blob;
    return blobToPng(blob);
  });
  await navigator.clipboard.write([new ClipboardItem({ "image/png": png })]);
}
function downloadFile(url, name) {
  const link = document.createElement("a");
  link.href = url;
  link.download = name || "image";
  link.rel = "noopener";
  document.body.appendChild(link);
  link.click();
  link.remove();
}
async function copyText(text) {
  const value = String(text || "");
  if (!value) return;
  try {
    await navigator.clipboard.writeText(value);
  } catch (_) {
    const ta = document.createElement("textarea");
    ta.value = value;
    document.body.appendChild(ta);
    ta.select();
    document.execCommand("copy");
    ta.remove();
  }
}
function hideNavMenu() {
  const menu = document.getElementById("nav-menu");
  const button = document.getElementById("nav-btn");
  if (menu) menu.hidden = true;
  if (button) button.setAttribute("aria-expanded", "false");
}
function showNavMenu() {
  const menu = document.getElementById("nav-menu");
  const button = document.getElementById("nav-btn");
  if (!menu || !button) return;
  hideConvoMenu();
  hideMessageMenu();
  const rect = button.getBoundingClientRect();
  menu.hidden = false;
  menu.style.left = Math.max(8, rect.left) + "px";
  menu.style.top = Math.min(rect.bottom + 6, window.innerHeight - 280) + "px";
  button.setAttribute("aria-expanded", "true");
}
function openComputer() {
  const modal = document.getElementById("computer-modal");
  const line = document.getElementById("computer-conn");
  if (line && connLabel) line.textContent = connLabel.textContent || "";
  if (modal) modal.hidden = false;
}
const navBtn = document.getElementById("nav-btn");
if (navBtn) {
  navBtn.addEventListener("click", (e) => {
    e.stopPropagation();
    const menu = document.getElementById("nav-menu");
    if (menu && !menu.hidden) hideNavMenu();
    else showNavMenu();
  });
}
document.querySelectorAll("#nav-menu button").forEach((button) => {
  button.addEventListener("click", (e) => {
    e.stopPropagation();
    hideNavMenu();
    const dest = button.dataset.nav;
    if (dest === "computer") openComputer();
    else if (dest === "settings") openTheme();
    else if (dest) setFolder(dest);
  });
});
const folderBack = document.getElementById("folder-back");
if (folderBack) folderBack.addEventListener("click", () => setFolder("inbox"));
const closeComputer = document.getElementById("close-computer");
const computerModal = document.getElementById("computer-modal");
if (closeComputer && computerModal) {
  closeComputer.addEventListener("click", () => { computerModal.hidden = true; });
  computerModal.addEventListener("click", (e) => { if (e.target === computerModal) computerModal.hidden = true; });
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

function formatMessageStamp(ts) {
  if (!ts) return "";
  const d = new Date(ts);
  const now = new Date();
  const time = d.toLocaleTimeString([], { hour: "numeric", minute: "2-digit", second: "2-digit" });
  const dayStart = (date) => new Date(date.getFullYear(), date.getMonth(), date.getDate()).getTime();
  const day = dayStart(d);
  const today = dayStart(now);
  const yesterday = new Date(today);
  yesterday.setDate(yesterday.getDate() - 1);
  if (day === today) return time;
  if (day === yesterday.getTime()) return "Yesterday " + time;
  const sameYear = d.getFullYear() === now.getFullYear();
  const date = d.toLocaleDateString([], sameYear
    ? { month: "short", day: "numeric" }
    : { month: "short", day: "numeric", year: "numeric" });
  return date + ", " + time;
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
  location.reload();
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
