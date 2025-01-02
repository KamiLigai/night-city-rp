<script setup lang="ts">

import client from '@/Clients/Client'
import {computed, onMounted, ref} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {toast} from 'vue3-toastify'
import {UpdateWeaponRequest} from "@/dto/weapons/UpdateWeaponRequest";

const route = useRoute()
const router = useRouter()
const request = ref<UpdateWeaponRequest>(new UpdateWeaponRequest())

const weaponId = computed(() => route.params.weaponId as string)

onMounted(() => {
  request.value.isMelee = false
  loadWeapon()
})

function loadWeapon() {
  client.getWeapon(weaponId.value)
      .then(response => {
        request.value.name = response.data.name
        request.value.isMelee = response.data.isMelee
        request.value.weaponType = response.data.weaponType
        request.value.penetration = response.data.penetration
        request.value.reputationRequirement = response.data.reputationRequirement
      }).catch(() => toast('Не удалось загрузить оружие', {type: toast.TYPE.ERROR}))
}

function updateWeapon() {
  client.updateWeapon(weaponId.value, request.value!)
      .then(() => router.push({name: 'weapon', params: {weaponId: weaponId.value}})
          .then(() => toast('Оружие изменено', {type: toast.TYPE.SUCCESS}))
      ).catch(() => toast('Не удалось изменить оружие', {type: toast.TYPE.ERROR}))
}
</script>

<template>
  <div class="container">
    <h1>Изменить оружие</h1>
    <input class="item" placeholder="Название" v-model="request.name">
    <div>
      <label>Холодное оружие: </label>
      <input class="item" type="checkbox" v-model="request.isMelee"/>
    </div>
    <input class="item" placeholder="Тип" v-model="request.weaponType"/>
    <input class="item" placeholder="Пробитие" type="number" v-model="request.penetration"/>
    <input class="item" placeholder="Требуемая репутация" type="number" v-model="request.reputationRequirement"/>
    <button class="item" v-on:click="updateWeapon">Сохранить</button>
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