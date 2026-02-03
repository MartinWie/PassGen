// Fallback copy method for browsers without clipboard API or insecure contexts
function fallbackCopyToClipboard(text) {
    const textArea = document.createElement('textarea');
    textArea.value = text;
    // Avoid scrolling to bottom
    textArea.style.top = '0';
    textArea.style.left = '0';
    textArea.style.position = 'fixed';
    textArea.style.opacity = '0';
    document.body.appendChild(textArea);
    textArea.focus();
    textArea.select();
    
    let success = false;
    try {
        success = document.execCommand('copy');
    } catch (err) {
        console.error('Fallback copy failed:', err);
    }
    
    document.body.removeChild(textArea);
    return success;
}

function copyToClipboard(elementId) {
    const element = document.getElementById(elementId);
    if (!element) {
        console.error('Copy target element not found:', elementId);
        const tooltip = document.getElementById('copy-tooltip-failed');
        if (tooltip) removeHideThenFadeout(tooltip);
        return;
    }
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

    // Use modern clipboard API if available, otherwise fallback
    if (navigator.clipboard && navigator.clipboard.writeText) {
        navigator.clipboard.writeText(textToCopy).then(
            () => {
                // Show copy success tooltip
                const tooltip = document.getElementById('copy-tooltip');
                if (tooltip) removeHideThenFadeout(tooltip);
            },
            (err) => {
                /* clipboard write failed, try fallback */
                console.warn('Clipboard API failed, trying fallback:', err);
                if (fallbackCopyToClipboard(textToCopy)) {
                    const tooltip = document.getElementById('copy-tooltip');
                    if (tooltip) removeHideThenFadeout(tooltip);
                } else {
                    const tooltip = document.getElementById('copy-tooltip-failed');
                    if (tooltip) removeHideThenFadeout(tooltip);
                }
            },
        );
    } else {
        // Fallback for browsers without clipboard API
        if (fallbackCopyToClipboard(textToCopy)) {
            const tooltip = document.getElementById('copy-tooltip');
            if (tooltip) removeHideThenFadeout(tooltip);
        } else {
            console.error('Failed to copy to clipboard');
            const tooltip = document.getElementById('copy-tooltip-failed');
            if (tooltip) removeHideThenFadeout(tooltip);
        }
    }
}

function copyShareUrl() {
    // Get the URL from the share-result area
    const shareResult = document.getElementById('share-result');
    const anchor = shareResult?.querySelector('a');
    const shareUrl = anchor?.href;

    if (!shareUrl) {
        console.error('No share URL found');
        const tooltip = document.getElementById('copy-tooltip-failed');
        if (tooltip) removeHideThenFadeout(tooltip);
        return;
    }

    // Use modern clipboard API if available, otherwise fallback
    if (navigator.clipboard && navigator.clipboard.writeText) {
        navigator.clipboard.writeText(shareUrl).then(
            () => {
                // Show copy success tooltip
                const tooltip = document.getElementById('copy-tooltip');
                if (tooltip) removeHideThenFadeout(tooltip);
            },
            (err) => {
                /* clipboard write failed, try fallback */
                console.warn('Clipboard API failed, trying fallback:', err);
                if (fallbackCopyToClipboard(shareUrl)) {
                    const tooltip = document.getElementById('copy-tooltip');
                    if (tooltip) removeHideThenFadeout(tooltip);
                } else {
                    const tooltip = document.getElementById('copy-tooltip-failed');
                    if (tooltip) removeHideThenFadeout(tooltip);
                }
            },
        );
    } else {
        // Fallback for browsers without clipboard API
        if (fallbackCopyToClipboard(shareUrl)) {
            const tooltip = document.getElementById('copy-tooltip');
            if (tooltip) removeHideThenFadeout(tooltip);
        } else {
            console.error('Failed to copy to clipboard');
            const tooltip = document.getElementById('copy-tooltip-failed');
            if (tooltip) removeHideThenFadeout(tooltip);
        }
    }
}

