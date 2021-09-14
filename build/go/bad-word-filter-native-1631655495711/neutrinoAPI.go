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
	return &NeutrinoAPI{http.Client{Timeout: 30 * time.Second}}
}

// BadWordFilter function for NeutrinoAPI
func (api NeutrinoAPI) BadWordFilter(params url.Values) (map[string]interface{}, error) {
	url, err := url.Parse("https://neutrinoapi.net:1234/bad-word-filter")
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

// Detect bad words, swear words and profanity in a given text
func main() {
	// Request data, see: https://www.neutrinoapi.com/api/bad-word-filter
	params := make(url.Values, 2)
	params.Add("catalog", "strict") // Which catalog of bad words to use
	params.Add("content", "https://en.wikipedia.org/wiki/Profanity") // The content to scan

	neutrinoAPI := NewNeutrinoAPI()
	result, err := neutrinoAPI.BadWordFilter(params)
	if err == nil {
		fmt.Printf("%+v\n", result)
	} else {
		fmt.Fprintln(os.Stderr, "API request failed!") // you should handle this gracefully!
	}
}
