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
        // Request data, see: https://www.neutrinoapi.com/api/html-render
        Dictionary<string, string> paramDict = new Dictionary<string, string>();
        paramDict.Add("margin", "0"); // The document margin (in mm)
        paramDict.Add("image-width", "1024"); // If rendering to an image format (PNG or JPG) use
                                              // this image width (in pixels)
        paramDict.Add("format", "PDF"); // Which format to output
        paramDict.Add("zoom", "1"); // Set the zoom factor when rendering the page (2.0 for double
                                    // size
        paramDict.Add("content", "[Ф][Ö][©][ش𐹧][的] Some HTML with UTF chars...");
                                                              // The HTML content
        paramDict.Add("timeout", "300"); // Timeout in seconds
        paramDict.Add("margin-right", "0"); // The document right margin (in mm)
        paramDict.Add("grayscale", "false"); // Render the final document in grayscale
        paramDict.Add("margin-left", "0"); // The document left margin (in mm)
        paramDict.Add("page-size", "A4"); // Set the document page size
        paramDict.Add("delay", "0"); // Number of seconds to wait before rendering the page (can be
                                     // useful for pages with animations etc)
        paramDict.Add("ignore-certificate-errors", "false"); // Ignore any TLS/SSL certificate
                                                             // errors
        paramDict.Add("margin-top", "0"); // The document top margin (in mm)
        paramDict.Add("margin-bottom", "0"); // The document bottom margin (in mm)
        paramDict.Add("landscape", "false"); // Set the document to landscape orientation

        NeutrinoAPI neutrinoApi = new NeutrinoAPI();
        if (neutrinoApi.HtmlRender(paramDict))
        {
            Console.Write("Success: ");
            Console.WriteLine(neutrinoApi.Filename);
        }
        else
        {
            Console.WriteLine("API request failed!"); // you should handle this gracefully!
        }
    }

    /// <summary>Render HTML content to PDF, JPG or PNG</summary>
    /// <param name="paramDict">The API request parameters</param>
    /// <returns>bool</returns>
    public bool HtmlRender(Dictionary<string, string> paramDict)
    {
        bool result = false;
        int readTimeout = 300;
        string url = "https://neutrinoapi.net/html-render";
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
                    "{0}tmp_{1}.pdf",
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
