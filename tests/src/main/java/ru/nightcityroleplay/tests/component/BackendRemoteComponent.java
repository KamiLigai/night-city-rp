package ru.nightcityroleplay.tests.component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Cleanup;
import lombok.SneakyThrows;
import okhttp3.Response;
import ru.nightcityroleplay.tests.dto.*;
import ru.nightcityroleplay.tests.exception.AppContextException;
import ru.nightcityroleplay.tests.remote.BackendRemote;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.fail;

public record BackendRemoteComponent(BackendRemote remote, ObjectMapper objectMapper) {

    @SneakyThrows
    public CharacterDto createCharacter(CreateCharacterRequest request) {
        @Cleanup Response response = remote.createCharacter(request);
        if (!response.isSuccessful()) {
            fail("Не удалось создать персонажа " + request.name() + ", " + response);
        }
        var jsonBody = response.body().string();
        return objectMapper.readValue(jsonBody, CharacterDto.class);
    }

    @SneakyThrows
    public UserDto createUser(String username, String password) {
        @Cleanup Response response = remote.createUser(new CreateUserRequest(username, password));
        if (!response.isSuccessful()) {
            throw new AppContextException("Тестовый пользователь не создан: " + response);
        }
        var jsonBody = response.body().string();
        return objectMapper.readValue(jsonBody, UserDto.class);
    }

    public void setCurrentUser(UUID id, String username, String password) {
        remote.setCurrentUser(id, username, password);
    }

    public HttpResponse makeCreateCharacterRequest(CreateCharacterRequest request) {
        @Cleanup Response response = remote.createCharacter(request);
        return toHttpResponse(response);
    }

    @SneakyThrows
    private HttpResponse toHttpResponse(Response response) {
        return new HttpResponse(
            response.code(),
            response.body().string()
        );
    }

    public void deleteCharacter(UUID characterId) {
        @Cleanup Response response = remote.deleteCharacter(characterId);
        if (!response.isSuccessful()) {
            fail("Не удалось удалить персонажа " + characterId + ", " + response);
        }
    }

    public HttpResponse makeDeleteCharacterRequest(UUID characterId) {
        @Cleanup Response response = remote.deleteCharacter(characterId);
        return toHttpResponse(response);
    }

    @SneakyThrows
    public CharacterDto getCharacter(UUID characterId) {
        @Cleanup Response response = remote.getCharacter(characterId);
        if (!response.isSuccessful()) {
            throw new AppContextException("Не удалось получить персонажа " + response);
        }
        var jsonBody = response.body().string();
        return objectMapper.readValue(jsonBody, CharacterDto.class);
    }

    @SneakyThrows
    public PageDto<Object> getCharacterPage(Integer size) {
        @Cleanup Response response = remote.getCharacterPage(size);
        var jsonBody = response.body().string();
        return objectMapper.readValue(jsonBody, new TypeReference<>() {
        });
    }

    public HttpResponse makeGetCharacterRequest(UUID characterId) {
        @Cleanup Response character = remote.getCharacter(characterId);
        return toHttpResponse(character);
    }

    public void updateCharacter(UUID characterId, UpdateCharacterRequest request) {
        @Cleanup Response response = remote.updateCharacter(characterId, request);
        if (!response.isSuccessful()) {
            fail("Не удалось обновить персонажа " + characterId.toString() + ", " + response);
        }
    }

    public HttpResponse makeUpdateCharacterRequest(UUID characterId, UpdateCharacterRequest request) {
        @Cleanup Response response = remote.updateCharacter(characterId, request);
        return toHttpResponse(response);
    }

    public HttpResponse makeUpdateCharacterWithoutAuthentication(UUID characterId, UpdateCharacterRequest request) {
        @Cleanup Response response = remote.updateCharacterWithoutAutentication(characterId, request);
        return toHttpResponse(response);
    }

    @SneakyThrows
    public WeaponDto getWeapon(UUID weaponId) {
        @Cleanup Response response = remote.getWeapon(weaponId);
        if (!response.isSuccessful()) {
            throw new AppContextException("Оружие не найдено " + response);
        }
        var jsonBody = response.body().string();
        return objectMapper.readValue(jsonBody, WeaponDto.class);
    }

    @SneakyThrows
    public void createWeapon(CreateWeaponRequest request) {
        @Cleanup Response response = remote.createWeapon(request);
        if (!response.isSuccessful()) {
            fail("Не удалось создать оружие " + request.name() + ", " + response);
        }
        var jsonBody = response.body().string();
        objectMapper.readValue(jsonBody, CreateWeaponResponse.class);
    }

    public void deleteWeapon(UUID weaponId) {
        @Cleanup Response response = remote.deleteWeapon(weaponId);
        if (!response.isSuccessful()) {
            fail("Не удалось удалить Оружие " + weaponId + ", " + response);
        }
    }

