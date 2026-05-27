<template>
  <v-main>
    <main class="page-full">
      <v-container fluid class="pa-0 h-100">
        <v-row no-gutters class="h-100">
          <v-col cols="12" md="6" class="form-col">
            <h1>Registro</h1>
            <RegisterForm />
          </v-col>
          <v-col cols="12" md="6" class="map-col">
            <v-sheet class="h-100">
              <BaseMap>
                <LayerLocationPicker @location-selected="coords => selectedLocation = coords" />
              </BaseMap>
            </v-sheet>
          </v-col>
        </v-row>
      </v-container>
    </main>
  </v-main>
</template>

<script setup>
import { onMounted, onUnmounted } from "vue";
import BaseMap from "../components/map/BaseMap.vue";
import LayerLocationPicker from "../components/map/LayerLocationPicker.vue";
import RegisterForm from "../components/RegisterForm.vue";
import { useMapState } from "../composables/useMapState";

const { currentMapMode, selectedLocation } = useMapState();

onMounted(() => {
  currentMapMode.value = "register_user";
});

onUnmounted(() => {
  selectedLocation.value = null;
  currentMapMode.value = "viewing_tasks";
});
</script>

<style scoped>
.page-full {
  height: 100vh;
  width: 100%;
  font-family: Arial, sans-serif;
}

.form-col {
  padding: 2rem;
  overflow-y: auto;
}

.map-col {
  min-height: 50vh;
}

@media (max-width: 960px) {
  .map-col {
    height: 50vh;
  }
}
</style>
