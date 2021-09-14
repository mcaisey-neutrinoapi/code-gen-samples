'use strict';

const axios = require('axios');

class NeutrinoAPI {
    constructor () {
        this.USER_ID = 'my-user-id';
        this.API_KEY = 'my-api-key';
    }

    /**
     * Browser bot can extract content, interact with keyboard and mouse events, and execute JavaScript on a website
     * Using HTTPS GET to: /browser-bot
     * @param {*} params
     * @return object|undefined Returns the decoded API response object or undefined on any API errors
     */
    browserBot (params) {
        const readTimeout = 300 * 1000;

        return axios({
            'url': 'https://neutrinoapi.net/browser-bot',
            'method': 'POST',
            'headers': {
                'User-id': this.USER_ID,
                'API-Key': this.API_KEY
            },
            'data': params,
            'timeout': readTimeout
        });
    }
}

const neutrinoAPI = new NeutrinoAPI();

// Request data, see: https://www.neutrinoapi.com/api/browser-bot
const params = {
    'delay': '3', // Delay in seconds to wait before capturing any page data
    'ignore-certificate-errors': 'false', // Ignore any TLS/SSL certificate errors and load the page
                                          // anyway
    'selector': '.header-link', // Extract content from the page DOM using this selector
    'exec[1]': '"Hello".toUpperCase()',
    'url': 'https://www.neutrinoapi.com/', // The URL to load
    'timeout': '30', // Timeout in seconds
    'exec[0]': 'document.getElementsByTagName(\'title\')[0].innerText' // Execute JavaScript on the
                                                                       // page
};
neutrinoAPI.browserBot(params)
    .then((result) => {
        console.log(result.data);
    })
    .catch((error) => {
        if (error.response) {
            console.error(error.response.data);
        } else {
            console.error(error.message);
        }
        console.error('API request failed!'); // you should handle this gracefully!
    });
