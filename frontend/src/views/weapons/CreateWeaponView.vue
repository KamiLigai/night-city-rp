<script setup lang="ts">

import client from '@/Clients/Client'
import {onMounted, ref} from 'vue'
import {useRouter} from 'vue-router'
import {toast} from 'vue3-toastify'
import {CreateWeaponRequest} from "@/dto/weapons/CreateWeaponRequest";

const router = useRouter()
const request = ref<CreateWeaponRequest>(new CreateWeaponRequest())

onMounted(() => {
  request.value.isMelee = false
})

function createWeapon() {
  client.createWeapon(request.value!)
      .then(response => router.push({name: 'weapon', params: {weaponId: response.data.id}}))
      .catch(() => toast('Не удалось создать оружие', {type: toast.TYPE.ERROR}))
}
</script>

<template>
  <div class="container">
    <h1>Создать оружие</h1>
    <input class="item" placeholder="Название" v-model="request.name">
    <div>
      <label>Холодное оружие: </label>
      <input class="item" type="checkbox" v-model="request.isMelee"/>
    </div>
    <input class="item" placeholder="Тип" v-model="request.weaponType"/>
    <input class="item" placeholder="Пробитие" type="number" v-model="request.penetration"/>
    <input class="item" placeholder="Требуемая репутация" type="number" v-model="request.reputationRequirement"/>
    <button class="item" v-on:click="createWeapon">Создать</button>
  </div>
</template>

<style scoped>
.container {
  display: flex;
  flex-direction: column;
  align-items: center
}

.container {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.item {
  margin: 4px;
}
</style>