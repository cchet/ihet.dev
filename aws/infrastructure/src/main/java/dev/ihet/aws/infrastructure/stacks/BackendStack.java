package dev.ihet.aws.infrastructure.stacks;

import dev.ihet.aws.infrastructure.constructs.FunctionConstruct;
import dev.ihet.aws.infrastructure.constructs.GatewayConstruct;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.constructs.Construct;

import static dev.ihet.aws.infrastructure.helper.Util.resourceId;

public class BackendStack extends Stack {

    public BackendStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        // TODO: I think we need to validate the certificate via DNS before cdk deploy completes
        // var certificate = new CertificateConstruct(this, resourceId("CertificateConstruct")).getCertificate();
        var function = new FunctionConstruct(this, resourceId("FunctionConstruct")).getFunction();
        new GatewayConstruct(this, resourceId("GatewayConstruct"), function, null);
    }
}
