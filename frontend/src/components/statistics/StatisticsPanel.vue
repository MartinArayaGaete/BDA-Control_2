<template>
  <div class="d-flex flex-column ga-4 h-100">
    <v-alert v-if="error" type="error" variant="tonal">
      {{ error }}
    </v-alert>

    <!-- Pregunta 4, 8 -->
    <v-card text="Promedio de distancia de tus tareas completadas respecto tu ubicación:">
      <v-card-text>
        <v-icon left>mdi-map-marker-distance</v-icon> {{ averageDistanceLabel }}
      </v-card-text>
    </v-card>
    
    <v-card text="Promedio de distancia de tus tareas completadas respecto a la ubicación registrada:">
      <v-card-text>
        <v-icon left>mdi-map-marker-distance</v-icon> {{ averageDistanceLabel }}
      </v-card-text>
    </v-card>

    <!-- Pregunta 3 y 7 -->
    <v-expansion-panels>
      <v-expansion-panel>
        <v-expansion-panel-title collapse-icon="mdi-minus" expand-icon="mdi-plus">
          <v-icon class="mr-2">mdi-map</v-icon>
          Sectores con más tareas completadas por radio
        </v-expansion-panel-title>
        <v-expansion-panel-text>
          <v-list v-if="topSectorsByRadius.length > 0" lines="two">
            <v-list-item
              v-for="sector in topSectorsByRadius"
              :key="sector.radiusKm"
            >
              <template #prepend>
                <v-icon>mdi-map-marker-radius</v-icon>
              </template>

              <v-list-item-title>
                Radio: {{ sector.radiusKm }} km
              </v-list-item-title>

              <v-list-item-subtitle>
                Sector: {{ sector.name || "Sin datos" }}
              </v-list-item-subtitle>
            </v-list-item>
          </v-list>

          <v-alert v-else-if="topSectorsLoading" type="info" variant="tonal">
            Cargando sectores...
          </v-alert>

          <v-alert v-else type="info" variant="tonal">
            No hay sectores con tareas completadas en los radios solicitados.
          </v-alert>
        </v-expansion-panel-text>
      </v-expansion-panel>
    </v-expansion-panels>

    <!-- Pregunta 1 -->
    <v-expansion-panels>
      <v-expansion-panel>
        <v-expansion-panel-title collapse-icon="mdi-minus" expand-icon="mdi-plus">
          <v-icon class="mr-2" >mdi-check-circle</v-icon>
          Tu numero de tareas completadas por sector
        </v-expansion-panel-title>
        <v-expansion-panel-text>
          <v-list v-if="sectorStats.length > 0" lines="one">
            <v-list-item
              v-for="sector in sectorStats"
              :key="sector.sectorName"
            >
              <template #prepend>
                <v-icon>mdi-map-marker-radius</v-icon>
              </template>

              <v-list-item-title>
                {{ sector.sectorName }}
              </v-list-item-title>

              <template #append>
                <v-chip color="primary" variant="flat" size="small">
                  {{ sector.taskCount }}
                </v-chip>
              </template>
            </v-list-item>
          </v-list>

          <v-alert v-else-if="sectorStatsLoading" type="info" variant="tonal">
            Cargando sectores...
          </v-alert>

          <v-alert v-else type="info" variant="tonal">
            No hay tareas completadas por sector.
          </v-alert>
        </v-expansion-panel-text>
      </v-expansion-panel>
    </v-expansion-panels>

    <!-- Pregunta 5 -->
    <v-card text="">
      <v-card-text>
      </v-card-text>
    </v-card>

    <!-- Pregunta 6 -->
    <v-card text="">
      <v-card-text>
      </v-card-text>
    </v-card>

  </div>
</template>


<script setup>
import { computed, onMounted, ref } from "vue";
import { useAuthState } from "../../composables/useAuthState";

const API_BASE = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api";
const { user, bootstrapAuth } = useAuthState();

const averageDistance = ref(null);
const averageDistanceLoading = ref(false);
const sectorStats = ref([]);
const sectorStatsLoading = ref(false);
const topSectorsByRadius = ref([]);
const topSectorsLoading = ref(false);
const error = ref("");

const TOP_SECTOR_RADII_KM = [2, 5];

const averageDistanceLabel = computed(() => {
  if (averageDistanceLoading.value) {
    return "Cargando...";
  }

  if (averageDistance.value === null) {
    return "Sin datos";
  }

  return `${averageDistance.value.toFixed(2)} km`;
});

const getUserId = () => user.value?.id ?? null;

const fetchAverageDistance = async () => {
  const userId = getUserId();
  if (userId === null || userId === undefined) {
    averageDistance.value = null;
    error.value = "No hay usuario autenticado para cargar estadísticas.";
    return;
  }

  averageDistanceLoading.value = true;
  error.value = "";

  try {
    const response = await fetch(`${API_BASE}/tasks/stats/average-distance/${userId}`, {
      credentials: "include"
    });

    if (response.status === 204) {
      averageDistance.value = null;
      return;
    }

    if (!response.ok) {
      throw new Error(await response.text());
    }

    const distance = Number(await response.json());
    if (!Number.isFinite(distance)) {
      throw new Error("Respuesta de distancia inválida.");
    }

    averageDistance.value = distance;
  } catch (fetchError) {
    averageDistance.value = null;
    error.value = `Error cargando estadísticas: ${fetchError.message}`;
  } finally {
    averageDistanceLoading.value = false;
  }
};

const fetchSectorStats = async () => {
  const userId = getUserId();
  if (userId === null || userId === undefined) {
    sectorStats.value = [];
    error.value = "No hay usuario autenticado para cargar estadísticas.";
    return;
  }

  sectorStatsLoading.value = true;
  error.value = "";

  try {
    const response = await fetch(`${API_BASE}/tasks/stats/user/${userId}`, {
      credentials: "include"
    });

    if (response.status === 204) {
      sectorStats.value = [];
      return;
    }

    if (!response.ok) {
      throw new Error(await response.text());
    }

    sectorStats.value = await response.json();
  } catch (fetchError) {
    sectorStats.value = [];
    error.value = `Error cargando estadísticas por sector: ${fetchError.message}`;
  } finally {
    sectorStatsLoading.value = false;
  }
};

const fetchTopSectorsByRadius = async () => {
  const userId = getUserId();
  if (userId === null || userId === undefined) {
    topSectorsByRadius.value = [];
    error.value = "No hay usuario autenticado para cargar estadísticas.";
    return;
  }

  topSectorsLoading.value = true;
  error.value = "";

  try {
    const sectors = await Promise.all(
      TOP_SECTOR_RADII_KM.map(async (radiusKm) => {
        const response = await fetch(`${API_BASE}/sectors/stats/top-sector/${userId}?radiusKm=${radiusKm}.0`, {
          credentials: "include"
        });

        if (response.status === 204) {
          return {
            radiusKm,
            name: null
          };
        }

        if (!response.ok) {
          throw new Error(await response.text());
        }

        const sector = await response.json();
        return {
          radiusKm,
          name: sector.name ?? null
        };
      })
    );

    topSectorsByRadius.value = sectors;
  } catch (fetchError) {
    topSectorsByRadius.value = [];
    error.value = `Error cargando sectores por radio: ${fetchError.message}`;
  } finally {
    topSectorsLoading.value = false;
  }
};

onMounted(() => {
  bootstrapAuth().finally(() => {
    fetchAverageDistance();
    fetchSectorStats();
    fetchTopSectorsByRadius();
  });
});

</script>

<style scoped>
.statistics-panel {
  height: 100%;
  width: 100%;
}
</style>
