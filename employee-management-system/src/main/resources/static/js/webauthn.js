/*
 * Minimal WebAuthn client for Spring Security's built-in passkey endpoints:
 *   POST /webauthn/register/options     -> creation options        (signed-in user)
 *   POST /webauthn/register             -> store the new credential (signed-in user)
 *   POST /webauthn/authenticate/options -> request options          (anonymous)
 *   POST /login/webauthn                -> verify assertion, log in (anonymous)
 * CSRF token/header are read from <meta name="_csrf"> / <meta name="_csrf_header">.
 */
(function () {
  function b64uToBuf(s) {
    s = s.replace(/-/g, '+').replace(/_/g, '/');
    var pad = s.length % 4 ? '='.repeat(4 - (s.length % 4)) : '';
    var bin = atob(s + pad);
    var buf = new Uint8Array(bin.length);
    for (var i = 0; i < bin.length; i++) buf[i] = bin.charCodeAt(i);
    return buf.buffer;
  }
  function bufToB64u(b) {
    var bytes = new Uint8Array(b), s = '';
    for (var i = 0; i < bytes.length; i++) s += String.fromCharCode(bytes[i]);
    return btoa(s).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
  }
  function csrfHeaders() {
    var t = document.querySelector('meta[name="_csrf"]');
    var h = document.querySelector('meta[name="_csrf_header"]');
    var o = {};
    if (t && h) o[h.content] = t.content;
    return o;
  }
  function post(url, body) {
    var headers = csrfHeaders();
    var opts = { method: 'POST', headers: headers, credentials: 'same-origin' };
    if (body !== undefined) {
      headers['Content-Type'] = 'application/json';
      opts.body = JSON.stringify(body);
    }
    return fetch(url, opts);
  }
  function supported() {
    return window.PublicKeyCredential && navigator.credentials;
  }

  window.registerPasskey = async function (label) {
    if (!supported()) throw new Error('This browser does not support passkeys.');
    var r = await post('/webauthn/register/options');
    if (!r.ok) throw new Error('Could not start passkey registration (HTTP ' + r.status + ').');
    var options = await r.json();
    options.challenge = b64uToBuf(options.challenge);
    options.user.id = b64uToBuf(options.user.id);
    (options.excludeCredentials || []).forEach(function (c) { c.id = b64uToBuf(c.id); });

    var cred = await navigator.credentials.create({ publicKey: options });
    var body = {
      publicKey: {
        credential: {
          id: cred.id,
          rawId: bufToB64u(cred.rawId),
          response: {
            attestationObject: bufToB64u(cred.response.attestationObject),
            clientDataJSON: bufToB64u(cred.response.clientDataJSON),
            transports: cred.response.getTransports ? cred.response.getTransports() : []
          },
          type: cred.type,
          clientExtensionResults: cred.getClientExtensionResults(),
          authenticatorAttachment: cred.authenticatorAttachment
        },
        label: label
      }
    };
    var res = await post('/webauthn/register', body);
    if (!res.ok) throw new Error('The server rejected the passkey (HTTP ' + res.status + ').');
  };

  window.loginWithPasskey = async function () {
    if (!supported()) throw new Error('This browser does not support passkeys.');
    var r = await post('/webauthn/authenticate/options');
    if (!r.ok) throw new Error('Could not start passkey sign-in (HTTP ' + r.status + ').');
    var options = await r.json();
    options.challenge = b64uToBuf(options.challenge);
    (options.allowCredentials || []).forEach(function (c) { c.id = b64uToBuf(c.id); });

    var cred = await navigator.credentials.get({ publicKey: options });
    var body = {
      id: cred.id,
      rawId: bufToB64u(cred.rawId),
      response: {
        authenticatorData: bufToB64u(cred.response.authenticatorData),
        clientDataJSON: bufToB64u(cred.response.clientDataJSON),
        signature: bufToB64u(cred.response.signature),
        userHandle: cred.response.userHandle ? bufToB64u(cred.response.userHandle) : null
      },
      type: cred.type,
      clientExtensionResults: cred.getClientExtensionResults(),
      authenticatorAttachment: cred.authenticatorAttachment
    };
    var res = await post('/login/webauthn', body);
    if (!res.ok) throw new Error('Passkey sign-in failed (HTTP ' + res.status + ').');
    var data = await res.json();
    window.location.href = data.redirectUrl || '/';
  };
})();
