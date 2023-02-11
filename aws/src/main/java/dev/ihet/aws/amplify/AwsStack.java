package dev.ihet.aws.amplify;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import software.amazon.awscdk.CfnTag;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.amplify.CfnApp;
import software.amazon.awscdk.services.amplify.CfnApp.AutoBranchCreationConfigProperty;
import software.amazon.awscdk.services.amplify.CfnBranch;
import software.amazon.awscdk.services.amplify.CfnDomain;
import software.constructs.Construct;

public class AwsStack extends Stack {

        private final Configuration config;

        public AwsStack(final Construct scope, final String id) {
                this(scope, id, null);
        }

        public AwsStack(final Construct scope, final String id, final StackProps props) {
                super(scope, id, props);
                this.config = Configuration.load();
                // standard tags
                var tags = List.of(
                        CfnTag.builder().key("timestamp")
                                        .value(DateTimeFormatter.ISO_DATE_TIME
                                                        .format(LocalDateTime.now()))
                                        .build(),
                        CfnTag.builder().key("owner").value(getAccount()).build(),
                        CfnTag.builder().key("name").value(config.name).build());

                // Create the amplify application
                var app = CfnApp.Builder.create(this, buildResourceId("app"))
                                .name(config.name)
                                .accessToken(config.accessToken)
                                .platform("WEB")
                                .repository(config.repository)
                                .autoBranchCreationConfig(AutoBranchCreationConfigProperty.builder()
                                                .autoBranchCreationPatterns(List.of("deploy"))
                                                .enableAutoBranchCreation(true)
                                                .enablePerformanceMode(true)
                                                .enableAutoBuild(true)
                                                .build())
                                .enableBranchAutoDeletion(true)
                                .tags(tags)
                                .build();

                // Connect the branch which gets deployed automatically on a change
                var branch = CfnBranch.Builder.create(this, buildResourceId("branch-deploy"))
                                .appId(app.getAttrAppId())
                                .branchName("deploy")
                                .enableAutoBuild(true)
                                .enablePerformanceMode(true)
                                .framework("javascript")
                                .tags(tags)
                                .build();
                branch.addDependency(app);

                // Create the domain settings
                var domain = CfnDomain.Builder.create(this, buildResourceId("domain"))
                                .appId(app.getAttrAppId())
                                .domainName(config.domain)
                                .enableAutoSubDomain(true)
                                .subDomainSettings(List.of(
                                                Map.of(
                                                                "branchName", "deploy",
                                                                "prefix", "www")))
                                .build();
                domain.addDependency(branch);
        }

        private String buildResourceId(String suffix) {
                return config.name + "-" + suffix;
        }
}
