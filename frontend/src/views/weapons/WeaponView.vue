<script setup lang="ts">

import {computed, onMounted, ref} from 'vue'
import client from '@/Clients/Client'
import {useRoute} from 'vue-router'
import {toast} from 'vue3-toastify'
import type {WeaponDto} from "@/dto/weapons/WeaponDto";
import router from "@/router";

const route = useRoute()
const weapon = ref<WeaponDto>()

const weaponId = computed(() => route.params.weaponId as string)

onMounted(() => {
  client.getWeapon(weaponId.value)
      .then(response => weapon.value = response.data)
      .catch(() => toast('Ошибка запроса оружия', {type: toast.TYPE.ERROR}))
})

function goToUpdateWeapon() {
  router.push({name: 'update-weapon', params: {weaponId: weaponId.value}})
}
</script>

<template>
  <h1>Оружие</h1>
  <p>Название: {{ weapon?.name }}</p>
  <p>Холодное: {{ weapon?.isMelee }}</p>
  <p>Тип: {{ weapon?.weaponType }}</p>
  <p>Пробитие: {{ weapon?.penetration }}</p>
  <p>Требуемая репутация: {{ weapon?.reputationRequirement }}</p>
  <button v-on:click="goToUpdateWeapon">Изменить</button>
</template>

<style scoped>

</style>