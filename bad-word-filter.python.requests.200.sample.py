from requests import HTTPError
import pprint, requests, sys

# API credentials
USER_ID = 'my-user-id'
API_KEY = 'my-api-key'

def bad_word_filter(data):
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
    data['output_case'] = 'snake'
    headers = {
        'User-ID': USER_ID,
        'API-Key': API_KEY
    }
    try:
        response = requests.post(url, data=data, headers=headers, timeout=readTimeout)
        response.raise_for_status()
        return response.json()
    except (HTTPError) as http_err:
        print("HTTPError:", response.text, http_err)
    except Exception as err:
        print("Error:", str(err), sys.exc_info()[0])

# Request data, see: https://www.neutrinoapi.com/api/bad-word-filter
request = {
    "catalog": "strict", # Which catalog of bad words to use
    "content": "https://en.wikipedia.org/wiki/Profanity" # The content to scan
}
response = bad_word_filter(request)
if response:
    pprint.pprint(response)
else:
    print("API request failed!") # you should handle this gracefully!
