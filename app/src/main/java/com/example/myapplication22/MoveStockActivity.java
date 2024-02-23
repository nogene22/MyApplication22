package com.example.myapplication22;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import androidx.appcompat.widget.SearchView;

public class MoveStockActivity extends AppCompatActivity {

    private ArrayList<String> itemList; // List to store all item names
    private ArrayList<String> itemListIds; // list to store item ids
    private ArrayList<String> filteredItemList; // List to store filtered item names
    private ArrayAdapter<String> adapter; // Adapter for ListView

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_move_stock);

        // Setting up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        // Initialize the list view and adapter
        ListView listView = findViewById(R.id.listView);
        itemList = new ArrayList<>();
        itemListIds = new ArrayList<>();
        filteredItemList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, filteredItemList);
        listView.setAdapter(adapter);

        // Fetch items from the API
        fetchItems();

        // Handle item click
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the selected item and its object ID from the filtered list
                String selectedItemName = filteredItemList.get(position); // Retrieve from filteredItemList
                Log.d("MoveStock", "Selected Item: " + selectedItemName);

                // Open a new activity to modify the location of the selected item
                Intent intent = new Intent(MoveStockActivity.this, ModifyLocationActivity.class);
                intent.putExtra("selectedItemName", selectedItemName);
                //intent.putExtra("selectedItemId", selectedItemId);

                startActivity(intent);
            }
        });

        // Initialize search view
        SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Filter the list based on the search query
                filterItems(newText);
                return true;
            }
        });

        Button goBackButton = findViewById(R.id.goBackButton);
        goBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create intent to return to the main activity
                Intent intent = new Intent(MoveStockActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // Close the current activity
            }
        });
    }

    private void fetchItems() {
        String url = "http://172.16.0.45:8080/items/";

        // Initialize a new JsonArrayRequest instance
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        // Process the JSON array response
                        Log.d("MoveStock", "Response is: " + response);
                        // Initialize lists to store item names and IDs
                        ArrayList<String> itemIds = new ArrayList<>();

                        // Iterate through the JSON array
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                // Extract item ID from each JSON object
                                JSONObject item = response.getJSONObject(i);
                                String itemId = item.getString("id");
                                itemIds.add(itemId);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.e("FetchItems", "Error parsing JSON response: " + e.getMessage());
                            }
                        }
                        // Update the itemListIds and notify the adapter
                        itemListIds.clear();
                        itemListIds.addAll(itemIds);
                        adapter.notifyDataSetChanged();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle errors
                        Log.e("FetchItems", "Error fetching items: " + error.getMessage());
                        Toast.makeText(MoveStockActivity.this, "Error fetching items", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Add the request to the RequestQueue
        Volley.newRequestQueue(this).add(jsonArrayRequest);
    }

    private void filterItems(String query) {
        // Clear the filtered list
        filteredItemList.clear();

        // If the search query is empty, display all items
        if (query.isEmpty()) {
            filteredItemList.addAll(itemListIds);
        } else {
            // Filter items based on the search query
            for (String itemId : itemListIds) {
                if (itemId.toLowerCase().contains(query.toLowerCase())) {
                    filteredItemList.add(itemId);
                }
            }
        }

        // Notify the adapter that the list has been updated
        adapter.notifyDataSetChanged();
    }
}
