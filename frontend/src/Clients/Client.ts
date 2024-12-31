import axios, {type AxiosResponse} from 'axios'
import {useUserStore} from '@/stores/userStore'
import type {UserDto} from '@/dto/users/UserDto'
import {CreateUserRequest} from '@/dto/users/CreateUserRequest'
import {Base64} from 'js-base64'
import type {CreateCharacterRequest} from '@/dto/characters/CreateCharacterRequest'
import type {CreateCharacterResponse} from '@/dto/characters/CreateCharacterResponse'
import type {CharacterDto} from '@/dto/characters/CharacterDto'
import type {Page} from "@/dto/Page";
import type {ImplantDto} from "@/dto/implants/ImplantDto";
import type {CreateImplantRequest} from "@/dto/implants/CreateImplantRequest";
import type {CreateImplantResponse} from "@/dto/implants/CreateImplantResponse";
import type {SkillDto} from "@/dto/skills/SkillDto";
import type {CreateSkillRequest} from "@/dto/skills/CreateSkillRequest";
import type {CreateSkillResponse} from "@/dto/skills/CreateSkillResponse";
import type {WeaponDto} from "@/dto/weapons/WeaponDto";
import type {CreateWeaponRequest} from "@/dto/weapons/CreateWeaponRequest";
import type {CreateWeaponResponse} from "@/dto/weapons/CreateWeaponResponse";

// todo: make useUserStore() lazy loading field
class Client {

    axiosClient = axios.create({
        baseURL: import.meta.env.VITE_BACKEND_BASE_URL || '/api',
        headers: {'X-Requested-With': 'XMLHttpRequest'}
    })

    public login(username: string, password: string): Promise<AxiosResponse<UserDto>> {
        const creds = Base64.encode(`${username}:${password}`)
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
        const createUserRequest: CreateUserRequest = {username, password}
        return this.axiosClient.post('/users', createUserRequest)
    }

    getCharacters(page: number, size: number): Promise<AxiosResponse<Page<CharacterDto>>> {
        return this.axiosClient.get('/characters', {
            params: {page, size}
        })
    }

    createCharacter(request: CreateCharacterRequest): Promise<AxiosResponse<CreateCharacterResponse>> {
        return this.axiosClient.post('/characters', request)
    }

    getCharacter(id: string): Promise<AxiosResponse<CharacterDto>> {
        return this.axiosClient.get('/characters/' + id)
    }

    getImplants(page: number, size: number): Promise<AxiosResponse<Page<ImplantDto>>> {
        return this.axiosClient.get('/implants', {
            params: {page, size}
        })
    }

    createImplant(request: CreateImplantRequest): Promise<AxiosResponse<CreateImplantResponse>> {
        return this.axiosClient.post('/implants', request)
    }

    getImplant(id: string): Promise<AxiosResponse<ImplantDto>> {
        return this.axiosClient.get('/implants/' + id)
    }

    getSkills(page: number, size: number): Promise<AxiosResponse<Page<SkillDto>>> {
        return this.axiosClient.get('/skills', {
            params: {page, size}
        })
    }

    createSkill(request: CreateSkillRequest): Promise<AxiosResponse<CreateSkillResponse>> {
        return this.axiosClient.post('/skills', request)
    }

    getSkill(id: string): Promise<AxiosResponse<SkillDto>> {
        return this.axiosClient.get('/skills/' + id)
    }

    getWeapons(page: number, size: number): Promise<AxiosResponse<Page<WeaponDto>>> {
        return this.axiosClient.get('/weapons', {
            params: {page, size}
        })
    }

    createWeapon(request: CreateWeaponRequest): Promise<AxiosResponse<CreateWeaponResponse>> {
        return this.axiosClient.post('/weapons', request)
    }

    getWeapon(id: string): Promise<AxiosResponse<WeaponDto>> {
        return this.axiosClient.get('/weapons/' + id)
    }
}

export default new Client()
