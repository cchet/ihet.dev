package dev.ihet.aws.infrastructure.constructs;

import dev.ihet.aws.infrastructure.helper.Configuration;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.services.apigateway.*;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.HttpMethod;
import software.constructs.Construct;

import java.net.http.HttpHeaders;
import java.util.List;
import java.util.UUID;

import static dev.ihet.aws.infrastructure.helper.Util.resourceId;

public class GatewayConstruct extends Construct {

    private static final Configuration config = Configuration.load();

    private final Stage prodStage;

    private final Stage testStage;


    public GatewayConstruct(@NotNull Construct scope, @NotNull String id, Function function) {
        super(scope, id);

        // REST -API
        var restApi = LambdaRestApi.Builder.create(this, resourceId("BackendRestApi"))
                .restApiName("backend")
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
                .defaultCorsPreflightOptions(CorsOptions.builder()
                        .allowOrigins(List.of(
                                config.webOrigin(),
                                config.testBranch()))
                        .allowMethods(List.of(HttpMethod.POST.name()))
                        .allowHeaders(List.of(
                                "Content-Type",
                                "Accept",
                                "X-API-KEY"
                        ))
                        .statusCode(200)
                        .build())
                .build();
        restApi.getRoot().addResource("contactMe", ResourceOptions.builder()
                        .defaultMethodOptions(
                                MethodOptions.builder()
                                        .apiKeyRequired(true)
                                        .build())
                        .build())
                .addMethod(HttpMethod.POST.name());
        // IMPORTANT: Workaround to disable the api-key for OPTIONS method
        restApi.getMethods().stream()
                .filter(m -> m.getHttpMethod().equalsIgnoreCase("OPTIONS"))
                .forEach(m -> ((CfnMethod)m.getNode().getDefaultChild()).setApiKeyRequired(false));

        // REST-API deployment
        var deployment = Deployment.Builder.create(this, resourceId("BackendDeployment"))
                .retainDeployments(false)
                .api(restApi)
                .build();
        deployment.addToLogicalId(UUID.randomUUID().toString());

        prodStage = restApi.getDeploymentStage();
        testStage = Stage.Builder.create(this, resourceId("TestBackend"))
                .stageName("test")
                .deployment(deployment)
                .loggingLevel(MethodLoggingLevel.INFO)
                .build();

        createUsagePlan(restApi, prodStage, "Prod", config.apiKey);
        createUsagePlan(restApi, testStage, "Test", config.testApiKey);

        function.grantInvoke(ServicePrincipal.Builder.create("apigateway.amazonaws.com")
                .region(config.region)
                .build()
        );
    }

    private void createUsagePlan(RestApi restApi, Stage stage, String idSuffix, String apiKeyValue) {
        var apiKey = restApi.addApiKey(resourceId(idSuffix + "ApiKey"), ApiKeyOptions.builder()
                .apiKeyName(stage.getStageName())
                .value(apiKeyValue)
                .build());
        var testUsagePlan = restApi.addUsagePlan(resourceId(idSuffix + "UsagePlan"), UsagePlanProps.builder()
                .name(stage.getStageName())
                .apiStages(List.of(
                        UsagePlanPerApiStage.builder()
                                .api(restApi)
                                .stage(stage)
                                .build()
                ))
                .throttle(ThrottleSettings.builder()
                        .rateLimit(2)
                        .burstLimit(10)
                        .build())
                .build());
        testUsagePlan.addApiKey(apiKey);
    }

    public Stage getProdStage() {
        return prodStage;
    }

    public Stage getTestStage() {
        return testStage;
    }
}
