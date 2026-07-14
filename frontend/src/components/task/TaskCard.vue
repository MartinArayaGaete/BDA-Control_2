<template>
    <v-card color="grey-darken-3" variant="elevated" class="mb-2">
        <v-card-text class="pa-3 pb-1">
            
            <v-row density="compact">
                <!-- Columna izquierda: Título y descripción -->
                <v-col cols="8" class="text-left py-1" style="min-width: 0;">
                    <v-card-title class="text-subtitle-1 font-weight-bold px-0 pt-0 pb-1 text-truncate" style="line-height: 1.2;" :title="titulo">
                        {{ titulo }}
                    </v-card-title>
                    <v-card-subtitle class="px-0 pb-0 text-caption desc-truncate" :title="descripcion">
                        {{ descripcion }}
                    </v-card-subtitle>
                </v-col>
                
                <!-- Columna derecha: Estado, Sector, Fecha -->
                <v-col cols="4" class="d-flex flex-column align-end py-1">
                    <v-chip :color="statusDisplay.color" text-color="white" variant="flat" size="small" class="mb-1">
                        {{ statusDisplay.text }}
                    </v-chip>
                    <v-chip density="compact" variant="text" prepend-icon="mdi-map-marker" class="text-medium-emphasis w-100 justify-end text-truncate px-0 mt-n1" :title="'Sector: ' + (sector || 'Sin sector asignado')">
                        Sector: {{ sector || 'Sin sector asignado' }}
                    </v-chip>
                    <v-chip density="compact" variant="text" prepend-icon="mdi-calendar-clock" class="text-medium-emphasis w-100 justify-end text-truncate px-0 mt-n1" :title="'Vence: ' + fechaVencimiento">
                        Vence: {{ fechaVencimiento }}
                    </v-chip>
                </v-col>
            </v-row>
        </v-card-text>
        <v-card-actions class="py-1 px-2">
            <v-btn @click="handleEdit" color="primary" density="compact">
            Editar
            </v-btn>
            <v-btn @click="handleDelete" color="error" density="compact">
            Eliminar
            </v-btn>
        </v-card-actions>
    </v-card>
</template>


<script setup>
import { computed } from "vue";

const props = defineProps({
    id: [String, Number],
    titulo: String,
    descripcion: String,
    sector: String,
    fechaVencimiento: String,
    status: String
});

const emit = defineEmits(['taskDeleted', 'taskEdit']);

const API_BASE = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api";

const statusDisplay = computed(() => {
    switch (props.status) {
        case 'PENDING': return { text: 'Disponible', color: 'light-green' };
        case 'IN_PROGRESS': return { text: 'En progreso', color: 'orange' };
        case 'COMPLETED': return { text: 'Completado', color: 'green-darken-3' };
        case 'EXPIRED': return { text: 'Expirado', color: 'red' };
        default: return { text: 'Desconocido', color: 'grey' };
    }
});

const handleEdit = () => {
    emit('taskEdit', props.id);
};

const handleDelete = async () => {
    if (confirm('¿Estás seguro de que deseas eliminar esta tarea?')) {
        try {
            const response = await fetch(`${API_BASE}/tasks/${props.id}`, {
                method: 'DELETE',
                credentials: "include"
            });
            if (!response.ok) {
                throw new Error('Error al eliminar la tarea');
            }
            alert('Tarea eliminada exitosamente');
            emit('taskDeleted', props.id);
        } catch (error) {
            alert(`Error: ${error.message}`);
        }
    }
};


</script>

<style scoped>
.desc-truncate {
    display: -webkit-box;
    -webkit-line-clamp: 2; /* Limita la descripción a un maximo de 2 lineas */
    -webkit-box-orient: vertical;
    overflow: hidden;
    white-space: normal;
}
</style>
