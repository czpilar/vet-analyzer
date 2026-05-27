const $ = (id) => document.getElementById(id);

const POLL_MS = 3000;

const state = {
    selectedId: null,
    sessionsKey: null,
    detailKey: null,
    seen: {},
    summaries: [],
    detailCount: null,
    initialized: false,
};

async function fetchJson(url, options = {}) {
    const res = await fetch(url, options);
    if (!res.ok) {
        throw new Error(`${res.status} ${res.statusText}`);
    }
    return res.json();
}

async function refreshStatus() {
    try {
        const { running } = await fetchJson("/api/analyzer/status");
        renderStatus(running);
    } catch (e) {
        renderStatus(null);
    }
}

function renderStatus(running) {
    const dot = $("status-dot");
    const text = $("status-text");
    dot.className = "dot " + (running === true ? "dot-running" : running === false ? "dot-stopped" : "dot-unknown");
    text.textContent = running === true ? "running" : running === false ? "stopped" : "unknown";

    $("btn-start").disabled = running === true;
    $("btn-stop").disabled = running === false;
    $("btn-restart").disabled = running === null;
}

async function callLifecycle(action) {
    try {
        await fetchJson(`/api/analyzer/${action}`, { method: "POST" });
    } catch (e) {
        alert(`Failed to ${action}: ${e.message}`);
    }
    await refreshStatus();
}

async function refreshSessions() {
    try {
        const summaries = await fetchJson("/api/sessions");
        updateSeen(summaries);
        state.summaries = summaries;
        const key = summaries.map((s) => `${s.id}|${s.messageCount}|${s.endedAt || ""}`).join("\n");
        if (key !== state.sessionsKey || !state.sessionsKey) {
            state.sessionsKey = key;
            renderSessionsList(summaries);
        }
    } catch (e) {
        $("sessions-list").innerHTML = `<li style="color:#ef4444;padding:16px;">Failed to load: ${e.message}</li>`;
    }
}

function updateSeen(summaries) {
    for (const s of summaries) {
        if (!state.initialized || s.id === state.selectedId) {
            state.seen[s.id] = s.messageCount;
        }
    }
    state.initialized = true;
}

function hasUnseen(s) {
    if (s.id === state.selectedId) {
        return false;
    }
    const seen = state.seen[s.id];
    if (seen === undefined) {
        return true;
    }
    return s.messageCount > seen;
}

function renderSessionsList(summaries) {
    const ul = $("sessions-list");
    ul.innerHTML = "";
    if (summaries.length === 0) {
        ul.innerHTML = `<li style="color:#9ca3af;padding:16px;text-align:center;">No sessions yet</li>`;
        return;
    }
    for (const s of summaries) {
        const li = document.createElement("li");
        li.dataset.id = s.id;
        if (s.id === state.selectedId) {
            li.classList.add("selected");
        } else if (hasUnseen(s)) {
            li.classList.add("unseen");
        }
        const active = !s.endedAt;
        li.innerHTML = `
            <div class="session-line1">
                <span class="session-analyzer">${escapeHtml(s.analyzer || "(unknown)")}</span>
                <span class="session-badge ${active ? "badge-active" : "badge-closed"}">${active ? "active" : "closed"}</span>
            </div>
            <div class="session-line2">
                <span>${escapeHtml(s.remote || "")} &middot; ${s.messageCount} msg</span>
                <span class="session-time">${escapeHtml(s.startedAt || "")}</span>
            </div>
        `;
        li.addEventListener("click", () => selectSession(s.id));
        ul.appendChild(li);
    }
}

async function selectSession(id) {
    state.selectedId = id;
    state.detailKey = null;
    state.detailCount = null;
    const s = state.summaries.find((x) => x.id === id);
    if (s) {
        state.seen[id] = s.messageCount;
    }
    document.querySelectorAll("#sessions-list li").forEach((li) => {
        const match = li.dataset.id === id;
        li.classList.toggle("selected", match);
        if (match) {
            li.classList.remove("unseen");
        }
    });
    await refreshDetail();
}

async function refreshDetail() {
    const id = state.selectedId;
    if (!id) {
        return;
    }
    try {
        const detail = await fetchJson(`/api/sessions/${encodeURIComponent(id)}`);
        const key = `${detail.summary.messageCount}|${detail.summary.endedAt || ""}`;
        if (key !== state.detailKey) {
            state.detailKey = key;
            renderDetail(detail);
        }
    } catch (e) {
        renderDetailError(e.message);
    }
}

