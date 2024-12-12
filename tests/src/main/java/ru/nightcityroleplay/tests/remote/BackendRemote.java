package ru.nightcityroleplay.tests.remote;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import okhttp3.*;
import ru.nightcityroleplay.tests.dto.*;
import ru.nightcityroleplay.tests.exception.AppContextException;
import ru.nightcityroleplay.tests.exception.HttpRemoteException;

import java.util.UUID;

import static ru.nightcityroleplay.tests.constants.HttpConstants.Headers.AUTHORIZATION;
import static ru.nightcityroleplay.tests.constants.HttpConstants.MediaTypes.APP_JSON;
import static ru.nightcityroleplay.tests.util.HttpUtils.getBasicAuthorization;

@RequiredArgsConstructor
public class BackendRemote {

    private final String baseUrl;
    private final OkHttpClient client;
    private final ObjectMapper objectMapper;

    @Getter
    private UUID userId;

    @Getter
    private String username;

    @Getter
    private String password;

    public void setCurrentUser(UUID id, String username, String password) {
        this.userId = id;
        this.username = username;
        this.password = password;
    }

    @SneakyThrows
    public void checkHealth() {
        try {
            var httpRequest = new Request.Builder()
                .url(baseUrl + "actuator/health")
                .build();
            String jsonBody;
            try (Response response = client.newCall(httpRequest).execute()) {
                if (!response.isSuccessful()) {
                    throw new AppContextException("Health чек не пройден: " + response);
                }
                jsonBody = response.body().string();
            }
            var health = objectMapper.readValue(jsonBody, HealthDto.class);
            if (!health.status().equals("UP")) {
                throw new HttpRemoteException("Health чек не пройден: " + jsonBody);
            }
        } catch (HttpRemoteException e) {
            throw e;
        } catch (Exception e) {
            throw new HttpRemoteException("Health чек не пройден", e);
        }
    }

    @SneakyThrows
    public Response createUser(CreateUserRequest request) {
        byte[] body = objectMapper.writeValueAsBytes(request);
        var httpRequest = new Request.Builder()
            .url(baseUrl + "users")
            .post(RequestBody.create(body, APP_JSON))
            .build();
        Call call = client.newCall(httpRequest);
        return call.execute();
    }

    @SneakyThrows
    public Response createCharacter(CreateCharacterRequest request) {
        byte[] body = objectMapper.writeValueAsBytes(request);
        Request httpRequest = new Request.Builder()
            .url(baseUrl + "characters")
            .post(RequestBody.create(body, APP_JSON))
            .header(AUTHORIZATION, getBasicAuthorization(username, password))
            .build();
        Call call = client.newCall(httpRequest);
        return call.execute();
    }

    @SneakyThrows
    public Response getCharacterPage(Integer size) {
        Request httpRequest = new Request.Builder()
            .url(baseUrl + "characters" + "?size=" + size)
            .get()
            .header(AUTHORIZATION, getBasicAuthorization(username, password))
            .build();
        Call call = client.newCall(httpRequest);
        return call.execute();
    }

    @SneakyThrows
    public Response updateCharacter(UUID characterId, UpdateCharacterRequest request) {
        byte[] body = objectMapper.writeValueAsBytes(request);
        Request httpRequest = new Request.Builder()
            .url(baseUrl + "characters/" + characterId)
            .put(RequestBody.create(body, APP_JSON))
            .header(AUTHORIZATION, getBasicAuthorization(username, password))
            .build();
        Call call = client.newCall(httpRequest);
        return call.execute();
    }

    @SneakyThrows
    public Response updateCharacterWithoutAutentication(UUID characterId, UpdateCharacterRequest request) {
        byte[] body = objectMapper.writeValueAsBytes(request);
        Request httpRequest = new Request.Builder()
            .url(baseUrl + "characters/" + characterId)
            .put(RequestBody.create(body, APP_JSON))
            .build();
        Call call = client.newCall(httpRequest);
        return call.execute();
    }

    @SneakyThrows
    public Response deleteCharacter(UUID characterId) {
        Request httpRequest = new Request.Builder()
            .url(baseUrl + "characters/" + characterId)
            .delete()
            .header(AUTHORIZATION, getBasicAuthorization(username, password))
            .build();
        Call call = client.newCall(httpRequest);
        return call.execute();
    }
    @SneakyThrows
    public Response getCharacter(UUID characterId) {
        Request httpRequest = new Request.Builder()
            .url(baseUrl + "characters/" + characterId)
            .get()
            .header(AUTHORIZATION, getBasicAuthorization(username, password))
            .build();
        Call call = client.newCall(httpRequest);
        return call.execute();
    }

    @SneakyThrows
    public Response createWeapon(CreateWeaponRequest request) {
        byte[] body = objectMapper.writeValueAsBytes(request);
        Request httpRequest = new Request.Builder()
            .url(baseUrl + "weapons")
            .post(RequestBody.create(body, APP_JSON))
            .header(AUTHORIZATION, getBasicAuthorization(username, password))
            .build();
        Call call = client.newCall(httpRequest);
        return call.execute();
    }

    @SneakyThrows
    public Response getWeapon(UUID weaponId) {
        Request httpRequest = new Request.Builder()
            .url(baseUrl + "weapons/" + weaponId)
            .get()
            .header(AUTHORIZATION, getBasicAuthorization(username, password))
            .build();
        Call call = client.newCall(httpRequest);
        return call.execute();
    }

    @SneakyThrows
    public Response deleteWeapon(UUID weaponid) {
        Request httpRequest = new Request.Builder()
            .url(baseUrl + "weapons/" + weaponid)
            .delete()
            .header(AUTHORIZATION, getBasicAuthorization(username, password))
            .build();
        Call call = client.newCall(httpRequest);
        return call.execute();
    }


    @SneakyThrows
    public Response updateWeapon(UUID weaponId, UpdateWeaponRequest request) {
        byte[] body = objectMapper.writeValueAsBytes(request);
        Request httpRequest = new Request.Builder()
            .url(baseUrl + "weapons/" + weaponId)
            .put(RequestBody.create(body, APP_JSON))
            .header(AUTHORIZATION, getBasicAuthorization(username, password))
            .build();
        Call call = client.newCall(httpRequest);
        return call.execute();
    }
}
