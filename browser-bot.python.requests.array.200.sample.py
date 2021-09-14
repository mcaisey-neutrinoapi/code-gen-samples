from requests import HTTPError
import pprint, requests, sys

# API credentials
USER_ID = 'my-user-id'
API_KEY = 'my-api-key'

def browser_bot(data):
    """
    Browser bot can extract content, interact with keyboard and mouse events, and execute JavaScript on a website
    Using HTTPS POST to: /browser-bot
    
    Parameters
    ----------
    params : dict
        The API request parameters

    Returns
    -------
    dict | None 
        Returns the decoded API response dict or None on any API errors
    """
    url = 'https://neutrinoapi.net/browser-bot'
    readTimeout = 300
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

# Request data, see: https://www.neutrinoapi.com/api/browser-bot
request = {
    "delay": "3", # Delay in seconds to wait before capturing any page data
    "ignore-certificate-errors": "false", # Ignore any TLS/SSL certificate errors and load the page
                                          # anyway
    "selector": ".header-link", # Extract content from the page DOM using this selector
    "exec[1]": "\"Hello\".toUpperCase()",
    "url": "https://www.neutrinoapi.com/", # The URL to load
    "timeout": "30", # Timeout in seconds
    "exec[0]": "document.getElementsByTagName('title')[0].innerText" # Execute JavaScript on the
                                                                     # page
}
response = browser_bot(request)
if response:
    pprint.pprint(response)
else:
    print("API request failed!") # you should handle this gracefully!
