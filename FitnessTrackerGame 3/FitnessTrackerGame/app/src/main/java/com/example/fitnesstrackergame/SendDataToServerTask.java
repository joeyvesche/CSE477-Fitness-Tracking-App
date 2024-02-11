package com.example.fitnesstrackergame;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class SendDataToServerTask extends AsyncTask<String, Void, String> {
    @Override
    protected String doInBackground(String... params) {
        try {
            // The URL for the PHP script
            URL url = new URL("http://10.0.2.2/FitnessTracker.php");

            // Create a connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");

            // Enable input/output streams
            connection.setDoOutput(true);
            connection.setDoInput(true);

            // Get login info and game data from params
            String username = URLEncoder.encode(params[0], "UTF-8");
            String password = URLEncoder.encode(params[1], "UTF-8");
            String cash = URLEncoder.encode(params[2], "UTF-8");

            // Build the POST parameters
            String postData = "username=" + username + "&password=" + password + "&cash=" + cash;

            // Write data to the connection
            OutputStream os = connection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(postData);
            writer.flush();
            writer.close();
            os.close();

            // Get the server response
            InputStream is = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            is.close();

            // Disconnect
            connection.disconnect();

            // Return the server response
            return response.toString();
        } catch (Exception e) {
            Log.e("Error", "Error sending data to server: " + e.getMessage());
            return null;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        // Handle the server response (e.g., show a Toast)
        if (result != null) {
            Log.d("ServerResponse", "Response from server: " + result);
        } else {
            Log.e( "Error from server:", "Error sending data to server");
        }
    }
}