package ca.judacribz.gainzassist.activity_how_to_videos;

import android.os.AsyncTask;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class SearchVideosTask extends AsyncTask<String, Void, JSONObject> {

    // Interfaces
    // --------------------------------------------------------------------------------------------
    private YouTubeSearchObserver youTubeSearchObserver;

    public interface YouTubeSearchObserver {
        void videoSearchDataReceived(ArrayList<String> videoIds, ArrayList<String> videoTitles);
    }

    void setYouTubeSearchObserver(YouTubeSearchObserver youTubeSearchObserver) {
        this.youTubeSearchObserver = youTubeSearchObserver;
    }
    // --------------------------------------------------------------------------------------------


    // AsyncTask<String, Void, JSONObject> Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected JSONObject doInBackground(String... urls) {
        HttpsURLConnection conn = null;


        JSONObject jsonData = null;
        StringBuilder sb = new StringBuilder();

        // Get the JSON format data from url
        try {
            conn = (HttpsURLConnection) (new URL(urls[0])).openConnection();
            conn.connect();

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line.concat("\n"));
            }
            br.close();

            jsonData = new JSONObject(sb.toString());


        } catch (IOException | JSONException ex) {
            ex.printStackTrace();

        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        return jsonData;
    }

    @Override
    protected void onPostExecute(JSONObject jsonData) {

        // Get the video ids from the JSON data retrieved above
        if (jsonData != null) {
            try {
                ArrayList<String> videoIds = new ArrayList<>();
                ArrayList<String> videoTitles = new ArrayList<>();

                JSONArray items = jsonData.getJSONArray("items");

                for (int i = 0; i < items.length(); i++) {
                    videoIds.add(((JSONObject) items.get(i))
                            .getJSONObject("id")
                            .getString("videoId"));

                    videoTitles.add(((JSONObject) items.get(i))
                            .getJSONObject("snippet")
                            .getString("title"));
                }

                youTubeSearchObserver.videoSearchDataReceived(videoIds, videoTitles);
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
    }
    //AsyncTask<String//Void//JSONObject>//Override////////////////////////////////////////////////
}
