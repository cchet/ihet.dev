package dev.ihet.aws.functions.contact;

import dev.ihet.aws.functions.Configuration;
import dev.ihet.aws.functions.ResourceResponse;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.*;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/contactMe")
public class ContactMeResource {

    @Inject
    Configuration config;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResourceResponse contactMe(@Valid Contact contact) {
        sendEmail(contact);
        return ResourceResponse.ok();
    }

    private void sendEmail(Contact contact) {
        var destination = Destination.builder().toAddresses(contact.email).build();
        var content = Content.builder().data(contact.message).build();
        var sub = Content.builder().data("Contact request from " + contact.email).build();
        var body = Body.builder().html(content).build();
        var msg = Message.builder().subject(sub).body(body).build();
        var emailContent = EmailContent.builder().simple(msg).build();

        var emailRequest = SendEmailRequest.builder()
                .destination(destination)
                .content(emailContent)
                .fromEmailAddress(contact.email)
                .build();

        SesV2Client.builder()
                .region(Region.of(config.region))
                .build()
                .sendEmail(emailRequest);
    }
}
