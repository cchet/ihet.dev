import consent from './consentManager.js';
import contactManager from './contactManager.js';

const navbarShrink = (options) => {
    const {
        navId
    } = options;
    const navbar = document.getElementById(navId);
    if (window.scrollY === 0) {
        navbar.classList.remove('navbar-shrink')
    } else {
        navbar.classList.add('navbar-shrink')
    }
};

const configureNavbar = (options) => {
    const {
        navId
    } = options;
    // Shrink the navbar 
    navbarShrink(options);

    // Shrink the navbar when page is scrolled
    document.addEventListener('scroll', () => navbarShrink(options));

    // Activate Bootstrap scrollspy on the main nav element
    new bootstrap.ScrollSpy(document.body, {
        target: `#${navId}`,
        offset: 72,
    });

    // Collapse responsive navbar when toggler is visible
    const navbarToggler = document.querySelector('.navbar-toggler');
    const responsiveNavItems = [].slice.call(
        document.querySelectorAll(`#${navId} .nav-link`)
    );
    responsiveNavItems.map(function(responsiveNavItem) {
        responsiveNavItem.addEventListener('click', () => {
            if (window.getComputedStyle(navbarToggler).display !== 'none') {
                navbarToggler.click();
            }
        });
    });
};

const configureForm = (options) => {
    document.querySelectorAll('.needs-validation').forEach(form => form.addEventListener('submit', event => {
        if (!form.checkValidity()) {
            event.preventDefault()
            event.stopPropagation()
        }

        form.classList.add('was-validated')
    }));
};

const loadConfig = async (options) => {
    return fetch('config.json', {
            method: 'GET',
            headers: {
                'Accept': 'application/json',
            }
        }).then(response => {
            if (response.ok) {
                return response.json();
            }
            throw Error(`Loading of config.josn failed with: ${response.status}`)
        })
        .catch(error => console.error(error));
}

const init = async (options) => {
    loadConfig(options).then(config => {
        if (config) {
            console.log(`Loaded configuration: ${JSON.stringify(config)}`);
            options.configLoaded = true;
            options.config = config;
        } else {
            options.configLoaded = false;
        }
        contactManager.init(options);
        consent.init(options);
        configureForm(options);
        configureNavbar(options);
    });
};

export default {
    init,
}