package main

import (
	"encoding/json"
	"errors"
	"fmt"
	"io/ioutil"
	"net/http"
	"net/url"
	"os"
	"time"
)

// User credentials
const (
	UserID = "my-user-id"
	APIKey = "1234"
)

// NeutrinoAPI object
type NeutrinoAPI struct {
	Client http.Client
}

// NewNeutrinoAPI initialiser for NeutrinoAPI
func NewNeutrinoAPI() *NeutrinoAPI {
	return &NeutrinoAPI{http.Client{Timeout: 10 * time.Second}}
}

// IPInfo function for NeutrinoAPI
func (api NeutrinoAPI) IPInfo(params url.Values) (map[string]interface{}, error) {
	url, err := url.Parse("https://neutrinoapi.net/ip-info")
	if err != nil {
		fmt.Fprintln(os.Stderr, err)
		return nil, err
	}
	url.RawQuery = params.Encode()
	
	req, err := http.NewRequest("GET", url.String(), nil)
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
		decoder := json.NewDecoder(resp.Body)
		for {
			var result map[string]interface{}
			if err := decoder.Decode(&result); err != nil {
				fmt.Fprintln(os.Stderr, err)
				return nil, err
			}
			return result, nil
		}
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

// Get location information about an IP address and do reverse DNS (PTR) lookups
func main() {
	// Request data, see: https://www.neutrinoapi.com/api/ip-info
	params := make(url.Values, 2)
	params.Add("ip", "1.1.1.1") // IPv4 or IPv6 address
	params.Add("reverse-lookup", "false") // Do a reverse DNS (PTR) lookup

	neutrinoAPI := NewNeutrinoAPI()
	result, err := neutrinoAPI.IPInfo(params)
	if err == nil {
		fmt.Printf("%+v\n", result)
	} else {
		fmt.Fprintln(os.Stderr, "API request failed!") // you should handle this gracefully!
	}
}
