<template>
  <div class="d-flex flex-column h-100">
    <v-row density="comfortable" class="mb-2 flex-grow-0">
      <v-col cols="12" class="d-flex align-center">
        <v-icon class="mr-2">mdi-bell</v-icon>
        <h2 class="text-h6 mb-0">Notificaciones</h2>
        <v-spacer />
        <v-btn variant="text" :loading="loading" @click="fetchNotifications">
          Recargar
        </v-btn>
      </v-col>
    </v-row>

    <v-sheet class="pa-4 flex-grow-1 overflow-auto">
      <v-alert v-if="error" type="error" variant="tonal" class="mb-4">
        {{ error }}
      </v-alert>
      <v-alert v-else-if="!loading && notifications.length === 0" type="info" variant="tonal">
        No hay notificaciones pendientes.
      </v-alert>

      <v-card
        v-for="notification in notifications"
        :key="notification.id"
        class="mb-3"
        variant="elevated"
      >
        <v-card-title class="text-subtitle-1">
          {{ notification.message }}
        </v-card-title>
        <v-card-text class="text-medium-emphasis">
          <div>Task ID: {{ notification.taskId }}</div>
          <div>Fecha: {{ formatDate(notification.createdAt) }}</div>
        </v-card-text>
        <v-card-actions>
          <v-btn color="primary" variant="text" @click="markAsRead(notification.taskId)">
            Marcar como leida
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-sheet>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from "vue";
import { useRoute } from "vue-router";

const API_BASE = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api";
const route = useRoute();
const isActiveTab = computed(() => route.name === "notifications");

const notifications = ref([]);
const loading = ref(false);
const error = ref("");
const usersLoading = ref(false);
let refreshTimer = null;

const getUserId = () => {
  const stored = localStorage.getItem("userId");
  return stored ? Number(stored) : null;
};

const hydrateUserIdFromUsername = async () => {
  const storedUserId = localStorage.getItem("userId");
  if (storedUserId) return;

  const username = localStorage.getItem("username");
  if (!username) return;

  if (usersLoading.value) return;
  usersLoading.value = true;
  try {
    const response = await fetch(`${API_BASE}/users?page=0&size=200`);
    if (!response.ok || response.status === 204) return;
    const data = await response.json();
    const match = (data.content || []).find((user) => user.username === username);
    if (match?.id) {
      localStorage.setItem("userId", String(match.id));
    }
  } catch (fetchError) {
    // noop
  } finally {
    usersLoading.value = false;
  }
};

const fetchNotifications = async () => {
  const userId = getUserId();
  if (!userId) {
    error.value = "No hay usuario autenticado para cargar notificaciones.";
    notifications.value = [];
    return;
  }

  loading.value = true;
  error.value = "";
  try {
    const response = await fetch(`${API_BASE}/notifications/user/${userId}`);
    if (response.status === 204) {
      notifications.value = [];
      return;
    }
    if (!response.ok) {
      throw new Error(await response.text());
    }
    notifications.value = await response.json();
  } catch (fetchError) {
    error.value = `Error cargando notificaciones: ${fetchError.message}`;
    notifications.value = [];
  } finally {
    loading.value = false;
  }
};

const markAsRead = async (taskId) => {
  try {
    const response = await fetch(`${API_BASE}/notifications/task/${taskId}/read`, {
      method: "PATCH"
    });
    if (!response.ok) {
      throw new Error(await response.text());
    }
    await fetchNotifications();
  } catch (fetchError) {
    error.value = `Error marcando como leida: ${fetchError.message}`;
  }
};

const formatDate = (value) => {
  if (!value) return "";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return String(value);
  return date.toLocaleString();
};

onMounted(() => {
  hydrateUserIdFromUsername().finally(() => {
    fetchNotifications();
  });
  refreshTimer = setInterval(() => {
    if (isActiveTab.value) {
      fetchNotifications();
    }
  }, 20000);
});

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer);
    refreshTimer = null;
  }
});
</script>
