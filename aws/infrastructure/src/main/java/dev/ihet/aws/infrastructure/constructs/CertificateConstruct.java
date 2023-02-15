package dev.ihet.aws.infrastructure.constructs;

import dev.ihet.aws.infrastructure.helper.Configuration;
import dev.ihet.aws.infrastructure.helper.Util;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.services.certificatemanager.Certificate;
import software.amazon.awscdk.services.certificatemanager.CertificateValidation;
import software.constructs.Construct;

public class CertificateConstruct extends Construct {

    private static final Configuration config = Configuration.load();

    private Certificate certificate;

    public CertificateConstruct(@NotNull Construct scope, @NotNull String id) {
        super(scope, id);

        certificate = Certificate.Builder.create(this, Util.resourceId("CertificateManager"))
                .certificateName(config.name)
                .domainName(config.apiSubdomain())
                .transparencyLoggingEnabled(false)
                .validation(CertificateValidation.fromDns())
                .build();
    }

    public Certificate getCertificate() {
        return certificate;
    }
}
