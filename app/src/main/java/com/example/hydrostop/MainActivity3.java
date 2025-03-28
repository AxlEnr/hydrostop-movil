package com.example.hydrostop;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;

public class MainActivity3 extends AppCompatActivity {

    private AutoCompleteTextView timeLimitView, alertTimeView;
    private Button btnSave;
    private OkHttpClient client;
    private int showerId = 1; // ID de la regadera a configurar

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        // Inicialización de vistas
        timeLimitView = findViewById(R.id.ACVWflujo);
        alertTimeView = findViewById(R.id.ACVWalarma);
        btnSave = findViewById(R.id.btn_save);
        client = new OkHttpClient();

        // Configurar opciones para los dropdowns
        setupDropdowns();

        // Cargar configuración actual
        loadCurrentConfig();

        // Configurar listeners
        setupListeners();

        // Configurar navegación
        setupNavigation();
    }

    private void setupDropdowns() {
        // Opciones para tiempo de flujo
        String[] flowTimes = new String[]{
                "10 segundos (prueba)",
                "5 minutos",
                "8 minutos",
                "10 minutos",
                "15 minutos",
                "15 segundos (prueba)"
        };

        // Opciones para tiempo de alerta
        String[] alertTimes = new String[]{
                "2 segundos (prueba)",
                "30 segundos",
                "1 minuto",
                "2 minutos",
                "3 minutos"
        };

        ArrayAdapter<String> flowAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, flowTimes);
        timeLimitView.setAdapter(flowAdapter);

        ArrayAdapter<String> alertAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, alertTimes);
        alertTimeView.setAdapter(alertAdapter);
    }

    private void loadCurrentConfig() {
        String url = "http://192.168.0.204:8000/api/shower/config/" + showerId + "/";

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(MainActivity3.this, "Error al cargar configuración", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    // Aquí deberías parsear el JSON de respuesta
                    // y actualizar los AutoCompleteTextView con los valores actuales
                    // Ejemplo:
                    // timeLimitView.setText("10 minutos");
                    // alertTimeView.setText("1 minuto");
                }
            }
        });
    }

    private void setupListeners() {
        btnSave.setOnClickListener(v -> saveConfiguration());
    }

    private void saveConfiguration() {
        String timeLimitStr = timeLimitView.getText().toString();
        String alertTimeStr = alertTimeView.getText().toString();

        // Validar que se hayan seleccionado valores
        if (timeLimitStr.isEmpty() || alertTimeStr.isEmpty()) {
            Toast.makeText(this, "Selecciona ambos tiempos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convertir a valores numéricos (segundos)
        int timeLimit = convertTimeToSeconds(timeLimitStr);
        int alertTime = convertTimeToSeconds(alertTimeStr);

        if (alertTime >= timeLimit) {
            Toast.makeText(this, "El tiempo de alerta debe ser menor que el tiempo de flujo", Toast.LENGTH_SHORT).show();
            return;
        }

        // Guardar en el backend
        String url = "http://192.168.0.204:8000/api/shower/update/" + showerId + "/";

        RequestBody formBody = new FormBody.Builder()
                .add("shower_time", String.valueOf(timeLimit))
                .add("alert_time", String.valueOf(alertTime))
                .build();

        Request request = new Request.Builder()
                .url(url)
                .put(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(MainActivity3.this, "Error al guardar configuración", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(MainActivity3.this, "Configuración guardada", Toast.LENGTH_SHORT).show();

                        // Devolver resultados a la actividad anterior
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("TIME_LIMIT", timeLimitStr);
                        resultIntent.putExtra("ALERT_TIME", alertTimeStr);
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    } else {
                        Toast.makeText(MainActivity3.this, "Error al guardar", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private int convertTimeToSeconds(String timeStr) {
        if (timeStr.contains("segundo")) {
            return Integer.parseInt(timeStr.split(" ")[0]);
        } else if (timeStr.contains("minuto")) {
            return Integer.parseInt(timeStr.split(" ")[0]) * 60;
        }
        return 60; // Valor por defecto
    }

    private void setupNavigation() {
        findViewById(R.id.nav_home).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity3.this, MainActivity2.class);
            startActivity(intent);
        });

        findViewById(R.id.nav_settings).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity3.this, MainActivity4.class);
            startActivity(intent);
        });

        findViewById(R.id.nav_profile).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity3.this, CreateUsers.class);
            startActivity(intent);
        });

        findViewById(R.id.nav_notification).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity3.this, notification.class);
            startActivity(intent);
        });
    }
}