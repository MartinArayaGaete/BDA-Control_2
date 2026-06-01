<template>
</template>

<script setup>
import { inject, onMounted, onUnmounted, watch } from "vue";
import maplibregl from "maplibre-gl";

const map = inject("map");

const props = defineProps(['sectors']);

// Función para parsear WKT (POLYGON) a coordenadas de GeoJSON
const parseWktPolygon = (wkt) => {
  if (!wkt) return null;
  const match = wkt.match(/POLYGON\s*\(\((.*)\)\)/);
  if (!match) return null;
  
  const coordStrings = match[1].split(',');
  const coordinates = coordStrings.map(coordStr => {
    const [lng, lat] = coordStr.trim().split(/\s+/);
    return [parseFloat(lng), parseFloat(lat)];
  });
  console.log('Parsed WKT:', wkt, 'to coordinates:', coordinates);
  return [coordinates]; // GeoJSON Polygon espera un arreglo de anillos
};

// Función para transformar los sectores en formato GeoJSON
const getGeoJSON = (sectorsList) => {
  return {
    type: 'FeatureCollection',
    features: (sectorsList || []).filter(s => s.wkt).map(s => {
      const coords = parseWktPolygon(s.wkt);
      if (!coords) return null;
      return {
        type: 'Feature',
        geometry: {
          type: 'Polygon',
          coordinates: coords
        },
        properties: {
          id: s.id,
          name: s.name
        }
      };
    }).filter(f => f !== null)
  };
};

const handlePointerCursor = () => { map.value.getCanvas().style.cursor = 'pointer'; };
const handleDefaultCursor = () => { map.value.getCanvas().style.cursor = ''; };

const handleMapClick = (e) => {
  // Evitar que el popup del sector se abra si estamos haciendo click en una tarea
  if (map.value.getLayer('tasks-layer')) {
    const taskFeatures = map.value.queryRenderedFeatures(e.point, { layers: ['tasks-layer'] });
    if (taskFeatures.length > 0) return;
  }

  if (!e.features || e.features.length === 0) return;
  const properties = e.features[0].properties;

  new maplibregl.Popup()
    .setLngLat(e.lngLat)
    .setHTML(`
      <div style="color: #333; font-family: sans-serif; min-width: 150px;">
        <h4 style="margin: 0 0 4px 0;">${properties.name}</h4>
        <div style="font-size: 0.8em;"><strong>ID Sector:</strong> ${properties.id}</div>
      </div>
    `)
    .addTo(map.value);
};

onMounted(() => {
  if (!map.value) return;

  // 1. Agregar Origen (Source) en formato GeoJSON
  if (!map.value.getSource('sectors-source')) {
    map.value.addSource('sectors-source', {
      type: 'geojson',
      data: getGeoJSON(props.sectors)
    });
  }

  // Verificar si la capa de puntos (tareas) ya existe, 
  // para poder renderizar los polígonos por debajo de ellos usando "beforeId".
  const beforeId = map.value.getLayer('tasks-layer') ? 'tasks-layer' : undefined;

  // 2. Agregar Capa (Layer) para el relleno del sector
  map.value.addLayer({
    id: 'sectors-layer-fill',
    type: 'fill',
    source: 'sectors-source',
    paint: {
      'fill-color': '#088',
      'fill-opacity': 0.3
    }
  }, beforeId);

  // Capa opcional para el borde del sector
  map.value.addLayer({
    id: 'sectors-layer-outline',
    type: 'line',
    source: 'sectors-source',
    paint: {
      'line-color': '#006666',
      'line-width': 2
    }
  }, beforeId);

  // 3. Manejar interacciones y clics
  map.value.on('click', 'sectors-layer-fill', handleMapClick);
  map.value.on('mouseenter', 'sectors-layer-fill', handlePointerCursor);
  map.value.on('mouseleave', 'sectors-layer-fill', handleDefaultCursor);
});

// Mantener el mapa reactivo si cambian los sectores que nos llegan
watch(() => props.sectors, (newSectors) => {
  if (map.value && map.value.getSource('sectors-source')) {
    map.value.getSource('sectors-source').setData(getGeoJSON(newSectors));
  }
}, { deep: true });

onUnmounted(() => {
  if (!map.value) return;

  // Remover los listeners
  map.value.off('click', 'sectors-layer-fill', handleMapClick);
  map.value.off('mouseenter', 'sectors-layer-fill', handlePointerCursor);
  map.value.off('mouseleave', 'sectors-layer-fill', handleDefaultCursor);

  // Limpiar capa y origen del mapa al desmontar
  if (map.value.getLayer('sectors-layer-outline')) map.value.removeLayer('sectors-layer-outline');
  if (map.value.getLayer('sectors-layer-fill')) map.value.removeLayer('sectors-layer-fill');
  if (map.value.getSource('sectors-source')) map.value.removeSource('sectors-source');
});
</script>