'use strict';

const axios = require('axios');

class NeutrinoAPI {
    constructor () {
        this.USER_ID = 'my-user-id';
        this.API_KEY = 'my-api-key';
    }

    /**
     * Detect bad words, swear words and profanity in a given text
     * Using HTTPS GET to: /bad-word-filter
     * @param {*} params
     * @return object|undefined Returns the decoded API response object or undefined on any API errors
     */
    badWordFilter (params) {
        const readTimeout = 30 * 1000;

        return axios({
            'url': 'https://neutrinoapi.net/bad-word-filter',
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

// Request data, see: https://www.neutrinoapi.com/api/bad-word-filter
const params = {
    'censor-character': 'λ', // The character to use to censor out the bad words found
    'catalog': 'strict', // Which catalog of bad words to use
    'content': '[Ф][Ö][©][ش𐹧][的] Some HTML with UTF chars...' // The content to scan
};
neutrinoAPI.badWordFilter(params)
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
