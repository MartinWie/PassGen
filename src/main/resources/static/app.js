function copyToClipboard(elementId) {
    const element = document.getElementById(elementId);
    let textToCopy;

    // Check if element is an input or textarea
    if (element.tagName === 'INPUT' || element.tagName === 'TEXTAREA') {
        element.select();
        element.setSelectionRange(0, 99999);
        textToCopy = element.value;
    } else {
        // For other elements like p, div, etc.
        textToCopy = element.textContent || element.innerText;
    }

    navigator.clipboard.writeText(textToCopy).then(
        () => {
            // Show copy success tooltip
            const tooltip = document.getElementById('copy-tooltip');
            removeHideThenFadeout(tooltip);
        },
        () => {
            /* clipboard write failed */
            console.error('Failed to copy to clipboard :(');
            // Show copy failure tooltip
            const tooltip = document.getElementById('copy-tooltip-failed');
            removeHideThenFadeout(tooltip);
        },
    );
}

function copyShareUrl() {
    // Get the URL from the share-result area
    const shareUrl = document.getElementById('share-result').querySelector('a').href

    if (!shareUrl) {
        console.error('No share URL found');
        const tooltip = document.getElementById('copy-tooltip-failed');
        removeHideThenFadeout(tooltip);
        return;
    }

    navigator.clipboard.writeText(shareUrl).then(
        () => {
            // Show copy success tooltip
            const tooltip = document.getElementById('copy-tooltip');
            removeHideThenFadeout(tooltip);
        },
        () => {
            /* clipboard write failed */
            console.error('Failed to copy to clipboard :(');
            // Show copy failure tooltip
            const tooltip = document.getElementById('copy-tooltip-failed');
            removeHideThenFadeout(tooltip);
        },
    );
}

function removeHideThenFadeout(element) {
    element.classList.remove('hidden');

    // Fade out tooltip after 2 seconds
    setTimeout(() => {
        // Start the fade out transition
        element.style.opacity = '0';

        // Wait for transition to complete before hiding
        setTimeout(() => {
            // After fading is complete, hide the element
            element.classList.add('hidden');
            // Only reset opacity after the element is hidden
            element.style.opacity = '1';
        }, 300); // Match this with your CSS transition duration
    }, 6000);
}

document.addEventListener("DOMContentLoaded", (event) => {
    document.body.addEventListener('htmx:beforeSwap', function (evt) {
        if (evt.detail.xhr.status === 422 || evt.detail.xhr.status === 404 || evt.detail.xhr.status === 401 || evt.detail.xhr.status === 400) {
            // Allow 422, 404, 401 and 400 responses to swap.
            //
            // set isError to false to avoid error logging in console
            evt.detail.shouldSwap = true;
            evt.detail.isError = false;
        }
    });
});

// Default loading animation for elements that trigger a request(add skeleton class from daisyUI)
document.addEventListener("htmx:configRequest", function (evt) {
    // Overriding the event when htmx starts a request
    let element = evt.detail.elt;
    element.classList.add('skeleton');

    // Adding an event listener to remove the class after the request completes
    element.addEventListener('htmx:afterRequest', function clearLoading() {
        element.classList.remove('skeleton');

        // Optionally remove the event listener afterward to prevent memory leaks
        element.removeEventListener('htmx:afterRequest', clearLoading);
    });
});

function algoBaseName(algo, purpose) {
    if (purpose === 'ssh') {
        if (algo === 'ed25519') return 'id_ed25519';
        if (algo.startsWith('ecdsa')) return 'id_ecdsa';
        if (algo.startsWith('rsa-')) return 'id_rsa';
    } else { // git signing
        if (algo === 'ed25519') return 'ed25519_signing_key';
        if (algo.startsWith('ecdsa')) return algo.replace('ecdsa-', '').replace('-', '_') + '_signing_key';
        if (algo.startsWith('rsa-')) return algo.replace('rsa-', 'rsa_') + '_signing_key';
    }
    return 'key';
}

