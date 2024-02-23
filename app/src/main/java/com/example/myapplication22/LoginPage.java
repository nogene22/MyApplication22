package com.example.myapplication22;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class LoginPage extends AppCompatActivity {

    private EditText editTextUsername, editTextPassword;
    private Button buttonLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Setting up the Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
         //Ensuring the action bar is not null and setting the title
        Objects.requireNonNull(getSupportActionBar()).setTitle("");

        // Initialize UI elements
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);

        // Set a click listener for the login button
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Retrieve entered username and password
                String username = editTextUsername.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();

                // Perform login via API
                performLogin(username, password);
            }
        });
    }

    private void performLogin(final String username, final String password) {
        String url = "http://172.16.0.45:8080/users/login";
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("username", username); // Change to "username"
            jsonBody.put("password", password);
            Log.d("LoginRequest", "Request payload: " + jsonBody.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                response -> {
                    boolean success = response.has("message"); // Check for presence of "message"
                    if (success) {
                        Toast.makeText(LoginPage.this, "Login successful", Toast.LENGTH_SHORT).show();
                        // Navigate to Homepage
                        Intent intent = new Intent(LoginPage.this, MainActivity.class);
                        startActivity(intent);
                        finish(); // Optionally finish LoginPage
                    } else {
                        Toast.makeText(LoginPage.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(LoginPage.this, "Invalid username or password: " , Toast.LENGTH_SHORT).show();
                }
        );

        // Add the request to the RequestQueue.
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }
}

