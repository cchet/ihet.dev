package dev.ihet.aws.infrastructure.constructs;

import dev.ihet.aws.infrastructure.helper.Configuration;
import dev.ihet.aws.infrastructure.helper.Util;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.services.apigateway.*;
import software.amazon.awscdk.services.lambda.Function;
import software.constructs.Construct;

import java.util.List;
import java.util.UUID;

public class GatewayConstruct extends Construct {

    private static final Configuration config = Configuration.load();

    public GatewayConstruct(@NotNull Construct scope, @NotNull String id, Function function) {
        super(scope, id);

        // REST -API
        var restApi = LambdaRestApi.Builder.create(this, "RestApiContactMe")
                .restApiName("restApiContactMe")
                .description("The rest-api for sending emails")
                .cloudWatchRole(true)
                .endpointConfiguration(
                        EndpointConfiguration.builder()
                                .types(List.of(EndpointType.REGIONAL))
                                .build())
                .apiKeySourceType(ApiKeySourceType.HEADER)
                .defaultMethodOptions(
                        MethodOptions.builder()
                                .apiKeyRequired(true)
                                .build())
                .deploy(true)
                .handler(function)
                .build();
        restApi.addApiKey(Util.resourceId("RestApiContactMeApiKey"), ApiKeyOptions.builder()
                .description("The api key for the rest-api access")
                .apiKeyName("restApiContactMeApiKey")
                .value(config.apiKey)
                .build());

        // REST-API deployment
        var restApiDeployment = Deployment.Builder.create(this, Util.resourceId("RestApiContactMeDeployment"))
                .description("The rest-api deployment for sending emails")
                .retainDeployments(false)
                .api(restApi)
                .build();
        restApiDeployment.addToLogicalId(UUID.randomUUID().toString());

        // REST-API Deployment stage
        var stage = Stage.Builder.create(this, Util.resourceId("RestApiContactMeStage"))
                .stageName("prod")
                .deployment(restApiDeployment)
                .cachingEnabled(false)
                .tracingEnabled(true)
                .metricsEnabled(true)
                .loggingLevel(MethodLoggingLevel.ERROR)
                .build();
        restApi.addUsagePlan(Util.resourceId("RestApiContactMeUsagePlan"), UsagePlanProps.builder()
                .name("RestApiContactMeUsagePlan")
                .apiStages(List.of(UsagePlanPerApiStage.builder()
                        .api(restApi)
                        .stage(stage)
                        .build()))
                .throttle(ThrottleSettings.builder()
                        .rateLimit(2)
                        .burstLimit(10)
                        .build())
                .build());
    }
}
