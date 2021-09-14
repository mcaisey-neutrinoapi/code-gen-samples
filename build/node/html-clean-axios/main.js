'use strict';

const axios = require('axios');
const fs = require('fs');
const os = require('os');
const path = require('path');

class NeutrinoAPI {
    constructor () {
        this.USER_ID = 'my-user-id';
        this.API_KEY = 'my-api-key';
    }

    /**
     * Clean and sanitize untrusted HTML
     * Using HTTPS GET to: /html-clean
     * @param {*} params
     * @return object|undefined Returns the decoded API response object or undefined on any API errors
     */
    htmlClean (params) {
        const readTimeout = 30 * 1000;

        return axios({
            'url': 'https://neutrinoapi.net/html-clean',
            'method': 'POST',
            'headers': {
                'User-id': this.USER_ID,
                'API-Key': this.API_KEY
            },
            'data': params,
            'timeout': readTimeout,
            'responseType': 'stream'
        })
        .then((response) => {
            const filename = path.join(os.tmpdir(), `${Date.now()}${Math.random(10000)}.txt`);
            const fileStream = fs.createWriteStream(filename);
            response.data.pipe(fileStream);
            return filename;
        });
    }
}

const neutrinoAPI = new NeutrinoAPI();

// Request data, see: https://www.neutrinoapi.com/api/html-clean
const params = {
    'output-type': 'plain-text', // The level of sanitization
    'content': '[Ф][Ö][©][ش𐹧][的] Some HTML with UTF chars...' // The HTML content
};
neutrinoAPI.htmlClean(params)
    .then((result) => {
        console.log(result);
    })
    .catch((error) => {
        if (error.response) {
            error.response.data.pipe(process.stderr);
        } else {
            console.error(error.message);
        }
        console.error('API request failed!'); // you should handle this gracefully!
    });
