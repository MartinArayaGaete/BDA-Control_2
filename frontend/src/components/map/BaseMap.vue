<template>
  <div ref="mapContainer" class="map-container">
    <slot v-if="mapReady"></slot>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, shallowRef, ref, provide } from "vue";
import maplibregl from "maplibre-gl";
import "maplibre-gl/dist/maplibre-gl.css";

const mapContainer = ref(null);
const mapReady = ref(false);

// Proveemos el mapa a los componentes hijos
const map = shallowRef(null);
provide("map", map);

onMounted(() => {
  const mapInstance = new maplibregl.Map({
    container: mapContainer.value,
    style: "https://tiles.openfreemap.org/styles/liberty",
    center: [-70.6693, -33.4489], // Santiago: [lng, lat]
    zoom: 12, // Aumentado para notar mejor el 3D
    pitch: 60, // Ángulo de inclinación inicial para la vista 3D
    bearing: 20 // Rotación inicial
  });

  mapInstance.on("load", () => {
    map.value = mapInstance;
    mapReady.value = true; // Habilita el slot
  });

  // Habilitar la extrusión de edificios 3D una vez que carga el estilo base
  mapInstance.on('style.load', () => {
    mapInstance.addLayer({
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

  // Add zoom and rotation controls to the map.
  mapInstance.addControl(new maplibregl.NavigationControl({
      visualizePitch: true,
      visualizeRoll: true,
      showZoom: true,
      showCompass: true
  }));
});

onUnmounted(() => {
  map.value?.remove();
  map.value = null;
});

</script>

<style scoped>
.map-container {
  width: 100%;
  height: 100%;
}
</style>