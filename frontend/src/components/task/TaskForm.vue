<template>
	<v-dialog v-model="isOpen" max-width="720" persistent>
		<v-card color="grey-darken-3" variant="elevated">
			<v-card-title class="text-wrap">
				{{ isEdit ? "Editar tarea" : "Nueva tarea" }}
			</v-card-title>
			<v-card-text>
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
								:disabled="!isEdit"
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
						<v-col cols="12" md="6">
							<v-text-field
								v-model="form.latitude"
								label="Latitud"
								type="number"
								step="any"
								:disabled="isEdit"
								:rules="isEdit ? [] : [rules.required]"
								required
							/>
						</v-col>
						<v-col cols="12" md="6">
							<v-text-field
								v-model="form.longitude"
								label="Longitud"
								type="number"
								step="any"
								:disabled="isEdit"
								:rules="isEdit ? [] : [rules.required]"
								required
							/>
						</v-col>
					</v-row>
					<v-alert v-if="errorMessage" type="error" variant="tonal" class="mt-2">
						{{ errorMessage }}
					</v-alert>
				</v-form>
			</v-card-text>
			<v-card-actions class="justify-end">
				<v-btn variant="text" @click="close" :disabled="isSaving">Cancelar</v-btn>
				<v-btn color="primary" @click="submit" :loading="isSaving">
					{{ isEdit ? "Guardar" : "Crear" }}
				</v-btn>
			</v-card-actions>
		</v-card>
	</v-dialog>
</template>

<script setup>
import { computed, ref, watch } from "vue";

const props = defineProps({
	modelValue: { type: Boolean, default: false },
	mode: { type: String, default: "create" },
	task: { type: Object, default: null }
});

const emit = defineEmits(["update:modelValue", "saved"]);

const API_BASE = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api";

const statusOptions = [
	{ label: "Disponible", value: "PENDING" },
	{ label: "En progreso", value: "IN_PROGRESS" },
	{ label: "Completado", value: "COMPLETED" }
];

const isOpen = computed({
	get: () => props.modelValue,
	set: (value) => emit("update:modelValue", value)
});

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
	if (isEdit.value && props.task) {
		form.value = {
			title: props.task.title || "",
			description: props.task.description || "",
			dueDate: props.task.dueDate || "",
			status: props.task.status || "PENDING",
			userId: props.task.userId ?? null,
			latitude: props.task.latitude ?? "",
			longitude: props.task.longitude ?? ""
		};
	} else {
		form.value = emptyForm();
	}
};

watch(
	() => [props.mode, props.task, props.modelValue],
	([, , open]) => {
		if (open) {
			errorMessage.value = "";
			hydrateForm();
			fetchUsers();
		}
	},
	{ immediate: true }
);

const fetchUsers = async (searchTerm = "") => {
	if (usersLoading.value) {
		return;
	}

	usersLoading.value = true;
	try {
		const response = await fetch(`${API_BASE}/users?page=0&size=100`);
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
	emit("update:modelValue", false);
};

const submit = async () => {
	errorMessage.value = "";

	const valid = await formRef.value?.validate?.();
	if (valid?.valid === false) {
		return;
	}

	isSaving.value = true;

	const payload = {
		title: form.value.title.trim(),
		description: form.value.description.trim(),
		dueDate: form.value.dueDate,
		status: form.value.status,
		userId: form.value.userId ? Number(form.value.userId) : null,
		latitude: Number(form.value.latitude),
		longitude: Number(form.value.longitude)
	};

	if (isEdit.value && props.task) {
		payload.latitude = Number(props.task.latitude);
		payload.longitude = Number(props.task.longitude);
	}

	try {
		const url = isEdit.value && props.task
			? `${API_BASE}/tasks/${props.task.id}`
			: `${API_BASE}/tasks`;
		const method = isEdit.value ? "PUT" : "POST";

		const response = await fetch(url, {
			method,
			headers: { "Content-Type": "application/json" },
			body: JSON.stringify(payload)
		});

		if (!response.ok) {
			const message = await response.text();
			throw new Error(message || "Error al guardar la tarea");
		}

		emit("saved");
		close();
	} catch (error) {
		errorMessage.value = error.message || "Error inesperado";
	} finally {
		isSaving.value = false;
	}
};
</script>

<style scoped>
</style>
