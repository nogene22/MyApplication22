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
import android.widget.TextView;
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

public class ModifyQuantityActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_quantity);

        // Setting up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");


        Button goBackButton = findViewById(R.id.goBackButton);
        Button fetchDetailsButton = findViewById(R.id.fetchDetailsButton); // Initialize the button
        EditText quantityEditText = findViewById(R.id.quantityEditText); // Initialize the EditText

        String selectedItem = getIntent().getStringExtra("selectedItemName");

        fetchDetailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Retrieve the quantity entered by the user
                int selectedQuantity = Integer.parseInt(quantityEditText.getText().toString());

                // Call fetchItemDetails with selected quantity
                fetchItemDetails(selectedItem, selectedQuantity);
                Intent intent = new Intent(ModifyQuantityActivity.this, AdjustStockActivity.class);
                startActivity(intent);
            }
        });

        // Handle click on goBackButton
        goBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create intent to return to the main activity
                Intent intent = new Intent(ModifyQuantityActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // Close the current activity
            }
        });
    }


    private void updateQuantity(String selectedItem, String name, String batchno, String company, int selectedQuantity, String date, String quality, int location) {
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
            requestBody.put("quantity", selectedQuantity);
            requestBody.put("date", currentDate);
            requestBody.put("quality", quality);
            requestBody.put("location", location);
            Log.d("PUT", "id and local: " + requestBody );
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Create PUT request using JsonObjectRequest
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, url, requestBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(ModifyQuantityActivity.this, "Quantity updated successfully", Toast.LENGTH_SHORT).show();

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(ModifyQuantityActivity.this, "Error updating location", Toast.LENGTH_SHORT).show();
                    }
                });

        // Add the request to the RequestQueue
        Volley.newRequestQueue(this).add(request);
    }

    private void fetchItemDetails(String selectedItem, int selectedQuantity) {
        // Define your API endpoint for fetching item details
        String url = "http://172.16.0.45:8080/items/" + selectedItem;
        Log.d("Made it", "Made it here!: " + selectedQuantity);
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
                            int location = response.getInt("location"); // may need to change to string if bad request error

                            updateQuantity(selectedItem, name, batchno, company, selectedQuantity, date, quality,location);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(ModifyQuantityActivity.this, "Error parsing item details", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(ModifyQuantityActivity.this, "Error fetching item details", Toast.LENGTH_SHORT).show();
                    }
                });

        // Add the request to the RequestQueue
        Volley.newRequestQueue(this).add(request);
    }
}
