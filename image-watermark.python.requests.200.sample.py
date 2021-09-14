from requests import HTTPError
import pprint, os, requests, tempfile, sys

# API credentials
USER_ID = 'my-user-id'
API_KEY = 'my-api-key'

def image_watermark(data):
    """
    Watermark one image with another image
    Using HTTPS POST to: /image-watermark
    
    Parameters
    ----------
    params : dict
        The API request parameters

    Returns
    -------
    str | None 
        Returns the output file path or None on any API errors
    """
    url = 'https://neutrinoapi.net/image-watermark'
    readTimeout = 20
    data['output_case'] = 'snake'
    headers = {
        'User-ID': USER_ID,
        'API-Key': API_KEY
    }
    try:
        response = requests.post(url, data=data, headers=headers, stream=True, timeout=readTimeout)
        response.raise_for_status()
        filename = tempfile.mkstemp(suffix='.png', prefix='image-watermark_')[1]
        with open(filename, 'wb') as fp:
            for buf in response.iter_content(chunk_size=2048):
                if not buf:
                    break
                fp.write(buf)

            return os.path.abspath(filename)
    except (HTTPError) as http_err:
        print("HTTPError:", response.text, http_err)
    except Exception as err:
        print("Error:", str(err), sys.exc_info()[0])

# Request data, see: https://www.neutrinoapi.com/api/image-watermark
request = {
    "format": "png", # The output image format
    "image-url": "https://www.neutrinoapi.com/img/LOGO.png", # The URL or Base64 encoded Data URL
                                                             # for the source image (you can also
                                                             # upload an image file directly in
                                                             # which case this field is ignored)
    "position": "center", # The position of the watermark image
    "watermark-url": "https://www.neutrinoapi.com/img/icons/security.png", # The URL or Base64
                                                                           # encoded Data URL for
                                                                           # the watermark image
                                                                           # (you can also upload an
                                                                           # image file directly in
                                                                           # which case this field
                                                                           # is ignored)
    "opacity": "50" # The opacity of the watermark (0 to 100)
}
response = image_watermark(request)
if response:
    pprint.pprint(response)
else:
    print("API request failed!") # you should handle this gracefully!
