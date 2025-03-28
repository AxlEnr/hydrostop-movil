package com.example.hydrostop;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UsuarioScreen extends AppCompatActivity {

    private static final int DEFAULT_SHOWER_ID = 1;
    private static final int ALERT_DURATION_MS = 2500; // 2.5 segundos de sonido

    private TextView showerTimeInfo, alertTimeInfo;
    private Button encenderButton;
    private OkHttpClient client;
    private Handler alertHandler;
    private MediaPlayer mediaPlayer;
    private int currentShowerTime = 600;
    private int currentAlertTime = 60;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario_screen);

        // Inicializar vistas
        showerTimeInfo = findViewById(R.id.shower_time_info);
        alertTimeInfo = findViewById(R.id.alert_time_info);
        encenderButton = findViewById(R.id.encenderButton);

        // Configurar cliente HTTP y handlers
        client = new OkHttpClient();
        alertHandler = new Handler();

        // Inicializar reproductor con sonido personalizado
        initMediaPlayer();

        // Cargar configuración
        loadShowerConfig();

        // Configurar listeners
        encenderButton.setOnClickListener(v -> {
            if (currentShowerTime > 0) {
                encenderValvula(currentShowerTime);
            } else {
                showToast("Configuración no cargada correctamente");
            }
        });
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

    private void loadShowerConfig() {
        String url = "http://192.168.0.204:8000/api/shower/config/" + DEFAULT_SHOWER_ID + "/";

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> showToast("Error al cargar configuración"));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseData = response.body().string();
                        JSONObject json = new JSONObject(responseData);

                        currentShowerTime = json.getInt("shower_time");
                        currentAlertTime = json.getInt("alert_time");

                        runOnUiThread(() -> updateUIWithConfig());

                    } catch (JSONException e) {
                        showToast("Error en formato de respuesta");
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void updateUIWithConfig() {
        String showerTimeText = formatTime(currentShowerTime);
        String alertTimeText = formatTime(currentAlertTime);

        showerTimeInfo.setText("Tiempo de flujo: " + showerTimeText);
        alertTimeInfo.setText("Alerta sonará " + alertTimeText + " antes");
    }

    private String formatTime(int seconds) {
        return seconds < 60 ? seconds + " segundos" : (seconds / 60) + " minutos";
    }

    private void encenderValvula(int tiempo) {
        String url = "http://192.168.0.97/encender?tiempo=" + tiempo;

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