package com.example.hydrostop;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class UpdateUserActivity extends AppCompatActivity {

    private EditText etFirstName, etLastName, etAge, etPhone, etShowers;
    private Button btnSave;
    private SharedPreferences sharedPreferences;
    private RequestQueue requestQueue;
    private String accessToken, userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_user);

        sharedPreferences = getSharedPreferences("HydroStopPrefs", MODE_PRIVATE);
        requestQueue = Volley.newRequestQueue(this);
        accessToken = sharedPreferences.getString("access_token", "");
        userId = sharedPreferences.getString("user_id", "");

        initViews();
        loadUserData();
        setupListeners();
    }

    private void initViews() {
        etFirstName = findViewById(R.id.et_first_name);
        etLastName = findViewById(R.id.et_last_name);
        etAge = findViewById(R.id.et_age);
        etPhone = findViewById(R.id.et_phone);
        etShowers = findViewById(R.id.et_showers);
        btnSave = findViewById(R.id.btn_save);
    }

    private void loadUserData() {
        etFirstName.setText(sharedPreferences.getString("firstName", ""));
        etLastName.setText(sharedPreferences.getString("lastName", ""));
        etAge.setText(String.valueOf(sharedPreferences.getInt("age", 18)));
        etPhone.setText(sharedPreferences.getString("phone", ""));
        etShowers.setText(String.valueOf(sharedPreferences.getInt("showers", 5)));
    }

    private void setupListeners() {
        btnSave.setOnClickListener(v -> updateUser());
    }

    private void updateUser() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String ageStr = etAge.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String showersStr = etShowers.getText().toString().trim();

        int age = ageStr.isEmpty() ? 18 : Integer.parseInt(ageStr);
        int showers = showersStr.isEmpty() ? 5 : Integer.parseInt(showersStr);

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("first_name", firstName);
            requestBody.put("last_name", lastName);
            requestBody.put("age", age);
            requestBody.put("phone_number", phone);
            requestBody.put("shower_per_week", showers);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String apiUrl = getString(R.string.api_url);
        String url = apiUrl + "users/update/" + userId + "/";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PUT, url, requestBody,
                response -> {
                    // Actualizar SharedPreferences
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("firstName", firstName);
                    editor.putString("lastName", lastName);
                    editor.putInt("age", age);
                    editor.putString("phone_number", phone);
                    editor.putInt("showers_per_week", showers);
                    editor.apply();

                    Toast.makeText(this, "Información actualizada", Toast.LENGTH_SHORT).show();
                    finish();
                },
                error -> {
                    Toast.makeText(this, "Error al actualizar información", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + accessToken);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        requestQueue.add(request);
    }
}