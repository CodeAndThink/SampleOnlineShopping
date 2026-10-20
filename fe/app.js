const $ = id => document.getElementById(id);
let auth, sdk, user;
function error(e) { $('status').textContent = 'Không thực hiện được'; $('output').textContent = e.message || String(e); }
function account(u) {
  user = u;
  for (const id of ['logout', 'refresh', 'copy']) $(id).disabled = !u;
  if (!u) { $('identity').textContent = 'Chưa đăng nhập'; $('authenticated').checked = false; }
}
async function showIdentity(force = false) {
  if (!user) return;
  const token = await user.getIdTokenResult(force);
  $('identity').textContent = JSON.stringify({email: user.email, uid: user.uid, roles: token.claims.roles || [], expires: token.expirationTime}, null, 2);
}
$('setup').onsubmit = async event => {
  event.preventDefault();
  const button = event.submitter;
  button.disabled = true;
  try {
    const {initializeApp} = await import('https://www.gstatic.com/firebasejs/13.0.0/firebase-app.js');
    sdk = await import('https://www.gstatic.com/firebasejs/13.0.0/firebase-auth.js');
    const config = Object.fromEntries(['apiKey', 'projectId', 'authDomain', 'appId'].map(k => [k, $(k).value.trim()]));
    auth = sdk.getAuth(initializeApp(config));
    await sdk.setPersistence(auth, sdk.inMemoryPersistence);
    sdk.onIdTokenChanged(auth, u => { account(u); showIdentity().catch(error); });
    $('login').disabled = false;
    $('sdk-status').textContent = 'Đã khởi tạo SDK. Đăng nhập để kiểm tra cấu hình.';
    for (const input of $('setup').querySelectorAll('input')) input.disabled = true;
  } catch (e) { error(e); $('sdk-status').textContent = 'Không tải/khởi tạo được SDK. Kiểm tra cấu hình và mạng; tải lại trang để thử lại.'; }
};
$('login').onclick = async () => {
  try { const result = await sdk.signInWithPopup(auth, new sdk.GoogleAuthProvider()); account(result.user); $('authenticated').checked = true; await showIdentity(); }
  catch(e) { error(e); }
};
$('logout').onclick = async () => { try { await sdk.signOut(auth); account(null); } catch(e) { error(e); } };
$('refresh').onclick = () => showIdentity(true).catch(error);
$('copy').onclick = async () => {
  try { await navigator.clipboard.writeText(await user.getIdToken()); $('status').textContent = 'Đã sao chép ID token'; } catch(e) { error(e); }
};
async function request(method, path, body, button) {
  button.disabled = true;
  $('status').textContent = 'Đang gửi…';
  $('request-headers').textContent = 'Đang chuẩn bị request…';
  $('response-headers').textContent = 'Chưa có response';
  $('output').textContent = '';
  try {
    const headers = {};
    if ($('authenticated').checked) {
      if (!user) throw new Error('Hãy đăng nhập hoặc bỏ chọn gửi token.');
      headers.Authorization = `Bearer ${await user.getIdToken()}`;
    }
    if (body) headers['Content-Type'] = 'application/json';
    const visibleHeaders = {...headers};
    if (visibleHeaders.Authorization) visibleHeaders.Authorization = 'Bearer [ID token đã che]';
    $('request-headers').textContent = `${method} ${path}\n` + (Object.entries(visibleHeaders).map(([key, value]) => `${key}: ${value}`).join('\n') || '(Trang không đặt thêm header)');
    const res = await fetch(path, {method, headers, body: body ? JSON.stringify(body) : undefined});
    $('response-headers').textContent = Array.from(res.headers, ([key, value]) => `${key}: ${value}`).join('\n') || '(Không có header đọc được)';
    const raw = await res.text();
    $('status').textContent = `${method} · HTTP ${res.status}`;
    try { $('output').textContent = JSON.stringify(JSON.parse(raw), null, 2); }
    catch { $('output').textContent = raw || '(Response không có body)'; }
  } catch(e) { error(e); } finally { button.disabled = false; }
}
$('search').onsubmit = event => {
  event.preventDefault();
  const query = new URLSearchParams({keyword: $('keyword').value, page: $('page').value, size: $('size').value});
  request('GET', `/api/products?${query}`, null, event.submitter);
};
$('create').onsubmit = event => {
  event.preventDefault();
  request('POST', '/api/products', {name: $('name').value, description: $('description').value, price: Number($('price').value)}, event.submitter);
};
