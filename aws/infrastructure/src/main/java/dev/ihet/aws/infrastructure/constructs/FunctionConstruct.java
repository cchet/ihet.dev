package dev.ihet.aws.infrastructure.constructs;

import dev.ihet.aws.infrastructure.helper.Configuration;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.BundlingOptions;
import software.amazon.awscdk.BundlingOutput;
import software.amazon.awscdk.DockerVolume;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.services.iam.PolicyStatement;
import software.amazon.awscdk.services.lambda.Architecture;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.amazon.awscdk.services.s3.assets.AssetOptions;
import software.constructs.Construct;

import java.util.List;
import java.util.Map;

import static dev.ihet.aws.infrastructure.helper.Util.resourceId;

public class FunctionConstruct extends Construct {

    private static final Configuration config = Configuration.load();

    private final Function function;

    public FunctionConstruct(@NotNull Construct scope, @NotNull String id) {
        super(scope, id);

        // Functions
        function = Function.Builder.create(this, "BackendFunction")
                .functionName(resourceId("ContactMeFunction"))
                .description("The lambda for sending contact me emails")
                .runtime(Runtime.JAVA_11)
                .architecture(Architecture.X86_64)
                .memorySize(1024) // 0.5 vpu
                .timeout(Duration.seconds(10))
                .logRetention(RetentionDays.ONE_WEEK)
                .code(Code.fromAsset("../functions", AssetOptions.builder()
                        .bundling(BundlingOptions.builder()
                                .image(Runtime.JAVA_11.getBundlingImage())
                                .command(List.of(
                                        "/bin/sh",
                                        "-c",
                                        "mvn clean install && cp /asset-input/target/function.zip /asset-output/"
                                ))
                                .user("root")
                                .volumes(List.of(
                                        DockerVolume.builder()
                                                .hostPath(System.getProperty("user.home") + "/.m2/")
                                                .containerPath("/root/.m2/")
                                                .build()
                                ))
                                .outputType(BundlingOutput.ARCHIVED)
                                .build())
                        .build()))
                .handler("io.quarkus.amazon.lambda.runtime.QuarkusStreamHandler::handleRequest")
                .initialPolicy(List.of(
                        PolicyStatement.Builder.create()
                                .actions(List.of(
                                        "ses:SendEmail",
                                        "ses:SendRawEmail",
                                        "ses:SendTemplatedEmail"
                                ))
                                .resources(List.of("*"))
                                .build()
                ))
                .environment(Map.of("AWS_EMAIL_SENDER", config.emailSender,
                        "AWS_EMAIL_RECIPIENT", config.emailRecipient))
                .build();
    }

    public Function getFunction() {
        return function;
    }
}