function downloadKey(elementId) {
    const algo = document.getElementById('key-algorithm')?.value || 'ed25519';
    const purpose = document.getElementById('key-purpose')?.value || 'ssh';
    const isPublic = elementId.includes('public');
    const baseName = algoBaseName(algo, purpose);
    const filename = isPublic ? baseName + '.pub' : (purpose === 'ssh' ? baseName : baseName + '.pem');
    const text = document.getElementById(elementId).value;
    const blob = new Blob([text + (text.endsWith('\n') ? '' : '\n')], {type: 'application/x-pem-file'});
    const a = document.createElement('a');
    a.href = URL.createObjectURL(blob);
    a.download = filename;
    a.click();
    setTimeout(() => URL.revokeObjectURL(a.href), 1500);
}

// Base64 helpers
function bytesToBase64(bytes) {
    let binary = '';
    const len = bytes.length;
    for (let i = 0; i < len; i++) binary += String.fromCharCode(bytes[i]);
    return btoa(binary);
}

function strToBytes(str) {
    return new TextEncoder().encode(str);
}

// Build SSH authorized_keys blob: string algo, string pubkeyRaw
function buildSshBufferEd25519(pubKey) {
    const algo = strToBytes('ssh-ed25519');
    const totalLen = 4 + algo.length + 4 + pubKey.length;
    const buf = new Uint8Array(totalLen);
    let offset = 0;
    const view = new DataView(buf.buffer);
    view.setUint32(offset, algo.length);
    offset += 4;
    buf.set(algo, offset);
    offset += algo.length;
    view.setUint32(offset, pubKey.length);
    offset += 4;
    buf.set(pubKey, offset);
    return buf;
}

function toPem(label, derBytes) {
    const b64 = bytesToBase64(new Uint8Array(derBytes));
    const wrapped = b64.replace(/(.{64})/g, '$1\n');
    return `-----BEGIN ${label}-----\n${wrapped}\n-----END ${label}-----`;
}

async function generateKey() {
    const algo = document.getElementById('key-algorithm').value;
    const purpose = document.getElementById('key-purpose').value;
    const identifier = document.getElementById('key-identifier').value.trim();
    const pubOut = document.getElementById('public-key-output');
    const privOut = document.getElementById('private-key-output');
    const errAlert = document.getElementById('key-error-alert');
    const errText = document.getElementById('key-error-text');
    const spinner = document.getElementById('keygen-loading');
    errAlert.classList.add('hidden');
    pubOut.value = '';
    privOut.value = '';
    spinner.classList.remove('hidden');
    try {
        let publicKeyText = '';
        let privateKeyText = '';
        if (algo === 'ed25519') {
            if (!window.nacl || !nacl.sign) throw new Error('TweetNaCl not loaded');
            const kp = nacl.sign.keyPair();
            const pub = kp.publicKey; // 32 bytes
            const sec = kp.secretKey; // 64 bytes (seed+pub)
            if (purpose === 'ssh') {
                const blob = buildSshBufferEd25519(pub);
                const auth = 'ssh-ed25519 ' + bytesToBase64(blob) + (identifier ? ' ' + identifier : '');
                publicKeyText = auth;
            } else {
                publicKeyText = 'ed25519 ' + bytesToBase64(pub) + (identifier ? ' ' + identifier : '');
            }
            privateKeyText = toPem('PRIVATE KEY', sec);
        } else if (algo.startsWith('ecdsa-')) {
            const curveMap = {'ecdsa-p256': 'P-256', 'ecdsa-p384': 'P-384', 'ecdsa-p521': 'P-521'};
            const namedCurve = curveMap[algo];
            if (!namedCurve) throw new Error('Unsupported ECDSA curve');
            const keyPair = await crypto.subtle.generateKey({name: 'ECDSA', namedCurve}, true, ['sign', 'verify']);
            const spki = await crypto.subtle.exportKey('spki', keyPair.publicKey);
            const pkcs8 = await crypto.subtle.exportKey('pkcs8', keyPair.privateKey);
            const pubB64 = bytesToBase64(new Uint8Array(spki));
            if (purpose === 'ssh') {
                publicKeyText = `ecdsa-sha2-${namedCurve.toLowerCase()} ${pubB64}${identifier ? ' ' + identifier : ''} (NOT OPENSSH WIRE FORMAT)`;
            } else {
                publicKeyText = `${algo} ${pubB64}${identifier ? ' ' + identifier : ''}`;
            }
            privateKeyText = toPem('PRIVATE KEY', pkcs8);
        } else if (algo.startsWith('rsa-')) {
            const size = parseInt(algo.split('-')[1], 10);
            const keyPair = await crypto.subtle.generateKey({
                name: 'RSA-PSS',
                modulusLength: size,
                publicExponent: new Uint8Array([1, 0, 1]),
                hash: 'SHA-256'
            }, true, ['sign', 'verify']);
            const spki = await crypto.subtle.exportKey('spki', keyPair.publicKey);
            const pkcs8 = await crypto.subtle.exportKey('pkcs8', keyPair.privateKey);
            const pubB64 = bytesToBase64(new Uint8Array(spki));
            if (purpose === 'ssh') {
                publicKeyText = 'ssh-rsa ' + pubB64 + (identifier ? ' ' + identifier : '') + ' (NOT OPENSSH FORMAT)';
            } else {
                publicKeyText = algo + ' ' + pubB64 + (identifier ? ' ' + identifier : '');
            }
            privateKeyText = toPem('PRIVATE KEY', pkcs8);
        } else {
            throw new Error('Unknown algorithm');
        }
        pubOut.value = publicKeyText;
        privOut.value = privateKeyText;
        updateInstructions(purpose, algo, identifier);
    } catch (e) {
        errText.textContent = e.message || String(e);
        errAlert.classList.remove('hidden');
    } finally {
        spinner.classList.add('hidden');
    }
}

