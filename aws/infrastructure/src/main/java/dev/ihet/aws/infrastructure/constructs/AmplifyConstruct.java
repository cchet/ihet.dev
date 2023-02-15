package dev.ihet.aws.infrastructure.constructs;

import dev.ihet.aws.infrastructure.helper.Configuration;
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

    public AmplifyConstruct(@NotNull Construct scope, @NotNull String id) {
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
        var branch = CfnBranch.Builder.create(this, resourceId("BranchDeploy"))
                .appId(app.getAttrAppId())
                .branchName(config.branchName)
                .enableAutoBuild(true)
                .enablePerformanceMode(true)
                .framework("javascript")
                .build();
        branch.addDependency(app);

        // Create the domain settings
        CfnDomain.Builder.create(this, resourceId("Domain"))
                .appId(app.getAttrAppId())
                .domainName(config.domain)
                .enableAutoSubDomain(true)
                .subDomainSettings(List.of(
                        Map.of(
                                "branchName", config.branchName,
                                "prefix", "www")))
                .build()
                .addDependency(branch);
    }

    public CfnApp getApp() {
        return app;
    }
}
