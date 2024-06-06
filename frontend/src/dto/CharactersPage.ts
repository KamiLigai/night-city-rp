import type { CharacterDto } from '@/dto/CharacterDto'

export class CharactersPage {
    content: CharacterDto[]
    last: boolean

    constructor(content: CharacterDto[], last: boolean) {
        this.content = content
        this.last = last
    }
}