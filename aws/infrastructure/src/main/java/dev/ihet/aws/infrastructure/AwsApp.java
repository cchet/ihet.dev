package dev.ihet.aws.infrastructure;

import dev.ihet.aws.infrastructure.helper.Configuration;
import dev.ihet.aws.infrastructure.stacks.BackendStack;
import dev.ihet.aws.infrastructure.stacks.FrontEndStack;
import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.Tags;

import static dev.ihet.aws.infrastructure.helper.Util.resourceId;

public class AwsApp {
    private static final Configuration config = Configuration.load();

    public static void main(final String[] args) {
        var app = new App();

        // standard tags
        Tags.of(app).add("owner", config.account);
        Tags.of(app).add("name", config.name);

        var backendStack = new BackendStack(app, resourceId("BackendStack"), StackProps.builder()
                .env(Environment.builder()
                        .account(config.account)
                        .region(config.region)
                        .build())
                .build());

         new FrontEndStack(app, resourceId("FrontendStack"), StackProps.builder()
                .env(Environment.builder()
                        .account(config.account)
                        .region(config.region)
                        .build())
                .build(),
                 backendStack);

        app.synth();
    }
}
