package com.example.myapplication22;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;

import android.widget.Button;
import android.widget.ListView;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class allStock extends AppCompatActivity {

    private ListView dataListView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_stock);

        dataListView = findViewById(R.id.dataListView);

        // Setting up the Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");


        Button goBackButton = findViewById(R.id.goBackButton);
        goBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        // Fetch and display data
        fetchDataAndDisplay();
    }


    private void fetchDataAndDisplay() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;
                String jsonData = null;
                try {
                    // Construct the URL
                    URL url = new URL("http://172.16.0.45:8080/items/");
                    // Open connection
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();

                    // Read the input stream into a String
                    StringBuilder buffer = new StringBuilder();
                    reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line).append("\n");
                    }
                    if (buffer.length() == 0) {
                        // Stream was empty. No point in parsing.
                        return;
                    }
                    jsonData = buffer.toString();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    // Close the HttpURLConnection and BufferedReader
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (final IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                // Parse JSON data and display
                if (jsonData != null) {
                    final List<String> dataList = new ArrayList<>();
                    try {
                        JSONArray jsonArray = new JSONArray(jsonData);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            int id = jsonObject.getInt("id");
                            String name = jsonObject.getString("name");
                            String company = jsonObject.getString("company");
                            int quantity = jsonObject.getInt("quantity");
                            String date = jsonObject.getString("date");
                            String quality = jsonObject.getString("quality");
                            String locationId = jsonObject.getString("location");

                            String locationName = getLocationName(locationId);


                            // Check if quantity is greater than 0
                            if (quantity > 0) {
                                // Construct the data string
                                String dataString = "ID: " + id + "\n" +
                                        "Name: " + name + "\n" +
                                        "Company: " + company + "\n" +
                                        "Quantity: " + quantity + "\n" +
                                        "Date: " + date + "\n" +
                                        "Quality: " + quality + "\n" +
                                        "Location: " + locationName + "\n";


                                // Add data to the list
                                dataList.add(dataString);
                            }
                        }

                        // Update UI on the main thread
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // Display data in ListView using ArrayAdapter
                                ArrayAdapter<String> adapter = new ArrayAdapter<>(allStock.this,
                                        android.R.layout.simple_list_item_1, dataList);
                                dataListView.setAdapter(adapter);
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();
    }

    private String getLocationName(String locationId) {
        String locationName = "";
        try {
            // Construct the URL
            URL url = new URL("http://172.16.0.45:8080/locations/" + locationId);
            // Open connection
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            // Read the input stream into a String
            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
            // Parse JSON response
            JSONObject jsonObject = new JSONObject(stringBuilder.toString());
            locationName = jsonObject.getString("name");
            // Close connection
            urlConnection.disconnect();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return locationName;
    }
}
