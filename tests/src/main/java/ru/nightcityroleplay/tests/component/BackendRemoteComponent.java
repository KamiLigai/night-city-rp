package ru.nightcityroleplay.tests.component;

import okhttp3.Response;
import ru.nightcityroleplay.tests.dto.CreateCharacterRequest;
import ru.nightcityroleplay.tests.remote.BackendRemote;

import static org.assertj.core.api.Assertions.fail;

public record BackendRemoteComponent(BackendRemote remote) {

    public void createCharacter(CreateCharacterRequest request) {
        try (Response response = remote.createCharacter(request)) {
            if (!response.isSuccessful()) {
                fail("Не удалось создать персонажа " + request.name() + ", " + response);
            }
        }
    }

    public Response makeCreateCharacterRequest(CreateCharacterRequest request) {
        return remote.createCharacter(request);
    }
}
