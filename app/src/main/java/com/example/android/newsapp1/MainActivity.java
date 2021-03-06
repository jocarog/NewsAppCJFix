package com.example.android.newsapp1;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.example.android.newsapp1.R;
import com.example.android.newsapp1.News;

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
import java.text.SimpleDateFormat;


/**
 * Displays information about a fire.
 */
public class MainActivity extends AppCompatActivity

{

    /** Tag for the log messages */
    public static final String LOG_TAG = MainActivity.class.getSimpleName();

    /** URL to query the USGS dataset for fire information */
    private static final String USGS_REQUEST_URL =
            "https://www.yahoo.com/news/city-malibu-under-mandatory-evacuation-155321682.html";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Kick off an {@link AsyncTask} to perform the network request
        NewsAsyncTask task = new NewsAsyncTask();
        task.execute();
    }

    /**
     * Update the screen to display information from the given {@link News}.
     */
    private void updateUi(News news) {
        // Display the news title in the UI
        TextView titleTextView = (TextView) findViewById(R.id.title);
        titleTextView.setText(news.title);

        // Display the news date in the UI
        TextView dateTextView = (TextView) findViewById(R.id.date);
        dateTextView.setText(news.date);

        // Display whether or not there was a news alert about fires in the UI
        TextView authorTextView = (TextView) findViewById(R.id.author);
        authorTextView.setText(news.author);
    }

    /**
     * Returns a formatted date and time string for when the fire happened.
     private String getDateString(long timeInMilliseconds) {
     SimpleDateFormat formatter = new SimpleDateFormat("EEE, d MMM yyyy 'at' HH:mm:ss z");
     return formatter.format(timeInMilliseconds);
     }
     */
    /**
     * Return the display string for whether or not there was a news alert for a fire.
     */
    private String getFireAlertString(int newsAlert) {
        switch (newsAlert) {
            case 0:
                return getString(R.string.alert_no);
            case 1:
                return getString(R.string.alert_yes);
            default:
                return getString(R.string.alert_not_available);
        }
    }

    /**
     * {@link AsyncTask} to perform the network request on a background thread, and then
     * update the UI with the first earthquake in the response.
     */
    private class NewsAsyncTask extends AsyncTask<URL, Void, News> {

        @Override
        protected News doInBackground(URL... urls) {
            // Create URL object
            URL url = createUrl(USGS_REQUEST_URL);

            // Perform HTTP request to the URL and receive a JSON response back
            String jsonResponse = "";
            try {
                jsonResponse = makeHttpRequest(url);
            } catch (IOException e) {
                // TODO Handle the IOException
            }

            // Extract relevant fields from the JSON response and create an {@link Event} object
            News fires = extractFeatureFromJson(jsonResponse);

            // Return the {@link Event} object as the result fo the {@link FiresAsyncTask}
            return fires;
        }

        /**
         * Update the screen with the given earthquake (which was the result of the
         * {@link NewsAsyncTask}).
         */
        @Override
        protected void onPostExecute(News fires) {
            if (fires == null) {
                return;
            }

            updateUi(fires);
        }

        /**
         * Returns new URL object from the given string URL.
         */
        private URL createUrl(String stringUrl) {
            URL url = null;
            try {
                url = new URL(stringUrl);
            } catch (MalformedURLException exception) {
                Log.e(LOG_TAG, "Error with creating URL", exception);
                return null;
            }
            return url;
        }

        /**
         * Make an HTTP request to the given URL and return a String as the response.
         */
        private String makeHttpRequest(URL url) throws IOException {
            String jsonResponse = "";
            HttpURLConnection urlConnection = null;
            InputStream inputStream = null;
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setReadTimeout(10000 /* milliseconds */);
                urlConnection.setConnectTimeout(15000 /* milliseconds */);
                urlConnection.connect();
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } catch (IOException e) {
                // TODO: Handle the exception
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (inputStream != null) {
                    // function must handle java.io.IOException here
                    inputStream.close();
                }
            }
            return jsonResponse;
        }

        /**
         * Convert the {@link InputStream} into a String which contains the
         * whole JSON response from the server.
         */
        private String readFromStream(InputStream inputStream) throws IOException {
            StringBuilder output = new StringBuilder();
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
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
         * Return an {@link News} object by parsing out information
         * about the first fire from the input newsJSON string.
         */
        private News extractFeatureFromJson(String newsJSON) {
            try {
                JSONObject baseJsonResponse = new JSONObject(newsJSON);
                JSONArray featureArray = baseJsonResponse.getJSONArray("features");

                // If there are results in the features array
                if (featureArray.length() > 0) {
                    // Extract out the first feature (which is a fire)
                    JSONObject firstFeature = featureArray.getJSONObject(0);
                    JSONObject properties = firstFeature.getJSONObject("properties");

                    // Extract out the title, time, and author values
                    String title = properties.getString("title");
                    String date = properties.getString("date");
                    String author = properties.getString("author");

                    // Create a new {@link News} object
                    return new News(title, date, author);
                }
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Problem parsing the news JSON results", e);
            }
            return null;
        }
    }
}