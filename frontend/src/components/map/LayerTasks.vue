<template>
</template>

<script setup>
import { inject, onMounted, onUnmounted, ref, watch } from "vue";
import maplibregl from "maplibre-gl";
import { useAuthState } from "../../composables/useAuthState";
import { useMapState } from "../../composables/useMapState";

const map = inject("map");
const { user, bootstrapAuth } = useAuthState();
const { selectedTask } = useMapState();
const userLocationMarker = ref(null);

const props = defineProps(['tasks']);

const hasValidCoordinates = (item) => {
  const longitude = Number(item?.longitude);
  const latitude = Number(item?.latitude);

  return Number.isFinite(longitude) && Number.isFinite(latitude);
};

// Función para transformar las tareas en formato GeoJSON
const getGeoJSON = (tasksList) => {
  return {
    type: 'FeatureCollection',
    features: (tasksList || []).filter(hasValidCoordinates).map(t => ({
      type: 'Feature',
      geometry: {
        type: 'Point',
        coordinates: [Number(t.longitude), Number(t.latitude)]
      },
      properties: {
        id: t.id,
        title: t.title,
        description: t.description,
        status: t.status,
        dueDate: t.dueDate,
        sectorName: t.sectorName
      }
    }))
  };
};

const handlePointerCursor = () => { map.value.getCanvas().style.cursor = 'pointer'; };
const handleDefaultCursor = () => { map.value.getCanvas().style.cursor = ''; };

const handleMapClick = (e) => {
  const coordinates = e.features[0].geometry.coordinates.slice();
  const properties = e.features[0].properties;


  let statusText = properties.status;
  if (properties.status === 'PENDING') statusText = 'Disponible';
  else if (properties.status === 'IN_PROGRESS') statusText = 'En progreso';
  else if (properties.status === 'COMPLETED') statusText = 'Completado';
  else if (properties.status === 'EXPIRED') statusText = 'Expirado';

  new maplibregl.Popup()
    .setLngLat(coordinates)
    .setHTML(`
      <div style="color: #333; font-family: sans-serif; min-width: 150px;">
        <h4 style="margin: 0 0 4px 0;">${properties.title}</h4>
        <p style="margin: 0 0 8px 0; font-size: 0.9em; color: #555;">${properties.description || 'Sin descripción'}</p>
        <div style="font-size: 0.8em; margin-bottom: 2px;"><strong>Estado:</strong> ${statusText}</div>
        <div style="font-size: 0.8em; margin-bottom: 2px;"><strong>Sección:</strong> ${properties.sectorName || 'N/A'}</div>
        <div style="font-size: 0.8em;"><strong>Vence:</strong> ${properties.dueDate || 'N/A'}</div>
      </div>
    `)
    .addTo(map.value);
};

const updateUserLocationMarker = () => {
  if (!map.value) return;

  if (!hasValidCoordinates(user.value)) {
    if (userLocationMarker.value) {
      userLocationMarker.value.remove();
      userLocationMarker.value = null;
    }
    return;
  }

  const coordinates = [Number(user.value.longitude), Number(user.value.latitude)];

  if (!userLocationMarker.value) {
    userLocationMarker.value = new maplibregl.Marker({ color: '#1976d2' })
      .setLngLat(coordinates)
      .setPopup(
        new maplibregl.Popup({ offset: 25 }).setHTML(`
          <div style="color: #333; font-family: sans-serif; min-width: 150px;">
            <h4 style="margin: 0 0 4px 0;">Tu ubicación</h4>
          </div>
        `)
      )
      .addTo(map.value);
    return;
  }

  userLocationMarker.value.setLngLat(coordinates);
};

const flyToTask = (task) => {
  if (!map.value || !hasValidCoordinates(task)) return;

  map.value.flyTo({
    center: [Number(task.longitude), Number(task.latitude)],
    zoom: 16,
    speed: 1.2,
    curve: 1.4,
    essential: true
  });

  handleMapClick({
    features: [{
      geometry: {
        coordinates: [Number(task.longitude), Number(task.latitude)]
      },
      properties: {
        id: task.id,
        title: task.title,
        description: task.description,
        status: task.status,
        dueDate: task.dueDate,
        sectorName: task.sectorName
      }
    }]
  });
};

onMounted(() => {
  if (!map.value) return;

  // 1. Agregar Origen (Source) en formato GeoJSON
  map.value.addSource('tasks-source', {
    type: 'geojson',
    data: getGeoJSON(props.tasks)
  });

  // 2. Agregar Capa (Layer) con colores específicos según el Estado
  map.value.addLayer({
    id: 'tasks-layer',
    type: 'circle',
    source: 'tasks-source',
    paint: {
      'circle-radius': 7,
      'circle-color': [
        'match',
        ['get', 'status'],
        'PENDING', '#8bc34a',     // light-green
        'IN_PROGRESS', '#ff9800', // orange
        'COMPLETED', '#2e7d32',   // green-darken-3
        'EXPIRED', '#f44336',     // red
        /* default */ '#9e9e9e'   // grey
      ],
      'circle-stroke-width': 2,
      'circle-stroke-color': '#ffffff'
    }
  });

  // 3. Manejar interacciones y clics
  map.value.on('click', 'tasks-layer', handleMapClick);
  map.value.on('mouseenter', 'tasks-layer', handlePointerCursor);
  map.value.on('mouseleave', 'tasks-layer', handleDefaultCursor);

  bootstrapAuth().finally(updateUserLocationMarker);
});

// Mantener el mapa reactivo si cambian las tareas que nos llegan mediante fetch 
watch(() => props.tasks, (newTasks) => {
  if (map.value && map.value.getSource('tasks-source')) {
    map.value.getSource('tasks-source').setData(getGeoJSON(newTasks));
  }
}, { deep: true });

watch(user, updateUserLocationMarker);
watch(selectedTask, flyToTask);

onUnmounted(() => {
  if (!map.value) return;

  // Remover los listeners
  map.value.off('click', 'tasks-layer', handleMapClick);
  map.value.off('mouseenter', 'tasks-layer', handlePointerCursor);
  map.value.off('mouseleave', 'tasks-layer', handleDefaultCursor);

  if (userLocationMarker.value) {
    userLocationMarker.value.remove();
    userLocationMarker.value = null;
  }

  // Limpiar capa y origen del mapa al desmontar el componente (por ejemplo cambiar modo)
  if (map.value.getLayer('tasks-layer')) map.value.removeLayer('tasks-layer');
  if (map.value.getSource('tasks-source')) map.value.removeSource('tasks-source');
});
</script>
