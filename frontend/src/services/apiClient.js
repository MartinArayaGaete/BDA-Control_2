const API_BASE = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api";
const CSRF_COOKIE_NAME = "XSRF-TOKEN";
const CSRF_META_NAME = "csrf-token";
const CSRF_HEADER_NAME = "X-XSRF-TOKEN";
const CSRF_ENDPOINT = "/users/csrf";
const SAFE_METHODS = new Set(["GET", "HEAD", "OPTIONS", "TRACE"]);

let csrfTokenPromise = null;

const buildApiUrl = (path) => {
  if (/^https?:\/\//i.test(path)) {
    return path;
  }

  return `${API_BASE}${path.startsWith("/") ? path : `/${path}`}`;
};

export const getCsrfToken = () => {
  if (typeof document === "undefined") {
    return null;
  }

  const cookie = document.cookie
    .split("; ")
    .find((item) => item.startsWith(`${CSRF_COOKIE_NAME}=`));

  if (cookie) {
    return decodeURIComponent(cookie.slice(CSRF_COOKIE_NAME.length + 1));
  }

  return document.querySelector(`meta[name="${CSRF_META_NAME}"]`)?.getAttribute("content") || null;
};

const fetchCsrfFromServer = async () => {
  try {
    const response = await fetch(buildApiUrl(CSRF_ENDPOINT), { credentials: "include" });
    if (!response.ok) return null;

    const data = await response.json();
    return data?.token || null;
  } catch {
    return null;
  }
};

const getServerCsrfToken = () => {
  if (!csrfTokenPromise) {
    csrfTokenPromise = fetchCsrfFromServer().finally(() => {
      csrfTokenPromise = null;
    });
  }

  return csrfTokenPromise;
};

export const apiFetch = async (path, options = {}) => {
  const headers = new Headers(options.headers || {});
  const method = (options.method || "GET").toUpperCase();
  let csrfToken = null;

  if (!headers.has(CSRF_HEADER_NAME)) {
    csrfToken = SAFE_METHODS.has(method) ? getCsrfToken() : await getServerCsrfToken();
  }

  if (csrfToken) {
    headers.set(CSRF_HEADER_NAME, csrfToken);
  }

  return fetch(buildApiUrl(path), {
    credentials: "include",
    ...options,
    headers
  });
};
