package com.example.android.thisisnotfakenews.utilities;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.example.android.thisisnotfakenews.R;
import com.example.android.thisisnotfakenews.model.Article;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Reggie on 14/07/2017.
 * Utility class for dealing handle the get request and parse the JSON response to be stored in a
 * list of article objects.
 */

public class QueryUtils {

    private static final String LOG_TAG = QueryUtils.class.getSimpleName();

    private static final int RESPONSE_CODE = 200;
    private static final int TIMEOUT = 10000;
    private static final int CONNECT_TIMEOUT = 15000;
    private static final String REQUEST_METHOD = "GET";
    private static final String CHARSET_NAME = "UTF-8";
    private static final int SLEEP_MILLIS = 2000;

    private static Context context;
    private static URL requestUrl;

    // News API keys
    private static final String API_KEY_RESPONSE = "response";
    private static final String API_KEY_RESULTS = "results";
    private static final String API_KEY_SECTION = "sectionName";
    private static final String API_KEY_DATE = "webPublicationDate";
    private static final String API_KEY_TITLE = "webTitle";
    private static final String API_KEY_SUBTITLE = "trailText";
    private static final String API_KEY_THUMBNAIL_URL = "thumbnail";
    private static final String API_KEY_URL = "webUrl";
    private static final String API_KEY_AUTHOR = "byline";
    private static final String API_KEY_FIELDS = "fields";

    private QueryUtils(){
    }

    public static List<Article> getArticleData(Context context, String requestUrlString) {
        QueryUtils.context = context;

        requestUrl = createUrl(requestUrlString);

        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(requestUrl);
        } catch (IOException e) {
            Log.e(LOG_TAG, context.getString(R.string.http_request_exception), e);
        }

        // Getting the list of {@link Article}
        List<Article> articleList = extractDetailsFromJSON(jsonResponse);

        try {
            Thread.sleep(SLEEP_MILLIS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return articleList;
    }

    /**
     * This method checks the URL request String to ensure it is properly formed and returns a
     * valid URL if so, or null otherwise.
     */
    private static URL createUrl(String requestUrlString) {
        URL url = null;
        try {
            url = new URL(requestUrlString);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, context.getString(R.string.url_exception), e);
        }
        return url;
    }

    /**
     * Make A HTTP request to the given URL and return a JSON response in String form.
     * @param url       the url.
     * @return String   the JSON response in String form
     */
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        // If the URL is null, then return early
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(TIMEOUT);
            urlConnection.setConnectTimeout(CONNECT_TIMEOUT);
            urlConnection.setRequestMethod(REQUEST_METHOD);
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == RESPONSE_CODE) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, context.getString(R.string.response_code_exception,
                        urlConnection.getResponseCode()));
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, context.getString(R.string.json_exception), e);
        } catch(NullPointerException e){
            Log.e(LOG_TAG, context.getString(R.string.null_pointer_exception), e);
        } finally{
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     * Convert the {@link InputStream} into a String of the JSON response
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName(CHARSET_NAME));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }


    /**
     * Return a list of {@link Article} objects by parsing the JSON response.
     * @param jsonResponse  String version of the JSON response.
     * @return              List of {@link Article} objects
     */
    private static List<Article> extractDetailsFromJSON(String jsonResponse) {

        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(jsonResponse)) {
            return null;
        }

        List<Article> articleList = new ArrayList<>();

        try{
            JSONObject jsonReply = new JSONObject(jsonResponse);

            JSONObject jsonData = jsonReply.getJSONObject(API_KEY_RESPONSE);

            JSONArray articleArray;

            JSONObject currentArticle;

            JSONObject fieldsObject;

            // Variables used to create the article object.
            String title = "";
            String subtitle = "";
            String author = "Unknown";
            String section = "";
            String articleUrl = "";
            String thumbnailUrl = "";
            String date = "";

            if(jsonData.has(API_KEY_RESULTS)){
                articleArray = jsonData.getJSONArray(API_KEY_RESULTS);

                for (int i = 0; i < articleArray.length(); i++) {
                    currentArticle = articleArray.getJSONObject(i);

                    if (currentArticle.has(API_KEY_TITLE)) {
                        title = currentArticle.getString(API_KEY_TITLE);
                    }
                    if (currentArticle.has(API_KEY_SECTION)) {
                        section = currentArticle.getString(API_KEY_SECTION);
                    }
                    if (currentArticle.has(API_KEY_URL)) {
                        articleUrl = currentArticle.getString(API_KEY_URL);
                    }
                    if (currentArticle.has(API_KEY_DATE)) {
                        date = currentArticle.getString(API_KEY_DATE);
                    }
                    if (currentArticle.has(API_KEY_FIELDS)) {
                        fieldsObject = currentArticle.getJSONObject(API_KEY_FIELDS);

                        if(fieldsObject.has(API_KEY_AUTHOR)){
                            author = fieldsObject.getString(API_KEY_AUTHOR);
                        }
                        if(fieldsObject.has(API_KEY_SUBTITLE)){
                            subtitle = fieldsObject.getString(API_KEY_SUBTITLE);
                        }
                        if(fieldsObject.has(API_KEY_THUMBNAIL_URL)){
                            thumbnailUrl = fieldsObject.getString(API_KEY_THUMBNAIL_URL);
                        }
                    }
                    date = formatDate(date);

                    articleList.add(new Article(title, subtitle, section, articleUrl, thumbnailUrl, date, author));
                }
            }
        }catch(JSONException e){
            Log.e(LOG_TAG, context.getString(R.string.json_exception));
        }
        return articleList;
    }

    /**
     * Format date string from the current format, eg: 2017-05-11T08:03:47Z to a more readable
     * format for display. Specifically the format: Mon, 05 November 2017 08:03 AM
     *
     * @param currentDate the current date
     * @return the string
     */
    public static String formatDate(String currentDate) {
        String newDate = "";
        try {
            //transform current date format into Date object
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
            Date date = format.parse(currentDate);

            //change format -> we do this because the format() only takes a Date object
            format = new SimpleDateFormat("EEE, dd MMMM yyyy hh:mm a", Locale.ENGLISH);
            newDate = format.format(date);

        } catch (ParseException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, context.getString(R.string.parse_exception), e);
        }
        return newDate;
    }
}