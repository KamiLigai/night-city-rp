import {createRouter, createWebHistory} from 'vue-router'

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
            component: () => import('../views/characters/CharactersView.vue')
        },
        {
            path: '/characters/create',
            name: 'create-character',
            component: () => import('../views/characters/CreateCharacterView.vue')
        },
        {
            path: '/characters/:characterId',
            name: 'character',
            component: () => import('../views/characters/CharacterView.vue')
        },
        {
            path: '/characters/:characterId/weapons/update',
            name: 'update-character-weapons',
            component: () => import('../views/characters/UpdateWeaponsView.vue')
        },
        {
            path: '/user/me',
            name: 'current-user',
            component: () => import('../views/users/CurrentUserView.vue')
        },
        {
            path: '/implants',
            name: 'implants',
            component: () => import('../views/implants/ImplantsView.vue')
        },
        {
            path: '/implants/create',
            name: 'create-implant',
            component: () => import('../views/implants/CreateImplantView.vue')
        },
        {
            path: '/implants/:implantId',
            name: 'implant',
            component: () => import('../views/implants/ImplantView.vue')
        },
        {
            path: '/skills',
            name: 'skills',
            component: () => import('../views/skills/SkillsView.vue')
        },
        {
            path: '/skills/create',
            name: 'create-skill',
            component: () => import('../views/skills/CreateSkillView.vue')
        },
        {
            path: '/skills/:skillId',
            name: 'skill',
            component: () => import('../views/skills/SkillView.vue')
        },
        {
            path: '/weapons',
            name: 'weapons',
            component: () => import('../views/weapons/WeaponsView.vue')
        },
        {
            path: '/weapons/create',
            name: 'create-weapon',
            component: () => import('../views/weapons/CreateWeaponView.vue')
        },
        {
            path: '/weapons/:weaponId',
            name: 'weapon',
            component: () => import('../views/weapons/WeaponView.vue')
        },
    ]
})

export default router
