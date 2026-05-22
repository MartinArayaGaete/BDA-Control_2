<template>
  <main class="page-full">
    <v-container fluid class="pa-0 h-100">

      <v-row no-gutters class="h-100 golden-row">

        <v-col class="table-col h-100">
          <v-sheet class="pa-4 h-100">
            <TaskTable />
          </v-sheet>
        </v-col>

        <v-col class="map-col h-100">
          <v-sheet class="h-100">
            <MapReadOnly />
          </v-sheet>
        </v-col>

      </v-row>
    </v-container>
  </main>
</template>

<script setup>
import { useRouter } from "vue-router";
import MapReadOnly from "../components/map/MapReadOnly.vue";
import TaskTable from "../components/task/TaskTable.vue";

const router = useRouter();

function goLogin() {
  localStorage.removeItem("isAuth");
  router.push({ name: "login" });
}
</script>

<style scoped>

.page-full {
  height: calc(100vh - 64px); 
  width: 100%;
  font-family: Arial, sans-serif;
  overflow: hidden;
}

.golden-row {
  display: flex;
  flex-wrap: wrap;
}

.table-col {
  flex: 0 0 38.196%;
  max-width: 38.196%;
}

.map-col {
  flex: 0 0 61.803%;
  max-width: 61.803%;
}

@media (max-width: 960px) {
  .table-col, .map-col {
    flex: 0 0 100%;
    max-width: 100%;
  }
  
  .map-col {
    height: 50vh; /* Para que en celular no desaparezca ocupe todo */
  }
}

.logout {
  margin-top: 1rem;
}

</style>