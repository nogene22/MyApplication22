package com.example.myapplication22;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
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

public class AddStockActivity extends AppCompatActivity {

    EditText itemNameEditText, quantityEditText, batchNoEditText, companyEditText, dateEditText, qualityEditText;
    Button addButton;
    Spinner locationSpinner;

    int selectedLocationId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_stock);

        Button goBackButton = findViewById(R.id.goBackButton);
        goBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        itemNameEditText = findViewById(R.id.itemName);
        batchNoEditText = findViewById(R.id.batchNo);
        companyEditText = findViewById(R.id.company);
        quantityEditText = findViewById(R.id.itemQuantity);
        qualityEditText = findViewById(R.id.itemQuality);
        addButton = findViewById(R.id.addButton);
        locationSpinner = findViewById(R.id.locationSpinner);

        // Fetch location options from API
        fetchLocationOptions();

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get data from EditText fields
                String itemName = itemNameEditText.getText().toString();
                String batchNo = batchNoEditText.getText().toString();
                String company = companyEditText.getText().toString();
                String quantity = quantityEditText.getText().toString();
                int quantityInt = Integer.parseInt(quantity);
                // Set the current date
                String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                String itemQuality = qualityEditText.getText().toString();

                // Get selected location
                String selectedLocation = locationSpinner.getSelectedItem().toString();

                // Create JSON object
                JSONObject inventoryObject = new JSONObject();
                try {
                    inventoryObject.put("name", itemName);
                    inventoryObject.put("batchno", batchNo);
                    inventoryObject.put("company", company);
                    inventoryObject.put("quantity", quantityInt);
                    inventoryObject.put("date", currentDate);
                    inventoryObject.put("quality", itemQuality);
                    inventoryObject.put("location", selectedLocationId);// Add location to JSON
                    Log.d("MainActivity", "Json: " + inventoryObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // Send JSON data via HTTP POST request
                sendPostRequest(inventoryObject);
            }
        });
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
                                Toast.makeText(AddStockActivity.this, "Failed to fetch location options. HTTP error code: " + responseCode, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    conn.disconnect();
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(AddStockActivity.this, "Error fetching location options", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                // Update UI with location options
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Set up the spinner with location names
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(AddStockActivity.this,
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


    private void sendPostRequest(final JSONObject inventoryObject) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("http://172.16.0.45:8080/items/"); // Replace with your API URL
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    conn.setDoOutput(true);

                    DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                    os.writeBytes(inventoryObject.toString());

                    os.flush();
                    os.close();

                    int responseCode = conn.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_CREATED) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // Show success message
                                Toast.makeText(AddStockActivity.this, "Item added successfully", Toast.LENGTH_SHORT).show();

                                // Clear EditText fields
                                itemNameEditText.setText("");
                                batchNoEditText.setText("");
                                companyEditText.setText("");
                                quantityEditText.setText("");
                                qualityEditText.setText("");
                            }
                        });
                    } else {
                        // Handle error response
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(AddStockActivity.this, "Failed to add item. Please try again.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    conn.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(AddStockActivity.this, "Error connecting to server", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }
}

