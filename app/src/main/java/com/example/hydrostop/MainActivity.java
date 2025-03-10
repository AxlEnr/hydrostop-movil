package com.example.hydrostop;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.MediaType;

public class MainActivity extends AppCompatActivity {
    Button btnsignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Referencias a los campos y el botón
        EditText usernameField = findViewById(R.id.usernameField);
        EditText passwordField = findViewById(R.id.passwordField);
        btnsignIn = findViewById(R.id.btnsignIn);

        btnsignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameField.getText().toString().trim();
                String password = passwordField.getText().toString().trim();

                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Por favor, ingrese usuario y contraseña", Toast.LENGTH_SHORT).show();
                } else {
                    // Intentar iniciar sesión solo si los campos no están vacíos
                    login(username, password);
                }
            }
        });
    }

    // Método para hacer la solicitud de inicio de sesión
    private void login(String username, String password) {
        // URL de la API para autenticación (modificar con tu URL)
        String url = "http://192.168.1.125:8000/api/users/login/";  // Usa la IP de tu máquina en la red local o URL remota

        OkHttpClient client = new OkHttpClient();

        // Crear JSON con las credenciales del usuario
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("email", username);
            jsonBody.put("password", password);
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error creando el JSON", Toast.LENGTH_SHORT).show());
            return;
        }

        // Crear RequestBody y definir el tipo de contenido
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonBody.toString());

        // Crear la solicitud HTTP (POST)
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        // Realizar la solicitud en segundo plano (fuera del hilo principal)
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error en la conexión", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    runOnUiThread(() -> {
                        try {
                            // Parsear la respuesta JSON y verificar el token
                            JSONObject jsonResponse = new JSONObject(responseData);
                            if (jsonResponse.has("access") && jsonResponse.has("refresh")) {
                                String accessToken = jsonResponse.getString("access");
                                String refreshToken = jsonResponse.getString("refresh");

                                // Guardar los tokens (ejemplo con SharedPreferences)
                                getSharedPreferences("UserPrefs", MODE_PRIVATE)
                                        .edit()
                                        .putString("access_token", accessToken)
                                        .putString("refresh_token", refreshToken)
                                        .apply();

                                // Redirigir al usuario a la siguiente pantalla
                                Intent intent = new Intent(MainActivity.this, MainActivity2.class);
                                startActivity(intent);
                                finish();  // Cierra esta actividad
                            } else {
                                Toast.makeText(MainActivity.this, "Respuesta inesperada del servidor", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, "Error al parsear la respuesta", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }
}