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
        // Request data, see: https://www.neutrinoapi.com/api/ip-info
        Dictionary<string, string> paramDict = new Dictionary<string, string>();
        paramDict.Add("ip", "1.1.1.1"); // IPv4 or IPv6 address
        paramDict.Add("reverse-lookup", "false"); // Do a reverse DNS (PTR) lookup

        NeutrinoAPI neutrinoApi = new NeutrinoAPI();
        if (neutrinoApi.IpInfo(paramDict))
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

    /// <summary>Get location information about an IP address and do reverse DNS (PTR) lookups</summary>
    /// <param name="paramDict">The API request parameters</param>
    /// <returns>bool</returns>
    public bool IpInfo(Dictionary<string, string> paramDict)
    {
        bool result = false;
        int readTimeout = 10;
        string url = "https://neutrinoapi.net:1234/ip-info";
        string paramStr = new FormUrlEncodedContent(paramDict).ReadAsStringAsync().Result;
        url = string.Format("{0}?{1}", url, paramStr);

        try
        {
            Client.Timeout = TimeSpan.FromSeconds(readTimeout);
            Client.DefaultRequestHeaders.Add("User-ID", UserId);
            Client.DefaultRequestHeaders.Add("API-Key", ApiKey);

            using (Task<HttpResponseMessage> responseTask = Client.GetAsync(url, HttpCompletionOption.ResponseHeadersRead))
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
                    Console.WriteLine("HttpError: {0} {1}", (int)responseMsg.StatusCode,  responseMsg.ReasonPhrase);
                    Console.WriteLine(jsonStr);
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
