<template>
  <main class="page">
    <v-container>
      <v-row>

        <v-col>
          <v-sheet class="pa-2">
          </v-sheet>
        </v-col>

        <v-col>
          <v-sheet class="pa-2">
            <MapReadOnly />
          </v-sheet>
        </v-col>

      </v-row>
    </v-container>
  </main>
</template>

<script setup>
import { useRouter } from "vue-router";
import MapReadOnly from "../components/MapReadOnly.vue";

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
