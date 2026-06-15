<template>
  <main class="page-full">
    <v-container fluid class="pa-0 h-100">

      <v-row no-gutters class="h-100 golden-row">

        <v-col class="table-col h-100 d-flex flex-column pa-2">
          <router-view />
        </v-col>

        <v-col class="map-col h-100">
          <v-sheet class="h-100">
            <BaseMap>
              <LayerSectors :sectors="mapSectors" />
              <LayerTasks :tasks="mapTasks"/>
              <LayerLocationPicker
                  v-if="currentMapMode === 'creating_task'"
                  @location-selected="coords => selectedLocation = coords" 
              />
            </BaseMap>
          </v-sheet>
        </v-col>
      </v-row>
    </v-container>
  </main>
</template>

<script setup>
import { onMounted } from "vue";
import BaseMap from "../components/map/BaseMap.vue";
import LayerSectors from "../components/map/LayerSectors.vue";
import LayerTasks from "../components/map/LayerTasks.vue";
import LayerLocationPicker from "../components/map/LayerLocationPicker.vue";
import { useMapState } from "../composables/useMapState";

const { currentMapMode, selectedLocation, mapTasks, mapSectors, fetchMapTasks, fetchMapSectors } = useMapState();

onMounted(() => {
  fetchMapSectors();
  fetchMapTasks();
});
</script>

<style scoped>

.page-full {
  height: calc(100vh - 64px); 
  width: 100%;
  font-family: Arial, sans-serif;
  overflow: hidden;
}

.golden-row {
  display: flex;
  flex-wrap: wrap;
}

.table-col {
  flex: 0 0 38.196%;
  max-width: 38.196%;
}

.map-col {
  flex: 0 0 61.803%;
  max-width: 61.803%;
}

@media (max-width: 960px) {
  .table-col, .map-col {
    flex: 0 0 100%;
    max-width: 100%;
  }
  
  .map-col {
    height: 50vh; /* Para que en celular no desaparezca ocupe todo */
  }
}

.logout {
  margin-top: 1rem;
}

</style>