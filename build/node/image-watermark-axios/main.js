'use strict';

const axios = require('axios');
const fs = require('fs');
const os = require('os');
const path = require('path');

class NeutrinoAPI {
    constructor () {
        this.USER_ID = 'my-user-id';
        this.API_KEY = '1234';
    }

    /**
     * Watermark one image with another image
     * Using HTTPS GET to: /image-watermark
     * @param {*} params
     * @return object|undefined Returns the decoded API response object or undefined on any API errors
     */
    imageWatermark (params) {
        const readTimeout = 20 * 1000;

        return axios({
            'url': 'https://neutrinoapi.net/image-watermark',
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
            const filename = path.join(os.tmpdir(), `${Date.now()}${Math.random(10000)}.png`);
            const fileStream = fs.createWriteStream(filename);
            response.data.pipe(fileStream);
            return filename;
        });
    }
}

const neutrinoAPI = new NeutrinoAPI();

// Request data, see: https://www.neutrinoapi.com/api/image-watermark
const params = {
    'format': 'png', // The output image format
    'image-url': 'https://www.neutrinoapi.com/img/LOGO.png', // The URL or Base64 encoded Data URL
                                                             // for the source image (you can also
                                                             // upload an image file directly in
                                                             // which case this field is ignored)
    'position': 'center', // The position of the watermark image
    'watermark-url': 'https://www.neutrinoapi.com/img/icons/security.png', // The URL or Base64
                                                                           // encoded Data URL for
                                                                           // the watermark image
                                                                           // (you can also upload
                                                                           // an image file directly
                                                                           // in which case this
                                                                           // field is ignored)
    'opacity': '50' // The opacity of the watermark (0 to 100)
};
neutrinoAPI.imageWatermark(params)
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
