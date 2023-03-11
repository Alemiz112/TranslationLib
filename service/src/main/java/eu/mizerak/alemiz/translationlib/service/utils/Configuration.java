package eu.mizerak.alemiz.translationlib.service.utils;

import lombok.Builder;
import lombok.Data;

import java.util.*;

@Data
@Builder
public class Configuration {
    private final Properties properties;

    private String httpHost;
    private int httpPort;

    private String mongoUrl;
    private String mongoDatabase;
    private int maxMongoPoolSize;

    private List<String> accessTokens;

    private String scrapperName;

    public static Configuration parse(Properties properties) {
        return Configuration.builder()
                .properties(properties)
                .httpHost(parseValue("http.host", properties))
                .httpPort(Integer.parseInt(parseValue("http.port", properties)))
                .mongoUrl(parseValue("mongodb.connection-string", properties))
                .mongoDatabase(parseValue("mongodb.database", properties))
                .maxMongoPoolSize(Integer.parseInt(parseValue("mongodb.max-pool-size", properties)))
                .accessTokens(Collections.unmodifiableList(Arrays.asList(parseValue("service.access-tokens", properties).split(","))))
                .scrapperName(parseValue("scrapper.name", properties))
                .build();
    }

    public String getProperty(String property) {
        return parseValue(property, properties, true);
    }

    public String getProperty(String property, boolean check) {
        return parseValue(property, properties, !check);
    }

    private static String parseValue(String key, Properties properties) {
        return parseValue(key, properties, false);
    }

    private static String parseValue(String key, Properties properties, boolean failSafe) {
        String property = properties.getProperty(key);
        if (property == null && !failSafe) {
            throw new IllegalStateException("Missing configuration property " + key);
        }

        if (!property.startsWith("${")) {
            return property;
        }

        String value = property.substring(2, property.lastIndexOf('}'));

        int index = value.indexOf(':');
        String envName = value.substring(0, index == -1 ? value.length() : index);
        String defaultVal = index > 0 && index + 1 < value.length() ? value.substring(index + 1) : null;

        String env = System.getenv(envName);
        return env == null ? defaultVal : env;
    }
}
