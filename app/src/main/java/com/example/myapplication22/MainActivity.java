package com.example.myapplication22;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    EditText productIdEditText;
    Button fetchButton;
    Button addStockButton;
    Button moveStockButton;
    Button logOutButton;
    Button adjustStockButton;
    Button showAllButton; // New button for showing all information

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        productIdEditText = findViewById(R.id.productIdEditText);
        fetchButton = findViewById(R.id.fetchButton);
        fetchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String productId = productIdEditText.getText().toString();
                fetchDataFromApi(productId);
            }
        });

        addStockButton = findViewById(R.id.addStockButton);
        addStockButton.setOnClickListener(view -> onButtonClicked(AddStockActivity.class));

        moveStockButton = findViewById(R.id.moveStockButton);
        moveStockButton.setOnClickListener(view -> onButtonClicked(MoveStockActivity.class));

        adjustStockButton = findViewById(R.id.adjustStockButton);
        adjustStockButton.setOnClickListener(view -> onButtonClicked(AdjustStockActivity.class));

        showAllButton = findViewById(R.id.showAllButton);
        showAllButton.setOnClickListener(view -> onButtonClicked(allStock.class));

        logOutButton = findViewById(R.id.endProcess);
        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the current activity
                finish();

                // login page
                Intent intent = new Intent(MainActivity.this, LoginPage.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });




    }

    private void onButtonClicked(Class<?> destinationClass) {
        Log.d("MainActivity", "Destination class: " + destinationClass.getSimpleName());
        Intent intent = new Intent(MainActivity.this, destinationClass);
        Log.d("MainActivity", "intent is: " + intent);
        startActivity(intent);
    }


    private void fetchDataFromApi(final String productId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;
                String jsonData = null;
                try {
                    // Construct the URL with productId
                    String apiUrl = "http://172.16.0.45:8080/items/" + productId;
                    URL url = new URL(apiUrl);

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
                    // Display toast message if connection fails
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Item not in Stock", Toast.LENGTH_SHORT).show();
                        }
                    });
                    Log.e("MainActivity", "Error ", e);
                } finally {
                    // Close the HttpURLConnection and BufferedReader
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (final IOException e) {
                            Log.e("MainActivity", "Error closing stream", e);
                        }
                    }
                }

                // Parse JSON data and append each section on a new line
                if (jsonData != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(jsonData);
                        int id = jsonObject.getInt("id");
                        String name = jsonObject.getString("name");
                        String company = jsonObject.getString("company");
                        int quantity = jsonObject.getInt("quantity");
                        String date = jsonObject.getString("date");
                        String quality = jsonObject.getString("quality");

                        // Create data string
                        final String dataToShow = "ID: " + id + "\n" +
                                "Name: " + name + "\n" +
                                "Company: " + company + "\n" +
                                "Quantity: " + quantity + "\n" +
                                "Date: " + date + "\n" +
                                "Quality: " + quality;

                        // Display data in a dialog
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setTitle("Product Details");
                                builder.setMessage(dataToShow);
                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                                AlertDialog dialog = builder.create();
                                dialog.show();
                            }
                        });

                    } catch (JSONException e) {
                        Log.e("MainActivity", "Error parsing JSON", e);
                    }
                }
            }
        }).start();
    }
}
