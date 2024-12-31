import { ref, computed, type Ref } from 'vue'
import { defineStore } from 'pinia'
import type { UserDto } from '@/dto/users/UserDto'

export const useUserStore = defineStore('userStore', () => {

  const userValue: Ref<UserDto | null> =  ref(null)

  function setUser(user: UserDto, creds: string) {
    userValue.value = user
    localStorage.creds = creds
  }

  function clearUser() {
    userValue.value = null
    localStorage.creds = null
  }

  const user: Ref<UserDto | null> = computed(() => userValue.value)

  return { setUser, clearUser, user }
})
