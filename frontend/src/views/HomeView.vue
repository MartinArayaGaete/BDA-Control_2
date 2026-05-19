<template>
  <main class="page">
    <h1>Principal</h1>
    <p>Buenas, aun no hay nada, vuelva en 15 minutos, si no hay nada lealo de nuevo.</p>
    <v-btn class="logout" color="primary" variant="elevated" @click="goLogin">
      Cerrar sesion
    </v-btn>
  </main>
</template>

<script setup>
import { useRouter } from "vue-router";

const router = useRouter();

function goLogin() {
  localStorage.removeItem("isAuth");
  router.push({ name: "login" });
}
</script>

<style scoped>
.page {
  max-width: 720px;
  margin: 3rem auto;
  padding: 0 1.5rem;
  font-family: Arial, sans-serif;
}

.logout {
  margin-top: 1rem;
}
</style>
