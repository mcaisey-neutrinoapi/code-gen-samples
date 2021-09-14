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
	return &NeutrinoAPI{http.Client{Timeout: 300 * time.Second}}
}

// HTMLRender function for NeutrinoAPI
func (api NeutrinoAPI) HTMLRender(params url.Values) (*string, error) {
	url, err := url.Parse("https://neutrinoapi.net/html-render")
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
		file, err := ioutil.TempFile("", "html-render-*.pdf")
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

// Render HTML content to PDF, JPG or PNG
func main() {
	// Request data, see: https://www.neutrinoapi.com/api/html-render
	params := make(url.Values, 15)
	params.Add("margin", "0") // The document margin (in mm)
	params.Add("image-width", "1024") // If rendering to an image format (PNG or JPG) use this image
                                   // width (in pixels)
	params.Add("format", "PDF") // Which format to output
	params.Add("zoom", "1") // Set the zoom factor when rendering the page (2.0 for double size
	params.Add("content", "[Ф][Ö][©][ش𐹧][的] Some HTML with UTF chars...") // The HTML content
	params.Add("timeout", "300") // Timeout in seconds
	params.Add("margin-right", "0") // The document right margin (in mm)
	params.Add("grayscale", "false") // Render the final document in grayscale
	params.Add("margin-left", "0") // The document left margin (in mm)
	params.Add("page-size", "A4") // Set the document page size
	params.Add("delay", "0") // Number of seconds to wait before rendering the page (can be useful for
                          // pages with animations etc)
	params.Add("ignore-certificate-errors", "false") // Ignore any TLS/SSL certificate errors
	params.Add("margin-top", "0") // The document top margin (in mm)
	params.Add("margin-bottom", "0") // The document bottom margin (in mm)
	params.Add("landscape", "false") // Set the document to landscape orientation

	neutrinoAPI := NewNeutrinoAPI()
	result, err := neutrinoAPI.HTMLRender(params)
	if err == nil {
		fmt.Printf("%s\n", *result)
	} else {
		fmt.Fprintln(os.Stderr, "API request failed!") // you should handle this gracefully!
	}
}
