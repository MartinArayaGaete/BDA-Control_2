import { createRouter, createWebHistory } from "vue-router";
import LoginView from "../views/LoginView.vue";
import RegisterView from "../views/RegisterView.vue";
import HomeView from "../views/HomeView.vue";
import HomeLayout from "../layouts/HomeLayout.vue";

const routes = [
  { path: "/", redirect: "/login" },
  { path: "/login", name: "login", component: LoginView, meta: { public: true } },
  { path: "/register", name: "register", component: RegisterView, meta: { public: true } },
  { 
    path: "/home", 
    component: HomeLayout,
    meta: { requiresAuth: true },
    children: [
      { path: "", name: "home", component: HomeView },
      { path: "tasks/add", name: "tasks", component: HomeView },
      { path: "tasks/edit/:id", name: "editTask", component: HomeView },
      { path: "tasks/view/:id", name: "viewTask", component: HomeView },
    ]
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

router.beforeEach((to) => {
  const isAuth = localStorage.getItem("isAuth") === "true";
  const isPublic = to.meta.public === true;

  if (!isAuth && !isPublic) {
    return { name: "login" };
  }

  if (isAuth && isPublic) {
    return { name: "home" };
  }

  return true;
});

export default router;
