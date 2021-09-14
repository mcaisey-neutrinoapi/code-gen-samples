// <copyright file="NeutrinoAPI.cs" company="NeutrinoAPI">
//     Copyright NeutrinoAPI
// </copyright>
using System;
using System.Collections.Generic;
using System.IO;
using System.Net.Http;
using System.Text.Json;
using System.Threading.Tasks;

/// <summary>NeutrinoAPI</summary>
public class NeutrinoAPI
{
    // API credentials
    private const string UserId = "my-user-id";
    private const string ApiKey = "my-api-key";
    private static readonly HttpClient Client = new HttpClient();

    /// <summary>Gets result as JSON object.</summary>
    public JsonElement JsonElement { get; private set; } = default(JsonElement);

    /// <summary>Main</summary>
    public static void Main()
    {
        // Request data, see: https://www.neutrinoapi.com/api/browser-bot
        Dictionary<string, string> paramDict = new Dictionary<string, string>();
        paramDict.Add("delay", "3"); // Delay in seconds to wait before capturing any page data
        paramDict.Add("ignore-certificate-errors", "false"); // Ignore any TLS/SSL certificate
                                                             // errors and load the page anyway
        paramDict.Add("selector", ".header-link"); // Extract content from the page DOM using this
                                                   // selector
        paramDict.Add("exec[1]", "\"Hello\".toUpperCase()");
        paramDict.Add("url", "https://www.neutrinoapi.com/"); // The URL to load
        paramDict.Add("timeout", "30"); // Timeout in seconds
        paramDict.Add("exec[0]", "document.getElementsByTagName('title')[0].innerText");
                                                      // Execute JavaScript on the page

        NeutrinoAPI neutrinoApi = new NeutrinoAPI();
        if (neutrinoApi.BrowserBot(paramDict))
        {
            Console.WriteLine("Success: ");
            foreach (JsonProperty element in neutrinoApi.JsonElement.EnumerateObject())
            {
                Console.WriteLine(element);
            }
        }
        else
        {
            Console.WriteLine("API request failed!"); // you should handle this gracefully!
        }
    }

    /// <summary>Browser bot can extract content, interact with keyboard and mouse events, and execute JavaScript on a website</summary>
    /// <param name="paramDict">The API request parameters</param>
    /// <returns>bool</returns>
    public bool BrowserBot(Dictionary<string, string> paramDict)
    {
        bool result = false;
        int readTimeout = 300;
        string url = "https://neutrinoapi.net/browser-bot";

        try
        {
            using (FormUrlEncodedContent content = new FormUrlEncodedContent(paramDict))
            {
                Client.Timeout = TimeSpan.FromSeconds(readTimeout);
                Client.DefaultRequestHeaders.Add("User-ID", UserId);
                Client.DefaultRequestHeaders.Add("API-Key", ApiKey);

                using (Task<HttpResponseMessage> responseTask = Client.PostAsync(url, content))
                {
                    HttpResponseMessage responseMsg = responseTask.Result;
                    string jsonStr = responseTask.Result.Content.ReadAsStringAsync().Result;
                    if (responseMsg.IsSuccessStatusCode)
                    {
                        this.JsonElement = JsonSerializer.Deserialize<JsonElement>(jsonStr);
                        result = true;
                    }
                    else
                    {
                        Console.WriteLine("HttpError: {0} {1}", (int)responseMsg.StatusCode, responseMsg.ReasonPhrase);
                        Console.WriteLine(jsonStr);
                    }
                }
            }
        }
        catch (Exception ex)
        {
            Console.WriteLine("Exception: {0}", ex.Message);
        }

        return result;
    }
}