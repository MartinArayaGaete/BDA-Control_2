<template>
	<v-card color="grey-darken-3" variant="elevated" class="h-100 d-flex flex-column">
		<v-card-title class="text-wrap mt-2">
			{{ isEdit ? "Editar tarea" : "Nueva tarea" }}
		</v-card-title>
		<v-card-text class="flex-grow-1 overflow-auto">
			<v-form ref="formRef" v-model="isValid">
				<v-row density="comfortable">
					<v-col cols="12" md="6">
						<v-text-field
							v-model="form.title"
							label="Nombre"
							:rules="[rules.required]"
							required
						/>
					</v-col>
					<v-col cols="12" md="6">
						<v-text-field
							v-model="form.dueDate"
							label="Fecha de vencimiento"
							type="date"
							:rules="[rules.required]"
							required
						/>
					</v-col>
					<v-col cols="12">
						<v-textarea
							v-model="form.description"
							label="Descripcion"
							:rules="[rules.required]"
							rows="3"
							auto-grow
							required
						/>
					</v-col>
					<v-col cols="12" md="6">
						<v-select
							v-model="form.status"
							:items="statusOptions"
							item-title="label"
							item-value="value"
							label="Estado"
							:disabled="false"
							:rules="[rules.required]"
							required
						/>
					</v-col>
					<v-col cols="12" md="6">
						<v-autocomplete
								v-model="form.userId"
								v-model:search="userSearch"
								:items="users"
								item-title="username"
								item-value="id"
								label="Usuario"
								:loading="usersLoading"
								@update:search="handleUserSearch"
								clearable
								hint="Opcional"
								persistent-hint
								no-data-text="No hay usuarios"
							/>
						</v-col>
						
						<v-col cols="12" class="py-0 my-0">
							<div class="text-error font-weight-bold text-subtitle-1 pb-1"> 
								Seleccionar ubicación en el mapa para asignar latitud y longitud.
							</div>
						</v-col>

						<v-col cols="12" md="6">
							<v-text-field
								v-model="form.latitude"
								label="Latitud"
								type="number"
								step="any"
								readonly
								persistent-hint
								:rules="[rules.required]"
								required
							/>
						</v-col>
						<v-col cols="12" md="6">
							<v-text-field
								v-model="form.longitude"
								label="Longitud"
								type="number"
								step="any"
								readonly
								persistent-hint
								:rules="[rules.required]"
								required
							/>
						</v-col>
					</v-row>
					<v-alert v-if="errorMessage" type="error" variant="tonal" class="mt-2">
						{{ errorMessage }}
					</v-alert>
				</v-form>
			</v-card-text>
			<v-card-actions class="justify-end px-4 pb-4">
				<v-btn variant="text" @click="close" :disabled="isSaving">Cancelar</v-btn>
				<v-btn color="primary" @click="submit" :loading="isSaving">
					{{ isEdit ? "Guardar" : "Crear" }}
				</v-btn>
			</v-card-actions>
		</v-card>
</template>

<script setup>
import { computed, ref, watch, onMounted, onUnmounted } from "vue";
import { useRouter } from "vue-router";
import { useMapState } from "../../composables/useMapState";

const props = defineProps({
	mode: { type: String, default: "create" }
});

const emit = defineEmits(["saved"]);

const router = useRouter();
const { selectedLocation, currentMapMode, selectedTask, fetchMapTasks } = useMapState();

const API_BASE = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api";

const statusOptions = [
	{ label: "Disponible", value: "PENDING" },
	{ label: "En progreso", value: "IN_PROGRESS" },
	{ label: "Completado", value: "COMPLETED" }
];

const isEdit = computed(() => props.mode === "edit");
const isSaving = ref(false);
const isValid = ref(false);
const errorMessage = ref("");
const formRef = ref(null);
const users = ref([]);
const usersLoading = ref(false);
const userSearch = ref("");
let userSearchTimer = null;

const emptyForm = () => ({
	title: "",
	description: "",
	dueDate: "",
	status: "PENDING",
	userId: null,
	latitude: "",
	longitude: ""
});

const form = ref(emptyForm());

const rules = {
	required: (value) => {
		const isEmpty = value === null || value === undefined || String(value).trim() === "";
		return isEmpty ? "Campo obligatorio" : true;
	}
};

