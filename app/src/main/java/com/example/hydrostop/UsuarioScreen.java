package com.example.hydrostop;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UsuarioScreen extends AppCompatActivity {

    private static final int DEFAULT_SHOWER_ID = 1;
    private static final int ALERT_DURATION_MS = 2500; // 2.5 segundos de sonido

    private TextView showerTimeInfo, alertTimeInfo;
    private Button encenderButton, btnRefresh;
    private OkHttpClient client;
    private Handler alertHandler;
    private MediaPlayer mediaPlayer;
    private int currentAlertTime = 60;
    private ImageView nav_settings;
    SharedPreferences sharedPreferences;
    private TextView textUser;
    private String loggedInUserGender; // Para almacenar el género del usuario logueado

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario_screen);

        nav_settings = findViewById(R.id.nav_settings);
        textUser = findViewById(R.id.text_user);
        sharedPreferences = getSharedPreferences("HydroStopPrefs", MODE_PRIVATE);
        btnRefresh = findViewById(R.id.btn_refresh);

        nav_settings.setOnClickListener(v -> {
            Intent intent = new Intent(UsuarioScreen.this, MainActivity4.class);
            startActivity(intent);
            finish();
        });

        btnRefresh.setOnClickListener(v -> {
            loadAllShowers();
        });

        // Configurar cliente HTTP y handlers
        client = new OkHttpClient();
        alertHandler = new Handler();

        // Inicializar reproductor con sonido personalizado
        initMediaPlayer();
        loadUserDataAndGender(); // Método modificado para cargar género
    }

    private void loadUserDataAndGender() {
        String first_name = sharedPreferences.getString("first_name", "Usuario");
        String role = sharedPreferences.getString("role", "user");
        loggedInUserGender = String.valueOf(sharedPreferences.getInt("genre", 2)); // 2 = ambos géneros por defecto

        textUser.setText(first_name + " (" + role + ")");
        loadAllShowers(); // Ahora cargamos las regaderas después de tener el género
    }

    private void loadAllShowers() {
        String apiUrl = getString(R.string.api_url);
        String url = apiUrl + "showers";
        String token = sharedPreferences.getString("access_token", "");

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(UsuarioScreen.this, "Error al cargar regaderas", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseData = response.body().string();
                        JSONArray showersArray = new JSONArray(responseData);

                        runOnUiThread(() -> updateShowersUI(showersArray));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void updateShowersUI(JSONArray showersArray) {
        LinearLayout showersContainer = findViewById(R.id.showers_container);
        showersContainer.removeAllViews();

        for (int i = 0; i < showersArray.length(); i++) {
            try {
                JSONObject shower = showersArray.getJSONObject(i);
                addShowerView(shower, showersContainer);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void addShowerView(JSONObject shower, LinearLayout container) throws JSONException {
        View showerView = getLayoutInflater().inflate(R.layout.shower_item, container, false);

        TextView showerName = showerView.findViewById(R.id.shower_name);
        TextView showerGender = showerView.findViewById(R.id.shower_gender);
        TextView showerTime = showerView.findViewById(R.id.shower_time);
        TextView showerAlert = showerView.findViewById(R.id.shower_alert);
        TextView showerStatus = showerView.findViewById(R.id.shower_status);
        Button btnOnOff = showerView.findViewById(R.id.btn_delete_shower); // Reutilizamos el botón

        // Configurar datos básicos
        showerName.setText(shower.getString("name"));

        // Configurar género
        String genderValue = shower.getString("gender");
        String genderText = "Género: ";
        switch (genderValue) {
            case "0":
                genderText += "Masculino";
                break;
            case "1":
                genderText += "Femenino";
                break;
            case "2":
                genderText += "Unisex";
                break;
            default:
                genderText += "Desconocido";
                break;
        }
        showerGender.setText(genderText);

        showerTime.setText("Tiempo: " + formatTime(shower.getInt("time")));
        showerAlert.setText("Alerta: " + formatTime(shower.getInt("alert_time")) + " antes");

        // Configurar estado
        int status = shower.getInt("status");
        int available = shower.getInt("available");
        String ipAddress = shower.getString("ip_address");
        int showerId = shower.getInt("id");
        int tiempo = shower.getInt("time");

        String statusText = "SIN USO";
        int statusColor = getResources().getColor(R.color.red);

        if (available == 0) {
            statusText = "NO DISPONIBLE";
        } else if (status == 1) {
            statusText = "EN USO";
            statusColor = getResources().getColor(R.color.green);
        }

        showerStatus.setText(statusText);
        showerStatus.setTextColor(statusColor);

        // Configurar botón de encendido/apagado
        updateButtonState(btnOnOff, status, available);

        // Lógica de filtrado por género
        boolean shouldShowShower = true;
        String showerGenderValue = shower.getString("gender");

        if (!"2".equals(showerGenderValue) && !"2".equals(loggedInUserGender)) {
            shouldShowShower = showerGenderValue.equals(loggedInUserGender);
        }

        if (shouldShowShower) {
            btnOnOff.setOnClickListener(v -> {
                boolean wantsToTurnOn = btnOnOff.getText().toString().equals("ENCENDER");

                if (wantsToTurnOn) {
                    updateShowerStatus(showerId, true, () -> {
                        btnOnOff.setText("APAGAR");
                        showerStatus.setText("EN USO");
                        showerStatus.setTextColor(getResources().getColor(R.color.green));
                        encenderValvula(tiempo, ipAddress);
                    });
                } else {
                    updateShowerStatus(showerId, false, () -> {
                        btnOnOff.setText("ENCENDER");
                        showerStatus.setText("SIN USO");
                        showerStatus.setTextColor(getResources().getColor(R.color.red));
                        apagarValvula(ipAddress);
                    });
                }
            });
            container.addView(showerView);
        } else {
            showerView.setVisibility(View.GONE);
        }
    }

    private void updateButtonState(Button button, int status, int available) {
        if (available == 0) {
            button.setEnabled(false);
            button.setText("NO DISPONIBLE");
        } else {
            button.setEnabled(true);
            button.setText(status == 1 ? "APAGAR" : "ENCENDER");
        }
    }

    private void updateShowerStatus(int showerId, boolean encender, Runnable callback) {
        String url = getString(R.string.api_url) + "shower/updates/" + showerId + "/";
        String status = encender ? "1" : "0";
        String token = sharedPreferences.getString("access_token", "");

        RequestBody formBody = new FormBody.Builder()
                .add("status", status)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .put(formBody)
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(UsuarioScreen.this,
                            "Error al actualizar regadera: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        try {
                            String responseBody = response.body().string();
                            JSONObject jsonResponse = new JSONObject(responseBody);
                            callback.run();
                            loadAllShowers();

                            if (encender) {
                                int tiempo = jsonResponse.getInt("time");
                                startShowerHistory(showerId, tiempo); // Envía la duración aquí
                                scheduleAutoShutdown(showerId, tiempo);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            String errorBody = response.body().string();
                            Toast.makeText(UsuarioScreen.this,
                                    "Error: " + response.code() + " - " + errorBody,
                                    Toast.LENGTH_LONG).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }
    // 1. Modifica startShowerHistory para NO enviar end_time prematuramente
    private void startShowerHistory(int showerId, int tiempo) {
        String url = getString(R.string.api_url) + "shower-history/start/";
        String token = sharedPreferences.getString("access_token", "");

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("shower_id", showerId);
            requestBody.put("duration", tiempo);
            // SOLO envía shower_id, el backend debe asignar start_time
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(
                requestBody.toString(),
                okhttp3.MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("ShowerHistory", "Error al iniciar historial: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        int historyId = jsonResponse.getInt("id");
                        // Guarda el historyId en SharedPreferences para usarlo luego
                        sharedPreferences.edit().putInt("current_history_id", historyId).apply();
                    } catch (Exception e) {
                        Log.e("ShowerHistory", "Error al parsear respuesta", e);
                    }
                }
            }
        });
    }

    // 2. Modifica completeShowerHistory para usar el tiempo REAL
    private void completeShowerHistory(int showerId, int duration) {
        int historyId = sharedPreferences.getInt("current_history_id", -1);
        if (historyId == -1) {
            Log.e("ShowerHistory", "No hay historyId guardado");
            return;
        }

        String url = getString(R.string.api_url) + "shower-history/end/" + historyId + "/";
        String token = sharedPreferences.getString("access_token", "");

        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("duration", duration);

            RequestBody body = RequestBody.create(
                    requestBody.toString(),
                    okhttp3.MediaType.parse("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url(url)
                    .put(body)
                    .addHeader("Authorization", "Bearer " + token)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("ShowerHistory", "Error al finalizar historial", e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        String errorBody = response.body() != null ? response.body().string() : "null";
                        Log.e("ShowerHistory", "Error en el servidor: " + response.code() + " - " + errorBody);
                    } else {
                        Log.d("ShowerHistory", "Historial completado exitosamente");
                    }
                }
            });
        } catch (JSONException e) {
            Log.e("ShowerHistory", "Error creando JSON", e);
        }
    }
    // 3. En scheduleAutoShutdown:
    private void scheduleAutoShutdown(int showerId, int tiempoSegundos) {
        new Handler().postDelayed(() -> {
            checkShowerStatusAndShutdown(showerId, tiempoSegundos); // Pasa el tiempo conocido
        }, tiempoSegundos * 1000L);
    }

    private void checkShowerStatusAndShutdown(int showerId, int duration) {
        String url = getString(R.string.api_url) + "shower/config/" + showerId + "/";
        String token = sharedPreferences.getString("access_token", "");

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(UsuarioScreen.this, "Error al verificar estado", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONObject shower = new JSONObject(response.body().string());
                        if (shower.getInt("status") == 1) { // Si todavía está encendida
                            updateShowerStatus(showerId, false, () -> {
                                completeShowerHistory(showerId, duration); // Usamos la duración que ya tenemos
                                runOnUiThread(() ->
                                        Toast.makeText(UsuarioScreen.this,
                                                "Regadera apagada automáticamente",
                                                Toast.LENGTH_SHORT).show());
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
    private void apagarValvula(String ipAddress) {
        String url = "http://" + ipAddress + "/apagar";

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(UsuarioScreen.this,
                                "Error al apagar: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) {
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(UsuarioScreen.this,
                                "Válvula apagada",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void loadUserData() {
        String first_name = sharedPreferences.getString("first_name", "Usuario");
        String role = sharedPreferences.getString("role", "user");
        textUser.setText(first_name + " (" + role + ")");
    }

    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        if (minutes > 0) {
            return remainingSeconds > 0 ?
                    String.format("%d min %d sec", minutes, remainingSeconds) :
                    String.format("%d minutos", minutes);
        }
        return String.format("%d segundos", seconds);
    }

    private void initMediaPlayer() {
        try {
            // Usar sonido personalizado desde res/raw
            mediaPlayer = MediaPlayer.create(this, R.raw.sonido);
            mediaPlayer.setLooping(false);
            mediaPlayer.setVolume(1.0f, 1.0f); // Volumen máximo
        } catch (Exception e) {
            showToast("Error al cargar sonido de alerta");
            e.printStackTrace();
        }
    }

    /* EL QUE FUNCIONABA POR LAS DUDAS
    private void encenderValvula(int tiempo, String ip) {
        String url = "http://" + ip + "/encender?tiempo=" + tiempo;

        Request request = new Request.Builder()
                .url(url)
                .build();

        // Programar la alerta
        long tiempoAlerta = (tiempo - currentAlertTime) * 1000L;

        alertHandler.postDelayed(() -> {
            if (mediaPlayer != null) {
                try {
                    mediaPlayer.start();

                    // Detener después de ALERT_DURATION_MS
                    new Handler().postDelayed(() -> {
                        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                            mediaPlayer.pause();
                            mediaPlayer.seekTo(0); // Rebobinar
                        }
                    }, ALERT_DURATION_MS);

                    showToast("La válvula se cerrará en " + currentAlertTime + " segundos", Toast.LENGTH_LONG);
                } catch (IllegalStateException e) {
                    showToast("Error al reproducir sonido");
                }
            }
        }, tiempoAlerta);

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    showToast("Error al conectar con la válvula: " + e.getMessage());
                    alertHandler.removeCallbacksAndMessages(null);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        showToast("Válvula activada por " + formatTime(tiempo));
                    } else {
                        showToast("Error en la respuesta del servidor");
                        alertHandler.removeCallbacksAndMessages(null);
                    }
                });
            }
        });
    }
     */

    private void encenderValvula(int tiempo, String ip) {
        String url = "http://" + ip + "/encender?tiempo=" + tiempo;

        Request request = new Request.Builder()
                .url(url)
                .build();

        // Programar la alerta
        long tiempoAlerta = (tiempo - currentAlertTime) * 1000L;

        alertHandler.postDelayed(() -> {
            if (mediaPlayer != null) {
                try {
                    mediaPlayer.start();
                    new Handler().postDelayed(() -> {
                        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                            mediaPlayer.pause();
                            mediaPlayer.seekTo(0);
                        }
                    }, ALERT_DURATION_MS);

                    showToast("La válvula se cerrará en " + currentAlertTime + " segundos", Toast.LENGTH_LONG);
                } catch (IllegalStateException e) {
                    showToast("Error al reproducir sonido");
                }
            }
        }, tiempoAlerta);

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    showToast("Error al conectar con la válvula: " + e.getMessage());
                    alertHandler.removeCallbacksAndMessages(null);
                });
            }

            @Override
            public void onResponse(Call call, Response response) {
                runOnUiThread(() -> {
                    if (!response.isSuccessful()) {
                        showToast("Error en la respuesta del servidor");
                        alertHandler.removeCallbacksAndMessages(null);
                    }
                });
            }
        });
    }
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showToast(String message, int duration) {
        Toast.makeText(this, message, duration).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        alertHandler.removeCallbacksAndMessages(null);
    }
}