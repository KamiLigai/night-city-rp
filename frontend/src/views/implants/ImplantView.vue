<script setup lang="ts">

import {onMounted, ref} from 'vue'
import client from '@/Clients/Client'
import {useRoute} from 'vue-router'
import {toast} from 'vue3-toastify'
import type {ImplantDto} from "@/dto/implants/ImplantDto";

const route = useRoute()
const implant = ref<ImplantDto>()

onMounted(() => {

  const id = route.params.implantId as string
  client.getImplant(id)
      .then(response => implant.value = response.data)
      .catch(() => toast('Ошибка запроса импланта', {type: toast.TYPE.ERROR}))
})
</script>

<template>
  <h1>Имплант</h1>
  <p>Название: {{ implant?.name }}</p>
  <p>Тип: {{ implant?.implantType }}</p>
  <p>Описание: {{ implant?.description }}</p>
  <p>Требуемая репутация: {{ implant?.reputationRequirement }}</p>
  <p>Стоимость в очках имплантов: {{ implant?.implantPointsCost }}</p>
  <p>Стоимость в специальных очках имплантов: {{ implant?.specialImplantPointsCost }}</p>
</template>

<style scoped>

</style>