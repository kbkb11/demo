export function resolveRoleRoute(role) {
  const routes = {
    ADMIN: '/manage.html',
    TEACHER: '/analysis.html',
    STUDENT: '/student.html'
  };
  return routes[(role || '').toUpperCase()] || '/dashboard.html';
}

export async function fetchCurrentUser() {
  const response = await fetch('/api/auth/me', { credentials: 'include' });
  if (!response.ok) {
    return null;
  }
  return response.json();
}

export async function ensureRoleAccess(pageMode) {
  const user = await fetchCurrentUser();
  if (!user) {
    window.location.href = '/login.html';
    return null;
  }

  const role = (user.role || '').toUpperCase();
  const roleRoute = resolveRoleRoute(role);

  if (pageMode === 'manage' && role !== 'ADMIN') {
    window.location.href = roleRoute;
    return null;
  }
  if (pageMode === 'analysis' && role === 'STUDENT') {
    window.location.href = roleRoute;
    return null;
  }
  if (pageMode === 'materials' && role !== 'ADMIN') {
    window.location.href = roleRoute;
    return null;
  }

  return user;
}

export async function bindUserBar(user) {
  const nameEl = document.getElementById('currentUser');
  const roleEl = document.getElementById('currentRole');
  if (nameEl) {
    nameEl.textContent = user?.displayName || user?.username || '--';
  }
  if (roleEl) {
    roleEl.textContent = user?.role || '--';
  }
  const logoutBtn = document.getElementById('logoutButton');
  if (logoutBtn) {
    logoutBtn.addEventListener('click', async () => {
      await fetch('/api/auth/logout', { method: 'POST', credentials: 'include' });
      window.location.href = '/login.html';
    });
  }
}
