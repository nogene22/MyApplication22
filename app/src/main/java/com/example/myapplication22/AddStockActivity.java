package com.example.myapplication22;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

public class AddStockActivity extends AppCompatActivity {

    EditText idEditText, nameEditText, companyEditText, quantityEditText, dateEditText, qualityEditText;
    Button addButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_stock);

        idEditText = findViewById(R.id.idEditText);
        nameEditText = findViewById(R.id.nameEditText);
        companyEditText = findViewById(R.id.companyEditText);
        quantityEditText = findViewById(R.id.quantityEditText);
        dateEditText = findViewById(R.id.dateEditText);
        qualityEditText = findViewById(R.id.qualityEditText);
        addButton = findViewById(R.id.addButton);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get data from EditText fields
                String id = idEditText.getText().toString();
                String name = nameEditText.getText().toString();
                String company = companyEditText.getText().toString();
                String quantity = quantityEditText.getText().toString();
                String date = dateEditText.getText().toString();
                String quality = qualityEditText.getText().toString();

                // Create JSON object
                JSONObject stockObject = new JSONObject();
                try {
                    stockObject.put("id", id);
                    stockObject.put("name", name);
                    stockObject.put("company", company);
                    stockObject.put("quantity", quantity);
                    stockObject.put("date", date);
                    stockObject.put("quality", quality);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // Send JSON data via HTTP POST request
                sendPostRequest(stockObject);
            }
        });

        // Setup go back button
        Button goBackButton = findViewById(R.id.goBackButton);
        goBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create intent to return to the main activity
                Intent intent = new Intent(AddStockActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // Close the current activity
            }
        });
    }

    private void sendPostRequest(final JSONObject stockObject) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("http://172.16.0.45:8000/items");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setDoOutput(true);

                    DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                    os.writeBytes(stockObject.toString());

                    os.flush();
                    os.close();

                    int responseCode = conn.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_CREATED) {
                        // Successfully added stock
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(AddStockActivity.this, "Stock added successfully", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        // Error occurred while adding stock
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(AddStockActivity.this, "Failed to add stock", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    conn.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
