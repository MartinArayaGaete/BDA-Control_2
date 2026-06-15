<template>
  <v-app-bar :elevation="2" rounded>
    <v-app-bar-title>Gestión de tareas</v-app-bar-title>
    <v-tabs v-model="activeTab" align-tabs="end" color="primary" class="ml-4">
      <v-tab value="tasks" :to="{ name: 'home' }">Tareas</v-tab>
      <v-tab value="statistics" :to="{ name: 'statistics' }">Estadísticas</v-tab>
      <v-tab value="notifications" :to="{ name: 'notifications' }">Notificaciones</v-tab>
    </v-tabs>
    <v-spacer></v-spacer>
    <v-btn size="Large" @click="goLogin" color="error" class="ml-2">
      Cerrar Sesión
    </v-btn>
  </v-app-bar>
</template>

<script setup>
import { ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useAuthState } from "../composables/useAuthState";

const route = useRoute();
const router = useRouter();
const { logout } = useAuthState();

const activeTab = ref("tasks");

const syncTabWithRoute = () => {
  const routeName = String(route.name || "");
  const routePath = String(route.path || "");

  if (routeName === "notifications" || routePath.includes("/home/notifications")) {
    activeTab.value = "notifications";
    return;
  }

  if (routeName === "statistics" || routePath.includes("/home/statistics")) {
    activeTab.value = "statistics";
    return;
  }

  activeTab.value = "tasks";
};

watch(
  () => route.fullPath,
  () => syncTabWithRoute(),
  { immediate: true }
);

async function goLogin() {
  await logout();
  router.push({ name: "login" });
}
</script>