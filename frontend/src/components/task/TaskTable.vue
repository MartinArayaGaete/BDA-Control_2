<template>
  <div class="d-flex flex-column h-100">
    <!-- Bloque 1: Botón Nueva tarea, Búsqueda y Filtro de Estado -->
    <v-row density="comfortable" class="mb-2 flex-grow-0 align-center">

      <v-col cols="12" md="2" class="d-flex justify-start">
        <v-btn color="primary" @click="openCreate">
          Nueva tarea
        </v-btn>
      </v-col>
      
      <v-col cols="12" md="6">
        <v-text-field
          v-model="searchKeyword"
          label="Buscar tarea..."
          prepend-inner-icon="mdi-magnify"
          density="compact"
          variant="outlined"
          hide-details
          clearable
          @update:model-value="onSearchChange"
        ></v-text-field>
      </v-col>

      <v-col cols="12" md="4">
        <v-select
          v-model="searchStatus"
          :items="statusOptions"
          item-title="label"
          item-value="value"
          label="Estado"
          density="compact"
          variant="outlined"
          hide-details
          clearable
          @update:model-value="onSearchChange"
        ></v-select>
      </v-col>
    </v-row>
    
    <!-- Bloque 2: Grid de las cartas de tareas -->
    <v-sheet class="pa-2 flex-grow-1 overflow-auto d-flex flex-column" position="relative">
      <v-container class="pa-0 flex-grow-1">
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
      </v-container>
    </v-sheet>

    <!-- Bloque 3: Paginación -->
    <v-row justify="center" class="mt-2 flex-grow-0">
      <v-pagination
        v-model="page"
        :length="totalPages"
        :total-visible="5"
        color="primary"
        @update:model-value="fetchTasks"
      ></v-pagination>
    </v-row>
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

// Campos de búsqueda
const searchKeyword = ref("");
const searchStatus = ref(null);
let searchTimer = null;

const statusOptions = [
  { label: "Cualquiera", value: null },
	{ label: "Disponible", value: "PENDING" },
	{ label: "En progreso", value: "IN_PROGRESS" },
	{ label: "Completado", value: "COMPLETED" },
	{ label: "Expirado", value: "EXPIRED" }
];

// Estados de la paginación
const page = ref(1); // Vuetify pagination usa 1-based index
const size = ref(4);
const totalPages = ref(0);

const fetchTasks = async () => {
    tasksLoading.value = true;
    try {
        let url = `${API_BASE}/tasks`;
        
        // Si hay algún filtro, usamos el endpoint /search
        if (searchKeyword.value || searchStatus.value) {
            url += `/search?page=${page.value - 1}&size=${size.value}`;
            if (searchKeyword.value) url += `&keyword=${encodeURIComponent(searchKeyword.value)}`;
            if (searchStatus.value) url += `&status=${searchStatus.value}`;
        } else {
            // Sin filtros, el endpoint general
            url += `?page=${page.value - 1}&size=${size.value}`;
        }
        
        const response = await fetch(url);
        
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

// Timeout básico (debounce) para no sobrecargar el endpoint al tipear
const onSearchChange = () => {
    if (searchTimer) clearTimeout(searchTimer);
    searchTimer = setTimeout(() => {
        page.value = 1; // Volvemos a la pagina 1 al buscar
        fetchTasks();
    }, 400);
};

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
    currentMapMode.value = 'viewing_tasks'; // Asegurarse de que el mapa este en modo vista
    fetchTasks();
});

</script>

<style scoped>
</style>
