import { createRouter, createWebHistory } from "vue-router";
import LoginView from "../views/LoginView.vue";
import RegisterView from "../views/RegisterView.vue";
import HomeView from "../views/HomeView.vue";
import HomeLayout from "../layouts/HomeLayout.vue";

const routes = [
  { path: "/", redirect: "/login" },
  { path: "/login", name: "login", component: LoginView },
  { path: "/register", name: "register", component: RegisterView },
  { 
    path: "/home", 
    component: HomeLayout,
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
  if (to.name === "home" && !isAuth) {
    return { name: "login" };
  }
  return true;
});

export default router;
