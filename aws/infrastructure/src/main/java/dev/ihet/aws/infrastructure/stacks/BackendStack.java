package dev.ihet.aws.infrastructure.stacks;

import dev.ihet.aws.infrastructure.constructs.FunctionConstruct;
import dev.ihet.aws.infrastructure.constructs.GatewayConstruct;
import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.constructs.Construct;

import static dev.ihet.aws.infrastructure.helper.Util.resourceId;

public class BackendStack extends Stack {

    private final GatewayConstruct gatewayConstruct;

    public BackendStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        var function = new FunctionConstruct(this, resourceId("BackendFunction")).getFunction();
        gatewayConstruct = new GatewayConstruct(this, resourceId("BackendGateway"), function);

        // Output GatewayApi url
        CfnOutput.Builder.create(this, resourceId("ProdStageUrl"))
                .value(gatewayConstruct.getProdStage().urlForPath())
                .build();
        CfnOutput.Builder.create(this, resourceId("TestStageUrl"))
                .value(gatewayConstruct.getTestStage().urlForPath())
                .build();
    }

    public GatewayConstruct getGatewayConstruct() {
        return gatewayConstruct;
    }
}
