<template>
  <v-main>
    <v-container>
      <v-row density="comfortable">
        <v-col cols="12">
            <TaskCard v-for="task in tasks" :key="task.id"
            :id="task.id"
            :titulo="task.titulo || task.title"
            :sector="task.sectorName || task.sector"
            :fechaVencimiento="task.dueDate || task.fechaVencimiento"
            :status="task.status"
            @task-deleted="handleTaskDeleted"
            />
        </v-col>
      </v-row>
      <v-row justify="center" class="mt-4">
        <v-pagination
          v-model="page"
          :length="totalPages"
          @update:model-value="fetchTasks"
        ></v-pagination>
      </v-row>
    </v-container>
  </v-main>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import TaskCard from "./TaskCard.vue";

const API_BASE = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api";

const tasks = ref([]);
const tasksLoading = ref(false);
const tasksError = ref(null);
const taskMessageErr = ref(null);

// Estados de la paginación
const page = ref(1); // Vuetify pagination usa 1-based index
const size = ref(10);
const totalPages = ref(0);

const fetchTasks = async () => {
    tasksLoading.value = true;
    try {
        // Restar 1 al page porque Spring Data usa 0-based paging
        const response = await fetch(`${API_BASE}/tasks?page=${page.value - 1}&size=${size.value}`);
        
        if (response.status === 204) {
            tasks.value = [];
            totalPages.value = 0;
            return;
        }
        const data = await response.json();
        tasks.value = data.content || [];
        totalPages.value = data.totalPages || 0;
    } catch (error) {
        tasksError.value = error.message;
    } finally {
        tasksLoading.value = false;
    }
}

const handleTaskDeleted = () => {
    fetchTasks();
};

onMounted(() => {
    fetchTasks();
});

</script>

<style scoped>
</style>
