import { computed, ref } from "vue";

const API_BASE = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api";

const user = ref(null);
const initialized = ref(false);
const loading = ref(false);

const apiFetch = (path, options = {}) => {
  return fetch(`${API_BASE}${path}`, {
    credentials: "include",
    ...options
  });
};

const setUser = (userData) => {
  user.value = userData;
  initialized.value = true;
};

const clearAuth = () => {
  user.value = null;
  initialized.value = true;
};

const bootstrapAuth = async () => {
  if (initialized.value || loading.value) {
    return user.value;
  }

  loading.value = true;
  try {
    const response = await apiFetch("/users/me");
    if (response.ok) {
      user.value = await response.json();
    } else {
      user.value = null;
    }

    initialized.value = true;
    return user.value;
  } catch (error) {
    user.value = null;
    initialized.value = true;
    return null;
  } finally {
    loading.value = false;
  }
};

const login = async (credentials) => {
  const response = await apiFetch("/users/login", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(credentials)
  });

  if (!response.ok) {
    throw new Error(await response.text());
  }

  const meResponse = await apiFetch("/users/me");
  if (meResponse.ok) {
    user.value = await meResponse.json();
  } else {
    user.value = null;
  }

  initialized.value = true;
  return user.value;
};

const logout = async () => {
  try {
    await apiFetch("/users/logout", {
      method: "POST"
    });
  } finally {
    clearAuth();
  }
};

export function useAuthState() {
  return {
    user: computed(() => user.value),
    initialized: computed(() => initialized.value),
    loading: computed(() => loading.value),
    isAuthenticated: computed(() => Boolean(user.value)),
    bootstrapAuth,
    login,
    logout,
    setUser,
    clearAuth
  };
}