const hydrateForm = () => {
	if (isEdit.value && selectedTask.value) {
		form.value = {
			title: selectedTask.value.title || "",
			description: selectedTask.value.description || "",
			dueDate: selectedTask.value.dueDate || "",
			status: selectedTask.value.status || "PENDING",
			userId: selectedTask.value.userId ?? null,
			latitude: selectedTask.value.latitude ?? "",
			longitude: selectedTask.value.longitude ?? ""
		};
	} else {
		form.value = emptyForm();
	}
};

watch(selectedLocation, (newLocation) => {
	if (newLocation) {
		form.value.longitude = newLocation.lng;
		form.value.latitude = newLocation.lat;
	}
});

watch(
	() => form.value.userId,
	(newUserId) => {
		if (isEdit.value) return;
		if (newUserId) {
			form.value.status = "IN_PROGRESS";
		} else if (form.value.status === "IN_PROGRESS") {
			form.value.status = "PENDING";
		}
	}
);

watch(
	() => form.value.status,
	(newStatus) => {
		if (newStatus === "PENDING") {
			form.value.userId = null;
		}
	}
);

onMounted(() => {
	currentMapMode.value = 'creating_task';
	errorMessage.value = "";
	hydrateForm();
	fetchUsers();
});

onUnmounted(() => {
	selectedLocation.value = null;
});

const fetchUsers = async (searchTerm = "") => {
	if (usersLoading.value) {
		return;
	}

	usersLoading.value = true;
	try {
		const response = await fetch(`${API_BASE}/users?page=0&size=100`, {
			credentials: "include"
		});
		if (response.status === 204) {
			users.value = [];
			return;
		}

		if (!response.ok) {
			throw new Error("No se pudo cargar usuarios");
		}

		const data = await response.json();
		const content = data.content || [];
		const normalized = searchTerm.trim().toLowerCase();
		users.value = normalized
			? content.filter((user) => user.username?.toLowerCase().includes(normalized))
			: content;
	} catch (error) {
		users.value = [];
	} finally {
		usersLoading.value = false;
	}
};

const handleUserSearch = (value) => {
	if (userSearchTimer) {
		clearTimeout(userSearchTimer);
	}

	userSearchTimer = setTimeout(() => {
		fetchUsers(value || "");
	}, 300);
};

const close = () => {
	router.push({ name: "home" });
};

const submit = async () => {
	errorMessage.value = "";

	const valid = await formRef.value?.validate?.();
	if (valid?.valid === false) {
		return;
	}

	isSaving.value = true;

	const hasUser = Boolean(form.value.userId);
	let nextStatus = form.value.status;
	let nextUserId = form.value.userId ? Number(form.value.userId) : null;

	if (nextStatus === "PENDING") {
		nextUserId = null;
	}

	if (nextStatus === "IN_PROGRESS" && !nextUserId) {
		errorMessage.value = "Debes asignar un usuario para poner la tarea en progreso.";
		isSaving.value = false;
		return;
	}

	if (nextStatus === "COMPLETED" && !nextUserId) {
		errorMessage.value = "Debes asignar el usuario que completo la tarea.";
		isSaving.value = false;
		return;
	}

	const payload = {
		title: form.value.title.trim(),
		description: form.value.description.trim(),
		dueDate: form.value.dueDate,
		status: nextStatus,
		userId: nextUserId,
		latitude: Number(form.value.latitude),
		longitude: Number(form.value.longitude)
	};

	try {
		const url = isEdit.value && selectedTask.value
			? `${API_BASE}/tasks/${selectedTask.value.id}`
			: `${API_BASE}/tasks`;
		const method = isEdit.value ? "PUT" : "POST";

		const response = await fetch(url, {
			method,
			headers: { "Content-Type": "application/json" },
			credentials: "include",
			body: JSON.stringify(payload)
		});

		if (!response.ok) {
			const message = await response.text();
			throw new Error(message || "Error al guardar la tarea");
		}

		fetchMapTasks();
		
		emit("saved");
		router.push({ name: "home" });
	} catch (error) {
		errorMessage.value = error.message || "Error inesperado";
	} finally {
		isSaving.value = false;
	}
};
</script>

<style scoped>
</style>
