package ru.nightcityroleplay.tests.component;

import org.yaml.snakeyaml.Yaml;
import ru.nightcityroleplay.tests.exception.TestAppException;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class PropertiesComponent {

    private Map<String, Object> props = Map.of();


    public void loadProperties() {
        Yaml yaml = new Yaml();
        try (InputStream inputStream = PropertiesComponent.class
            .getClassLoader()
            .getResourceAsStream("application.yml")
        ) {
            props = yaml.load(inputStream);
        } catch (Exception e) {
            throw new TestAppException("Ошибка загрузки properties", e);
        }
    }

    public <T> T getProperty(String key) {
        String[] splitArrayKey = key.split(Pattern.quote("."));
        List<String> splitKey = Arrays.asList(splitArrayKey);
        return getProperty(splitKey, props);
    }

    @SuppressWarnings("unchecked")
    public <T> T getProperty(List<String> key, Map<String, Object> source) {
        if (key.size() == 1) {
            return (T) source.get(key.get(0));
        }
        var nestedProps = (Map<String, Object>) source.get(key.get(0));
        return getProperty(key.subList(1, key.size()), nestedProps);
    }
}
