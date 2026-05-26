<template>
</template>

<script setup>
import { inject, onMounted, onUnmounted, ref } from "vue";
import maplibregl from "maplibre-gl";

const map = inject("map");
const emit = defineEmits(['location-selected']);
const marker = ref(null);

const handleMapClick = (e) => {
    const { lng, lat } = e.lngLat;

    if (marker.value) {
        marker.value.setLngLat([lng, lat]);
    } else {
        marker.value = new maplibregl.Marker({ color: 'red' })
        .setLngLat([lng, lat])
        .addTo(map.value);
    }
    
    // Emitimos las coordenadas al padre
    emit('location-selected', { lng, lat });
};


onMounted(() => {
    map.value.on("click", handleMapClick);
});

onUnmounted(() => {
    map.value.off("click", handleMapClick);
    if (marker.value) marker.value.remove();
});
</script>