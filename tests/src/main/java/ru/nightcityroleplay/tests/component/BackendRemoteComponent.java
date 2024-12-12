package ru.nightcityroleplay.tests.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import okhttp3.Response;
import ru.nightcityroleplay.tests.dto.*;
import ru.nightcityroleplay.tests.exception.AppContextException;
import ru.nightcityroleplay.tests.remote.BackendRemote;


import java.awt.print.Pageable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.fail;

public record BackendRemoteComponent(BackendRemote remote, ObjectMapper objectMapper) {

    public void createCharacter(CreateCharacterRequest request) {
        try (Response response = remote.createCharacter(request)) {
            if (!response.isSuccessful()) {
                fail("Не удалось создать персонажа " + request.name() + ", " + response);
            }
        }
    }

    @SneakyThrows
    public UserDto createUser(String username, String password) {
        String jsonBody;
        try(Response response = remote.createUser(new CreateUserRequest(username, password))) {
            if (!response.isSuccessful()) {
                throw new AppContextException("Тестовый пользователь не создан: " + response);
            }
            jsonBody = response.body().string();
        }
        return objectMapper.readValue(jsonBody, UserDto.class);
    }

    public void setCurrentUser(UUID id, String username, String password) {
        remote.setCurrentUser(id,username,password);
    }

    public Response makeCreateCharacterRequest(CreateCharacterRequest request) {
        return remote.createCharacter(request);
    }

    public void deleteCharacter(UUID characterId) {
        try (Response response = remote.deleteCharacter(characterId)) {
            if (!response.isSuccessful()) {
                fail("Не удалось удалить персонажа " + characterId + ", " + response);
            }
        }
    }

    public Response makeDeleteCharacterRequest(UUID characterId) {
        return remote.deleteCharacter(characterId);
    }

    @SneakyThrows
    public CharacterDto getCharacter(UUID characterId) {
        String jsonBody;
        try(Response response = remote.getCharacter(characterId)) {
            if (!response.isSuccessful()) {
                throw new AppContextException("Персонаж не найден " + response);
            }
            jsonBody = response.body().string();
        }
        return objectMapper.readValue(jsonBody, CharacterDto.class);
    }

    @SneakyThrows
    public PageDto getCharacterPage(Integer size) {
        String jsonBody;
        Response response = remote.getCharacterPage(size);
        jsonBody = response.body().string();
        return objectMapper.readValue(jsonBody,PageDto.class);

    }

    public Response makeGetCharacterRequest(UUID characterId) { return remote.getCharacter(characterId); }
}
