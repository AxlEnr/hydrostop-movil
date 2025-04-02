package com.example.hydrostop;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private Button btnsignIn;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Verificar si el usuario ya está logueado
        sharedPreferences = getSharedPreferences("HydroStopPrefs", MODE_PRIVATE);
        if (isUserLoggedIn()) {
            redirectBasedOnRole();
            return;
        }

        EditText usernameField = findViewById(R.id.usernameField);
        EditText passwordField = findViewById(R.id.passwordField);
        btnsignIn = findViewById(R.id.btnsignIn);

        btnsignIn.setOnClickListener(v -> {
            String username = usernameField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                showToast("Por favor, ingrese email y contraseña");
            } else {
                login(username, password);
            }
        });
    }

    private boolean isUserLoggedIn() {
        return sharedPreferences.contains("access_token");
    }

    private void redirectBasedOnRole() {
        String role = sharedPreferences.getString("role", "user");
        Intent intent = new Intent(MainActivity.this,
                role.equals("admin") ? MainActivity2.class : UsuarioScreen.class); // Cambiar a la pantalla adecuada para usuarios
        startActivity(intent);
        finish();
    }

    public static void logout(SharedPreferences prefs) {
        prefs.edit().clear().apply();
    }

    private void login(String username, String password) {
        String apiUrl = getString(R.string.api_url);
        String url = apiUrl + "users/login/";

        OkHttpClient client = new OkHttpClient();

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("email", username);
            jsonBody.put("password", password);

            RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Error en la conexión", e);
                    showToast("Error de conexión: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        String responseData = response.body().string();
                        Log.d(TAG, "Respuesta del servidor: " + responseData);

                        if (!response.isSuccessful()) {
                            handleErrorResponse(response, responseData);
                            return;
                        }

                        handleSuccessResponse(responseData);
                    } catch (Exception e) {
                        Log.e(TAG, "Error procesando respuesta", e);
                        showToast("Error procesando la respuesta");
                    }
                }
            });
        } catch (JSONException e) {
            Log.e(TAG, "Error creando JSON", e);
            showToast("Error creando la solicitud");
        }
    }

    private void handleSuccessResponse(String responseData) throws JSONException {
        JSONObject jsonResponse = new JSONObject(responseData);

        if (!jsonResponse.has("access") || !jsonResponse.has("refresh")) {
            showToast("Respuesta del servidor incompleta");
            return;
        }

        String accessToken = jsonResponse.getString("access");
        String refreshToken = jsonResponse.getString("refresh");
        String first_name = "";
        String role = "user";
        String userId = "";

        if (jsonResponse.has("user")) {
            try {
                JSONObject user = jsonResponse.getJSONObject("user");
                if (user.has("role")) {
                    role = user.getString("role");
                }
                if (user.has("first_name")) {
                    first_name = user.getString("first_name");
                }
                if (user.has("id")) {
                    userId = user.getString("id");
                }
            } catch (JSONException e) {
                Log.w(TAG, "El campo 'user' no tiene el formato esperado", e);
            }
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("access_token", accessToken);
        editor.putString("refresh_token", refreshToken);
        editor.putString("role", role);
        editor.putString("first_name", first_name);
        editor.putString("user_id", userId);
        editor.apply();

        redirectBasedOnRole();
    }

    private void handleErrorResponse(Response response, String responseData) {
        try {
            JSONObject errorResponse = new JSONObject(responseData);
            String errorMessage = errorResponse.optString("detail",
                    errorResponse.optString("error",
                            "Error: " + response.code() + " - " + response.message()));
            showToast(errorMessage);
        } catch (JSONException e) {
            showToast("Error de autenticación (código " + response.code() + ")");
        }
    }

    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show());
    }
}