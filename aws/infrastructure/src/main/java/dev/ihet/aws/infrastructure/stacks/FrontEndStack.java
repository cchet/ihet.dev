package dev.ihet.aws.infrastructure.stacks;

import dev.ihet.aws.infrastructure.constructs.AmplifyConstruct;
import dev.ihet.aws.infrastructure.constructs.AmplifyTopicConstruct;
import software.amazon.awscdk.CfnParameter;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.amplify.CfnApp;
import software.constructs.Construct;

import static dev.ihet.aws.infrastructure.helper.Util.resourceId;

public class FrontEndStack extends Stack {

    private final CfnApp amplify;

    public FrontEndStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        CfnParameter prodUrl = CfnParameter.Builder.create(this, "prodUrl").build();
        CfnParameter testUrl = CfnParameter.Builder.create(this, "testUrl").build();

        amplify = new AmplifyConstruct(this, resourceId("Amplify"), prodUrl, testUrl).getApp();
        new AmplifyTopicConstruct(this, resourceId("Topic"), amplify);
    }

    public CfnApp getAmplify() {
        return amplify;
    }
}
