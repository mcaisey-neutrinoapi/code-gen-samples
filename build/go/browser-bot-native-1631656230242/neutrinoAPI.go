package main

import (
	"encoding/json"
	"errors"
	"fmt"
	"io/ioutil"
	"net/http"
	"net/url"
	"os"
	"strings"
	"time"
)

// User credentials
const (
	UserID = "my-user-id"
	APIKey = "my-api-key"
)

// NeutrinoAPI object
type NeutrinoAPI struct {
	Client http.Client
}

// NewNeutrinoAPI initialiser for NeutrinoAPI
func NewNeutrinoAPI() *NeutrinoAPI {
	return &NeutrinoAPI{http.Client{Timeout: 300 * time.Second}}
}

// BrowserBot function for NeutrinoAPI
func (api NeutrinoAPI) BrowserBot(params url.Values) (map[string]interface{}, error) {
	url, err := url.Parse("https://neutrinoapi.net/browser-bot")
	if err != nil {
		fmt.Fprintln(os.Stderr, err)
		return nil, err
	}

	req, err := http.NewRequest("POST", url.String(), strings.NewReader(params.Encode()))
	req.Header.Add("Content-type", "application/x-www-form-urlencoded")
	req.Header.Add("User-ID", UserID)
	req.Header.Add("API-Key", APIKey)
	if err != nil {
		fmt.Fprintln(os.Stderr, err)
		return nil, err
	}

	resp, err := api.Client.Do(req)
	if err != nil {
		fmt.Fprintln(os.Stderr, err)
		return nil, err
	}
	defer resp.Body.Close()

	if resp.StatusCode == 200 {
		var result map[string]interface{}
		decoder := json.NewDecoder(resp.Body)
		if err := decoder.Decode(&result); err != nil {
			fmt.Fprintln(os.Stderr, err)
			return nil, err
		}
		return result, nil
	} else {
		body, err := ioutil.ReadAll(resp.Body)
		if err != nil {
			fmt.Fprintln(os.Stderr, err)
			return nil, err
		}
		if len(body) > 0 {
			fmt.Fprintf(os.Stderr, "HttpError: %s\n", body)
		} else {
			fmt.Fprintf(os.Stderr, "HttpError: %s\n", resp.Status)
		}
		return nil, errors.New(resp.Status)
	}
}

// Browser bot can extract content, interact with keyboard and mouse events, and execute JavaScript on a website
func main() {
	// Request data, see: https://www.neutrinoapi.com/api/browser-bot
	params := make(url.Values, 7)
	params.Add("delay", "3") // Delay in seconds to wait before capturing any page data
	params.Add("ignore-certificate-errors", "false") // Ignore any TLS/SSL certificate errors and load
                                                  // the page anyway
	params.Add("selector", ".header-link") // Extract content from the page DOM using this selector
	params.Add("exec[1]", "\"Hello\".toUpperCase()")
	params.Add("url", "https://www.neutrinoapi.com/") // The URL to load
	params.Add("timeout", "30") // Timeout in seconds
	params.Add("exec[0]", "document.getElementsByTagName('title')[0].innerText") // Execute JavaScript
                                                                              // on the page

	neutrinoAPI := NewNeutrinoAPI()
	result, err := neutrinoAPI.BrowserBot(params)
	if err == nil {
		fmt.Printf("%+v\n", result)
	} else {
		fmt.Fprintln(os.Stderr, "API request failed!") // you should handle this gracefully!
	}
}
