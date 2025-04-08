package com.example.hydrostop;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public class UpdateUserActivity extends AppCompatActivity {

    private EditText etFirstName, etLastName, etAge, etPhone, etEmail;
    private Button btnSave;
    private SharedPreferences sharedPreferences;
    private RequestQueue requestQueue;
    private String accessToken, userId;
    private OkHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_user);
        client = new OkHttpClient();
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
        btnSave = findViewById(R.id.btn_save);
        etEmail = findViewById(R.id.et_email);
    }

    private void loadUserData() {
        String apiUrl = getString(R.string.api_url);
        String url = apiUrl + "user/";
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(UpdateUserActivity.this,
                            "Error al cargar datos: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                runOnUiThread(() -> {
                    try {
                        if (response.isSuccessful() && response.body() != null) {
                            String responseData = response.body().string();
                            JSONObject userData = new JSONObject(responseData); // Cambia a JSONObject
                            getInfo(userData);
                        } else {
                            Toast.makeText(UpdateUserActivity.this,
                                    "Error en la respuesta: " + response.code(),
                                    Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(UpdateUserActivity.this,
                                "Error al procesar datos: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void getInfo(JSONObject userData) throws JSONException {
        if (userData.has("user")) {
            etFirstName.setText(userData.getString("user"));
        }
        if (userData.has("last_name")) {
            etLastName.setText(userData.getString("last_name"));
        }
        if (userData.has("age")) {
            etAge.setText(String.valueOf(userData.getInt("age"))); // Convierte int a String
        }
        if (userData.has("phone_number")) {
            etPhone.setText(userData.getString("phone_number"));
        }
    }

    private void setupListeners() {
        btnSave.setOnClickListener(v -> updateUser());
    }

    private void updateUser() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String ageStr = etAge.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        int age = ageStr.isEmpty() ? 18 : Integer.parseInt(ageStr);


        JSONObject requestBody = new JSONObject();
        try {
            if (!firstName.isEmpty()){
                requestBody.put("first_name", firstName);
            }
            if (!lastName.isEmpty()){
                requestBody.put("last_name", lastName);
            }
            if (!ageStr.isEmpty()) {
                requestBody.put("age", age);
            }
            if (!phone.isEmpty()){
                requestBody.put("phone_number", phone);
            }
            if (!email.isEmpty()){
                requestBody.put("email", email);
            }
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
                    editor.putString("first_name", firstName);
                    editor.putString("last_name", lastName);
                    editor.putInt("age", age);
                    editor.putString("phone_number", phone);
                    editor.putString("email", email);
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