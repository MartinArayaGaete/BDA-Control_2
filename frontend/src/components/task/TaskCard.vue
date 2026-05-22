<template>
    <v-card color="grey-darken-3" variant="elevated" class="mb-2">
        <v-card-text>
            <v-row>
                <!-- Columna izquierda: Título y descripción -->
                <v-col cols="8" class="text-left">
                    <v-card-title class="text-headline-small px-0 pt-0">
                        {{ titulo }}
                    </v-card-title>
                    <v-card-subtitle class="px-0 pb-0 text-wrap">
                        {{ descripcion }}
                    </v-card-subtitle>
                </v-col>
                
                <!-- Columna derecha: Estado, Sector, Fecha -->
                <v-col cols="4" class="d-flex flex-column align-end">
                    <v-chip :color="statusDisplay.color" text-color="white" variant="flat" class="mb-2">
                        {{ statusDisplay.text }}
                    </v-chip>
                    <v-chip size="small" variant="text" prepend-icon="mdi-map-marker" class="text-medium-emphasis w-100 justify-end text-truncate px-0" :title="'Sector: ' + (sector || 'Sin sector asignado')">
                        Sector: {{ sector || 'Sin sector asignado' }}
                    </v-chip>
                    <v-chip size="small" variant="text" prepend-icon="mdi-calendar-clock" class="text-medium-emphasis w-100 justify-end text-truncate px-0" :title="'Vence: ' + fechaVencimiento">
                        Vence: {{ fechaVencimiento }}
                    </v-chip>
                </v-col>
            </v-row>
        </v-card-text>
        <v-card-actions>
            <v-btn @click="handleEdit" color="primary">
            Editar
            </v-btn>
            <v-btn @click="handleDelete" color="error">
            Eliminar
            </v-btn>
        </v-card-actions>
    </v-card>
</template>


<script setup>
import { computed } from "vue";

const props = defineProps({
    id: String,
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
                method: 'DELETE'
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
</style>
