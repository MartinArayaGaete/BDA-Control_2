<template>
  <main class="page">
    <h1>Registro</h1>
    <RegisterForm />
  </main>
</template>

<script setup>
import RegisterForm from "../components/RegisterForm.vue";
</script>

<style scoped>
.page {
  max-width: 520px;
  margin: 3rem auto;
  padding: 0 1.5rem;
  font-family: Arial, sans-serif;
}
</style>
