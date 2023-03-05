package dev.ihet.aws.infrastructure;

import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Configuration {

    private static final JsonbConfig JSONB_CONFIG = new JsonbConfig().withNullValues(true).withFormatting(true);

    public static Configuration CONFIG = load();

    public String account;

    public String region;

    public String name;

    public String repository;

    public String branchNamePrefix;

    public String accessToken;

    public String domain;

    public String apiKey;

    public String testApiKey;
    public String gId;

    public String emailRecipient;

    public String emailSender;

    private static Configuration load() {
        try (var is = Files.newInputStream(Paths.get("configuration.json"))) {
            return JsonbBuilder.create(JSONB_CONFIG).fromJson(is, Configuration.class);
        } catch (Exception e) {
            throw new RuntimeException("Loading of the configuration failed", e);
        }
    }

    public String namePrefixedId(String... ids) {
        return name + prefixedId(ids);
    }

    public String prefixedId(String... ids) {
        return Stream.of(ids).collect(Collectors.joining());
    }

    public String webOrigin() {
        return "https://www." + domain;
    }

    public String testOrigin() {
        return "https://test." + domain;
    }

    public String prodBranch() {
        return branchNamePrefix + "-main";
    }

    public String testBranch() {
        return branchNamePrefix + "-test";
    }
}
