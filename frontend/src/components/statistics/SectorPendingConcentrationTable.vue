<template>
  <v-list v-if="pendingSectors.length > 0" lines="two" density="comfortable">
    <v-list-subheader>
      <v-icon class="mr-2" color="warning">mdi-map-search</v-icon>
      Concentración de tareas pendientes por sector
    </v-list-subheader>

    <v-list-item
      v-for="sector in pendingSectors"
      :key="sector.key"
    >
      <template #prepend>
        <v-avatar color="warning" variant="tonal" size="36">
          <v-icon>mdi-map-marker-alert</v-icon>
        </v-avatar>
      </template>

      <v-list-item-title class="font-weight-medium">
        {{ sector.name }}
      </v-list-item-title>

      <v-list-item-subtitle>
        Sector #{{ sector.id }}
      </v-list-item-subtitle>

      <template #append>
        <v-chip color="warning" variant="flat" size="small" prepend-icon="mdi-clipboard-alert">
          {{ sector.taskCount }}
        </v-chip>
      </template>
    </v-list-item>
  </v-list>

  <v-alert v-else type="info" variant="tonal">
    No hay tareas pendientes concentradas por sector.
  </v-alert>
</template>

<script setup>
import { computed } from "vue";

const props = defineProps({
  items: {
    type: Array,
    default: () => []
  }
});

const pendingSectors = computed(() => {
  const sectors = new Map();
  const items = Array.isArray(props.items) ? props.items : [];

  items.forEach((item) => {
    const id = item?.id ?? "sin-id";
    const name = item?.name || "Sin nombre";
    const key = `${id}-${name}`;
    const taskCount = Number(item?.taskCount ?? 0);

    if (!sectors.has(key)) {
      sectors.set(key, {
        id,
        key,
        name,
        taskCount: 0
      });
    }

    const sector = sectors.get(key);
    sector.taskCount += Number.isFinite(taskCount) ? taskCount : 0;
  });

  return Array.from(sectors.values())
    .sort((firstSector, secondSector) => {
      if (secondSector.taskCount !== firstSector.taskCount) {
        return secondSector.taskCount - firstSector.taskCount;
      }

      return firstSector.name.localeCompare(secondSector.name);
    });
});
</script>
