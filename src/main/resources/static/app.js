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
    const filename = isPublic ? baseName + '.pub' : baseName; // no extension for private
    const text = document.getElementById(elementId).value;
    const blob = new Blob([text + (text.endsWith('\n') ? '' : '\n')], {type: 'text/plain'});
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
            publicKeyText = buildOpenSshEcdsaPublic(jwkPub, named, identifier);
            privateKeyText = buildOpenSSHPrivateKeyECDSA(named, point, jwkPriv.d, identifier);
        } else if (algo.startsWith('rsa-')) {
            const size = parseInt(algo.split('-')[1], 10);
            const keyPair = await crypto.subtle.generateKey({
                name: 'RSA-PSS',
                modulusLength: size,
                publicExponent: new Uint8Array([1, 0, 1]),
                hash: 'SHA-256'
            }, true, ['sign', 'verify']);
            const jwkPub = await crypto.subtle.exportKey('jwk', keyPair.publicKey);
            const jwkPriv = await crypto.subtle.exportKey('jwk', keyPair.privateKey);
            publicKeyText = buildOpenSshRsaPublic(jwkPub, identifier);
            privateKeyText = buildOpenSSHPrivateKeyRSA(jwkPriv, identifier);
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
        el.innerHTML = `<p>1. Append Public Key to <code>~/.ssh/authorized_keys</code>.</p><p>2. Save Private Key as <code>${baseName}</code>; <code>chmod 600 ${baseName}</code>.</p><p>3. Use: <code>ssh -i ${baseName} user@host</code>.</p>`;
    } else { // git signing
        if (algo === 'ed25519') {
            el.innerHTML = `<p>Git SSH signing (Ed25519 OpenSSH key):</p><ol class='list-decimal list-inside'><li>Save private key: <code>${baseName}</code></li><li>Public key: <code>${baseName}.pub</code> → add as Signing key.</li><li><code>git config --global gpg.format ssh</code></li><li><code>git config --global user.signingkey ${baseName}.pub</code> (or <code>${baseName}</code> to reference private).</li><li>(Optional) <code>git config --global commit.gpgsign true</code></li><li>(Optional local verify) <code>echo '${identifier || 'comment'} $(cat ${baseName}.pub)' >> ~/.ssh/allowed_signers</code>; <code>git config --global gpg.ssh.allowedSignersFile ~/.ssh/allowed_signers</code></li><li>Sign commits: <code>git commit -S -m "msg"</code></li></ol>`;
        } else {
            el.innerHTML = `<p>For Git SSH signing, Ed25519 is strongly recommended. Current algorithm <code>${algo}</code> is usable for SSH auth but not ideal for signing portability.</p>`;
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
        // Reset form on purpose switch
        algoSel.value = 'ed25519';
        document.getElementById('key-identifier').value = '';
        document.getElementById('public-key-output').value = '';
        document.getElementById('private-key-output').value = '';
        document.getElementById('key-error-alert').classList.add('hidden');
        if (purposeSel.value === 'git') idWrap.classList.remove('hidden'); else idWrap.classList.add('hidden');
        updateInstructions(purposeSel.value, algoSel.value, '');
    });
    algoSel.addEventListener('change', () => updateInstructions(purposeSel.value, algoSel.value, document.getElementById('key-identifier').value.trim()));
    document.getElementById('key-identifier').addEventListener('input', () => updateInstructions(purposeSel.value, algoSel.value, document.getElementById('key-identifier').value.trim()));
    genBtn.addEventListener('click', generateKey);
}

document.addEventListener('DOMContentLoaded', attachKeyGenHandlers);
