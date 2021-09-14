package main

import (
	"errors"
	"fmt"
	"io"
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

// HTMLClean function for NeutrinoAPI
func (api NeutrinoAPI) HTMLClean(params url.Values) (*string, error) {
	url, err := url.Parse("https://neutrinoapi.net/html-clean")
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

	var filename string
	if resp.StatusCode == 200 {
		file, err := ioutil.TempFile("", "html-clean-*.txt")
		if err != nil {
			fmt.Fprintln(os.Stderr, err)
			return nil, err
		}
		defer file.Close()
		io.Copy(file, resp.Body)
		filename = file.Name()
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
	return &filename, nil
}

// Clean and sanitize untrusted HTML
func main() {
	// Request data, see: https://www.neutrinoapi.com/api/html-clean
	params := make(url.Values, 2)
	params.Add("output-type", "plain-text") // The level of sanitization
	params.Add("content", "[Ф][Ö][©][ش𐹧][的] Some HTML with UTF chars...") // The HTML content

	neutrinoAPI := NewNeutrinoAPI()
	result, err := neutrinoAPI.HTMLClean(params)
	if err == nil {
		fmt.Printf("%s\n", *result)
	} else {
		fmt.Fprintln(os.Stderr, "API request failed!") // you should handle this gracefully!
	}
}
