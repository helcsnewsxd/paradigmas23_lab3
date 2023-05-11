package httpRequest;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class HTTPRequester {
    public String getFeed(String urlFeed, String urlType) throws MalformedURLException, IOException {
        if (urlType.equals("rss"))
            return getFeedRss(urlFeed);
        else if (urlType.equals("reddit"))
            return getFeedReedit(urlFeed);
        else
            return null;
    }

    private String getFeedRss(String urlFeed) throws MalformedURLException, IOException {
        String feedRss = getResponse(urlFeed);
        return feedRss;
    }

    // TO COMPLETE
    private static String getFeedReedit(String urlFeed) throws MalformedURLException, IOException{
        String feedReeditJson = getResponse(urlFeed);
        return feedReeditJson;
    }

    private static String getResponse(String url) throws MalformedURLException, IOException {
        StringBuffer feedRssXml = new StringBuffer();

        URL urlObj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();
        con.setRequestMethod("GET");
        con.setInstanceFollowRedirects(true);

        int status = con.getResponseCode();

        BufferedReader streamReader;

        if (status > 299) {
            streamReader = new BufferedReader(new InputStreamReader(con.getErrorStream()));
        } else {
            streamReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
        }
        String inputLine;

        while ((inputLine = streamReader.readLine()) != null) {
            feedRssXml.append(inputLine);
        }

        return feedRssXml.toString();

    }
}