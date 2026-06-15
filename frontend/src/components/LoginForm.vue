<template>
  <section class="card">
    <h2>Iniciar sesion</h2>
    <form @submit.prevent="handleLogin">
      <label>
        Usuario
        <input v-model="login.username" type="text" required />
      </label>
      <label>
        Contraseña
        <input v-model="login.password" type="password" required />
      </label>
      <button type="submit">Ingresar</button>
      <p class="status" v-if="loginStatus">{{ loginStatus }}</p>
    </form>

    <button class="link" type="button" @click="goRegister">
      ¿No tienes una cuenta? Registrate aquí
    </button>
  </section>
</template>

<script setup>
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { useAuthState } from "../composables/useAuthState";

const router = useRouter();
const { login: performLogin } = useAuthState();
const login = reactive({
  username: "",
  password: ""
});

const loginStatus = ref("");

async function handleLogin() {
  loginStatus.value = "Ingresando...";
  try {
    const user = await performLogin(login);
    loginStatus.value = user ? "Login exitoso" : "Sesión iniciada";
    router.push({ name: "home" });
  } catch (error) {
    loginStatus.value = `Error: ${error.message}`;
  }
}

function goRegister() {
  router.push({ name: "register" });
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
  margin-top: 0.75rem;
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
