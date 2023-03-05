package dev.ihet.aws.functions;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.inject.Singleton;

@Singleton
public class Configuration {

    @ConfigProperty(name = "aws.region")
    public String region;

    @ConfigProperty(name = "aws.email.recipient")
    public String emailRecipient;

    @ConfigProperty(name = "aws.email.sender")
    public String emailSender;
    @ConfigProperty(name = "aws.stage")
    public String stage;
}
