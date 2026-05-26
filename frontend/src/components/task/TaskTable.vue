<template>
  <div class="d-flex flex-column h-100">
    <v-row density="comfortable" class="mb-2 flex-grow-0">
      <v-col cols="12" class="d-flex justify-start">
        <v-btn color="primary" @click="openCreate">
          Nueva tarea
        </v-btn>
      </v-col>
    </v-row>
    
    <v-sheet class="pa-4 flex-grow-1 overflow-auto">
      <v-container>
        <v-row density="comfortable">
          <v-col cols="12">
              <TaskCard v-for="task in tasks" :key="task.id"
              :id="task.id"
              :titulo="task.title"
              :descripcion="task.description"
              :sector="task.sectorName"
              :fechaVencimiento="task.dueDate"
              :status="task.status"
              @task-deleted="handleTaskDeleted"
              @task-edit="handleTaskEdit"
              />
          </v-col>
        </v-row>
        <v-row justify="center" class="mt-4">
          <v-pagination
            v-model="page"
            :length="totalPages"
            :total-visible="5"
            color="primary"
            @update:model-value="fetchTasks"
          ></v-pagination>
        </v-row>
      </v-container>
    </v-sheet>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import TaskCard from "./TaskCard.vue";
import { useMapState } from "../../composables/useMapState";

const router = useRouter();
const { currentMapMode, selectedTask, removeMapTaskPoint } = useMapState();

const API_BASE = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api";

const tasks = ref([]);
const tasksLoading = ref(false);
const tasksError = ref(null);

// Estados de la paginación
const page = ref(1); // Vuetify pagination usa 1-based index
const size = ref(4);
const totalPages = ref(0);

const fetchTasks = async () => {
    tasksLoading.value = true;
    try {
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

const handleTaskDeleted = (taskId) => {
    fetchTasks();
    removeMapTaskPoint(taskId);
};

const handleTaskEdit = (taskId) => {
  const task = tasks.value.find((item) => String(item.id) === String(taskId)) || null;
  selectedTask.value = task;
  router.push({ name: "editTask", params: { id: taskId } });
};

const openCreate = () => {
  selectedTask.value = null;
  router.push({ name: "createTask" });
};

onMounted(() => {
    currentMapMode.value = 'viewing_tasks'; // Asegurarse de que el mapa est\u00e1 en modo vista
    fetchTasks();
});

</script>

<style scoped>
</style>
