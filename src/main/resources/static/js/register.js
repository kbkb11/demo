import { resolveRoleRoute } from './auth.js';

const form = document.getElementById('registerForm');
const message = document.getElementById('registerMessage');
const roleSelect = document.getElementById('regRole');
const studentBlock = document.getElementById('studentNumberBlock');
const teacherBlock = document.getElementById('teacherNumberBlock');

function showMessage(text, level = 'info') {
  if (!message) {
    return;
  }
  message.textContent = text;
  message.className = 'status-message ' + level;
}

function toggleBlocks() {
  const role = roleSelect?.value || 'STUDENT';
  if (role === 'TEACHER') {
    if (teacherBlock) teacherBlock.style.display = 'flex';
    if (studentBlock) studentBlock.style.display = 'none';
  } else {
    if (teacherBlock) teacherBlock.style.display = 'none';
    if (studentBlock) studentBlock.style.display = 'flex';
  }
}

roleSelect?.addEventListener('change', toggleBlocks);

toggleBlocks();

async function register(payload) {
  const response = await fetch('/api/auth/register', {
    method: 'POST',
    credentials: 'include',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload)
  });
  if (!response.ok) {
    let msg = '注册失败';
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
  const username = document.getElementById('regUsername')?.value.trim();
  const password = document.getElementById('regPassword')?.value.trim();
  const role = roleSelect?.value || 'STUDENT';
  const studentNumber = document.getElementById('regStudentNumber')?.value.trim();
  const teacherNumber = document.getElementById('regTeacherNumber')?.value.trim();
  if (!username || !password) {
    showMessage('请输入用户名和密码', 'warning');
    return;
  }
  if (role === 'STUDENT' && !studentNumber) {
    showMessage('学生注册必须填写学号', 'warning');
    return;
  }
  if (role === 'TEACHER' && !teacherNumber) {
    showMessage('教师注册必须填写工号', 'warning');
    return;
  }
  try {
    showMessage('注册中...', 'info');
    const user = await register({ username, password, role, studentNumber, teacherNumber });
    showMessage('注册成功，正在跳转...', 'success');
    window.location.href = resolveRoleRoute(user.role);
  } catch (error) {
    showMessage(error.message, 'error');
  }
});
