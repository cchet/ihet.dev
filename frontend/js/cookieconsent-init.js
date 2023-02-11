window.dataLayer = window.dataLayer || [];
window.gtag = function gtag() {
    const dataLayer = window.dataLayer = window.dataLayer || [];
    dataLayer.push(arguments);
}

const state = {
    categories: {
        necessary: 'necessary',
        analytics: 'analytics'
    },
    gid: 'G-46RZRMC1YC',
}

const addEventListenerForPrivacySettingsLink = (options) => {
    const privacySettings = document.getElementById(options.privacySettingsLink);
    privacySettings.classList.remove('d-none');
    privacySettings.addEventListener('click', e => {
        e.preventDefault();
        options.cc.showSettings();
    });
};

const disableGoogleAnalytics = (options) => {
    window[`ga-disable-${state.gid}`] = true;
    gtag('consent', 'update', {
        'analytics_storage': 'denied',
    });
    console.log('google analytics disabled');
};

const enableGoogleAnalytics = (options) => {
    window[`ga-disable-${state.gid}`] = false;
    gtag('js', new Date());
    gtag('consent', 'default', {
        'ad_storage': 'denied',
        'analytics_storage': 'granted',
    });

    gtag('config', state.gid, {
        'anonymize_ip': true,
        'allow_ad_personalization_signals': false,
        'allow_google_signals': false,
        'cookie_flags': 'max-age=31536000 secure;samesite=strict'
    });
    console.log('google analytics enabled');
};

const runConsentDialog = (options) => {
    options.cc.run({
        current_lang: 'en',
        autoclear_cookies: true,
        cookie_name: 'consent.www.ihet.dev',
        cookie_expiration: 365,
        cookie_same_site: true,
        use_rfc_cookie: true,
        page_scripts: false,
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
            addEventListenerForPrivacySettingsLink(options);
            if (options.cc.allowedCategory(state.categories.analytics)) {
                enableGoogleAnalytics(options);
            } else {
                disableGoogleAnalytics(options);
            }
        },

        onChange: function(cookie, changed_preferences) {
            if (options.cc.allowedCategory(state.categories.analytics)) {
                enableGoogleAnalytics(options);
            } else {
                disableGoogleAnalytics(options);
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
                        description: 'These cookies are essential for the proper functioning of my website. Without these cookies, you cannot interact with this site',
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
                        description: 'These are the cookies of google analytics which are used to track your interactions with my website.',
                        toggle: {
                            value: 'analytics',
                            enabled: false,
                            readonly: false
                        },
                        cookie_table: [{
                            col1: '^_ga',
                            col2: 'google.com',
                            col3: 'The google analytics cookies tracking your interactions',
                            col4: '1 year',
                            is_regex: true
                        }]
                    }]
                }
            }
        }
    });
};

const init = (options) => {
    options.cc = initCookieConsent();
    runConsentDialog(options);
};

export default {
    init,
}