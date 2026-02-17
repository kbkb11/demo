import { resolveRoleRoute } from './auth.js';

const form = document.getElementById('loginPageForm');
const message = document.getElementById('loginMessage');

function showMessage(text, level = 'info') {
  if (!message) {
    return;
  }
  message.textContent = text;
  message.className = 'status-message ' + level;
}

async function login(username, password) {
  const response = await fetch('/api/auth/login', {
    method: 'POST',
    credentials: 'include',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password })
  });
  if (!response.ok) {
    let msg = '登录失败';
    try {
      const payload = await response.json();
      msg = payload?.message || msg;
    } catch (err) {
      // ignore
    }
    throw new Error(msg);
  }
  return response.json();
}

form?.addEventListener('submit', async event => {
  event.preventDefault();
  const username = document.getElementById('loginUsername')?.value.trim();
  const password = document.getElementById('loginPassword')?.value.trim();
  if (!username || !password) {
    showMessage('请输入用户名和密码', 'warning');
    return;
  }
  try {
    showMessage('登录中...', 'info');
    const user = await login(username, password);
    window.location.href = resolveRoleRoute(user.role);
  } catch (error) {
    showMessage(error.message, 'error');
  }
});

(async () => {
  try {
    const response = await fetch('/api/auth/me', { credentials: 'include' });
    if (response.ok) {
      const user = await response.json();
      window.location.href = resolveRoleRoute(user.role);
    }
  } catch (err) {
    // ignore
  }
})();
