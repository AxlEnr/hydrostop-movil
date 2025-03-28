package com.example.hydrostop;

import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UsuarioScreen extends AppCompatActivity {

    private static final String SERVER_IP = "192.168.0.97";
    private static final int SERVER_PORT = 80;
    private static final int ALERTA_ANTES = 5; // segundos antes para la alerta

    private EditText tiempoEditText;
    private Button encenderButton;
    private OkHttpClient client;
    private Handler alertHandler;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario_screen);

        tiempoEditText = findViewById(R.id.tiempoEditText);
        encenderButton = findViewById(R.id.encenderButton);
        client = new OkHttpClient();
        alertHandler = new Handler();

        // Cargar el sonido de notificación del sistema
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mediaPlayer = MediaPlayer.create(this, notification);

        encenderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tiempoStr = tiempoEditText.getText().toString();
                if (!tiempoStr.isEmpty()) {
                    int tiempo = Integer.parseInt(tiempoStr);
                    if (tiempo > ALERTA_ANTES) { // Asegurarnos que hay tiempo para la alerta
                        encenderValvula(tiempo);
                    } else {
                        Toast.makeText(UsuarioScreen.this,
                                "El tiempo debe ser mayor a " + ALERTA_ANTES + " segundos",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(UsuarioScreen.this, "Ingresa un tiempo válido", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void encenderValvula(int tiempo) {
        String url = "http://" + SERVER_IP + "/encender?tiempo=" + tiempo;

        Request request = new Request.Builder()
                .url(url)
                .build();

        // Programar la alerta sonora
        long tiempoAlerta = (tiempo - ALERTA_ANTES) * 1000L; // Convertir a milisegundos

        alertHandler.postDelayed(() -> {
            if (mediaPlayer != null) {
                mediaPlayer.start();
                Toast.makeText(UsuarioScreen.this,
                        "La válvula se cerrará en " + ALERTA_ANTES + " segundos",
                        Toast.LENGTH_SHORT).show();
            }
        }, tiempoAlerta);

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(UsuarioScreen.this,
                            "Error al conectar con la válvula: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    // Cancelar la alerta si hubo error
                    alertHandler.removeCallbacksAndMessages(null);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> Toast.makeText(UsuarioScreen.this,
                            "Válvula activada por " + tiempo + " segundos",
                            Toast.LENGTH_SHORT).show());
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(UsuarioScreen.this,
                                "Error en la respuesta del servidor",
                                Toast.LENGTH_SHORT).show();
                        // Cancelar la alerta si hubo error
                        alertHandler.removeCallbacksAndMessages(null);
                    });
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Liberar recursos
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        alertHandler.removeCallbacksAndMessages(null);
    }
}