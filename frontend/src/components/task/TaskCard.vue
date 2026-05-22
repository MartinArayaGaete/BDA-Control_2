<template>
    <v-card color="grey-darken-3" variant="elevated" class="mb-2">
        <v-card-title class="d-flex justify-space-between align-center text-wrap">
            {{ titulo }}
            <v-chip :color="statusDisplay.color" text-color="white" class="ml-2" variant="flat">
                {{ statusDisplay.text }}
            </v-chip>
        </v-card-title>
        <v-card-subtitle>
            <div>Sector: {{ sector }}</div>
            <div>Vence: {{ fechaVencimiento }}</div>
        </v-card-subtitle>
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
import { useRouter } from "vue-router";

const props = defineProps({
    id: String,
    titulo: String,
    sector: String,
    fechaVencimiento: String,
    status: String
});

const emit = defineEmits(['taskDeleted']);

const API_BASE = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api";

const router = useRouter();

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
    router.push({ name: 'editTask', params: { id: props.id } });
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
