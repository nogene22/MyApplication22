package com.example.myapplication22;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddStockActivity extends AppCompatActivity {

    EditText itemNameEditText, itemDescriptionEditText, quantityEditText, batchNumberEditText;
    Button addButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_stock);

        itemNameEditText = findViewById(R.id.itemName);
        itemDescriptionEditText = findViewById(R.id.itemDescription);
        quantityEditText = findViewById(R.id.quantityEditText);
        batchNumberEditText = findViewById(R.id.batchNumber);
        addButton = findViewById(R.id.addButton);

        // Setting up the Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Add Inventory Item");

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get data from EditText fields
                String itemName = itemNameEditText.getText().toString();
                String itemDescription = itemDescriptionEditText.getText().toString();
                String quantity = quantityEditText.getText().toString();
                String batchNumber = batchNumberEditText.getText().toString();

                // Automatically set the current date and time for DateReceived
                String dateReceived = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(new Date());

                // Create JSON object
                JSONObject inventoryObject = new JSONObject();
                try {
                    inventoryObject.put("ItemName", itemName);
                    inventoryObject.put("ItemDescription", itemDescription);
                    inventoryObject.put("Quantity", quantity);
                    inventoryObject.put("DateReceived", dateReceived);
                    inventoryObject.put("BatchNumber", batchNumber);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // Send JSON data via HTTP POST request
                sendPostRequest(inventoryObject);
            }
        });

        Button goBackButton = findViewById(R.id.goBackButton);
        goBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void sendPostRequest(final JSONObject inventoryObject) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("http://172.16.0.45:8000/items");
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
                                // Clear the form fields
                                itemNameEditText.setText("");
                                itemDescriptionEditText.setText("");
                                quantityEditText.setText("");
                                batchNumberEditText.setText("");

                                // Show success message
                                try {
                                    Toast.makeText(AddStockActivity.this, "Item " + inventoryObject.getString("ItemName") + " has been added", Toast.LENGTH_SHORT).show();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Toast.makeText(AddStockActivity.this, "Item added successfully", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        // Handle generic error or parse server response for specific error details if available
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(AddStockActivity.this, "Failed to add inventory item. Please check all fields.", Toast.LENGTH_SHORT).show();
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