function removeHideThenFadeout(element) {
    if (!element) return;
    
    // Show element: remove invisible/opacity-0, add opacity-1
    element.classList.remove('invisible', 'opacity-0');
    element.classList.add('opacity-100');

    // Fade out tooltip after 2 seconds
    setTimeout(() => {
        // Start the fade out transition
        element.classList.remove('opacity-100');
        element.classList.add('opacity-0');

        // Wait for transition to complete before hiding
        setTimeout(() => {
            // After fading is complete, hide the element completely
            element.classList.add('invisible');
        }, 300); // Match this with CSS transition duration
    }, 2000);
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
    // Naming aligned to common OpenSSH conventions
    if (purpose === 'git' || purpose === 'ssh') {
        if (algo === 'ed25519') return purpose === 'git' ? 'id_ed25519_signing' : 'id_ed25519';
        if (algo.startsWith('ecdsa')) return purpose === 'git' ? 'id_ecdsa_signing' : 'id_ecdsa';
        if (algo.startsWith('rsa-')) return purpose === 'git' ? 'id_rsa_signing' : 'id_rsa';
    }
    return 'key';
}

function downloadKey(elementId) {
    const algo = document.getElementById('key-algorithm')?.value || 'ed25519';
    const purpose = document.getElementById('key-purpose')?.value || 'ssh';
    const isPublic = elementId.includes('public');
    const baseName = algoBaseName(algo, purpose);
    const filename = isPublic ? baseName + '.pub' : baseName; // keep no extension for private
    const text = document.getElementById(elementId).value;
    const needsNewline = text.endsWith('\n') ? '' : '\n';
    // Use octet-stream for private key without extension to avoid some browsers (Safari) appending .txt
    const mime = isPublic ? 'text/plain' : 'application/octet-stream';
    const blob = new Blob([text + needsNewline], {type: mime});
    const a = document.createElement('a');
    a.href = URL.createObjectURL(blob);
    a.download = filename;
    a.style.display = 'none';
    document.body.appendChild(a);
    a.click();
    setTimeout(() => {
        URL.revokeObjectURL(a.href);
        a.remove();
    }, 1500);
}

// Security helper: best-effort zeroing of Uint8Array buffers
// Note: JavaScript cannot guarantee memory clearing, but this reduces exposure window
function secureZero(arr) {
    if (arr && arr instanceof Uint8Array) {
        arr.fill(0);
    }
}