function updateInstructions(purpose, algo, identifier) {
    const el = document.getElementById('key-instructions');
    if (!el) return;
    const baseName = algoBaseName(algo, purpose);
    if (purpose === 'ssh') {
        el.innerHTML = `<p>1. Copy the Public Key field above and append it as a single line to <code>~/.ssh/authorized_keys</code> on the target host.</p><p>2. Save the Private Key locally as <code>${baseName}</code> (no extension). Run: <code>chmod 600 ${baseName}</code>.</p><p>3. Connect with: <code>ssh -i ${baseName} user@host</code>.</p>`;
    } else { // git signing
        el.innerHTML = `<p>Use this key for Git commit signing via SSH (Git 2.34+):</p><ol class='list-decimal list-inside'><li>Store the private key as <code>${baseName}.pem</code> (or convert to OpenSSH format if using SSH signing).</li><li>Add the public key to your hosting provider (GitHub / GitLab) as an SSH signing key.</li><li>Enable: <code>git config --global gpg.format ssh</code></li><li>Set: <code>git config --global user.signingkey &lt;path-to-private-key-or-public-key-id&gt;</code></li><li>Sign: <code>git commit -S -m "msg"</code></li></ol><p>Identifier: ${identifier || '(none)'}.</p>`;
    }
}

function attachKeyGenHandlers() {
    const purposeSel = document.getElementById('key-purpose');
    const algoSel = document.getElementById('key-algorithm');
    const idWrap = document.getElementById('identifier-wrapper');
    const genBtn = document.getElementById('generate-key-btn');
    if (!purposeSel || !algoSel || !idWrap || !genBtn) return;
    purposeSel.addEventListener('change', () => {
        if (purposeSel.value === 'git') idWrap.classList.remove('hidden'); else idWrap.classList.add('hidden');
        updateInstructions(purposeSel.value, algoSel.value, document.getElementById('key-identifier').value.trim());
    });
    algoSel.addEventListener('change', () => updateInstructions(purposeSel.value, algoSel.value, document.getElementById('key-identifier').value.trim()));
    document.getElementById('key-identifier').addEventListener('input', () => updateInstructions(purposeSel.value, algoSel.value, document.getElementById('key-identifier').value.trim()));
    genBtn.addEventListener('click', generateKey);
}

document.addEventListener('DOMContentLoaded', attachKeyGenHandlers);
