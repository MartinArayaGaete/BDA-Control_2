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
    style: "https://tiles.openfreemap.org/styles/liberty",
    center: [-70.6693, -33.4489], // Santiago: [lng, lat]
    zoom: 12, // Aumentado para notar mejor el 3D
    pitch: 60, // Ángulo de inclinación inicial para la vista 3D
    bearing: 20 // Rotación inicial
  });

  // Habilitar la extrusión de edificios 3D una vez que carga el estilo base
  map.on('style.load', () => {
    map.addLayer({
      'id': '3d-buildings',
      'source': 'openmaptiles',
      'source-layer': 'building',
      'filter': ['==', 'extrude', 'true'],
      'type': 'fill-extrusion',
      'minzoom': 15,
      'paint': {
          'fill-extrusion-color': '#aaa',
          'fill-extrusion-height': [
            'interpolate', ['linear'], ['zoom'],
            15, 0,
            15.05, ['get', 'height']
          ],
          'fill-extrusion-base': [
            'interpolate', ['linear'], ['zoom'],
            15, 0,
            15.05, ['get', 'min_height']
          ],
          'fill-extrusion-opacity': 0.6
      }
    });
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
  width: 100%;
  height: 100%;
  overflow: hidden;
}
</style>