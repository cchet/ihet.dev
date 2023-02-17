const state = {
    timeout: 15000
}

const sendEmail = async (options) => {
    const {
        contact,
        config,
    } = options;
    const contactRequestBody = {
        name: document.getElementById(contact.nameFieldId).value,
        email: document.getElementById(contact.emailFieldId).value,
        message: document.getElementById(contact.messageFieldId).value,
        stage: config.stage
    }
    console.log(`Sending email with body: ${JSON.stringify(contactRequestBody)}`)
    const abortController = new AbortController()
    const timeoutId = setTimeout(() => abortController.abort(), state.timeout)
    return fetch(`${config.apiUrl}contactMe`, {
            signal: abortController.signal,
            method: 'POST',
            headers: {
                'X-API-KEY': `${config.apiKey}`,
                'Content-Type': 'application/json',
                'Accept': 'application/json',
            },
            body: JSON.stringify(contactRequestBody)
        }).then(response => {
            if (timeoutId) {
                clearTimeout(timeoutId);
            }
            return response.json()
        })
        .then(responseBody => {
            console.log(`Sending email received response: ${JSON.stringify(responseBody)}`);
        }).catch(error => console.error(error));
};

const registerSubmitEventOnContactForm = (options) => {
    const {
        contact
    } = options;
    document.getElementById(contact.formId).addEventListener('submit', async (event) => {
        const form = event.target;
        const successContainer = document.getElementById(contact.successContainerId);
        const errorContainer = document.getElementById(contact.errorContainerId);
        successContainer.classList.add('d-noe');
        errorContainer.classList.add('d-noe');
        event.preventDefault();
        event.stopPropagation();
        if (form.checkValidity()) {
            form.querySelectorAll('input').forEach(element => {
                element.setAttribute('disabled', 'true');
            });
            sendEmail(options).then(response => {
                form.querySelectorAll('input').forEach(element => {
                    element.removeAttribute('disabled');
                });
                form.reset();
                form.classList.remove('was-validated');
                if (!response) {
                    errorContainer.classList.remove('d-none');
                } else {
                    successContainer.classList.remove('d-none');
                }
            });
        }
    });
};

const init = (options) => {
    if (options.configLoaded) {
        registerSubmitEventOnContactForm(options);
    }
}

export default {
    init
}