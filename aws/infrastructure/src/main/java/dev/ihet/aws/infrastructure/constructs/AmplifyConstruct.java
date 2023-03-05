package dev.ihet.aws.infrastructure.constructs;

import dev.ihet.aws.infrastructure.Configuration;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.CfnParameter;
import software.amazon.awscdk.services.amplify.CfnApp;
import software.amazon.awscdk.services.amplify.CfnBranch;
import software.amazon.awscdk.services.amplify.CfnDomain;
import software.constructs.Construct;

import java.util.List;
import java.util.Map;

public class AmplifyConstruct extends Construct {

    private static final Configuration config = Configuration.CONFIG;

    private final CfnApp app;

    public AmplifyConstruct(@NotNull Construct scope, @NotNull String id, CfnParameter prodUrl, CfnParameter testUrl) {
        super(scope, id);

        app = CfnApp.Builder.create(this, config.namePrefixedId("AmplifyApp"))
                .name(config.name)
                .accessToken(config.accessToken)
                .platform("WEB")
                .repository(config.repository)
                .enableBranchAutoDeletion(true)
                .environmentVariables(List.of(
                        CfnApp.EnvironmentVariableProperty.builder().name("GOOGLE_ANALYTICS_ID").value(config.gId).build()
                ))
                .build();

        // Connect the branch which gets deployed automatically on a change
        var prodBranch = CfnBranch.Builder.create(this, config.namePrefixedId("AmplifyProdBranch"))
                .appId(app.getAttrAppId())
                .branchName(config.prodBranch())
                .enableAutoBuild(true)
                .enablePerformanceMode(true)
                .stage("PRODUCTION")
                .environmentVariables(List.of(
                        CfnBranch.EnvironmentVariableProperty.builder().name("API_KEY").value(config.apiKey).build(),
                        CfnBranch.EnvironmentVariableProperty.builder().name("API_ROOT_URL").value(prodUrl.getValueAsString()).build()
                ))
                .build();
        prodBranch.addDependency(app);

        var testBranch = CfnBranch.Builder.create(this, config.namePrefixedId("AmplifyTestBranch"))
                .appId(app.getAttrAppId())
                .branchName(config.testBranch())
                .enableAutoBuild(true)
                .enablePerformanceMode(true)
                .stage("BETA")
                .environmentVariables(List.of(
                        CfnBranch.EnvironmentVariableProperty.builder().name("API_KEY").value(config.testApiKey).build(),
                        CfnBranch.EnvironmentVariableProperty.builder().name("API_ROOT_URL").value(testUrl.getValueAsString()).build()
                ))
                .build();
        testBranch.addDependency(prodBranch);

        // Create the domain settings
        CfnDomain.Builder.create(this, config.namePrefixedId("AmplifyDomain"))
                .appId(app.getAttrAppId())
                .domainName(config.domain)
                .enableAutoSubDomain(true)
                .subDomainSettings(List.of(
                        Map.of(
                                "branchName", prodBranch.getBranchName(),
                                "prefix", "www"),
                        Map.of(
                                "branchName", testBranch.getBranchName(),
                                "prefix", "test")))
                .build()
                .addDependency(testBranch);
    }

    public CfnApp getApp() {
        return app;
    }
}
