package com.example.hydrostop;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
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

public class MainActivity4 extends AppCompatActivity {

    private TextView textUser;
    private EditText newPassword, confirmPassword, passwordAct;
    private Button btnSavePassword, btnUpdateInfo, btnLogout;
    private ImageView navHome, navNotification, navProfile, navSettings;

    private SharedPreferences sharedPreferences;
    private RequestQueue requestQueue;
    private String accessToken, userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main4);

        // Inicializar SharedPreferences
        sharedPreferences = getSharedPreferences("HydroStopPrefs", MODE_PRIVATE);
        accessToken = sharedPreferences.getString("access_token", "");
        userId = sharedPreferences.getString("user_id", "");

        // Inicializar Volley
        requestQueue = Volley.newRequestQueue(this);

        // Inicializar vistas
        initViews();

        // Cargar datos del usuario
        loadUserData();

        // Configurar listeners
        setupListeners();
    }

    private void initViews() {
        textUser = findViewById(R.id.text_user);
        passwordAct = findViewById(R.id.password_actually);
        newPassword = findViewById(R.id.new_password);
        confirmPassword = findViewById(R.id.confirm_password);
        btnSavePassword = findViewById(R.id.btn_save_password);
        btnUpdateInfo = findViewById(R.id.btn_update_info);
        btnLogout = findViewById(R.id.btn_logout);

        // Navigation
        navHome = findViewById(R.id.nav_home);
        navNotification = findViewById(R.id.nav_notification);
        navProfile = findViewById(R.id.nav_profile);
        navSettings = findViewById(R.id.nav_settings);
    }

    private void loadUserData() {
        String first_name = sharedPreferences.getString("first_name", "Usuario");
        String role = sharedPreferences.getString("role", "user");
        textUser.setText(first_name + " (" + role + ")");
    }

    private void setupListeners() {
        String role = sharedPreferences.getString("role", "user");
        if (role.equals("user")){
            navNotification.setVisibility(View.GONE);
            navProfile.setVisibility(View.GONE);
        }
        // Botón para cambiar contraseña
        btnSavePassword.setOnClickListener(v -> changePassword());

        // Botón para actualizar información
        btnUpdateInfo.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity4.this, UpdateUserActivity.class);
            startActivity(intent);
        });

        // Botón para cerrar sesión
        btnLogout.setOnClickListener(v -> logout());

        // Listeners de navegación
        navHome.setOnClickListener(v -> {
            if(role.equals("admin")){
                Intent intent = new Intent(MainActivity4.this, MainActivity2.class);
                startActivity(intent);
                finish();
            } else{
                Intent intent = new Intent(MainActivity4.this, UsuarioScreen.class);
                startActivity(intent);
                finish();
            }


        });

        navNotification.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity4.this, notification.class);
            startActivity(intent);
            finish();
        });

        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity4.this, CreateUsers.class);
            startActivity(intent);
            finish();
        });

        navSettings.setOnClickListener(v -> {
            // Ya estamos en configuración
        });
    }

    private void changePassword() {
        String password = newPassword.getText().toString().trim();
        String confirm = confirmPassword.getText().toString().trim();
        String passwordA = passwordAct.getText().toString().trim();

        if (password.isEmpty() || confirm.isEmpty() || passwordA.isEmpty()) {
            Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirm)) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear el JSON para la solicitud
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("old_password", passwordA);
            requestBody.put("new_password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String apiUrl = getString(R.string.api_url);
        String url = apiUrl + "users/change_password/";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PUT, url, requestBody,
                response -> {
                    try {
                        Toast.makeText(MainActivity4.this, "Contraseña cambiada exitosamente", Toast.LENGTH_SHORT).show();
                        newPassword.setText("");
                        confirmPassword.setText("");
                        passwordAct.setText("");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    if (error.networkResponse != null) {
                        if (error.networkResponse.statusCode == 400) {
                            Toast.makeText(MainActivity4.this, "Contraseña actual incorrecta", Toast.LENGTH_SHORT).show();
                        } else if (error.networkResponse.statusCode == 401) {
                            Toast.makeText(MainActivity4.this, "Sesión expirada, por favor inicie sesión nuevamente", Toast.LENGTH_SHORT).show();
                            logout();
                        } else {
                            Toast.makeText(MainActivity4.this, "Error al cambiar contraseña", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(MainActivity4.this, "Error de conexión", Toast.LENGTH_SHORT).show();
                    }
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

    private void logout() {
        // Limpiar SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        // Redirigir a la pantalla de login
        Intent intent = new Intent(MainActivity4.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}