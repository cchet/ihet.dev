// obtain cookieconsent plugin
window.cc = initCookieConsent();

// run plugin with config object
cc.run({
    current_lang: 'en',
    autoclear_cookies: true,
    cookie_name: 'consent.www.ihet.dev',
    cookie_expiration: 365,
    page_scripts: true,
    force_consent: true, // default: false
    autorun: true,
    delay: 0,
    cookie_same_site: 'Strict',
    cookie_path: '/',
    revision: 1,
    gui_options: {
        consent_modal: {
            layout: 'cloud',
            position: 'top center',
            transition: 'slide'
        },
        settings_modal: {
            layout: 'cloud',
            position: 'center',
            transition: 'zoom'
        }
    },

    onAccept: function(cookie) {
        const privacySettings = document.getElementById('privacySettings');
        privacySettings.classList.remove('d-none');
        privacySettings.addEventListener('click', e => {
            e.preventDefault();
            cc.showSettings();
        });
    },

    onChange: function(cookie, changed_preferences) {
        console.log('onChange fired!');

        // If analytics category is disabled => disable google analytics
        if (!cc.allowedCategory('analytics')) {
            typeof gtag === 'function' && gtag('consent', 'update', {
                'analytics_storage': 'denied'
            });
        }
    },

    languages: {
        'en': {
            consent_modal: {
                title: "Hello visitor, it's time to take care about your privacy",
                description: 'My website uses tracking cookies to understand how you interact with it. The latter will be set only after consent. <button type="button" data-cc="c-settings" class="cc-link">Let me choose</button>',
                primary_btn: {
                    text: 'Accept all',
                    role: 'accept_all'
                },
                secondary_btn: {
                    text: 'Reject all',
                    role: 'accept_necessary'
                },
                revision_message: '<br><br> Dear visitor, terms and conditions have changed since the last time you have visited!'
            },
            settings_modal: {
                title: 'Privacy settings',
                save_settings_btn: 'Save current selection',
                accept_all_btn: 'Accept all',
                reject_all_btn: 'Reject all',
                close_btn_label: 'Close',
                cookie_table_headers: [{
                        col1: 'Name'
                    },
                    {
                        col2: 'Domain'
                    },
                    {
                        col3: 'Description'
                    },
                    {
                        col4: 'Expiration'
                    }
                ],
                blocks: [{
                    title: 'Cookie usage',
                    description: 'I use cookies to track the interactions with my website.'
                }, {
                    title: 'Strictly necessary cookies',
                    description: 'These cookies are essential for the proper functioning of my website. Without these cookies, the website would not work properly',
                    toggle: {
                        value: 'necessary',
                        enabled: true,
                        readonly: true
                    },
                    cookie_table: [{
                        col1: 'consent.www.ihet.dev',
                        col2: 'www.ihet.dev',
                        col3: 'The cookie remembering your privacy settings',
                        col4: '1 year',
                        is_regex: true
                    }]
                }, {
                    title: 'Analytics & Performance cookies',
                    description: 'This is the cookie of google which is used to track your interactions with my website',
                    toggle: {
                        value: 'analytics',
                        enabled: false,
                        readonly: false
                    },
                    cookie_table: [{
                        col1: '_ga',
                        col2: 'google.com',
                        col3: 'This is the cookie tracking your interactions',
                        col4: '2 years',
                        is_regex: true
                    }]
                }]
            }
        }
    }
});