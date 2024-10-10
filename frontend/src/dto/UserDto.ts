export class UserDto {
  id: string;
  username: string;
  roles: string[]

  constructor(id: string, username: string, roles: string[]) {
    this.id = id
    this.username = username
    this.roles = roles
  }
}
