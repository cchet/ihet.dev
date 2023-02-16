package dev.ihet.aws.infrastructure.constructs;

import dev.ihet.aws.infrastructure.helper.Configuration;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.services.apigateway.*;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.HttpMethod;
import software.constructs.Construct;

import java.util.List;
import java.util.UUID;

import static dev.ihet.aws.infrastructure.helper.Util.resourceId;

public class GatewayConstruct extends Construct {

    private static final Configuration config = Configuration.load();

    private final LambdaRestApi restApi;


    public GatewayConstruct(@NotNull Construct scope, @NotNull String id, Function function) {
        super(scope, id);

        // REST -API
        restApi = LambdaRestApi.Builder.create(this, "RestApiContactMe")
                .restApiName("restApiContactMe")
                .description("The rest-api for sending emails")
                .cloudWatchRole(true)
                .deploy(true)
                .proxy(false)
                .handler(function)
                .apiKeySourceType(ApiKeySourceType.HEADER)
                .endpointConfiguration(
                        EndpointConfiguration.builder()
                                .types(List.of(EndpointType.REGIONAL))
                                .build())
                // Meaning, all methods need the api.key
                .defaultMethodOptions(
                        MethodOptions.builder()
                                .apiKeyRequired(true)
                                .build())
                .defaultCorsPreflightOptions(CorsOptions.builder()
                        .allowOrigins(List.of(
                                config.webOrigin()))
                        .allowMethods(List.of(
                                HttpMethod.POST.name()))
                        .statusCode(200)
                        .build())
                .build();
        var apiKey = restApi.addApiKey(resourceId("RestApiContactMeApiKey"), ApiKeyOptions.builder()
                .description("The api key for the rest-api access")
                .apiKeyName("restApiContactMeApiKey")
                .value(config.apiKey)
                .build());
        restApi.getRoot().addResource("contactMe")
                .addMethod(HttpMethod.POST.name());

        // REST-API deployment
        var restApiDeployment = Deployment.Builder.create(this, resourceId("RestApiContactMeDeployment"))
                .description("The rest-api deployment for sending emails")
                .retainDeployments(false)
                .api(restApi)
                .build();
        restApiDeployment.addToLogicalId(UUID.randomUUID().toString());

        // REST-API usage plan
        var usagePlan = restApi.addUsagePlan(resourceId("RestApiContactMeUsagePlan"), UsagePlanProps.builder()
                .name("RestApiContactMeUsagePlan")
                .apiStages(List.of(UsagePlanPerApiStage.builder()
                        .api(restApi)
                        .stage(restApi.getDeploymentStage())
                        .build()))
                .throttle(ThrottleSettings.builder()
                        .rateLimit(2)
                        .burstLimit(10)
                        .build())
                .build());
        usagePlan.addApiKey(apiKey);
    }

    public LambdaRestApi getRestApi() {
        return restApi;
    }
}
