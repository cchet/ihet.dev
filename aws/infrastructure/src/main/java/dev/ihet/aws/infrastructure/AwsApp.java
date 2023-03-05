package dev.ihet.aws.infrastructure;

import dev.ihet.aws.infrastructure.stacks.BackendStack;
import dev.ihet.aws.infrastructure.stacks.FrontEndStack;
import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.Tags;

public class AwsApp {
    private static final Configuration config = Configuration.CONFIG;

    public static void main(final String[] args) {
        var app = new App();

        new BackendStack(app, config.namePrefixedId("BackendStack"), StackProps.builder()
                .description("The backend for the website " + config.webOrigin())
                .env(Environment.builder()
                        .account(config.account)
                        .region(config.region)
                        .build())
                .build());

        new FrontEndStack(app, config.namePrefixedId("FrontendStack"), StackProps.builder()
                .description("The frontend for the website " + config.webOrigin())
                .env(Environment.builder()
                        .account(config.account)
                        .region(config.region)
                        .build())
                .build());

        // standard tags
        Tags.of(app).add("owner", config.account);
        Tags.of(app).add("name", config.name);

        app.synth();
    }
}
