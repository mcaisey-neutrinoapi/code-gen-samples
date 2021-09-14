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
	return &NeutrinoAPI{http.Client{Timeout: 20 * time.Second}}
}

// ImageWatermark function for NeutrinoAPI
func (api NeutrinoAPI) ImageWatermark(params url.Values) (*string, error) {
	url, err := url.Parse("https://neutrinoapi.net/image-watermark")
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
		file, err := ioutil.TempFile("", "image-watermark-*.png")
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

// Watermark one image with another image
func main() {
	// Request data, see: https://www.neutrinoapi.com/api/image-watermark
	params := make(url.Values, 5)
	params.Add("format", "png") // The output image format
	params.Add("image-url", "https://www.neutrinoapi.com/img/LOGO.png") // The URL or Base64 encoded
                                                                     // Data URL for the source
                                                                     // image (you can also upload
                                                                     // an image file directly in
                                                                     // which case this field is
                                                                     // ignored)
	params.Add("position", "center") // The position of the watermark image
	params.Add("watermark-url", "https://www.neutrinoapi.com/img/icons/security.png")
      // The URL or Base64 encoded Data URL for the watermark image (you can also
      // upload an image file directly in which case this field is ignored)
	params.Add("opacity", "50") // The opacity of the watermark (0 to 100)

	neutrinoAPI := NewNeutrinoAPI()
	result, err := neutrinoAPI.ImageWatermark(params)
	if err == nil {
		fmt.Printf("%s\n", *result)
	} else {
		fmt.Fprintln(os.Stderr, "API request failed!") // you should handle this gracefully!
	}
}
