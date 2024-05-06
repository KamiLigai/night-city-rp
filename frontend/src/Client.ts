import axios, { type AxiosResponse } from 'axios'
import { useUserStore } from '@/stores/userStore'
import type { UserDto } from '@/dto/UserDto'
import { CreateUserRequest } from '@/dto/CreateUserRequest'

// todo: make useUserStore() lazy loading field
class Client {

  axiosClient = axios.create({
    baseURL: 'http://localhost:8080'
  })

  public login(username: string, password: string): Promise<AxiosResponse<UserDto>> {
    let creds = btoa(`${username}:${password}`)
    useUserStore().clearUser()
    this.axiosClient.defaults.headers.common['Authorization'] = null
    return this.axiosClient.get<UserDto>(
      '/users/me',
      {
        headers: {
          Authorization: 'Basic ' + creds
        }
      }
    ).then(response => {
      useUserStore().setUser(response.data, creds)
      this.axiosClient.defaults.headers.common['Authorization'] = 'Basic ' + localStorage.creds
      return response
    })
  }

  public getCurrentUser(): Promise<AxiosResponse<UserDto>> {
    return this.axiosClient.get<UserDto>('/users/me')
  }

  logout() {
    useUserStore().clearUser()
    this.axiosClient.defaults.headers.common['Authorization'] = null
  }

  createUser(username: string, password: string) {
    let createUserRequest = new CreateUserRequest(username, password)
    return this.axiosClient.post("/users", createUserRequest)
  }
}

export default new Client()
