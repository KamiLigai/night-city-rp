package ru.nightcityroleplay.tests.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import ru.nightcityroleplay.tests.dto.CreateUserRequest;
import ru.nightcityroleplay.tests.dto.UserDto;
import ru.nightcityroleplay.tests.exception.AppContextException;
import ru.nightcityroleplay.tests.remote.BackendRemote;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static java.util.UUID.randomUUID;
import static org.jooq.SQLDialect.POSTGRES;


@UtilityClass
public class AppContext {

    private static final Map<String, Object> DEPENDENCIES = new HashMap<>();

    static {
        try {
            // todo: вынести отсюда этот скрипт
            createPropertiesComponent();
            createDslContext();
            createJackson();
            createOkHttp();
            createBackendRemote();
            createBackendRemoteComponent();
            checkApplication();
            createTestUser();
        } catch (Exception e) {
            throw new AppContextException("Ошибка инициализации контекста", e);
        }
    }

    private static void createPropertiesComponent() {
        var props = new PropertiesComponent();
        props.loadProperties();
        put(props);
    }


    @SuppressWarnings("unchecked")
    public static <T> T get(Class<T> key) {
        return (T) DEPENDENCIES.get(key.getName());
    }

    public static void put(String key, Object value) {
        if (DEPENDENCIES.containsKey(key)) {
            throw new AppContextException("Duplicate dependency: " + key);
        }
        DEPENDENCIES.put(key, value);
    }

    public static void put(Class<?> key, Object value) {
        put(key.getName(), value);
    }

    public static void put(Object value) {
        put(value.getClass().getName(), value);
    }

    private static void createDslContext() throws SQLException {
        var props = get(PropertiesComponent.class);
        System.setProperty("org.jooq.no-logo", "true");
        System.setProperty("org.jooq.no-tips", "true");
        // todo: вынести в параметры
        String url = props.getProperty("datasource.url");
        String userName = props.getProperty("datasource.username");
        String password = props.getProperty("datasource.password");;
        Connection connection = DriverManager.getConnection(url, userName, password);
        DSLContext context = DSL.using(connection, POSTGRES);
        put(DSLContext.class, context);
    }

    private static void createJackson() {
        put(new ObjectMapper());
    }

    private static void createOkHttp() {
        put(new OkHttpClient());
    }

    private static void createBackendRemote() {
        put(new BackendRemote(
            get(PropertiesComponent.class).getProperty("backend.url"),
            get(OkHttpClient.class),
            get(ObjectMapper.class)
        ));
    }

    private static void createBackendRemoteComponent() {
        put(new BackendRemoteComponent(get(BackendRemote.class)));
    }

    private static void checkApplication() {
        get(BackendRemote.class)
            .checkHealth();
    }

    @SneakyThrows
    private static void createTestUser() {
        String username = "user-" + randomUUID();
        var backendRemote = get(BackendRemote.class);
        String jsonBody;
        try(Response response = backendRemote.createUser(new CreateUserRequest(username, username))) {
            if (!response.isSuccessful()) {
                throw new AppContextException("Тестовый пользователь не создан: " + response);
            }
            jsonBody = response.body().string();
        }
        UserDto userDto = get(ObjectMapper.class).readValue(jsonBody, UserDto.class);
        backendRemote.setCurrentUser(userDto.id(), username, username);
    }
}
