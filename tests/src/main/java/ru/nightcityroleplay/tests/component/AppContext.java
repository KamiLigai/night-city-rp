package ru.nightcityroleplay.tests.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import okhttp3.OkHttpClient;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.jooq.impl.DSL;
import ru.nightcityroleplay.tests.dto.UserDto;
import ru.nightcityroleplay.tests.entity.tables.records.UsersRecord;
import ru.nightcityroleplay.tests.exception.AppContextException;
import ru.nightcityroleplay.tests.remote.BackendRemote;
import ru.nightcityroleplay.tests.repo.WeaponRepo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static java.util.UUID.randomUUID;
import static org.jooq.SQLDialect.POSTGRES;
import static ru.nightcityroleplay.tests.entity.Tables.USERS;
import static ru.nightcityroleplay.tests.entity.tables.Roles.ROLES;
import static ru.nightcityroleplay.tests.entity.tables.UsersRoles.USERS_ROLES;


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
            createAdminUser();
            createWeaponRepo();
        } catch (Exception e) {
            throw new AppContextException("Ошибка инициализации контекста", e);
        }
    }

    private static void createPropertiesComponent() {
        var props = new PropertiesComponent();
        props.loadProperties();
        put(props);
    }


    public static <T> T get(Class<T> key) {
        return get(key.getName());
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(String key) {
        return (T) DEPENDENCIES.get(key);
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
        String password = props.getProperty("datasource.password");
        Connection connection = DriverManager.getConnection(url, userName, password);
        DSLContext context = DSL.using(connection, POSTGRES);
        put(DSLContext.class, context);
    }

    private static void createJackson() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule())
                .disable(FAIL_ON_UNKNOWN_PROPERTIES);
        put(objectMapper);
    }

    private static void createOkHttp() {
        Logger.getLogger(OkHttpClient.class.getName()).setLevel(Level.FINE);
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
        put(new BackendRemoteComponent(get(BackendRemote.class), get(ObjectMapper.class)));
    }

    private static void checkApplication() {
        get(BackendRemote.class)
            .checkHealth();
    }

    @SneakyThrows
    private static void createTestUser() {
        DSLContext dbContext = AppContext.get(DSLContext.class);
        String username = "test-1";
        Result<UsersRecord> testUserFetch = dbContext.select()
            .from(USERS)
            .where(USERS.USERNAME.eq(username))
            .fetchInto(USERS);


        if (testUserFetch.isNotEmpty()) {
            UsersRecord usersRecord = testUserFetch.get(0);
            put("defaultUser", new UserDto(usersRecord.getId(), usersRecord.getUsername()));
            return;
        }

        var backendRemoteComponent = get(BackendRemoteComponent.class);
        UserDto userDto = backendRemoteComponent.createUser(username, username);
        backendRemoteComponent.setCurrentUser(userDto.id(), username, username);
        put("defaultUser", userDto);
    }
    @SneakyThrows
    private static void createAdminUser() {
        String username = "admin-" + randomUUID();
        var backendRemoteComponent = get(BackendRemoteComponent.class);

        UserDto adminDto = backendRemoteComponent.createUser(username, username);
        put("defaultAdmin", adminDto);
        DSLContext dbContext = AppContext.get(DSLContext.class);
        // Получаем ID роли "ADMIN"
        UUID adminRoleId = dbContext.select(ROLES.ID)
            .from(ROLES)
            .where(ROLES.NAME.eq("ADMIN"))
            .fetchOne(ROLES.ID);

        // Получаем ID созданного пользователя из adminDto
        UUID userId = adminDto.id();

        // Вставляем данные в USERS_ROLES
        dbContext.insertInto(USERS_ROLES)
            .set(USERS_ROLES.USER_ID, userId) // ID пользователя
            .set(USERS_ROLES.ROLE_ID, adminRoleId) // ID роли "ADMIN"
            .execute();
    }

    private static void createWeaponRepo() {
        WeaponRepo weaponRepo = new WeaponRepo(get(DSLContext.class));
        put(weaponRepo);
    }
}
