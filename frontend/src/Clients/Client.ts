import axios, { type AxiosResponse } from 'axios'
import { useUserStore } from '@/stores/userStore'
import type { UserDto } from '@/dto/UserDto'
import { CreateUserRequest } from '@/dto/CreateUserRequest'
import { Base64 } from 'js-base64'
import type { CreateCharacterRequest } from '@/dto/CreateCharacterRequest'
import type { CreateCharacterResponse } from '@/dto/CreateCharacterResponse'
import type { CharacterDto } from '@/dto/CharacterDto'
import type { CharactersPage } from '@/dto/CharactersPage'

// todo: make useUserStore() lazy loading field
class Client {

    axiosClient = axios.create({
        baseURL: import.meta.env.VITE_BACKEND_BASE_URL || 'api',
        headers: { 'X-Requested-With': 'XMLHttpRequest' }
    })

    public login(username: string, password: string): Promise<AxiosResponse<UserDto>> {
        let creds = Base64.encode(`${username}:${password}`)
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
        return this.axiosClient.post('/users', createUserRequest)
    }

    getCharacters(page: number, size: number): Promise<AxiosResponse<CharactersPage>> {
        return this.axiosClient.get('/characters', {
            params: { page, size }
        })
    }

    createCharacter(request: CreateCharacterRequest): Promise<AxiosResponse<CreateCharacterResponse>> {
        return this.axiosClient.post('/characters', request)
    }

    getCharacter(id: string): Promise<AxiosResponse<CharacterDto>> {
        return this.axiosClient.get('/characters/' + id)
    }
}

export default new Client()
