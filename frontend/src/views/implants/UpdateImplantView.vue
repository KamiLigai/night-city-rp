<script setup lang="ts">

import client from '@/Clients/Client'
import {computed, ref} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {toast} from 'vue3-toastify'
import {UpdateImplantRequest} from "@/dto/implants/UpdateImplantRequest";

const route = useRoute()
const router = useRouter()
const request = ref<UpdateImplantRequest>(new UpdateImplantRequest())

const implantId = computed(() => route.params.implantId as string)

function updateImplant() {
  client.updateImplant(implantId.value, request.value!)
      .then(() => router.push({name: 'implant', params: {implantId: implantId.value}})
          .then(() => toast('Имплант изменен', {type: toast.TYPE.SUCCESS}))
      ).catch(() => toast('Не удалось изменить имплант', {type: toast.TYPE.ERROR}))
}
</script>

<template>
  <div class="container">
    <h1>Изменить имплант</h1>
    <input class="item" placeholder="Название" v-model="request.name">
    <input class="item" placeholder="Тип" v-model="request.implantType"/>
    <input class="item" placeholder="Описание" v-model="request.description"/>
    <input class="item" placeholder="Требуемая репутация" type="number" v-model="request.reputationRequirement"/>
    <input class="item" placeholder="Стоимость в очках имплантов" type="number" v-model="request.implantPointsCost"/>
    <input class="item" placeholder="Стоимость в специальных очках имплантов" type="number"
           v-model="request.specialImplantPointsCost"/>
    <button class="item" v-on:click="updateImplant">Сохранить</button>
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