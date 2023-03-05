package dev.ihet.aws.infrastructure.constructs;

import dev.ihet.aws.infrastructure.Configuration;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.services.sqs.DeadLetterQueue;
import software.amazon.awscdk.services.sqs.Queue;
import software.amazon.awscdk.services.sqs.QueueEncryption;
import software.constructs.Construct;

public class QueueConstruct extends Construct {

    private static final Configuration config = Configuration.CONFIG;

    private final Queue queue;

    public QueueConstruct(@NotNull Construct scope, @NotNull String id, String stage) {
        super(scope, id);

        queue = Queue.Builder.create(this, config.namePrefixedId("ContactQueue", stage))
                .encryption(QueueEncryption.KMS_MANAGED)
                .retentionPeriod(Duration.days(7))
                .queueName(config.prefixedId("contactRequests", stage))
                .maxMessageSizeBytes(1024)
                .build();
    }

    public Queue getQueue() {
        return queue;
    }
}
