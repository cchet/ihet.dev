package dev.ihet.aws.amplify;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import software.amazon.awscdk.CfnTag;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.amplify.CfnApp;
import software.amazon.awscdk.services.amplify.CfnApp.AutoBranchCreationConfigProperty;
import software.constructs.Construct;

public class AwsStack extends Stack {

    static final String NAME = "ihet.dev";

    public AwsStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public AwsStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        CfnApp.Builder.create(this, buildName("app"))
                .name(buildName("-app"))
                .accessToken("ACCESS_TOKEN") // From arguments!!!!
                .description("The www.ihet.dev application")
                .platform("WEB")
                .repository("https://github.com/cchet/ihet.dev")
                .autoBranchCreationConfig(AutoBranchCreationConfigProperty.builder()
                        .autoBranchCreationPatterns(List.of("deploy"))
                        .enableAutoBranchCreation(true)
                        .enablePerformanceMode(true)
                        .enableAutoBuild(true)
                        .build())
                .tags(List.of(
                        CfnTag.builder().key("timestamp")
                                .value(DateTimeFormatter.ISO_DATE_TIME.format(LocalDateTime.now())).build(),
                        CfnTag.builder().key("owner").value(getAccount()).build(),
                        CfnTag.builder().key("name").value(NAME).build()))
                .build();
    }

    private String buildName(String suffix) {
        return NAME + "-" + suffix;
    }
}
