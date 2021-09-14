from urllib.request import Request, urlopen
from urllib.error import HTTPError, URLError
import json, os, pprint, tempfile, sys

# API credentials
USER_ID = 'my-user-id'
API_KEY = 'my-api-key'

def image_watermark(params):
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
    params['output_case'] = 'snake'
    params = json.dumps(params).encode('utf-8')
    headers = {
        'Content-Type': 'application/json; charset=utf-8',
        'User-ID': USER_ID,
        'API-Key': API_KEY
    }
    try:
        req = Request(url, params, headers)
        with urlopen(req, timeout=readTimeout) as response:
            filename = tempfile.mkstemp(suffix='.png', prefix='image-watermark_')[1]
            with open(filename, 'wb') as fp:
                while True:
                    buf = response.read(2048)
                    if not buf:
                        break
                    fp.write(buf)

                return os.path.abspath(filename)
    except (HTTPError) as http_err:
        print("HTTPError:", http_err.code, http_err.read().decode("utf-8"))
    except (URLError) as url_err:
        print("URLError:", url_err)
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

