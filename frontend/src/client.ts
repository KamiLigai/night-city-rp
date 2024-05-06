import axios, { type AxiosResponse } from 'axios'

class Client {

  axiosClient = axios.create({
    baseURL: 'http://localhost:8080'
    // headers: { "Authorization": "Basic YWRtaW46YWRtaW4=" }
  })

  public login(username: string, password: string): Promise<AxiosResponse<UserDto>> {
    // this.axiosClient.defaults.headers.common['Authorization'] = 'Bearer ' + password
    return this.axiosClient.get<UserDto>(
      '/users/me',
      {
        headers: {
          Authorization: 'Basic ' + btoa(`${username}:${password}`),
        }
      }
    )
  }
}

export default new Client()
