package org.example.meta2;

import googol.queue.URLData;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

/**
 * Class to search Wikipedia for a given query and return the top search result
 */
public class WikipediaSearch {

    private static final String USER_AGENT = "Mozilla/5.0";

    /**
     * Search Wikipedia for the given query and return the top search result
     * @param query The query to search for
     * @return The top search result
     */
    public URLData topWikiSearch(String query) {
        query = query.replace(" ", "%20");
        try {
            JSONArray searchResults = searchWikipedia(query);
            if(searchResults.length() > 0) { // Check if the JSONArray is not empty
                JSONObject result = searchResults.getJSONObject(0);
                String title = result.getString("title");
                JSONObject summary = getPageSummary(title);


                URLData urlData = new URLData(summary.getJSONObject("content_urls").getJSONObject("desktop").getString("page"), "Top Wikipedia Article: " + title, new String[]{summary.optString("extract", "No summary available")});
                System.out.println("URL: " + urlData.getUrl());
                System.out.println("Title: " + urlData.getTitle());
                System.out.println("Content: " + urlData.getContent()[0]);

                return urlData;
            } else {
                System.out.println("No search results found for the query: " + query);
            }
        } catch (Exception e) {
            System.out.println("Failed attempting to search Wikipedia");
            e.printStackTrace();
        }
        return null;
    }


    /**
     * Search Wikipedia for the given query
     * @param query The query to search for
     * @return The search results
     * @throws Exception If the search fails
     */
    private JSONArray searchWikipedia(String query) throws Exception {
        String url = "https://en.wikipedia.org/w/api.php?action=query&list=search&srsearch=" + query + "&format=json";
        String response = sendGET(url);
        JSONObject jsonResponse = new JSONObject(response);
        return jsonResponse.getJSONObject("query").getJSONArray("search");
    }

    /**
     * Get the summary of a Wikipedia page
     * @param title The title of the page
     * @return The summary of the page
     * @throws Exception If the request fails
     */
    private JSONObject getPageSummary(String title) throws Exception {
        String url = "https://en.wikipedia.org/api/rest_v1/page/summary/" + title.replace(" ", "_");
        String response = sendGET(url);
        return new JSONObject(response);
    }

    /**
     * Send a GET request to the given URL
     * @param urlString The URL to send the request to
     * @return The response from the server
     * @throws Exception If the request fails
     */
    private String sendGET(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", USER_AGENT);

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();
        } else {
            throw new RuntimeException("GET request failed with response code: " + responseCode);
        }
    }
}