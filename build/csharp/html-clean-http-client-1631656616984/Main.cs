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
        // Request data, see: https://www.neutrinoapi.com/api/html-clean
        Dictionary<string, string> paramDict = new Dictionary<string, string>();
        paramDict.Add("output-type", "plain-text"); // The level of sanitization
        paramDict.Add("content", "[Ф][Ö][©][ش𐹧][的] Some HTML with UTF chars...");
                                                              // The HTML content

        NeutrinoAPI neutrinoApi = new NeutrinoAPI();
        if (neutrinoApi.HtmlClean(paramDict))
        {
            Console.Write("Success: ");
            Console.WriteLine(neutrinoApi.Filename);
        }
        else
        {
            Console.WriteLine("API request failed!"); // you should handle this gracefully!
        }
    }

    /// <summary>Clean and sanitize untrusted HTML</summary>
    /// <param name="paramDict">The API request parameters</param>
    /// <returns>bool</returns>
    public bool HtmlClean(Dictionary<string, string> paramDict)
    {
        bool result = false;
        int readTimeout = 30;
        string url = "https://neutrinoapi.net/html-clean";
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
                    "{0}tmp_{1}.txt",
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
