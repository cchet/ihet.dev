import consent from './cookieconsent-init.js';

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

const initDOMConentLoaded = (options) => {
    document.addEventListener('DOMContentLoaded', () => {
        configureForm(options);
        configureNavbar(options);
    });
};

const init = (options) => {
    consent.init(options);
    initDOMConentLoaded(options);
};

export default {
    init,
}