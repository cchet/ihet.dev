package dev.ihet.aws.infrastructure.stacks;

import dev.ihet.aws.infrastructure.constructs.FunctionConstruct;
import dev.ihet.aws.infrastructure.constructs.GatewayConstruct;
import software.amazon.awscdk.CfnParameter;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.constructs.Construct;

import static dev.ihet.aws.infrastructure.helper.Util.resourceId;

public class BackendStack extends Stack {


    public BackendStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        CfnParameter recreateDeployment = CfnParameter.Builder.create(this, "recreateDeployment").defaultValue("false").build();

        var function = new FunctionConstruct(this, resourceId("BackendFunction")).getFunction();
        new GatewayConstruct(this, resourceId("BackendGateway"), function, recreateDeployment);
    }
}
