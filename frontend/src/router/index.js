import { createRouter, createWebHistory } from "vue-router";
import LoginView from "../views/LoginView.vue";
import RegisterView from "../views/RegisterView.vue";
import HomeView from "../views/HomeView.vue";
import HomeLayout from "../layouts/HomeLayout.vue";
import TaskTable from "../components/task/TaskTable.vue";
import TaskForm from "../components/task/TaskForm.vue";
import NotificationsPanel from "../components/notifications/NotificationsPanel.vue";
import StatisticsPanel from "../components/statistics/StatisticsPanel.vue";
import { useAuthState } from "../composables/useAuthState";

const routes = [
  { path: "/", redirect: "/login" },
  { path: "/login", name: "login", component: LoginView, meta: { public: true } },
  { path: "/register", name: "register", component: RegisterView, meta: { public: true } },
  { 
    path: "/home", 
    component: HomeLayout,
    meta: { requiresAuth: true },
    children: [
      {
        path: "",
        component: HomeView,
        children: [
          { path: "", name: "home", component: TaskTable },
          { path: "tasks/add", name: "createTask", component: TaskForm, props: { mode: 'create' } },
          { path: "tasks/edit/:id", name: "editTask", component: TaskForm, props: { mode: 'edit' } },
          { path: "statistics", name: "statistics", component: StatisticsPanel },
          { path: "notifications", name: "notifications", component: NotificationsPanel }
        ]
      }
    ]
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

const authState = useAuthState();

router.beforeEach(async (to) => {
  const isPublic = to.meta.public === true;

  if (!authState.initialized.value) {
    await authState.bootstrapAuth();
  }

  const isAuth = authState.isAuthenticated.value;

  if (!isAuth && !isPublic) {
    return { name: "login" };
  }

  if (isAuth && isPublic) {
    return { name: "home" };
  }

  return true;
});

export default router;
