<template>
  <v-main>
    <main class="page">
      <h1>Login</h1>
      <LoginForm />
    </main>
  </v-main>
</template>

<script setup>
import LoginForm from "../components/LoginForm.vue";
</script>

<style scoped>
.page {
  max-width: 520px;
  margin: 3rem auto;
  padding: 0 1.5rem;
  font-family: Arial, sans-serif;
}
</style>
