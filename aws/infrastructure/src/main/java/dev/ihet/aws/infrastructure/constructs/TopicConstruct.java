package dev.ihet.aws.infrastructure.constructs;

import dev.ihet.aws.infrastructure.helper.Configuration;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.services.amplify.CfnApp;
import software.amazon.awscdk.services.sns.Topic;
import software.amazon.awscdk.services.sns.subscriptions.EmailSubscription;
import software.constructs.Construct;

import static dev.ihet.aws.infrastructure.helper.Util.resourceId;

public class TopicConstruct extends Construct {

    private static final Configuration config = Configuration.load();

    public TopicConstruct(@NotNull Construct scope, @NotNull String id, CfnApp app) {
        super(scope, id);

        Topic.Builder.create(this, resourceId("AmplifyDeployTopic"))
                .topicName(buildSnsTopicForAppIdAndBranch(app.getAttrAppId(), config.branchName))
                .build()
                .addSubscription(new EmailSubscription("herzog.thomas81@gmail.com"));
    }

    private String buildSnsTopicForAppIdAndBranch(String appId, String branchName) {
        return "amplify-" + appId + "_" + branchName;
    }
}
