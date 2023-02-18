package dev.ihet.aws.infrastructure.constructs;

import dev.ihet.aws.infrastructure.helper.Configuration;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.CfnParameter;
import software.amazon.awscdk.services.apigateway.*;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.HttpMethod;
import software.constructs.Construct;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static dev.ihet.aws.infrastructure.helper.Util.resourceId;

public class GatewayConstruct extends Construct {

    private static final Configuration config = Configuration.load();

    private final CfnParameter recreateDeployment;

    public GatewayConstruct(@NotNull Construct scope, @NotNull String id, Function function, CfnParameter recreateDeployment) {
        super(scope, id);
        this.recreateDeployment = recreateDeployment;

        var prodRestApi = createLambdaRestApi(function, config.webOrigin(), "Prod");
        var testRestApi = createLambdaRestApi(function, config.testOrigin(), "Test");

        var prodStage = prodRestApi.getDeploymentStage();
        var testStage = testRestApi.getDeploymentStage();

        createUsagePlan(prodRestApi, prodStage, "Prod", config.apiKey);
        createUsagePlan(testRestApi, testStage, "Test", config.testApiKey);

        function.grantInvoke(ServicePrincipal.Builder.create("apigateway.amazonaws.com")
                .region(config.region)
                .build()
        );
    }

    private RestApi createLambdaRestApi(Function function, String origin, String idSuffix) {
        var restApi = RestApi.Builder.create(this, resourceId(idSuffix + "RestApi"))
                .deploy(true)
                .restApiName(idSuffix + "RestApi")
                .description("The rest-api for sending emails")
                .defaultIntegration(MockIntegration.Builder.create()
                        .passthroughBehavior(PassthroughBehavior.NEVER)
                        .integrationResponses(List.of(IntegrationResponse.builder()
                                .statusCode("200")
                                .responseTemplates(Map.of("application/json", "{ \"message\": \"Nothing happens here\" }"))
                                .build()))
                        .build())
                .apiKeySourceType(ApiKeySourceType.HEADER)
                .endpointConfiguration(
                        EndpointConfiguration.builder()
                                .types(List.of(EndpointType.REGIONAL))
                                .build())
                .defaultCorsPreflightOptions(CorsOptions.builder()
                        .allowOrigins(List.of(origin))
                        .allowMethods(List.of(HttpMethod.POST.name()))
                        .allowHeaders(List.of(
                                "Content-Type",
                                "Accept",
                                "X-API-KEY"
                        ))
                        .statusCode(200)
                        .build()).deployOptions(StageOptions.builder()
                        .build())
                .defaultMethodOptions(MethodOptions.builder()
                        .apiKeyRequired(true)
                        .build())
                .build();
        // Create a proxy so we can disable ANY
        restApi.getRoot().addProxy(ProxyResourceOptions.builder()
                .anyMethod(false)
                .build());
        // This is the only resource the lambda supports right now
        var resource = restApi.getRoot().addResource("contactMe");
        resource.addMethod("POST", new LambdaIntegration(function));

        // IMPORTANT: Workaround to disable the api-key for OPTIONS method
        restApi.getMethods().stream()
                .filter(m -> m.getHttpMethod().equalsIgnoreCase("OPTIONS"))
                .forEach(m -> ((CfnMethod) m.getNode().getDefaultChild()).setApiKeyRequired(false));

        // REST-API deployment
        var deployment = Deployment.Builder.create(this, resourceId(idSuffix + "Deployment"))
                .retainDeployments(false)
                .api(restApi)
                .build();
        if(Boolean.valueOf(recreateDeployment.getValueAsString())) {
            deployment.addToLogicalId(UUID.randomUUID().toString());
        }

        return restApi;
    }

    private void createUsagePlan(RestApi restApi, Stage stage, String idSuffix, String apiKeyValue) {
        var apiKey = restApi.addApiKey(resourceId(idSuffix + "ApiKey"), ApiKeyOptions.builder()
                .apiKeyName(idSuffix + "ApiKey")
                .value(apiKeyValue)
                .build());
        var stageUsagePlan = restApi.addUsagePlan(resourceId(idSuffix + "UsagePlan"), UsagePlanProps.builder()
                .name(idSuffix + "Stage")
                .apiStages(List.of(
                        UsagePlanPerApiStage.builder()
                                .api(restApi)
                                .stage(stage)
                                .build()
                ))
                .throttle(ThrottleSettings.builder()
                        .rateLimit(1)
                        .burstLimit(1)
                        .build())
                .build());
        stageUsagePlan.addApiKey(apiKey);
    }
}
