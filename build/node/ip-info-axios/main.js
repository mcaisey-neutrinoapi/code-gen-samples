'use strict';

const axios = require('axios');

class NeutrinoAPI {
    constructor () {
        this.USER_ID = 'my-user-id';
        this.API_KEY = 'my-api-key';
    }

    /**
     * Get location information about an IP address and do reverse DNS (PTR) lookups
     * Using HTTPS GET to: /ip-info
     * @param {*} params
     * @return object|undefined Returns the decoded API response object or undefined on any API errors
     */
    ipInfo (params) {
        const readTimeout = 10 * 1000;

        return axios({
            'url': 'https://neutrinoapi.net/ip-info',
            'method': 'GET',
            'headers': {
                'User-id': this.USER_ID,
                'API-Key': this.API_KEY
            },
            'params': params,
            'timeout': readTimeout
        });
    }
}

const neutrinoAPI = new NeutrinoAPI();

// Request data, see: https://www.neutrinoapi.com/api/ip-info
const params = {
    'request-delay': '11000',
    'ip': '1.1.1.1', // IPv4 or IPv6 address
    'reverse-lookup': 'false' // Do a reverse DNS (PTR) lookup
};
neutrinoAPI.ipInfo(params)
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
