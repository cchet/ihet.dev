package dev.ihet.aws.infrastructure.helper;

import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;

import java.nio.file.Files;
import java.nio.file.Paths;

public class Configuration {

    private static final JsonbConfig JSONB_CONFIG = new JsonbConfig().withNullValues(true).withFormatting(true);

    private static Configuration CONFIG;

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

    public String sesArn;

    public String emailRecipient;

    public String emailSender;

    public static Configuration load() {
        if (CONFIG == null) {
            try (var is = Files.newInputStream(Paths.get("configuration.json"))) {
                CONFIG = JsonbBuilder.create(JSONB_CONFIG).fromJson(is, Configuration.class);
            } catch (Exception e) {
                throw new RuntimeException("Loading of the configuration failed", e);
            }
        }
        return CONFIG;
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
