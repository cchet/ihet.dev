package dev.ihet.aws.infrastructure.constructs;

import dev.ihet.aws.infrastructure.Configuration;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.CfnParameter;
import software.amazon.awscdk.services.apigateway.*;
import software.amazon.awscdk.services.iam.*;
import software.amazon.awscdk.services.lambda.HttpMethod;
import software.amazon.awscdk.services.sqs.Queue;
import software.constructs.Construct;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GatewayConstruct extends Construct {

    private static final Configuration config = Configuration.CONFIG;

    private final String idPrefix;
    private final String origin;
    private final CfnParameter recreateDeployment;

    public GatewayConstruct(@NotNull Construct scope, @NotNull String id, Queue queue, String origin, String apiKey, CfnParameter recreateDeployment) {
        super(scope, id);
        this.idPrefix = id;
        this.origin = origin;
        this.recreateDeployment = recreateDeployment;
        var role = createQueueRole(queue);
        var queueIntegration = createQueueIntegration(queue, role);
        var prodRestApi = createRestApi(queueIntegration);
        var usagePlan = createUsagePlan(prodRestApi, prodRestApi.getDeploymentStage(), apiKey);
    }

    private RestApi createRestApi(AwsIntegration integration) {
        var restApi = RestApi.Builder.create(this, idPrefix + "RestApi")
                .deploy(true)
                .restApiName(idPrefix + "RestApi")
                .cloudWatchRole(true)
                .description("The rest-api for sending emails")
                .endpointConfiguration(
                        EndpointConfiguration.builder()
                                .types(List.of(EndpointType.REGIONAL))
                                .build())
                .apiKeySourceType(ApiKeySourceType.HEADER)
                .defaultMethodOptions(MethodOptions.builder()
                        .apiKeyRequired(true)
                        .build())
                .defaultCorsPreflightOptions(CorsOptions.builder()
                        .allowOrigins(List.of(origin))
                        .allowMethods(List.of(HttpMethod.POST.name()))
                        .allowHeaders(List.of(
                                "Content-Type",
                                "Accept",
                                "X-API-KEY",
                                "Origin"
                        ))
                        .statusCode(200)
                        .build())
                .defaultIntegration(MockIntegration.Builder.create()
                        .passthroughBehavior(PassthroughBehavior.NEVER)
                        .integrationResponses(List.of(IntegrationResponse.builder()
                                .statusCode("200")
                                .responseTemplates(Map.of("application/json", "{ \"error\": true, \"message\": \"Nothing happens here\" }"))
                                .build()))
                        .build())
                .build();
        // Create a proxy so we can disable ANY
        restApi.getRoot().addProxy(ProxyResourceOptions.builder()
                .anyMethod(false)
                .build());
        // This is the only resource the lambda supports right now
        var contactMeResource = restApi.getRoot().addResource("contactMe");
        var contactMeMethodPost = contactMeResource.addMethod("POST", integration, MethodOptions.builder()
                .requestValidator(createContactRequestValidator(restApi))
                .requestModels(Map.of("application/json", createRequestContactModel(restApi)))
                .build());
        var methodResponseBuilder = MethodResponse.builder().responseParameters(Map.of("method.response.header.Content-Type", true, "method.response.header.Access-Control-Allow-Origin", true));
        contactMeMethodPost.addMethodResponse(methodResponseBuilder.statusCode("200").build());
        contactMeMethodPost.addMethodResponse(methodResponseBuilder.statusCode("400").build());
        contactMeMethodPost.addMethodResponse(methodResponseBuilder.statusCode("500").build());

        // IMPORTANT: Workaround to disable the api-key for OPTIONS method
        restApi.getMethods().stream()
                .filter(m -> m.getHttpMethod().equalsIgnoreCase("OPTIONS"))
                .forEach(m -> ((CfnMethod) m.getNode().getDefaultChild()).setApiKeyRequired(false));

        // REST-API deployment
        var deployment = Deployment.Builder.create(this, idPrefix + "Deployment")
                .retainDeployments(false)
                .api(restApi)
                .build();
        if (Boolean.valueOf(recreateDeployment.getValueAsString())) {
            deployment.addToLogicalId(UUID.randomUUID().toString());
        }

        return restApi;
    }

    private UsagePlan createUsagePlan(RestApi restApi, Stage stage, String apiKeyValue) {
        var apiKey = restApi.addApiKey(config.namePrefixedId(idPrefix + "ApiKey"), ApiKeyOptions.builder()
                .apiKeyName(idPrefix + "ApiKey")
                .value(apiKeyValue)
                .build());
        var stageUsagePlan = restApi.addUsagePlan(config.namePrefixedId(idPrefix + "UsagePlan"), UsagePlanProps.builder()
                .name(idPrefix + "Stage")
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

        return stageUsagePlan;
    }

    private Role createQueueRole(Queue queue) {
        var role = Role.Builder.create(this, idPrefix + "ApiGatewayQueueRole")
                .assumedBy(ServicePrincipal.Builder.create("apigateway.amazonaws.com").build())
                .build();
        role.attachInlinePolicy(Policy.Builder.create(this, idPrefix + "SendMessagePolicy")
                .statements(List.of(
                        PolicyStatement.Builder.create()
                                .actions(List.of("sqs:SendMessage"))
                                .effect(Effect.ALLOW)
                                .resources(List.of(queue.getQueueArn()))
                                .build()
                ))
                .build());

        return role;
    }

    private RequestValidator createContactRequestValidator(RestApi restApi) {
        return RequestValidator.Builder.create(this, idPrefix + "ContactRequestValidator")
                .requestValidatorName("contactModelValidator")
                .restApi(restApi)
                .validateRequestBody(true)
                .build();
    }

    private Model createRequestContactModel(RestApi restApi) {
        return Model.Builder.create(this, idPrefix + "ContactModel")
                .modelName("contactModel")
                .description("The contact request model")
                .restApi(restApi)
                .schema(JsonSchema.builder()
                        .required(List.of("name", "email", "message"))
                        .uniqueItems(true)
                        .maxItems(3)
                        .properties(Map.of(
                                "name", JsonSchema.builder()
                                        .type(JsonSchemaType.STRING)
                                        .minLength(5)
                                        .maxLength(50)
                                        .build(),
                                "email", JsonSchema.builder()
                                        .type(JsonSchemaType.STRING)
                                        .format("email")
                                        .minLength(6)
                                        .maxLength(127)
                                        .build(),
                                "message", JsonSchema.builder()
                                        .type(JsonSchemaType.STRING)
                                        .minLength(10)
                                        .maxLength(255)
                                        .build()))
                        .build())
                .build();
    }

    private AwsIntegration createQueueIntegration(Queue queue, Role role) {
        return AwsIntegration.Builder.create()
                .service("sqs")
                .region(config.region)
                .path(config.account + "/" + queue.getQueueName())
                .integrationHttpMethod("POST")
                .options(IntegrationOptions.builder()
                        .credentialsRole(role)
                        .passthroughBehavior(PassthroughBehavior.NEVER)
                        .requestParameters(Map.of(
                                "integration.request.header.Content-Type", "'application/x-www-form-urlencoded'"
                        ))
                        .requestTemplates(Map.of(
                                "application/json", "Action=SendMessage&MessageBody=$util.urlEncode($input.body)"
                        ))
                        .integrationResponses(
                                List.of(
                                        IntegrationResponse.builder().statusCode("200").responseParameters(Map.of(
                                                        "method.response.header.Content-Type", "'application/json'",
                                                        "method.response.header.Access-Control-Allow-Origin", String.format("'%s'", origin))
                                                )
                                                .responseTemplates(Map.of(
                                                        "application/json", "{ \"error\": false, \"messageId\": \"$input.path('$.SendMessageResponse.SendMessageResult.MessageId')\" }"
                                                )).build(),
                                        IntegrationResponse.builder().statusCode("400").responseParameters(Map.of(
                                                        "method.response.header.Content-Type", "'application/json'",
                                                        "method.response.header.Access-Control-Allow-Origin", String.format("'%s'", origin))
                                                )
                                                .responseTemplates(Map.of(
                                                        "application/json", "{ \"error\": true }"
                                                )).build(),
                                        IntegrationResponse.builder().statusCode("500").responseParameters(Map.of(
                                                        "method.response.header.Content-Type", "'application/json'",
                                                        "method.response.header.Access-Control-Allow-Origin", String.format("'%s'", origin))
                                                )
                                                .responseTemplates(Map.of(
                                                        "application/json", "{ \"error\": true }"
                                                )).build()
                                )
                        )
                        .build())
                .build();
    }
}
