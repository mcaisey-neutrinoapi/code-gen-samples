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
     * Render HTML content to PDF, JPG or PNG
     * Using HTTPS GET to: /html-render
     * @param {*} params
     * @return object|undefined Returns the decoded API response object or undefined on any API errors
     */
    htmlRender (params) {
        const readTimeout = 300 * 1000;

        return axios({
            'url': 'https://neutrinoapi.net/html-render',
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
            const filename = path.join(os.tmpdir(), `${Date.now()}${Math.random(10000)}.pdf`);
            const fileStream = fs.createWriteStream(filename);
            response.data.pipe(fileStream);
            return filename;
        });
    }
}

const neutrinoAPI = new NeutrinoAPI();

// Request data, see: https://www.neutrinoapi.com/api/html-render
const params = {
    'margin': '0', // The document margin (in mm)
    'image-width': '1024', // If rendering to an image format (PNG or JPG) use this image width (in
                           // pixels)
    'format': 'PDF', // Which format to output
    'zoom': '1', // Set the zoom factor when rendering the page (2.0 for double size
    'content': '[Ф][Ö][©][ش𐹧][的] Some HTML with UTF chars...', // The HTML content
    'timeout': '300', // Timeout in seconds
    'margin-right': '0', // The document right margin (in mm)
    'grayscale': 'false', // Render the final document in grayscale
    'margin-left': '0', // The document left margin (in mm)
    'page-size': 'A4', // Set the document page size
    'delay': '0', // Number of seconds to wait before rendering the page (can be useful for pages
                  // with animations etc)
    'ignore-certificate-errors': 'false', // Ignore any TLS/SSL certificate errors
    'margin-top': '0', // The document top margin (in mm)
    'margin-bottom': '0', // The document bottom margin (in mm)
    'landscape': 'false' // Set the document to landscape orientation
};
neutrinoAPI.htmlRender(params)
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
