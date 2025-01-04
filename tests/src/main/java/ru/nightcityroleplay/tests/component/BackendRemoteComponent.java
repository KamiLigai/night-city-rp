package ru.nightcityroleplay.tests.component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import okhttp3.Response;
import ru.nightcityroleplay.tests.dto.*;
import ru.nightcityroleplay.tests.exception.AppContextException;
import ru.nightcityroleplay.tests.remote.BackendRemote;

import java.util.UUID;

import static org.assertj.core.api.Assertions.fail;

public record BackendRemoteComponent(BackendRemote remote, ObjectMapper objectMapper) {

    @SneakyThrows
    public CreateCharacterResponse createCharacter(CreateCharacterRequest request) {
        String jsonBody;
        try (Response response = remote.createCharacter(request)) {
            if (!response.isSuccessful()) {
                fail("Не удалось создать персонажа " + request.name() + ", " + response);
            }
            jsonBody = response.body().string();
        }
        return objectMapper.readValue(jsonBody, CreateCharacterResponse.class);
    }

    @SneakyThrows
    public UserDto createUser(String username, String password) {
        String jsonBody;
        try (Response response = remote.createUser(new CreateUserRequest(username, password))) {
            if (!response.isSuccessful()) {
                throw new AppContextException("Тестовый пользователь не создан: " + response);
            }
            jsonBody = response.body().string();
        }
        return objectMapper.readValue(jsonBody, UserDto.class);
    }

    public void setCurrentUser(UUID id, String username, String password) {
        remote.setCurrentUser(id, username, password);
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
        try (Response response = remote.getCharacter(characterId)) {
            if (!response.isSuccessful()) {
                throw new AppContextException("Не удалось получить персонажа " + response);
            }
            jsonBody = response.body().string();
        }
        return objectMapper.readValue(jsonBody, CharacterDto.class);
    }

    @SneakyThrows
    public PageDto<Object> getCharacterPage(Integer size) {
        String jsonBody;
        Response response = remote.getCharacterPage(size);
        jsonBody = response.body().string();
        return objectMapper.readValue(jsonBody, new TypeReference<>() {
        });
    }

    public Response makeGetCharacterRequest(UUID characterId) {
        return remote.getCharacter(characterId);
    }

    public void updateCharacter(UUID characterId, UpdateCharacterRequest request) {
        try (Response response = remote.updateCharacter(characterId, request)) {
            if (!response.isSuccessful()) {
                fail("Не удалось обновить персонажа " + characterId.toString() + ", " + response);
            }
        }
    }

    public Response makeUpdateCharacterRequest(UUID characterId, UpdateCharacterRequest request) {
        return remote.updateCharacter(characterId, request);
    }

    public Response makeUpdateCharacterWithoutAutentication(UUID characterId, UpdateCharacterRequest request) {
        return remote.updateCharacterWithoutAutentication(characterId, request);
    }

    @SneakyThrows
    public WeaponDto getWeapon(UUID weaponId) {
        String jsonBody;
        try (Response response = remote.getWeapon(weaponId)) {
            if (!response.isSuccessful()) {
                throw new AppContextException("Оружие не найдено " + response);
            }
            jsonBody = response.body().string();
        }
        return objectMapper.readValue(jsonBody, WeaponDto.class);
    }

    @SneakyThrows
    public CreateWeaponResponse createWeapon(CreateWeaponRequest request) {
        String jsonBody;
        try (Response response = remote.createWeapon(request)) {
            if (!response.isSuccessful()) {
                fail("Не удалось создать оружие " + request.name() + ", " + response);
            }
            jsonBody = response.body().string();
        }
        return objectMapper.readValue(jsonBody, CreateWeaponResponse.class);
    }

    public void deleteWeapon(UUID weaponid) {
        try (Response response = remote.deleteWeapon(weaponid)) {
            if (!response.isSuccessful()) {
                fail("Не удалось удалить Оружие " + weaponid + ", " + response);
            }
        }
    }

    public void updateWeapon(UUID weaponId, UpdateWeaponRequest request) {
        try (Response response = remote.updateWeapon(weaponId, request)) {
            if (!response.isSuccessful()) {
                fail("Не удалось обновить Оружие " + weaponId.toString() + ", " + response);
            }
        }
    }

    public Response makeCreateWeaponRequest(CreateWeaponRequest request) {
        return remote.createWeapon(request);
    }

    public Response makeUpdateWeaponRequest(UUID weaponId, UpdateWeaponRequest request) {
        return remote.updateWeapon(weaponId, request);
    }

    public Response makeDeleteWeaponRequest(UUID weaponid) {
        return remote.deleteWeapon(weaponid);
    }
}
