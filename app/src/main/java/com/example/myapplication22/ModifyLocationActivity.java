package com.example.myapplication22;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ModifyLocationActivity extends AppCompatActivity {
    Spinner locationSpinner;
    int selectedLocationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_location);

        // Setting up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        Button goBackButton = findViewById(R.id.goBackButton);
        Button fetchDetailsButton = findViewById(R.id.fetchDetailsButton); // Initialize the button

        locationSpinner = findViewById(R.id.locationSpinner);
        fetchLocationOptions();

        fetchDetailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedItem = getIntent().getStringExtra("selectedItemName");
                fetchItemDetails(selectedItem, selectedLocationId);
            }
        });

        // Handle click on goBackButton
        goBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create intent to return to the main activity
                Intent intent = new Intent(ModifyLocationActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // Close the current activity
            }
        });
    }

    private void updateLocation(String selectedItem, String name, String batchno, String company, int quantity, String date, String quality, int newLocation) {
        // Define your API endpoint for updating location
        String url = "http://172.16.0.45:8080/items/" + selectedItem;
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        // Create JSON object to send in the request body
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("id", selectedItem);
            requestBody.put("name", name);
            requestBody.put("batchno", batchno);
            requestBody.put("company", company);
            requestBody.put("quantity", quantity);
            requestBody.put("date", currentDate);
            requestBody.put("quality", quality);
            requestBody.put("location", newLocation);
            Log.d("PUT", "id and local: " + requestBody );
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Create PUT request using JsonObjectRequest
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, url, requestBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(ModifyLocationActivity.this, "Location updated successfully", Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(ModifyLocationActivity.this, "Error updating location", Toast.LENGTH_SHORT).show();
                    }
                });

        // Add the request to the RequestQueue
        Volley.newRequestQueue(this).add(request);
    }

    private void fetchLocationOptions() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<String> locationNames = new ArrayList<>(); // Maintain a list of location names
                final List<Integer> locationIds = new ArrayList<>(); // Maintain a list of location IDs

                try {
                    URL url = new URL("http://172.16.0.45:8080/locations/");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");

                    int responseCode = conn.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        InputStream inputStream = conn.getInputStream();
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                        StringBuilder stringBuilder = new StringBuilder();
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            stringBuilder.append(line);
                        }
                        inputStream.close();

                        // Parse JSON response
                        JSONArray jsonArray = new JSONArray(stringBuilder.toString());
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject locationObject = jsonArray.getJSONObject(i);
                            String locationName = locationObject.getString("name");
                            int locationId = locationObject.getInt("id");
                            locationNames.add(locationName); // Add only the location name to the list
                            locationIds.add(locationId); // Add the ID to the separate list
                        }
                    } else {
                        // Handle HTTP error response
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ModifyLocationActivity.this, "Failed to fetch location options. HTTP error code: " + responseCode, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    conn.disconnect();
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ModifyLocationActivity.this, "Error fetching location options", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                // Update UI with location options
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Set up the spinner with location names
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(ModifyLocationActivity.this,
                                android.R.layout.simple_spinner_item, locationNames);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        locationSpinner.setAdapter(adapter);

                        // Add listener to log the ID of the selected location
                        locationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                                // Log the ID of the selected location
                                selectedLocationId = locationIds.get(position); // Get the ID from the separate list
                                Log.d("spinnerID", "Selected Location ID: " + selectedLocationId);
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parentView) {
                                // Do nothing
                            }
                        });
                    }
                });
            }
        }).start();
    }
    private void fetchItemDetails(String selectedItem, int selectedLocationId) {
        // Define your API endpoint for fetching item details
        String url = "http://172.16.0.45:8080/items/" + selectedItem;
        Log.d("Made it", "Made it here!: " + selectedLocationId);
        // Create GET request using JsonObjectRequest
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Parse the response and extract item details
                        try {
                            // Extract item details from the JSON response
                            int id = response.getInt("id");
                            String name = response.getString("name");
                            String batchno = response.getString("batchno");
                            String company = response.getString("company");
                            int quantity = response.getInt("quantity");
                            String date = response.getString("date");
                            String quality = response.getString("quality");

                            updateLocation(selectedItem, name, batchno, company, quantity, date, quality, selectedLocationId);


                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(ModifyLocationActivity.this, "Error parsing item details", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(ModifyLocationActivity.this, "Error fetching item details", Toast.LENGTH_SHORT).show();
                    }
                });

        // Add the request to the RequestQueue
        Volley.newRequestQueue(this).add(request);
    }
}
