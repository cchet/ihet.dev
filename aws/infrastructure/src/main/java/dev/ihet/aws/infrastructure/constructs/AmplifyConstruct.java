package dev.ihet.aws.infrastructure.constructs;

import dev.ihet.aws.infrastructure.helper.Configuration;
import dev.ihet.aws.infrastructure.stacks.BackendStack;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.services.amplify.CfnApp;
import software.amazon.awscdk.services.amplify.CfnBranch;
import software.amazon.awscdk.services.amplify.CfnDomain;
import software.constructs.Construct;

import java.util.List;
import java.util.Map;

import static dev.ihet.aws.infrastructure.helper.Util.resourceId;

public class AmplifyConstruct extends Construct {

    private static final Configuration config = Configuration.load();

    private final CfnApp app;

    public AmplifyConstruct(@NotNull Construct scope, @NotNull String id, BackendStack backendStack) {
        super(scope, id);

        app = CfnApp.Builder.create(this, resourceId("App"))
                .name(config.name)
                .accessToken(config.accessToken)
                .platform("WEB")
                .repository(config.repository)
                .autoBranchCreationConfig(CfnApp.AutoBranchCreationConfigProperty.builder()
                        .autoBranchCreationPatterns(List.of("deploy-*"))
                        .enableAutoBranchCreation(true)
                        .enablePerformanceMode(true)
                        .enableAutoBuild(true)
                        .build())
                .enableBranchAutoDeletion(true)
                .environmentVariables(List.of(
                        CfnApp.EnvironmentVariableProperty.builder().name("API_KEY").value(config.apiKey).build(),
                        CfnApp.EnvironmentVariableProperty.builder().name("GOOGLE_ANALYTICS_ID").value(config.gId).build()
                ))
                .build();

        // Connect the branch which gets deployed automatically on a change
        var branch = CfnBranch.Builder.create(this, resourceId("ProdBranch"))
                .appId(app.getAttrAppId())
                .branchName(config.branchName)
                .enableAutoBuild(true)
                .enablePerformanceMode(true)
                .stage("PRODUCTION")
                .environmentVariables(List.of(
                        CfnBranch.EnvironmentVariableProperty.builder().name("API_ROOT_URL").value(backendStack.getGatewayConstruct().getProdStage().urlForPath()).build(),
                        CfnBranch.EnvironmentVariableProperty.builder().name("STAGE").value(backendStack.getGatewayConstruct().getProdStage().getStageName()).build()
                ))
                .build();
        branch.addDependency(app);

        CfnBranch.Builder.create(this, resourceId("TestBranch"))
                .appId(app.getAttrAppId())
                .branchName(config.branchName + "-test")
                .enableAutoBuild(true)
                .stage("BETA")
                .environmentVariables(List.of(
                        CfnBranch.EnvironmentVariableProperty.builder().name("API_ROOT_URL").value(backendStack.getGatewayConstruct().getProdStage().urlForPath()).build(),
                        CfnBranch.EnvironmentVariableProperty.builder().name("STAGE").value(backendStack.getGatewayConstruct().getTestStage().getStageName()).build()
                ))
                .build()
                .addDependency(app);

        // Create the domain settings
        CfnDomain.Builder.create(this, resourceId("Domain"))
                .appId(app.getAttrAppId())
                .domainName(config.domain)
                .enableAutoSubDomain(true)
                .subDomainSettings(List.of(
                        Map.of(
                                "branchName", branch.getBranchName(),
                                "prefix", "www")))
                .build()
                .addDependency(branch);
    }

    public CfnApp getApp() {
        return app;
    }
}
