package com.example.hydrostop;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity2 extends AppCompatActivity {

    private Button btnConfigTime;
    private TextView showerTime, showerAlert, showerStatus;
    private static final int REQUEST_TIME_CONFIG = 1;
    private OkHttpClient client;
    private static final String SHOWER_ID = "1"; // ID de la regadera en el backend
    private TextView textUser;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main2);

        client = new OkHttpClient();
        btnConfigTime = findViewById(R.id.btnconfig_time);
        showerTime = findViewById(R.id.shower_time);
        showerAlert = findViewById(R.id.shower_alert);
        showerStatus = findViewById(R.id.shower_status);
        textUser = findViewById(R.id.text_user);
        sharedPreferences = getSharedPreferences("HydroStopPrefs", MODE_PRIVATE);

        // Cargar datos de la regadera al iniciar
        loadShowerData();
        loadUserData();

        setupNavigation();
        setupButtonListeners();
    }

    private void loadUserData() {
        String first_name = sharedPreferences.getString("first_name", "Usuario");
        String role = sharedPreferences.getString("role", "user");
        textUser.setText(first_name + " (" + role + ")");
    }


    private void loadShowerData() {
        String apiUrl = getString(R.string.api_url);
        String url = apiUrl + "shower/config/" + SHOWER_ID + "/";

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(MainActivity2.this, "Error al cargar datos", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseData = response.body().string();
                        JSONObject json = new JSONObject(responseData);

                        int time = json.getInt("shower_time");
                        int alertTime = json.getInt("alert_time");
                        int status = json.getInt("status");
                        int available = json.getInt("available");

                        runOnUiThread(() -> updateShowerUI(time, alertTime, status, available));

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void updateShowerUI(int time, int alertTime, int status, int available) {
        // Formatear tiempos
        String timeText = formatTime(time);
        String alertText = formatTime(alertTime) + " antes";

        showerTime.setText("Tiempo: " + timeText);
        showerAlert.setText("Alerta: " + alertText);

        // Actualizar estado
        String statusText = (status == 1) ? "EN USO" : "SIN USO";
        int statusColor = (status == 1) ? getResources().getColor(R.color.green) : getResources().getColor(R.color.red);

        showerStatus.setText(statusText);
        showerStatus.setTextColor(statusColor);

        // Actualizar disponibilidad si es necesario
        if (available == 0) {
            showerStatus.setText("NO DISPONIBLE");
            showerStatus.setTextColor(getResources().getColor(R.color.red));
        }
    }

    private String formatTime(int seconds) {
        if (seconds < 60) {
            return seconds + " segundos";
        } else {
            return (seconds / 60) + " minutos";
        }
    }

    private void setupButtonListeners() {
        btnConfigTime.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity2.this, MainActivity3.class);
            startActivityForResult(intent, REQUEST_TIME_CONFIG);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_TIME_CONFIG && resultCode == RESULT_OK) {
            // Recargar datos cuando volvemos de la configuración
            loadShowerData();

            if (data != null) {
                String timeLimit = data.getStringExtra("TIME_LIMIT");
                String notificationTime = data.getStringExtra("NOTIFICATION_TIME");
                btnConfigTime.setText("CONFIGURAR TIEMPO\n" + timeLimit + " / " + notificationTime);
            }
        }
    }

    private void setupNavigation() {
        findViewById(R.id.nav_home).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity2.this, MainActivity2.class);
            startActivity(intent);
        });

        findViewById(R.id.nav_settings).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity2.this, MainActivity4.class);
            startActivity(intent);
        });

        findViewById(R.id.nav_profile).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity2.this, CreateUsers.class);
            startActivity(intent);
        });

        findViewById(R.id.nav_notification).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity2.this, notification.class);
            startActivity(intent);
        });
    }
}