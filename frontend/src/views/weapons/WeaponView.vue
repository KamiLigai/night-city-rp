<script setup lang="ts">

import {onMounted, ref} from 'vue'
import client from '@/Clients/Client'
import {useRoute} from 'vue-router'
import {toast} from 'vue3-toastify'
import type {WeaponDto} from "@/dto/weapons/WeaponDto";

const route = useRoute()
const weapon = ref<WeaponDto>()

onMounted(() => {

  const id = route.params.weaponId as string
  client.getWeapon(id)
      .then(response => weapon.value = response.data)
      .catch(() => toast('Ошибка запроса оружия', {type: toast.TYPE.ERROR}))
})
</script>

<template>
  <h1>Оружие</h1>
  <p>Название: {{ weapon?.name }}</p>
  <p>Холодное: {{ weapon?.isMelee }}</p>
  <p>Тип: {{ weapon?.weaponType }}</p>
  <p>Пробитие: {{ weapon?.penetration }}</p>
  <p>Требуемая репутация: {{ weapon?.reputationRequirement }}</p>
</template>

<style scoped>

</style>