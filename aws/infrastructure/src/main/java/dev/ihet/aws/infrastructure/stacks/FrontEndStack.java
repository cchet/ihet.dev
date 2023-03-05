package dev.ihet.aws.infrastructure.stacks;

import dev.ihet.aws.infrastructure.Configuration;
import dev.ihet.aws.infrastructure.constructs.AmplifyConstruct;
import dev.ihet.aws.infrastructure.constructs.AmplifyTopicConstruct;
import software.amazon.awscdk.CfnParameter;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.amplify.CfnApp;
import software.constructs.Construct;

public class FrontEndStack extends Stack {

    private final CfnApp amplify;

    public FrontEndStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);
        var config = Configuration.CONFIG;

        CfnParameter prodUrl = CfnParameter.Builder.create(this, "prodUrl").build();
        CfnParameter testUrl = CfnParameter.Builder.create(this, "testUrl").build();

        amplify = new AmplifyConstruct(this, config.namePrefixedId("Amplify"), prodUrl, testUrl).getApp();
        new AmplifyTopicConstruct(this, config.namePrefixedId("Topic"), amplify);
    }

    public CfnApp getAmplify() {
        return amplify;
    }
}
