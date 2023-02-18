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
        })
        .then(response => response.json())
        .then(responseBody => {
            console.log(`Sending email success response: ${JSON.stringify(responseBody)}`);
            if (responseBody.error) {
                document.querySelector(`.${contact.feedbackClass}-success`).classList.remove('d-none');
            } else {
                document.querySelector(`.${contact.feedbackClass}-error`).classList.remove('d-none');
            }
            form.reset();
            form.classList.remove('was-validated');
        })
        .catch(error => {
            console.error(`Sending email error ${error}`);
            document.querySelector(`.${contact.feedbackClass}-error`).classList.remove('d-none');
        })
        .finally(() => {
            if (timeoutId) {
                clearTimeout(timeoutId);
            }
        });
};

const clearEmailFeedback = (options) => {
    const {
        contact
    } = options;
    document.querySelectorAll(`.${contact.feedbackClass}`).forEach(element => element.classList.add('d-none'));
};

const registerSubmitEventOnContactForm = (options) => {
    const {
        contact,
    } = options;
    document.getElementById(contact.formId).addEventListener('submit', async (event) => {
        const form = event.target;
        event.preventDefault();
        event.stopPropagation();
        clearEmailFeedback(options);
        if (form.checkValidity()) {
            form.querySelectorAll('input').forEach(element => {
                element.setAttribute('disabled', 'true');
            });
            sendEmail(options).then(() => form.querySelectorAll('input').forEach(element => {
                element.removeAttribute('disabled')
            }));
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