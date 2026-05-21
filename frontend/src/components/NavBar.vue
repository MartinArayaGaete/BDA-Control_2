<template>
    <v-app-bar :elevation="2" rounded>
        <v-app-bar-title>Gestión de tareas</v-app-bar-title>
        <v-spacer></v-spacer>
        <v-btn size="Large" @click="goLogin" color="error" class="ml-2">
          Cerrar Sesión
        </v-btn>
    </v-app-bar>
</template>

<script setup>
import { useRouter } from "vue-router";

const router = useRouter();

function goLogin() {
  localStorage.removeItem("isAuth");
  router.push({ name: "login" });
}
</script>