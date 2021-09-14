// <copyright file="NeutrinoAPI.cs" company="NeutrinoAPI">
//     Copyright NeutrinoAPI
// </copyright>
using System;
using System.Collections.Generic;
using System.IO;
using System.Net.Http;
using System.Threading.Tasks;

/// <summary>NeutrinoAPI</summary>
public class NeutrinoAPI
{
    // API credentials
    private const string UserId = "my-user-id";
    private const string ApiKey = "my-api-key";
    private static readonly HttpClient Client = new HttpClient();

    /// <summary>Gets temporary filename of response.</summary>
    public string Filename { get; private set; } = default(string);

    /// <summary>Main</summary>
    public static void Main()
    {
        // Request data, see: https://www.neutrinoapi.com/api/image-watermark
        Dictionary<string, string> paramDict = new Dictionary<string, string>();
        paramDict.Add("format", "png"); // The output image format
        paramDict.Add("image-url", "https://www.neutrinoapi.com/img/LOGO.png"); // The URL or Base64
                                                                                // encoded Data URL
                                                                                // for the source
                                                                                // image (you can
                                                                                // also upload an
                                                                                // image file
                                                                                // directly in which
                                                                                // case this field
                                                                                // is ignored)
        paramDict.Add("position", "center"); // The position of the watermark image
        paramDict.Add("watermark-url", "https://www.neutrinoapi.com/img/icons/security.png");
                 // The URL or Base64 encoded Data URL for the watermark image (you can also
                 // upload an image file directly in which case this field is ignored)
        paramDict.Add("opacity", "50"); // The opacity of the watermark (0 to 100)

        NeutrinoAPI neutrinoApi = new NeutrinoAPI();
        if (neutrinoApi.ImageWatermark(paramDict))
        {
            Console.Write("Success: ");
            Console.WriteLine(neutrinoApi.Filename);
        }
        else
        {
            Console.WriteLine("API request failed!"); // you should handle this gracefully!
        }
    }

    /// <summary>Watermark one image with another image</summary>
    /// <param name="paramDict">The API request parameters</param>
    /// <returns>bool</returns>
    public bool ImageWatermark(Dictionary<string, string> paramDict)
    {
        bool result = false;
        int readTimeout = 20;
        string url = "https://neutrinoapi.net:1234/image-watermark";
        try
        {
            using (FormUrlEncodedContent content = new FormUrlEncodedContent(paramDict))
            using (HttpRequestMessage request = new HttpRequestMessage())
            {
                request.Content = content;
                request.RequestUri = new Uri(url);
                request.Method = HttpMethod.Post;
                Client.Timeout = TimeSpan.FromSeconds(readTimeout);
                Client.DefaultRequestHeaders.Add("User-ID", UserId);
                Client.DefaultRequestHeaders.Add("API-Key", ApiKey);

                this.Filename = string.Format(
                    "{0}tmp_{1}.png",
                    Path.GetTempPath(),
                    Guid.NewGuid());

                using (Task<HttpResponseMessage> responseTask = Client.SendAsync(request, HttpCompletionOption.ResponseHeadersRead))
                using (Task<Stream> streamToReadFrom = responseTask.Result.Content.ReadAsStreamAsync())
                using (Stream streamToWriteTo = File.Open(this.Filename, FileMode.Create))
                {
                    HttpResponseMessage responseMsg = responseTask.Result;
                    streamToReadFrom.Result.CopyTo(streamToWriteTo);

                    if (responseMsg.IsSuccessStatusCode)
                    {
                        result = true;
                    }
                    else
                    {
                        streamToWriteTo.Close();
                        Console.Write("HttpError: {0}", responseMsg.ReasonPhrase);
                        Console.WriteLine(File.ReadAllText(this.Filename));
                    }
                }
            }
        }
        catch (IOException e)
        {
            Console.WriteLine(e.Message);
        }
        catch (FormatException e)
        {
            Console.WriteLine(e.Message);
        }
        catch (HttpRequestException e)
        {
            Console.WriteLine(e.Message);
        }
        catch (AggregateException e)
        {
            foreach (var ie in e.Flatten().InnerExceptions)
            {
                if (ie is HttpRequestException)
                {
                    Console.WriteLine(ie.InnerException.Message);
                }
                else
                {
                    Console.WriteLine(ie.Message);
                }
            }
        }

        return result;
    }
}
