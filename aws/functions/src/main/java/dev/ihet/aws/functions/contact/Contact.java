package dev.ihet.aws.functions.contact;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

public class Contact {

    @NotEmpty
    @Email
    public String email;

    @NotEmpty
    @Size(min = 5, max = 50)
    public String name;

    @NotEmpty
    @Size(min = 10, max = 255)
    public String message;

    @NotEmpty
    @Size(min = 1, max = 10)
    public String stage;
}
