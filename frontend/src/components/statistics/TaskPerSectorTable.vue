<template>
  <v-expansion-panels v-if="sectorGroups.length > 0" variant="accordion">
    <v-expansion-panel
      v-for="sector in sectorGroups"
      :key="sector.name"
    >
      <v-expansion-panel-title collapse-icon="mdi-minus" expand-icon="mdi-plus">
        <v-icon class="mr-2" color="primary">mdi-map-marker-radius</v-icon>
        <span class="font-weight-medium">{{ sector.name }}</span>

        <v-spacer />

        <v-chip color="primary" variant="flat" size="small" prepend-icon="mdi-clipboard-check">
          {{ sector.totalTasks }}
        </v-chip>
      </v-expansion-panel-title>

      <v-expansion-panel-text>
        <v-list lines="one" density="comfortable">
          <v-list-item
            v-for="user in sector.users"
            :key="`${sector.name}-${user.username}`"
          >
            <template #prepend>
              <v-icon color="success">mdi-account-check</v-icon>
            </template>

            <v-list-item-title>
              {{ user.username }}
            </v-list-item-title>

            <template #append>
              <v-chip color="success" variant="tonal" size="small" prepend-icon="mdi-check-circle">
                {{ user.taskCount }}
              </v-chip>
            </template>
          </v-list-item>
        </v-list>
      </v-expansion-panel-text>
    </v-expansion-panel>
  </v-expansion-panels>

  <v-alert v-else type="info" variant="tonal">
    No hay tareas completadas por usuario y sector.
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

const sectorGroups = computed(() => {
  const sectors = new Map();

  const items = Array.isArray(props.items) ? props.items : [];

  items.forEach((item) => {
    const sectorName = item?.sectorName || "Sin sector";
    const username = item?.username || "Sin usuario";
    const taskCount = Number(item?.taskCount ?? 0);

    if (!sectors.has(sectorName)) {
      sectors.set(sectorName, {
        name: sectorName,
        users: new Map()
      });
    }

    const sector = sectors.get(sectorName);
    const currentTaskCount = sector.users.get(username)?.taskCount ?? 0;

    sector.users.set(username, {
      username,
      taskCount: currentTaskCount + (Number.isFinite(taskCount) ? taskCount : 0)
    });
  });

  return Array.from(sectors.values())
    .map((sector) => {
      const users = Array.from(sector.users.values())
        .sort((firstUser, secondUser) => firstUser.username.localeCompare(secondUser.username));

      return {
        name: sector.name,
        users,
        totalTasks: users.reduce((total, user) => total + user.taskCount, 0)
      };
    })
    .sort((firstSector, secondSector) => firstSector.name.localeCompare(secondSector.name));
});
</script>
