from requests import HTTPError
import pprint, requests, sys

# API credentials
USER_ID = 'my-user-id'
API_KEY = 'my-api-key'

def ip_info(params):
    """
    Get location information about an IP address and do reverse DNS (PTR) lookups
    Using HTTPS GET to: /ip-info
    
    Parameters
    ----------
    params : dict
        The API request parameters

    Returns
    -------
    dict | None 
        Returns the decoded API response dict or None on any API errors
    """
    url = 'https://neutrinoapi.net/ip-info'
    readTimeout = 10
    params['output_case'] = 'snake'
    headers = {
        'User-ID': USER_ID,
        'API-Key': API_KEY
    }
    try:
        response = requests.get(url, params, headers=headers, timeout=readTimeout)
        response.raise_for_status()
        return response.json()
    except (HTTPError) as http_err:
        print("HTTPError:", response.text, http_err)
    except Exception as err:
        print("Error:", str(err), sys.exc_info()[0])

# Request data, see: https://www.neutrinoapi.com/api/ip-info
request = {
    "ip": "1.1.1.1", # IPv4 or IPv6 address
    "reverse-lookup": "false" # Do a reverse DNS (PTR) lookup
}
response = ip_info(request)
if response:
    pprint.pprint(response)
else:
    print("API request failed!") # you should handle this gracefully!