// Security helper: zero multiple arrays
function secureZeroAll(...arrays) {
    arrays.forEach(secureZero);
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

function base64UrlToBytes(b64url) {
    let b64 = b64url.replace(/-/g, '+').replace(/_/g, '/');
    while (b64.length % 4) b64 += '=';
    const str = atob(b64);
    const bytes = new Uint8Array(str.length);
    for (let i = 0; i < str.length; i++) bytes[i] = str.charCodeAt(i);
    return bytes;
}

function concatArrays(arrs) {
    return arrs.reduce((acc, cur) => {
        const out = new Uint8Array(acc.length + cur.length);
        out.set(acc, 0);
        out.set(cur, acc.length);
        return out;
    }, new Uint8Array());
}

function u32(n) {
    const b = new Uint8Array(4);
    new DataView(b.buffer).setUint32(0, n);
    return b;
}

function sshString(bytes) {
    return concatArrays([u32(bytes.length), bytes]);
}

function sshStringFromText(txt) {
    return sshString(new TextEncoder().encode(txt));
}

function mpint(bytes) { // minimal implementation
    // strip leading zeros
    let i = 0;
    while (i < bytes.length - 1 && bytes[i] === 0) i++;
    bytes = bytes.slice(i);
    // if high bit set prepend 0
    if (bytes.length && (bytes[0] & 0x80)) {
        const withZero = new Uint8Array(bytes.length + 1);
        withZero[0] = 0;
        withZero.set(bytes, 1);
        bytes = withZero;
    }
    return concatArrays([u32(bytes.length), bytes]);
}

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

function buildOpenSSHPrivateKeyEd25519(secretKey64, publicKey32, comment) {
    // secretKey64 is 64-byte Uint8Array (seed+pub), publicKey32 is 32-byte Uint8Array
    // Format: https://github.com/openssh/openssh-portable/blob/master/PROTOCOL.key
    function writeString(arr, str) {
        const enc = new TextEncoder();
        const bytes = enc.encode(str);
        const len = new Uint8Array(4);
        new DataView(len.buffer).setUint32(0, bytes.length);
        arr.push(len, bytes);
    }

    function writeBytes(arr, bytes) {
        const len = new Uint8Array(4);
        new DataView(len.buffer).setUint32(0, bytes.length);
        arr.push(len, bytes);
    }

    const algorithm = 'ssh-ed25519';
    const magic = new TextEncoder().encode('openssh-key-v1\0');
    const parts = [magic];
    // ciphername, kdfname, kdfoptions (none)
    writeString(parts, 'none');
    writeString(parts, 'none');
    writeString(parts, '');
    // number of keys (uint32 =1)
    const one = new Uint8Array(4);
    new DataView(one.buffer).setUint32(0, 1);
    parts.push(one);
    // public key (wire format)
    // wire: string alg, string pubkey
    const pubWireParts = [];
    writeString(pubWireParts, algorithm);
    writeBytes(pubWireParts, publicKey32);
    const pubWire = pubWireParts.reduce((acc, cur) => {
        const tmp = new Uint8Array(acc.length + cur.length);
        tmp.set(acc, 0);
        tmp.set(cur, acc.length);
        return tmp;
    }, new Uint8Array());
    // length-prefixed public wire
    const pubLen = new Uint8Array(4);
    new DataView(pubLen.buffer).setUint32(0, pubWire.length);
    parts.push(pubLen, pubWire);

    // Private block
    const privParts = [];
    const check = crypto.getRandomValues(new Uint8Array(4));
    privParts.push(check, check.slice()); // two identical check ints
    // key wire inside private: string alg, string pubkey, string priv(64 bytes), string comment
    writeString(privParts, algorithm);
    writeBytes(privParts, publicKey32);
    writeBytes(privParts, secretKey64);
    writeString(privParts, comment || '');
    // padding 1..n
    let totalLen = privParts.reduce((s, b) => s + b.length, 0);
    const padNeeded = (8 - (totalLen % 8)) % 8; // minimal to 8-byte block alignment
    for (let i = 1; i <= padNeeded; i++) {
        privParts.push(new Uint8Array([i]));
    }
    const privBlock = privParts.reduce((acc, cur) => {
        const tmp = new Uint8Array(acc.length + cur.length);
        tmp.set(acc, 0);
        tmp.set(cur, acc.length);
        return tmp;
    }, new Uint8Array());
    // length of private block
    const privLen = new Uint8Array(4);
    new DataView(privLen.buffer).setUint32(0, privBlock.length);
    parts.push(privLen, privBlock);

    const full = parts.reduce((acc, cur) => {
        const tmp = new Uint8Array(acc.length + cur.length);
        tmp.set(acc, 0);
        tmp.set(cur, acc.length);
        return tmp;
    }, new Uint8Array());
    const b64 = bytesToBase64(full).replace(/(.{70})/g, '$1\n');
    return `-----BEGIN OPENSSH PRIVATE KEY-----\n${b64}\n-----END OPENSSH PRIVATE KEY-----`;
}

function buildOpenSshEcdsaPublic(jwk, curveName, comment) {
    // curveName: nistp256/nistp384/nistp521
    const algo = 'ecdsa-sha2-' + curveName;
    const x = base64UrlToBytes(jwk.x);
    const y = base64UrlToBytes(jwk.y);
    const point = new Uint8Array(1 + x.length + y.length);
    point[0] = 0x04;
    point.set(x, 1);
    point.set(y, 1 + x.length);
    const blob = concatArrays([
        sshStringFromText(algo),
        sshStringFromText(curveName),
        sshString(point)
    ]);
    return algo + ' ' + bytesToBase64(blob) + (comment ? ' ' + comment : '');
}

function buildOpenSshRsaPublic(jwk, comment) {
    const algo = 'ssh-rsa';
    const e = base64UrlToBytes(jwk.e);
    const n = base64UrlToBytes(jwk.n);
    const blob = concatArrays([
        sshStringFromText(algo),
        mpint(e),
        mpint(n)
    ]);
    return algo + ' ' + bytesToBase64(blob) + (comment ? ' ' + comment : '');
}

function buildOpenSSHPrivateKeyECDSA(curveName, publicPoint, privateScalar, comment) {
    // curveName like nistp256 etc.
    const algo = 'ecdsa-sha2-' + curveName;

    function writeString(arr, data) {
        const len = new Uint8Array(4);
        new DataView(len.buffer).setUint32(0, data.length);
        arr.push(len, data);
    }

    function writeText(arr, text) {
        writeString(arr, new TextEncoder().encode(text));
    }

    function mpintRaw(bytes) { // mpint without outer length (we will add length as bignum2 per spec)
        // strip leading zeros
        let i = 0;
        while (i < bytes.length - 1 && bytes[i] === 0) i++;
        bytes = bytes.slice(i);
        if (bytes.length && (bytes[0] & 0x80)) {
            const withZero = new Uint8Array(bytes.length + 1);
            withZero[0] = 0;
            withZero.set(bytes, 1);
            bytes = withZero;
        }
        const len = new Uint8Array(4);
        new DataView(len.buffer).setUint32(0, bytes.length);
        return [len, bytes];
    }

    // Public key blob: string alg, string curve, string point
    const pointBlob = (() => {
        const parts = [];
        writeText(parts, algo);
        writeText(parts, curveName);
        writeString(parts, publicPoint);
        return parts.reduce((a, c) => {
            const t = new Uint8Array(a.length + c.length);
            t.set(a);
            t.set(c, a.length);
            return t;
        }, new Uint8Array());
    })();

    // Private block assembly similar to Ed25519 method
    const blocks = [];
    const magic = new TextEncoder().encode('openssh-key-v1\0');
    blocks.push(magic);
    writeText(blocks, 'none');
    writeText(blocks, 'none');
    writeText(blocks, '');
    const one = new Uint8Array(4);
    new DataView(one.buffer).setUint32(0, 1);
    blocks.push(one);
    // public key (length + blob)
    const pubLen = new Uint8Array(4);
    new DataView(pubLen.buffer).setUint32(0, pointBlob.length);
    blocks.push(pubLen, pointBlob);
    // private section
    const check = crypto.getRandomValues(new Uint8Array(4));
    const privParts = [];
    privParts.push(check, check.slice());
    writeText(privParts, algo);
    writeText(privParts, curveName);
    writeString(privParts, publicPoint); // public point
    // private scalar mpint
    const scalar = base64UrlToBytes(privateScalar); // JWK d is base64url
    const mp = mpintRaw(scalar);
    privParts.push(...mp);
    writeText(privParts, comment || '');
    // padding to 8
    let privLenCount = privParts.reduce((s, b) => s + b.length, 0);
    const pad = (8 - (privLenCount % 8)) % 8;
    for (let i = 1; i <= pad; i++) privParts.push(new Uint8Array([i]));
    const privBlock = privParts.reduce((a, c) => {
        const t = new Uint8Array(a.length + c.length);
        t.set(a);
        t.set(c, a.length);
        return t;
    }, new Uint8Array());
    const privLenBuf = new Uint8Array(4);
    new DataView(privLenBuf.buffer).setUint32(0, privBlock.length);
    blocks.push(privLenBuf, privBlock);
    const full = blocks.reduce((a, c) => {
        const t = new Uint8Array(a.length + c.length);
        t.set(a);
        t.set(c, a.length);
        return t;
    }, new Uint8Array());
    const b64 = bytesToBase64(full).replace(/(.{70})/g, '$1\n');
    return `-----BEGIN OPENSSH PRIVATE KEY-----\n${b64}\n-----END OPENSSH PRIVATE KEY-----`;
}

function buildOpenSSHPrivateKeyRSA(jwk, comment) {
    const algo = 'ssh-rsa';
    const n = base64UrlToBytes(jwk.n), e = base64UrlToBytes(jwk.e), d = base64UrlToBytes(jwk.d),
        p = base64UrlToBytes(jwk.p), q = base64UrlToBytes(jwk.q), iqmp = base64UrlToBytes(jwk.qi);

    function writeString(arr, data) {
        const len = new Uint8Array(4);
        new DataView(len.buffer).setUint32(0, data.length);
        arr.push(len, data);
    }

    function writeText(arr, text) {
        writeString(arr, new TextEncoder().encode(text));
    }

    function mpintFull(bytes) {
        let i = 0;
        while (i < bytes.length - 1 && bytes[i] === 0) i++;
        bytes = bytes.slice(i);
        if (bytes.length && (bytes[0] & 0x80)) {
            const withZero = new Uint8Array(bytes.length + 1);
            withZero[0] = 0;
            withZero.set(bytes, 1);
            bytes = withZero;
        }
        const len = new Uint8Array(4);
        new DataView(len.buffer).setUint32(0, bytes.length);
        return [len, bytes];
    }

    const pubBlobParts = [];
    writeText(pubBlobParts, algo);
    pubBlobParts.push(...mpintFull(e));
    pubBlobParts.push(...mpintFull(n));
    const pubBlob = pubBlobParts.reduce((a, c) => {
        const t = new Uint8Array(a.length + c.length);
        t.set(a);
        t.set(c, a.length);
        return t;
    }, new Uint8Array());
    const sections = [];
    const magic = new TextEncoder().encode('openssh-key-v1\0');
    sections.push(magic);
    writeText(sections, 'none');
    writeText(sections, 'none');
    writeText(sections, '');
    const one = new Uint8Array(4);
    new DataView(one.buffer).setUint32(0, 1);
    sections.push(one);
    const pubLen = new Uint8Array(4);
    new DataView(pubLen.buffer).setUint32(0, pubBlob.length);
    sections.push(pubLen, pubBlob);
    const check = crypto.getRandomValues(new Uint8Array(4));
    const priv = [];
    priv.push(check, check.slice());
    writeText(priv, algo);
    // key components: n,e,d,iqmp,p,q
    [n, e, d, iqmp, p, q].forEach(b => {
        const pair = mpintFull(b);
        priv.push(...pair);
    });
    writeText(priv, comment || '');
    let privLenCount = priv.reduce((s, b) => s + b.length, 0);
    const pad = (8 - (privLenCount % 8)) % 8;
    for (let i = 1; i <= pad; i++) priv.push(new Uint8Array([i]));
    const privBlock = priv.reduce((a, c) => {
        const t = new Uint8Array(a.length + c.length);
        t.set(a);
        t.set(c, a.length);
        return t;
    }, new Uint8Array());
    const privLenBuf = new Uint8Array(4);
    new DataView(privLenBuf.buffer).setUint32(0, privBlock.length);
    sections.push(privLenBuf, privBlock);
    const full = sections.reduce((a, c) => {
        const t = new Uint8Array(a.length + c.length);
        t.set(a);
        t.set(c, a.length);
        return t;
    }, new Uint8Array());
    const b64 = bytesToBase64(full).replace(/(.{70})/g, '$1\n');
    return `-----BEGIN OPENSSH PRIVATE KEY-----\n${b64}\n-----END OPENSSH PRIVATE KEY-----`;
}

async function generateKey() {
    const algo = document.getElementById('key-algorithm').value;
    const purpose = document.getElementById('key-purpose').value;
    const rawIdentifier = document.getElementById('key-identifier').value.trim();
    const identifier = isValidIdentifier(rawIdentifier) ? rawIdentifier : '';
    const pubOut = document.getElementById('public-key-output');
    const privOut = document.getElementById('private-key-output');
    const errAlert = document.getElementById('key-error-alert');
    const errText = document.getElementById('key-error-text');
    const spinner = document.getElementById('keygen-loading');
    const generateBtn = document.getElementById('generate-key-btn');
    
    if (rawIdentifier && !identifier) {
        errText.textContent = 'Invalid identifier: only printable ASCII up to 256 chars.';
        errAlert.classList.remove('hidden');
        return;
    }
    
    // Check for secure context requirement for ECDSA/RSA
    if (algo !== 'ed25519' && !window.isSecureContext) {
        errText.textContent = 'ECDSA and RSA require HTTPS. Use Ed25519 or access via HTTPS.';
        errAlert.classList.remove('hidden');
        return;
    }
    
    // Check crypto.subtle availability
    if (algo !== 'ed25519' && (!window.crypto || !window.crypto.subtle)) {
        errText.textContent = 'Web Crypto API not available. Use Ed25519 or a modern browser with HTTPS.';
        errAlert.classList.remove('hidden');
        return;
    }
    
    errAlert.classList.add('hidden');
    pubOut.value = '';
    privOut.value = '';
    spinner.classList.remove('hidden');
    generateBtn.disabled = true;
    
    // Track sensitive buffers for cleanup
    let sensitiveBuffers = [];
    
    try {
        let publicKeyText = '';
        let privateKeyText = '';
        if (algo === 'ed25519') {
            if (!window.nacl || !nacl.sign) throw new Error('TweetNaCl library not loaded. Please refresh the page.');
            const kp = nacl.sign.keyPair();
            const pub = kp.publicKey; // 32 bytes
            const sec = kp.secretKey; // 64 bytes (seed+pub)
            sensitiveBuffers.push(sec); // Track for cleanup
            const blob = buildSshBufferEd25519(pub);
            publicKeyText = 'ssh-ed25519 ' + bytesToBase64(blob) + (identifier ? ' ' + identifier : '');
            privateKeyText = buildOpenSSHPrivateKeyEd25519(sec, pub, identifier);
        } else if (algo.startsWith('ecdsa-')) {
            const curveMap = {'ecdsa-p256': 'nistp256', 'ecdsa-p384': 'nistp384', 'ecdsa-p521': 'nistp521'};
            const named = curveMap[algo];
            if (!named) throw new Error('Unsupported ECDSA curve');
            const webCurve = {nistp256: 'P-256', nistp384: 'P-384', nistp521: 'P-521'}[named];
            const keyPair = await crypto.subtle.generateKey({
                name: 'ECDSA',
                namedCurve: webCurve
            }, true, ['sign', 'verify']);
            const jwkPub = await crypto.subtle.exportKey('jwk', keyPair.publicKey);
            const jwkPriv = await crypto.subtle.exportKey('jwk', keyPair.privateKey);
            const x = base64UrlToBytes(jwkPub.x);
            const y = base64UrlToBytes(jwkPub.y);
            const point = new Uint8Array(1 + x.length + y.length);
            point[0] = 0x04;
            point.set(x, 1);
            point.set(y, 1 + x.length);
            // Track private scalar for cleanup (note: buildOpenSSHPrivateKeyECDSA internally 
            // converts jwkPriv.d to bytes again; we track our copy here for cleanup attempt,
            // but JS string immutability means the original string cannot be truly erased)
            const dBytes = base64UrlToBytes(jwkPriv.d);
            sensitiveBuffers.push(dBytes);
            publicKeyText = buildOpenSshEcdsaPublic(jwkPub, named, identifier);
            privateKeyText = buildOpenSSHPrivateKeyECDSA(named, point, jwkPriv.d, identifier);
            // Clear JWK private fields (best effort - strings are immutable in JS)
            jwkPriv.d = '';
        } else if (algo.startsWith('rsa-')) {
            const size = parseInt(algo.split('-')[1], 10);
            const keyPair = await crypto.subtle.generateKey({
                name: 'RSASSA-PKCS1-v1_5',
                modulusLength: size,
                publicExponent: new Uint8Array([1, 0, 1]),
                hash: 'SHA-256'
            }, true, ['sign', 'verify']);
            const jwkPub = await crypto.subtle.exportKey('jwk', keyPair.publicKey);
            const jwkPriv = await crypto.subtle.exportKey('jwk', keyPair.privateKey);
            // Track RSA private components for cleanup
            ['d', 'p', 'q', 'dp', 'dq', 'qi'].forEach(field => {
                if (jwkPriv[field]) {
                    sensitiveBuffers.push(base64UrlToBytes(jwkPriv[field]));
                }
            });
            publicKeyText = buildOpenSshRsaPublic(jwkPub, identifier);
            privateKeyText = buildOpenSSHPrivateKeyRSA(jwkPriv, identifier);
            // Clear JWK private fields (best effort - strings are immutable in JS)
            ['d', 'p', 'q', 'dp', 'dq', 'qi'].forEach(field => { jwkPriv[field] = ''; });
        } else {
            throw new Error('Unknown algorithm');
        }
        pubOut.value = publicKeyText;
        privOut.value = privateKeyText;
        updateInstructions(purpose, algo, identifier);
    } catch (e) {
        // Log detailed error to console for debugging
        console.error('Key generation failed:', e);
        
        let userMessage;
        // Check for specific error types and provide helpful messages
        if (e.message && e.message.includes('TweetNaCl')) {
            userMessage = e.message;
        } else if (e.message === 'Unsupported ECDSA curve' || e.message === 'Unknown algorithm') {
            userMessage = e.message;
        } else if (e.name === 'NotSupportedError') {
            userMessage = 'This algorithm is not supported by your browser.';
        } else if (e.name === 'OperationError') {
            userMessage = 'Key generation operation failed. Please try again.';
        } else if (!window.isSecureContext) {
            userMessage = 'Secure context required. Please use HTTPS or try Ed25519.';
        } else {
            userMessage = 'Key generation failed: ' + (e.message || 'Unknown error');
        }
        errText.textContent = userMessage;
        errAlert.classList.remove('hidden');
    } finally {
        spinner.classList.add('hidden');
        generateBtn.disabled = false;
        // Best-effort cleanup of sensitive buffers
        secureZeroAll(...sensitiveBuffers);
        sensitiveBuffers = null;
    }
}

function escapeHtml(str) {
    return str.replace(/[&<>"]/g, c => ({'&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;'}[c]));
}

// Identifier validation: printable ASCII (space 0x20 to ~ 0x7E), length 0..256
function isValidIdentifier(id) {
    return id.length <= 256 && /^[ -~]*$/.test(id);
}

function updateInstructions(purpose, algo, identifier) {
    const el = document.getElementById('key-instructions');
    if (!el) return;
    // Clear existing
    while (el.firstChild) el.removeChild(el.firstChild);
    const baseName = algoBaseName(algo, purpose);
    const safeAlgo = escapeHtml(algo);
    const safeBase = escapeHtml(baseName);
    const safeId = isValidIdentifier(identifier) ? identifier : '';
    if (purpose === 'ssh') {
        const p1 = document.createElement('p');
        p1.textContent = '1. Append Public Key to ~/.ssh/authorized_keys.';
        el.appendChild(p1);
        const p2 = document.createElement('p');
        p2.textContent = `2. Save Private Key as ${safeBase}; chmod 600 ${safeBase}.`;
        el.appendChild(p2);
        const p3 = document.createElement('p');
        p3.textContent = `3. Use: ssh -i ${safeBase} user@host.`;
        el.appendChild(p3);
    } else {
        if (algo === 'ed25519') {
            const intro = document.createElement('p');
            intro.textContent = 'Git SSH signing (Ed25519 OpenSSH key):';
            el.appendChild(intro);
            const ol = document.createElement('ol');
            ol.className = 'list-decimal list-inside';
            const steps = [
                `Save private key: ${safeBase}`,
                `Public key: ${safeBase}.pub → add as Public key.`,
                'git config --global gpg.format ssh',
                `git config --global user.signingkey ${safeBase}`,
                '(Optional) git config --global commit.gpgsign true',
                'Sign commits: git commit -S -m "msg"'
            ];
            steps.forEach(t => {
                const li = document.createElement('li');
                li.textContent = t;
                ol.appendChild(li);
            });
            el.appendChild(ol);
        } else {
            const p = document.createElement('p');
            p.innerHTML = `For Git SSH signing, Ed25519 is strongly recommended. Current algorithm <code>${safeAlgo}</code> is usable for SSH auth but not ideal for signing portability.`;
            el.appendChild(p);
        }
    }
}

function attachKeyGenHandlers() {
    const purposeSel = document.getElementById('key-purpose');
    const algoSel = document.getElementById('key-algorithm');
    const idWrap = document.getElementById('identifier-wrapper');
    const genBtn = document.getElementById('generate-key-btn');
    if (!purposeSel || !algoSel || !idWrap || !genBtn) return;
    purposeSel.addEventListener('change', () => {
        document.getElementById('key-identifier').value = '';
        document.getElementById('public-key-output').value = '';
        document.getElementById('private-key-output').value = '';
        document.getElementById('key-error-alert').classList.add('hidden');
        if (purposeSel.value === 'git') idWrap.classList.remove('hidden'); else idWrap.classList.add('hidden');
        updateInstructions(purposeSel.value, algoSel.value, '');
    });
    algoSel.addEventListener('change', () => {
        // Reset key fields when algorithm changes
        document.getElementById('public-key-output').value = '';
        document.getElementById('private-key-output').value = '';
        document.getElementById('key-error-alert').classList.add('hidden');
        updateInstructions(purposeSel.value, algoSel.value, document.getElementById('key-identifier').value.trim());
    });
    const idInput = document.getElementById('key-identifier');
    idInput.addEventListener('input', () => {
        const v = idInput.value.trim();
        if (v && !isValidIdentifier(v)) {
            idInput.classList.add('input-error');
        } else {
            idInput.classList.remove('input-error');
            updateInstructions(purposeSel.value, algoSel.value, v);
        }
    });
    genBtn.addEventListener('click', generateKey);
}

// Delegated handlers for copy/download buttons (CSP-safe)
document.addEventListener('click', (ev) => {
    const copyEl = ev.target.closest && ev.target.closest('.copy-btn');
    if (copyEl) {
        const targetId = copyEl.getAttribute('data-copy-target');
        if (targetId) copyToClipboard(targetId);
    }
    const dlEl = ev.target.closest && ev.target.closest('.download-btn');
    if (dlEl) {
        const targetId = dlEl.getAttribute('data-download-target');
        if (targetId) downloadKey(targetId);
    }
});

document.addEventListener('DOMContentLoaded', attachKeyGenHandlers);
