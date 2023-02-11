package dev.ihet.aws.amplify;

import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;

public class AwsApp {
        public static void main(final String[] args) {
                App app = new App();
                var config = Configuration.load();

                new AwsStack(app, config.name + "-aws-stack", StackProps.builder()
                                .env(Environment.builder()
                                                .account(config.account)
                                                .region(config.region)
                                                .build())
                                .build());
                app.synth();
        }
}
