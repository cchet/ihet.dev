package dev.ihet.aws.infrastructure.stacks;

import dev.ihet.aws.infrastructure.Configuration;
import dev.ihet.aws.infrastructure.constructs.FunctionConstruct;
import dev.ihet.aws.infrastructure.constructs.GatewayConstruct;
import dev.ihet.aws.infrastructure.constructs.QueueConstruct;
import software.amazon.awscdk.CfnParameter;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.constructs.Construct;

public class BackendStack extends Stack {


    public BackendStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);
        var config = Configuration.CONFIG;

        CfnParameter recreateDeployment = CfnParameter.Builder.create(this, "recreateDeployment").defaultValue("false").build();

        var prodQueue = new QueueConstruct(this, config.namePrefixedId("ProdBackendQueue"), "TEST").getQueue();
        var testQueue = new QueueConstruct(this, config.namePrefixedId("TestBackendQueue"), "PROD").getQueue();
        new FunctionConstruct(this, config.namePrefixedId("ProdBackendFunction"), prodQueue, "TEST");
        new FunctionConstruct(this, config.namePrefixedId("TestBackendFunction"), testQueue, "PROD");
        new GatewayConstruct(this, config.namePrefixedId("ProdBackendGateway"), prodQueue, config.webOrigin(), config.apiKey, recreateDeployment);
        new GatewayConstruct(this, config.namePrefixedId("TestBackendGateway"), testQueue, config.testOrigin(), config.testApiKey, recreateDeployment);
    }
}
