import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('../views/HomeView.vue')
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue')
    },
    {
      path: '/register',
      name: 'registration',
      component: () => import('../views/RegistrationView.vue')
    },
    {
      path: '/characters',
      name: 'characters',
      component: () => import('../views/CharactersView.vue')
    },
    {
      path: '/create-character',
      name: 'create-character',
      component: () => import('../views/CreateCharacterView.vue')
    },
    {
      path: '/characters/:characterId',
      name: 'character',
      component: () => import('../views/CharacterView.vue')
    }
  ]
})

export default router
