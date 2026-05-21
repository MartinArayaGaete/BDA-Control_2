<template>
  <div ref="mapContainer" class="map-container"></div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref } from "vue";
import maplibregl, { type Map } from "maplibre-gl";

const mapContainer = ref<HTMLDivElement | null>(null);
let map: Map | null = null;

onMounted(() => {
  if (!mapContainer.value) return;

  map = new maplibregl.Map({
    container: mapContainer.value,
    style: "https://tiles.openfreemap.org/styles/bright",
    center: [-70.6693, -33.4489], // Santiago: [lng, lat]
    zoom: 11,
  });

  map.addControl(new maplibregl.NavigationControl(), "top-right");

  const popup = new maplibregl.Popup().setHTML(`
    <strong>Santiago</strong>
    <br />
    Punto inicial del mapa
  `);

  new maplibregl.Marker()
    .setLngLat([-70.6693, -33.4489])
    .setPopup(popup)
    .addTo(map);

  map.on("click", (event) => {
    console.log("Longitud:", event.lngLat.lng);
    console.log("Latitud:", event.lngLat.lat);
  });
});

onUnmounted(() => {
  map?.remove();
  map = null;
});
</script>

<style scoped>
.map-container {
  width: 200%;
  height: 1000px;
  border-radius: 12px;
  overflow: hidden;
}
</style>