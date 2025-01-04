<script setup lang="ts">

import client from '@/Clients/Client'
import {ref} from 'vue'
import {useRouter} from 'vue-router'
import {toast} from 'vue3-toastify'
import {CreateImplantRequest} from "@/dto/implants/CreateImplantRequest";

const router = useRouter()
const request = ref<CreateImplantRequest>(new CreateImplantRequest())

function createImplant() {
  client.createImplant(request.value!)
      .then(response => router.push({name: 'implant', params: {implantId: response.data.id}})
          .then(() => toast('Новый имплант создан', {type: toast.TYPE.SUCCESS}))
      ).catch(() => toast('Не удалось создать имплант', {type: toast.TYPE.ERROR}))
}
</script>

<template>
  <div class="container">
    <h1>Создать имплант</h1>
    <input class="item" placeholder="Название" v-model="request.name">
    <input class="item" placeholder="Тип" v-model="request.implantType"/>
    <input class="item" placeholder="Описание" v-model="request.description"/>
    <input class="item" placeholder="Требуемая репутация" type="number" v-model="request.reputationRequirement"/>
    <input class="item" placeholder="Стоимость в очках имплантов" type="number" v-model="request.implantPointsCost"/>
    <input class="item" placeholder="Стоимость в специальных очках имплантов" type="number"
           v-model="request.specialImplantPointsCost"/>
    <button class="item" v-on:click="createImplant">Создать</button>
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