import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import Vue3Toastify, { toast, type ToastContainerOptions } from 'vue3-toastify'
import 'vue3-toastify/dist/index.css';

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(Vue3Toastify, {
    autoClose: 5000,
    position: toast.POSITION.BOTTOM_RIGHT
} as ToastContainerOptions);

app.mount('#app')
