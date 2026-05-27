<template>
  <section class="card">
    <h2>Registrarse</h2>
    <form @submit.prevent="handleRegister">
      <label>
        Usuario
        <input v-model="register.username" type="text" required />
      </label>
      <label>
        Contrasena
        <input v-model="register.password" type="password" required />
      </label>
      <label>
        Longitud
        <input v-model="register.longitude" type="text" inputmode="decimal" required />
      </label>
      <label>
        Latitud
        <input v-model="register.latitude" type="text" inputmode="decimal" required />
      </label>
      <button type="submit">Registrar</button>
      <p class="status" v-if="registerStatus">{{ registerStatus }}</p>
    </form>

    <button class="link" type="button" @click="goLogin">
      ¿Ya tienes una cuenta? Inicia sesión aquí
    </button>
  </section>
</template>

<script setup>
import { reactive, ref, watch } from "vue";
import { useRouter } from "vue-router";
import { useMapState } from "../composables/useMapState";

const API_BASE = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api";
const router = useRouter();
const { selectedLocation } = useMapState();

const register = reactive({
  username: "",
  password: "",
  longitude: null,
  latitude: null
});

const registerStatus = ref("");

watch(selectedLocation, (coords) => {
  if (!coords) return;
  register.longitude = Number(coords.lng);
  register.latitude = Number(coords.lat);
});

async function handleRegister() {
  registerStatus.value = "Registrando...";
  try {
    const response = await fetch(`${API_BASE}/users/register`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(register)
    });
    if (!response.ok) {
      throw new Error(await response.text());
    }
    const data = await response.json();
    registerStatus.value = `OK: ${data.username} (id ${data.id})`;
    router.push({ name: "login" });
  } catch (error) {
    registerStatus.value = `Error: ${error.message}`;
  }
}

function goLogin() {
  router.push({ name: "login" });
}
</script>

<style scoped>
.card {
  border: 1px solid #ddd;
  padding: 1.5rem;
  border-radius: 8px;
}

form {
  display: grid;
  gap: 0.75rem;
  margin-bottom: 1.5rem;
}

label {
  display: grid;
  gap: 0.4rem;
  font-size: 0.9rem;
}

input {
  padding: 0.5rem 0.6rem;
  border: 1px solid #ccc;
  border-radius: 6px;
}

button {
  width: fit-content;
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 6px;
  background: #1f5f56;
  color: white;
  cursor: pointer;
}

.link {
  background: transparent;
  color: #1f5f56;
  border: none;
  padding: 0;
  cursor: pointer;
  text-decoration: underline;
}

.status {
  font-size: 0.85rem;
  color: #333;
}
</style>
