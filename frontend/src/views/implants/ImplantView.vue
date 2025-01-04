<script setup lang="ts">

import {computed, onMounted, ref} from 'vue'
import client from '@/Clients/Client'
import {useRoute} from 'vue-router'
import {toast} from 'vue3-toastify'
import type {ImplantDto} from "@/dto/implants/ImplantDto";
import router from "@/router";

const route = useRoute()
const implant = ref<ImplantDto>()

const implantId = computed(() => route.params.implantId as string)

onMounted(() => {
  client.getImplant(implantId.value)
      .then(response => implant.value = response.data)
      .catch(() => toast('Ошибка запроса импланта', {type: toast.TYPE.ERROR}))
})

function goToUpdateImplant() {
  router.push({name: 'update-implant', params: {implantId: implantId.value}})
}
</script>

<template>
  <h1>Имплант</h1>
  <p>Название: {{ implant?.name }}</p>
  <p>Тип: {{ implant?.implantType }}</p>
  <p>Описание: {{ implant?.description }}</p>
  <p>Требуемая репутация: {{ implant?.reputationRequirement }}</p>
  <p>Стоимость в очках имплантов: {{ implant?.implantPointsCost }}</p>
  <p>Стоимость в специальных очках имплантов: {{ implant?.specialImplantPointsCost }}</p>
  <button v-on:click="goToUpdateImplant">Изменить</button>
</template>

<style scoped>

</style>