export function makeApiUrl(baseUrl, path) {
  if (!path) {
    return '';
  }
  const base = (baseUrl || '').replace(/\/$/, '');
  const suffix = path.startsWith('/') ? path : '/' + path;
  return base + suffix;
}

export function getBasicAuthHeader(auth) {
  const username = auth?.username || '';
  const password = auth?.password || '';
  if (!username || !password) {
    return null;
  }
  try {
    return 'Basic ' + btoa(username + ':' + password);
  } catch (error) {
    return null;
  }
}

export async function authenticatedFetch(url, options = {}, auth) {
  const headers = new Headers(options.headers || {});
  const authHeader = getBasicAuthHeader(auth);
  if (authHeader) {
    headers.set('Authorization', authHeader);
  }
  if (!headers.has('Accept')) {
    headers.set('Accept', 'application/json');
  }
  const response = await fetch(url, { ...options, headers, credentials: 'include' });
  if (!response.ok) {
    let message = 'HTTP ' + response.status;
    try {
      const payload = await response.clone().json();
      message = payload?.error || payload?.message || message;
    } catch (err) {
      // ignore
    }
    throw new Error(message);
  }
  return response;
}
