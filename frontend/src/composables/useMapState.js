import { ref } from 'vue';

const selectedLocation = ref(null);
const currentMapMode = ref('viewing_tasks');
const selectedTask = ref(null);

// Centralizamos las tareas del mapa aquí para que cualquier componente pueda pedir actualización
const mapTasks = ref([]);

const API_BASE = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api";

const fetchMapTasks = async () => {
  try {
      // El get de objetivos geográficos: ligero, trae la primera carga al iniciar la app.
      const response = await fetch(`${API_BASE}/tasks?page=0&size=1000`);
      if (response.status === 204) {
          mapTasks.value = [];
          return;
      }
      const data = await response.json();
      mapTasks.value = data.content || [];
  } catch (error) {
      console.error("Error cargando tareas para el mapa", error);
  }
};

// Función para actualizar EXCLUSIVAMENTE un punto en el mapa (cuando se edita o crea desde la tabla/formulario)
const fetchSingleMapTask = async (id) => {
  try {
      const response = await fetch(`${API_BASE}/tasks/${id}`);
      if (!response.ok) return;
      const taskData = await response.json();
      
      const index = mapTasks.value.findIndex(t => String(t.id) === String(id));
      if (index !== -1) {
          // Reemplazar la tarea existente con la info fresca
          mapTasks.value[index] = taskData;
      } else {
          // Es una tarea nueva, la agregamos
          mapTasks.value.push(taskData);
      }
      
      // Forzar reactividad del arreglo para que MapLibre actualice los GeoJSON
      mapTasks.value = [...mapTasks.value];
  } catch (error) {
      console.error("Error obteniendo la tarea especifica", error);
  }
};

// Función para remover un punto del mapa (cuando se elimina de la tabla)
const removeMapTaskPoint = (id) => {
    mapTasks.value = mapTasks.value.filter(t => String(t.id) !== String(id));
};

// Funcion para la comunicación entre componentes relacionados con el mapa
export function useMapState() {
  return {
    selectedLocation,
    currentMapMode,
    selectedTask,
    mapTasks,
    fetchMapTasks,
    fetchSingleMapTask,
    removeMapTaskPoint
  };
}