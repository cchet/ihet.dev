package dev.ihet.aws.functions.contact;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import dev.ihet.aws.functions.Configuration;
import org.jboss.logging.Logger;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.*;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbException;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import java.util.Objects;
import java.util.stream.Collectors;

@Singleton
public class ContactMeSqsConsumer implements RequestHandler<SQSEvent, Void> {

    private static final Logger logger = Logger.getLogger(ContactMeSqsConsumer.class);

    private static final Jsonb jsonb = JsonbBuilder.create();

    @Inject
    Configuration config;

    @Inject
    Validator validator;

    @Override
    public Void handleRequest(SQSEvent input, Context context) {
        var body = input.getRecords().stream()
                .map(SQSEvent.SQSMessage::getBody)
                .map(this::parseContact)
                .filter(Objects::nonNull)
                .map(this::contactToMessageString)
                .collect(Collectors.joining("\n----------------------\n"));
        try {
            sendEmail(body);
        } catch (Exception e) {
            logger.errorf("Email sending of contact request failed. message-body: %s", body);
        }
        return null;
    }

    private Contact parseContact(String message) {
        try {
            var contact = jsonb.fromJson(message, Contact.class);
            validator.validate(contact);
            return contact;
        } catch (JsonbException e) {
            logger.errorf(e, "Could not parse sqs-message-body: %s", message);
        } catch (ConstraintViolationException e) {
            var violations = e.getConstraintViolations().stream()
                    .map(v -> String.format("property: %s, message: %s", v.getPropertyPath(), v.getMessage()))
                    .collect(Collectors.joining("\n"));
            logger.errorf("Validation of contact request failed. sqs-message-body: %s\nviolations: %s", message, violations);
        } catch (Exception e) {
            logger.errorf(e, "Unexpected error occurred with sqs-message-body: %s", message);
        }
        return null;
    }

    private String contactToMessageString(Contact contact) {
        return String.format("Email: %s\nName: %s\nMessage: %s", contact.email, contact.name, contact.message);
    }

    private void sendEmail(String message) {
        var destination = Destination.builder().toAddresses(config.emailRecipient).build();
        var content = Content.builder().data(message).build();
        var sub = Content.builder().data("(" + config.stage + ") Contact request").build();
        var body = Body.builder().text(content).build();
        var msg = Message.builder().subject(sub).body(body).build();
        var emailContent = EmailContent.builder().simple(msg).build();

        var emailRequest = SendEmailRequest.builder()
                .destination(destination)
                .content(emailContent)
                .fromEmailAddress(config.emailSender)
                .build();

        SesV2Client.builder()
                .region(Region.of(config.region))
                .build()
                .sendEmail(emailRequest);
    }
}
