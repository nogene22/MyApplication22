package com.example.myapplication22;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
    Button sellStockButton;
    Button adjustStockButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        productIdEditText = findViewById(R.id.productIdEditText);
        fetchButton = findViewById(R.id.fetchButton);
        addStockButton = findViewById(R.id.addStockButton);
        moveStockButton = findViewById(R.id.moveStockButton);
        sellStockButton = findViewById(R.id.sellStockButton);
        adjustStockButton = findViewById(R.id.adjustStockButton);

        fetchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String productId = productIdEditText.getText().toString();
                fetchDataFromApi(productId);
                Toast.makeText(MainActivity.this, "Product ID: " + productId, Toast.LENGTH_SHORT).show();
            }
        });

        addStockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AddStockActivity.class));
            }
        });

        moveStockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MoveStockActivity.class));
            }
        });

        sellStockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SellStockActivity.class));
            }
        });

        adjustStockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AdjustStockActivity.class));
            }
        });
    }


    private void fetchDataFromApi(final String productId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;
                String jsonData = null;
                try {
                    // Construct the URL
                    URL url = new URL("http://172.16.0.45:8000/items/" + productId);
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

                // Parse JSON data
                if (jsonData != null) {
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

                            // Do something with the retrieved data
                            Log.d("MainActivity", "ID: " + id + ", Name: " + name + ", Company: " + company + ", Quantity: " + quantity + ", Date: " + date + ", Quality: " + quality);
                        }
                    } catch (JSONException e) {
                        Log.e("MainActivity", "Error parsing JSON", e);
                    }
                }
            }
        }).start();
    }
}
