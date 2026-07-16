<template>
  <v-card>
    <v-card-title class="d-flex align-center">
      <v-icon class="mr-2" :color="activeTable.color">
        {{ activeTable.icon }}
      </v-icon>

      <span class="text-subtitle-1 font-weight-medium">
        {{ activeTable.title }}
      </span>

      <v-spacer />

      <v-btn
        icon
        variant="text"
        aria-label="Cerrar tabla de estadísticas"
        @click="emit('close')"
      >
        <v-icon>mdi-close</v-icon>
      </v-btn>
    </v-card-title>

    <v-divider />

    <v-card-text class="statistics-table-content">
      <v-alert v-if="error" type="error" variant="tonal">
        {{ error }}
      </v-alert>

      <div v-else-if="loading" class="d-flex align-center justify-center ga-3 py-8">
        <v-progress-circular indeterminate :color="activeTable.color" />
        <span>Cargando estadísticas...</span>
      </div>

      <SectorPendingConcentrationTable
        v-else-if="table === 'pending-concentration'"
        :items="items"
      />

      <TaskPerSectorTable
        v-else-if="table === 'tasks-users-sector'"
        :items="items"
      />
    </v-card-text>
  </v-card>
</template>

<script setup>
import { computed, ref, watch } from "vue";
import SectorPendingConcentrationTable from "./SectorPendingConcentrationTable.vue";
import TaskPerSectorTable from "./TaskPerSectorTable.vue";

const API_BASE = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api";

const TABLES = {
  "pending-concentration": {
    title: "Sectores con mayor concentración de tareas pendientes",
    icon: "mdi-map-search",
    color: "warning",
    endpoint: "/sectors/stats/concentration"
  },
  "tasks-users-sector": {
    title: "Tareas realizadas por usuario y sector",
    icon: "mdi-account-group",
    color: "success",
    endpoint: "/tasks/stats/users-sectors"
  }
};

const props = defineProps({
  table: {
    type: String,
    required: true,
    validator: (value) => ["pending-concentration", "tasks-users-sector"].includes(value)
  }
});

const emit = defineEmits(["close"]);

const items = ref([]);
const loading = ref(false);
const error = ref("");
let requestId = 0;

const activeTable = computed(() => TABLES[props.table] ?? TABLES["pending-concentration"]);

const fetchTableData = async () => {
  const currentRequestId = requestId + 1;
  requestId = currentRequestId;

  items.value = [];
  loading.value = true;
  error.value = "";

  try {
    const response = await fetch(`${API_BASE}${activeTable.value.endpoint}`, {
      credentials: "include"
    });

    if (currentRequestId !== requestId) {
      return;
    }

    if (response.status === 204) {
      items.value = [];
      return;
    }

    if (!response.ok) {
      throw new Error(await response.text());
    }

    const data = await response.json();
    items.value = Array.isArray(data) ? data : [];
  } catch (fetchError) {
    if (currentRequestId === requestId) {
      items.value = [];
      error.value = `Error cargando tabla de estadísticas: ${fetchError.message}`;
    }
  } finally {
    if (currentRequestId === requestId) {
      loading.value = false;
    }
  }
};

watch(() => props.table, fetchTableData, { immediate: true });
</script>

<style scoped>
.statistics-table-content {
  max-height: 70vh;
  overflow-y: auto;
}
</style>
