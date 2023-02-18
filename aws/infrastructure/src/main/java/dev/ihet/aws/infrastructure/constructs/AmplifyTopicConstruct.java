package dev.ihet.aws.infrastructure.constructs;

import dev.ihet.aws.infrastructure.helper.Configuration;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.services.amplify.CfnApp;
import software.amazon.awscdk.services.sns.Topic;
import software.amazon.awscdk.services.sns.subscriptions.EmailSubscription;
import software.constructs.Construct;

import static dev.ihet.aws.infrastructure.helper.Util.resourceId;

public class AmplifyTopicConstruct extends Construct {

    private static final Configuration config = Configuration.load();

    public AmplifyTopicConstruct(@NotNull Construct scope, @NotNull String id, CfnApp app) {
        super(scope, id);

        createTopicForBranch(app, config.prodBranch(), "AmplifyProdTopic");
        createTopicForBranch(app, config.testBranch(), "AmplifyTestTopic");
    }

    private void createTopicForBranch(CfnApp app, String branchName, String idSuffix) {
        Topic.Builder.create(this, resourceId(idSuffix))
                .topicName(buildSnsTopicForAppIdAndBranch(app.getAttrAppId(), branchName))
                .build()
                .addSubscription(new EmailSubscription(config.email));
    }

    private String buildSnsTopicForAppIdAndBranch(String appId, String branchName) {
        return "amplify-" + appId + "_" + branchName;
    }
}
