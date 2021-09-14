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
        // Request data, see: https://www.neutrinoapi.com/api/bad-word-filter
        Dictionary<string, string> paramDict = new Dictionary<string, string>();
        paramDict.Add("catalog", "strict"); // Which catalog of bad words to use
        paramDict.Add("content", @"
multi
line
content
boob");
            // The content to scan

        NeutrinoAPI neutrinoApi = new NeutrinoAPI();
        if (neutrinoApi.BadWordFilter(paramDict))
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

    /// <summary>Detect bad words, swear words and profanity in a given text</summary>
    /// <param name="paramDict">The API request parameters</param>
    /// <returns>bool</returns>
    public bool BadWordFilter(Dictionary<string, string> paramDict)
    {
        bool result = false;
        int readTimeout = 30;
        string url = "https://neutrinoapi.net/bad-word-filter";

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