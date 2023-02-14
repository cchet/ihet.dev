package dev.ihet.aws.infrastructure.stacks;

import dev.ihet.aws.infrastructure.constructs.AmplifyConstruct;
import dev.ihet.aws.infrastructure.constructs.FunctionConstruct;
import dev.ihet.aws.infrastructure.constructs.GatewayConstruct;
import dev.ihet.aws.infrastructure.constructs.TopicConstruct;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.constructs.Construct;

import static dev.ihet.aws.infrastructure.helper.Util.resourceId;

public class AmplifyStack extends Stack {

    public AmplifyStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        var app = new AmplifyConstruct(this, resourceId("AmplifyConstruct")).getApp();
        var function = new FunctionConstruct(this, resourceId("FunctionConstruct")).getFunction();
        new GatewayConstruct(this, resourceId("GatewayConstruct"), function);
        new TopicConstruct(this, resourceId("TopicConstruct"), app);
    }
}