function renderDetail(detail) {
    $("detail-placeholder").hidden = true;
    $("detail-content").hidden = false;

    const s = detail.summary;
    $("detail-summary").innerHTML = `
        <div class="key">Analyzer</div><div class="val">${escapeHtml(s.analyzer || "(unknown)")}</div>
        <div class="key">Session ID</div><div class="val">${escapeHtml(s.sessionId)}</div>
        <div class="key">Remote</div><div class="val">${escapeHtml(s.remote || "")}</div>
        <div class="key">Started</div><div class="val">${escapeHtml(s.startedAt || "")}</div>
        <div class="key">Ended</div><div class="val">${escapeHtml(s.endedAt || "— active —")}</div>
        <div class="key">Messages</div><div class="val">${s.messageCount}</div>
    `;

    const msgsHost = $("detail-messages");
    msgsHost.innerHTML = "";
    const ordered = [...detail.messages].reverse();
    const added = state.detailCount == null ? 0 : Math.max(0, ordered.length - state.detailCount);
    ordered.forEach((m, i) => {
        const card = document.createElement("div");
        card.className = "message";
        if (i < added) {
            card.classList.add("flash-new");
        }
        card.innerHTML = `
            <div class="message-header">
                <span class="message-type">${escapeHtml(m.type || "(unknown)")}</span>
                <span class="message-time">${escapeHtml(m.timestamp || "")}</span>
            </div>
            <div class="message-section">
                <h4>Raw</h4>
                <pre>${escapeHtml(m.raw || "")}</pre>
            </div>
            ${m.parsed ? `
            <div class="message-section">
                <h4>Parsed</h4>
                ${renderParsed(m.parsed)}
            </div>` : ""}
        `;
        msgsHost.appendChild(card);
    });
    state.detailCount = ordered.length;
}

const PARSED_SKIP = new Set(["analyzerType", "rawData", "receivedAt"]);

function labelKey(key) {
    return String(key)
        .replace(/([a-z0-9])([A-Z])/g, "$1 $2")
        .replace(/([A-Z]+)([A-Z][a-z])/g, "$1 $2")
        .toLowerCase();
}

function renderParsed(parsed) {
    const scalars = [];
    const tables = [];

    for (const [key, value] of Object.entries(parsed)) {
        if (PARSED_SKIP.has(key)) {
            continue;
        }
        if (Array.isArray(value) && value.length > 0 && typeof value[0] === "object" && value[0] !== null) {
            tables.push({ key, rows: value });
        } else {
            scalars.push({ key, value });
        }
    }

    let html = "";
    if (scalars.length > 0) {
        html += `<div class="parsed-grid">`;
        for (const { key, value } of scalars) {
            html += `<div class="key">${escapeHtml(labelKey(key))}</div><div class="val">${formatScalar(value)}</div>`;
        }
        html += `</div>`;
    }
    for (const { key, rows } of tables) {
        html += renderTable(key, rows);
    }
    return html;
}

function renderTable(name, rows) {
    const columns = [...new Set(rows.flatMap((r) => Object.keys(r)))];
    let html = `<div class="parsed-table-name">${escapeHtml(labelKey(name))}</div>`;
    html += `<table class="parsed-table"><thead><tr>`;
    for (const col of columns) {
        html += `<th>${escapeHtml(labelKey(col))}</th>`;
    }
    html += `</tr></thead><tbody>`;
    for (const row of rows) {
        html += `<tr>`;
        for (const col of columns) {
            html += `<td>${formatScalar(row[col])}</td>`;
        }
        html += `</tr>`;
    }
    html += `</tbody></table>`;
    return html;
}

function formatScalar(value) {
    if (value == null || value === "") {
        return `<span class="empty">—</span>`;
    }
    return escapeHtml(value);
}

function renderDetailError(msg) {
    $("detail-placeholder").hidden = false;
    $("detail-content").hidden = true;
    $("detail-placeholder").textContent = `Failed to load: ${msg}`;
}

function escapeHtml(s) {
    if (s == null) {
        return "";
    }
    return String(s)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;");
}

function init() {
    $("btn-start").addEventListener("click", () => callLifecycle("start"));
    $("btn-stop").addEventListener("click", () => callLifecycle("stop"));
    $("btn-restart").addEventListener("click", () => callLifecycle("restart"));
    $("btn-refresh").addEventListener("click", () => {
        state.sessionsKey = null;
        state.detailKey = null;
        refreshSessions();
        refreshDetail();
    });

    refreshStatus();
    refreshSessions();

    setInterval(refreshStatus, 5000);
    setInterval(refreshSessions, POLL_MS);
    setInterval(refreshDetail, POLL_MS);
}

init();