    public void updateWeapon(UUID weaponId, UpdateWeaponRequest request) {
        @Cleanup Response response = remote.updateWeapon(weaponId, request);
        if (!response.isSuccessful()) {
            fail("Не удалось обновить Оружие " + weaponId.toString() + ", " + response);
        }
    }

    @SneakyThrows
    public void createImplant(CreateImplantRequest request) {
        @Cleanup Response response = remote.createImplant(request);
        if (!response.isSuccessful()) {
            fail("Не удалось создать имплант " + request.name() + ", " + response);
        }
        var jsonBody = response.body().string();
        objectMapper.readValue(jsonBody, CreateImplantRequest.class);
    }

    public void deleteImplant(UUID implantid, boolean ignoreAssignments) {
        @Cleanup Response response = remote.deleteImplant(implantid, ignoreAssignments);
        if (!response.isSuccessful()) {
            fail("Не удалось удалить Имплант " + implantid + ", " + response);
        }
    }

    @SneakyThrows
    public ImplantDto getImplant(UUID implantid) {
        @Cleanup Response response = remote.getImplant(implantid);
        if (!response.isSuccessful()) {
            throw new AppContextException("Имплант не найден " + response);
        }
        var jsonBody = response.body().string();
        return objectMapper.readValue(jsonBody, ImplantDto.class);
    }

    @SneakyThrows
    public Integer getImplantAssignmentsCount(UUID implantid) {
        @Cleanup Response response = remote.getImplantAssignmentsCount(implantid);
        if (!response.isSuccessful()) {
            throw new AppContextException("Имплант не найден " + response);
        }
        var jsonBody = response.body().string();
        return objectMapper.readValue(jsonBody, Integer.class);
    }

    @SneakyThrows
    public void updateImplant(UUID implantid, UpdateImplantRequest request) {
        @Cleanup Response response = remote.updateImplant(implantid, request);
        if (!response.isSuccessful()) {
            fail("Не удалось обновить Имплант " + implantid.toString() + ", " + response);
        }
    }

    public HttpResponse makeCreateImplantRequest(CreateImplantRequest request) {
        @Cleanup Response response = remote.createImplant(request);
        return toHttpResponse(response);
    }

    @SneakyThrows
    public SkillDto createSkill(CreateSkillRequest request) {
        @Cleanup Response response = remote.createSkill(request);
        if (!response.isSuccessful()) {
            fail("Не удалось создать навык " + request.name() + ", " + response);
        }
        var jsonBody = response.body().string();
        return objectMapper.readValue(jsonBody, SkillDto.class);
    }

    @SneakyThrows
    public SkillDto getSkill(UUID skillId) {
        @Cleanup Response response = remote.getSkill(skillId);
        if (!response.isSuccessful()) {
            throw new AppContextException("Не удалось получить навык " + response);
        }
        var jsonBody = response.body().string();
        return objectMapper.readValue(jsonBody, SkillDto.class);
    }

    @SneakyThrows
    public List<UUID> getSkillIds() {
        @Cleanup Response response = remote.getSkillIds();
        if (!response.isSuccessful()) {
            throw new AppContextException("Не удалось получить id навыков " + response);
        }
        var jsonBody = response.body().string();
        return objectMapper.readValue(jsonBody, new TypeReference<>() {});
    }

    @SneakyThrows
    public List<SkillDto> getSkillsBulk(IdsRequest request) {
        @Cleanup Response response = remote.getSkillsBulk(request);
        if (!response.isSuccessful()) {
            throw new AppContextException("Не удалось получить навыки " + response);
        }
        var jsonBody = response.body().string();
        return objectMapper.readValue(jsonBody, new TypeReference<>() {});
    }

    public HttpResponse makeCreateWeaponRequest(CreateWeaponRequest request) {
        @Cleanup Response response = remote.createWeapon(request);
        return toHttpResponse(response);
    }

    public HttpResponse makeUpdateWeaponRequest(UUID weaponId, UpdateWeaponRequest request) {
        @Cleanup Response response = remote.updateWeapon(weaponId, request);
        return toHttpResponse(response);
    }

    public HttpResponse makeUpdateImplantRequest(UUID implantid, UpdateImplantRequest request) {
        @Cleanup Response response = remote.updateImplant(implantid, request);
        return toHttpResponse(response);
    }

    public HttpResponse makeDeleteWeaponRequest(UUID weaponId) {
        @Cleanup Response response = remote.deleteWeapon(weaponId);
        return toHttpResponse(response);
    }

    public HttpResponse makeDeleteImplantRequest(UUID implantid, boolean redButton) {
        @Cleanup Response response = remote.deleteImplant(implantid, redButton);
        return toHttpResponse(response);
    }
}
