from urllib.request import Request, urlopen
from urllib.error import HTTPError, URLError
import json, pprint, sys

# API credentials
USER_ID = 'my-user-id'
API_KEY = 'my-api-key'

def bad_word_filter(params):
    """
    Detect bad words, swear words and profanity in a given text
    Using HTTPS POST to: /bad-word-filter
    
    Parameters
    ----------
    params : dict
        The API request parameters

    Returns
    -------
    dict | None 
        Returns the decoded API response dict or None on any API errors
    """
    url = 'https://neutrinoapi.net/bad-word-filter'
    readTimeout = 30
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
            return json.load(response)    
    except (HTTPError) as http_err:
        print("HTTPError:", http_err.code, http_err.read().decode("utf-8"))
    except (URLError) as url_err:
        print("URLError:", url_err)
    except Exception as err:
        print("Error:", str(err), sys.exc_info()[0])


# Request data, see: https://www.neutrinoapi.com/api/bad-word-filter
request = {
    "catalog": "strict", # Which catalog of bad words to use
    "content": '''multi
line
content
boob'''
    # The content to scan
}
response = bad_word_filter(request)
if response:
    pprint.pprint(response)
else:
    print("API request failed!") # you should handle this gracefully!

