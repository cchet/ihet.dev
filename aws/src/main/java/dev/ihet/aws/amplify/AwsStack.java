package dev.ihet.aws.amplify;

import software.amazon.awscdk.CfnTag;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.amplify.CfnApp;
import software.amazon.awscdk.services.amplify.CfnApp.AutoBranchCreationConfigProperty;
import software.amazon.awscdk.services.amplify.CfnBranch;
import software.amazon.awscdk.services.amplify.CfnDomain;
import software.amazon.awscdk.services.sns.Topic;
import software.amazon.awscdk.services.sns.subscriptions.EmailSubscription;
import software.constructs.Construct;

import java.util.List;
import java.util.Map;

public class AwsStack extends Stack {

    private final Configuration config;

    public AwsStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public AwsStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);
        this.config = Configuration.load();
        // standard tags
        var tags = List.of(
                CfnTag.builder().key("owner").value(getAccount()).build(),
                CfnTag.builder().key("name").value(config.name).build());

        // Create the amplify application
        var app = CfnApp.Builder.create(this, buildResourceId("app"))
                .name(config.name)
                .accessToken(config.accessToken)
                .platform("WEB")
                .repository(config.repository)
                .autoBranchCreationConfig(AutoBranchCreationConfigProperty.builder()
                        .autoBranchCreationPatterns(List.of("deploy-*"))
                        .enableAutoBranchCreation(true)
                        .enablePerformanceMode(true)
                        .enableAutoBuild(true)
                        .build())
                .enableBranchAutoDeletion(true)
                .tags(tags)
                .build();

        // Connect the branch which gets deployed automatically on a change
        var branch = CfnBranch.Builder.create(this, buildResourceId("branch-deploy"))
                .appId(app.getAttrAppId())
                .branchName(config.branchName)
                .enableAutoBuild(true)
                .enablePerformanceMode(true)
                .framework("javascript")
                .tags(tags)
                .build();
        branch.addDependency(app);

        // Create the domain settings
        CfnDomain.Builder.create(this, buildResourceId("domain"))
                .appId(app.getAttrAppId())
                .domainName(config.domain)
                .enableAutoSubDomain(true)
                .subDomainSettings(List.of(
                        Map.of(
                                "branchName", config.branchName,
                                "prefix", "www")))
                .build().addDependency(branch);

        // Create SNS Topic
        Topic.Builder.create(this, buildResourceId("topic"))
                .topicName(buildSnsTopicForAppIdAndBranch(app.getAttrAppId(), config.branchName))
                .build()
                .addSubscription(new EmailSubscription("herzog.thomas81@gmail.com"));
    }

    private String buildResourceId(String suffix) {
        return config.name + "-" + suffix;
    }

    /**
     * The syntax of the topic name if defined by Amplify
     */
    private String buildSnsTopicForAppIdAndBranch(String appId, String branchName) {
        return "amplify-" + appId + "_" + branchName;
    }
}
