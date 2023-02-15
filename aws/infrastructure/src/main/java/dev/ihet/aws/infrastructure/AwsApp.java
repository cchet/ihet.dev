package dev.ihet.aws.infrastructure;

import dev.ihet.aws.infrastructure.helper.Configuration;
import dev.ihet.aws.infrastructure.stacks.BackendStack;
import dev.ihet.aws.infrastructure.stacks.FrontEndStack;
import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.Tags;
import software.amazon.awscdk.services.amplify.CfnApp;

import java.util.List;

import static dev.ihet.aws.infrastructure.helper.Util.resourceId;

public class AwsApp {
    private static final Configuration config = Configuration.load();

    public static void main(final String[] args) {
        var app = new App();

        // standard tags
        Tags.of(app).add("owner", config.account);
        Tags.of(app).add("name", config.name);

        new BackendStack(app, resourceId("Backend"), StackProps.builder()
                .env(Environment.builder()
                        .account(config.account)
                        .region(config.region)
                        .build())
                .build());

         new FrontEndStack(app, resourceId("Frontend"), StackProps.builder()
                .env(Environment.builder()
                        .account(config.account)
                        .region(config.region)
                        .build())
                .build());

        app.synth();
    }
}
