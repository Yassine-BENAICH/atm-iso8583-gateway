const templates = {
    "0800": {
        "mti": "0800",
        "stan": "000001",
        "transmissionDateTime": "",
        "acquiringInstitutionId": "000001",
        "networkManagementCode": "301"
    },
    "0100": {
        "mti": "0100",
        "pan": "4111111111111111",
        "processingCode": "000000",
        "amount": "000000010000",
        "transmissionDateTime": "",
        "stan": "000001",
        "localTime": "",
        "localDate": "",
        "expirationDate": "2712",
        "merchantCategoryCode": "6011",
        "posEntryMode": "021",
        "posConditionCode": "00",
        "acquiringInstitutionId": "000001",
        "retrievalReferenceNumber": "123456789012",
        "terminalId": "TERM0001",
        "merchantId": "MERCHANT000001 ",
        "cardAcceptorNameLocation": "ATM BRANCH 01 PARIS FR",
        "currencyCode": "978"
    },
    "0200": {
        "mti": "0200",
        "pan": "5500000000000004",
        "processingCode": "010000",
        "amount": "000000025000",
        "transmissionDateTime": "",
        "stan": "000099",
        "localTime": "",
        "localDate": "",
        "terminalId": "TERM0042",
        "merchantId": "MERCHANT000001 ",
        "currencyCode": "840",
        "pinData": "1234567890ABCDEF"
    },
    "0400": {
        "mti": "0400",
        "pan": "4111111111111111",
        "processingCode": "000000",
        "amount": "000000010000",
        "transmissionDateTime": "",
        "stan": "000001",
        "acquiringInstitutionId": "000001",
        "retrievalReferenceNumber": "123456789012",
        "terminalId": "TERM0001",
        "originalDataElements": "01000000012345600000010000301020500"
    }
};

let stats = {
    sent: 0,
    success: 0,
    totalLatency: 0
};

// UI Elements
const jsonInput = document.getElementById('json-input');
const templateSelect = document.getElementById('template-select');
const sendBtn = document.getElementById('send-btn');
const jsonOutput = document.getElementById('json-output');
const loader = document.getElementById('loader');
const responseBadge = document.getElementById('response-badge');
const fieldExplorer = document.getElementById('field-explorer');
const logBody = document.getElementById('log-body');

// Initialize
function init() {
    loadConfig();
    updateTemplate();

    templateSelect.addEventListener('change', updateTemplate);
    sendBtn.addEventListener('click', transmit);
}

async function loadConfig() {
    try {
        const resp = await fetch('/api/iso8583/config');
        if (resp.ok) {
            const config = await resp.json();
            document.getElementById('cfg-host').textContent = config.host;
            document.getElementById('cfg-port').textContent = config.port;
            document.getElementById('cfg-inst-id').textContent = config.institutionId;
            document.getElementById('cfg-timeout').textContent = `${config.connectTimeout}ms / ${config.readTimeout}ms`;

            document.getElementById('connection-dot').classList.add('online');
            document.getElementById('connection-text').textContent = 'Gateway Ready';
        }
    } catch (e) {
        console.error("Config load failed", e);
    }
}

function updateTemplate() {
    const type = templateSelect.value;
    const template = JSON.parse(JSON.stringify(templates[type]));

    // Auto-fill dates
    const now = new Date();
    template.transmissionDateTime = formatDate(now, "MMDDHHmmss");
    if (template.localTime !== undefined) template.localTime = formatDate(now, "HHmmss");
    if (template.localDate !== undefined) template.localDate = formatDate(now, "MMDD");

    jsonInput.value = JSON.stringify(template, null, 2);
}

function formatDate(date, format) {
    const pad = (n) => String(n).padStart(2, '0');
    const MM = pad(date.getUTCMonth() + 1);
    const DD = pad(date.getUTCDate());
    const HH = pad(date.getUTCHours());
    const mm = pad(date.getUTCMinutes());
    const ss = pad(date.getUTCSeconds());

    if (format === "MMDDHHmmss") return MM + DD + HH + mm + ss;
    if (format === "HHmmss") return HH + mm + ss;
    if (format === "MMDD") return MM + DD;
    return "";
}

async function transmit() {
    const startTime = Date.now();
    let payload;
    try {
        payload = JSON.parse(jsonInput.value);
    } catch (e) {
        alert("Invalid JSON payload");
        return;
    }

    // UI Feedback
    sendBtn.disabled = true;
    loader.style.display = 'block';
    jsonOutput.textContent = "";
    responseBadge.style.display = 'none';
    fieldExplorer.innerHTML = "";
    document.getElementById('field-breakdown-title').style.display = 'none';

    try {
        const response = await fetch('/api/iso8583/send', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        const result = await response.json();
        const latency = Date.now() - startTime;

        displayResponse(result);
        updateStats(result.status === "SUCCESS", latency);
        addToLog(payload.mti, payload.stan, result);

    } catch (e) {
        jsonOutput.textContent = "// Network Error:\n" + e.message;
        updateStats(false, 0);
    } finally {
        sendBtn.disabled = false;
        loader.style.display = 'none';
    }
}

function displayResponse(data) {
    jsonOutput.textContent = JSON.stringify(data, null, 2);

    responseBadge.style.display = 'inline-block';
    responseBadge.className = 'response-status ' + (data.status === "SUCCESS" ? 'status-success' : 'status-error');
    responseBadge.textContent = data.status;

    if (data.status !== "ERROR") {
        document.getElementById('field-breakdown-title').style.display = 'block';

        // Show core fields
        const coreFields = {
            'MTI': data.mti,
            '3': data.processingCode,
            '4': data.amount,
            '11': data.stan,
            '39': data.responseCode,
            '70': data.networkManagementCode
        };

        Object.entries(coreFields).forEach(([tag, val]) => {
            if (val) addFieldTag(tag, val);
        });

        // Show additional fields
        if (data.additionalFields) {
            Object.entries(data.additionalFields).forEach(([tag, val]) => {
                addFieldTag(tag, val);
            });
        }
    }
}

function addFieldTag(tag, val) {
    const el = document.createElement('div');
    el.className = 'field-tag';
    el.innerHTML = `<span class="field-num">DE ${tag}</span><span class="field-val" title="${val}">${val}</span>`;
    fieldExplorer.appendChild(el);
}

function updateStats(isSuccess, latency) {
    stats.sent++;
    if (isSuccess) stats.success++;
    if (latency > 0) stats.totalLatency += latency;

    document.getElementById('stats-sent').textContent = stats.sent;
    document.getElementById('stats-rate').textContent = Math.round((stats.success / stats.sent) * 100) + "%";
    document.getElementById('stats-latency').textContent = stats.sent > 0 ? Math.round(stats.totalLatency / stats.sent) + "ms" : "0ms";
}

function addToLog(mti, stan, result) {
    const row = document.createElement('tr');
    const time = new Date().toLocaleTimeString();
    const statusClass = result.status === "SUCCESS" ? 'status-success' : 'status-error';
    const desc = result.status === "ERROR" ? result.errorMessage : (result.responseDescription || result.responseCode);

    row.innerHTML = `
        <td class="log-mti">${mti}</td>
        <td>${stan || '-'}</td>
        <td><span class="response-status ${statusClass}">${result.status}</span></td>
        <td>${desc}</td>
        <td class="log-time">${time}</td>
    `;

    logBody.prepend(row);
    if (logBody.children.length > 20) logBody.lastChild.remove();
}

init();
