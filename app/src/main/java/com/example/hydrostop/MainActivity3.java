    package com.example.hydrostop;

    import android.app.ProgressDialog;
    import android.content.Intent;
    import android.os.Bundle;
    import android.view.View;
    import android.widget.ArrayAdapter;
    import android.widget.AutoCompleteTextView;
    import android.widget.Button;
    import android.widget.Toast;

    import androidx.appcompat.app.AppCompatActivity;

    import org.json.JSONArray;
    import org.json.JSONException;
    import org.json.JSONObject;

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
        private View btnBack;
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
            btnBack = findViewById(R.id.btnBack);
            client = new OkHttpClient();
            btnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity3.this, MainActivity2.class);
                    startActivity(intent);
                    finish();
                }
            });


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
            timeLimitView.setThreshold(1); // Mostrar sugerencias desde el primer carácter
            timeLimitView.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) timeLimitView.showDropDown();
            });
            timeLimitView.setOnClickListener(v -> timeLimitView.showDropDown());
            timeLimitView.setKeyListener(null); // Evitar entrada de texto manual

            ArrayAdapter<String> alertAdapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_dropdown_item_1line, alertTimes);
            alertTimeView.setAdapter(alertAdapter);
            alertTimeView.setThreshold(1);
            alertTimeView.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) alertTimeView.showDropDown();
            });
            alertTimeView.setOnClickListener(v -> alertTimeView.showDropDown());
            alertTimeView.setKeyListener(null); // Evitar entrada de texto manual
        }

        private void loadCurrentConfig() {
            String apiUrl = getString(R.string.api_url);
            String url = apiUrl + "shower/config/" + showerId + "/";

            Request request = new Request.Builder()
                    .url(url)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() ->
                            showErrorToast("Error de conexión: " + e.getMessage()));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        String responseData = response.body().string();
                        JSONObject config = new JSONObject(responseData);

                        runOnUiThread(() -> {
                            try {
                                int currentTimeLimit = config.getInt("shower_time");
                                int currentAlertTime = config.getInt("alert_time");

                                timeLimitView.setText(convertSecondsToText(currentTimeLimit));
                                alertTimeView.setText(convertSecondsToText(currentAlertTime));
                            } catch (JSONException e) {
                            }
                        });
                    } catch (JSONException e) {
                        runOnUiThread(() -> showErrorToast("Error al procesar respuesta"));
                    }
                }
            });
        }

        private String convertSecondsToText(int seconds) {
            if (seconds < 60) {
                return seconds + " segundo" + (seconds != 1 ? "s" : "") + " (prueba)";
            } else {
                int minutes = seconds / 60;
                return minutes + " minuto" + (minutes != 1 ? "s" : "");
            }
        }

        private void showErrorToast(String message) {
            Toast.makeText(MainActivity3.this, message, Toast.LENGTH_LONG).show();
        }

        private void setupListeners() {
            btnSave.setOnClickListener(v -> saveConfiguration());
        }


        private void saveConfiguration() {
            String timeLimitStr = timeLimitView.getText().toString();
            String alertTimeStr = alertTimeView.getText().toString();

            if (timeLimitStr.isEmpty() || alertTimeStr.isEmpty()) {
                Toast.makeText(this, "Selecciona ambos tiempos", Toast.LENGTH_SHORT).show();
                return;
            }

            int timeLimit = convertTimeToSeconds(timeLimitStr);
            int alertTime = convertTimeToSeconds(alertTimeStr);

            if (alertTime >= timeLimit) {
                Toast.makeText(this, "El tiempo de alerta debe ser menor que el tiempo de flujo", Toast.LENGTH_SHORT).show();
                return;
            }

            ProgressDialog progressDialog = new ProgressDialog(MainActivity3.this);
            progressDialog.setMessage("Actualizando configuración...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            // Primero obtener todas las regaderas
            String getAllUrl = getString(R.string.api_url) + "showers";

            Request getAllRequest = new Request.Builder()
                    .url(getAllUrl)
                    .build();

            client.newCall(getAllRequest).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() ->
                            Toast.makeText(MainActivity3.this, "Error al obtener regaderas", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        try {
                            String responseData = response.body().string();
                            JSONArray showersArray = new JSONArray(responseData);
                            updateAllShowers(showersArray, timeLimit, alertTime);
                            progressDialog.dismiss();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }

        private void updateAllShowers(JSONArray showersArray, int timeLimit, int alertTime) {
            for (int i = 0; i < showersArray.length(); i++) {
                try {
                    JSONObject shower = showersArray.getJSONObject(i);
                    int showerId = shower.getInt("id");
                    updateSingleShower(showerId, timeLimit, alertTime, i == showersArray.length() - 1);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        private void updateSingleShower(int showerId, int timeLimit, int alertTime, boolean isLast) {
            String url = getString(R.string.api_url) + "shower/update/" + showerId + "/";

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
                            Toast.makeText(MainActivity3.this, "Error al actualizar regadera " + showerId, Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (isLast) { // Solo mostrar mensaje cuando la última regadera se actualice
                        runOnUiThread(() -> {
                            if (response.isSuccessful()) {
                                Toast.makeText(MainActivity3.this, "Configuración guardada en todas las regaderas", Toast.LENGTH_SHORT).show();

                                Intent resultIntent = new Intent();
                                resultIntent.putExtra("TIME_LIMIT", timeLimit);
                                resultIntent.putExtra("ALERT_TIME", alertTime);
                                setResult(RESULT_OK, resultIntent);
                                Intent intent = new Intent(MainActivity3.this, MainActivity2.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(MainActivity3.this, "Error al guardar", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
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