package com.example.hydrostop;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity4 extends AppCompatActivity {

    Spinner spinnerTimeLimit, spinnerNotificationTime;
    Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main4);

        btnSave = findViewById(R.id.btn_save); // Agrega este botón en tu XML

        // Configurar los listeners para los íconos de la barra de navegación
        findViewById(R.id.nav_home).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Enviar al MainActivity2
                Intent intent = new Intent(MainActivity4.this, MainActivity2.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.nav_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Enviar al MainActivity4 (Configuración)
                Intent intent = new Intent(MainActivity4.this, MainActivity4.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.nav_profile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Enviar al CreateUserActivity (Formulario para crear usuarios)
                Intent intent = new Intent(MainActivity4.this, CreateUsers.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.nav_notification).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Enviar a la pantalla de notificaciones
                Intent intent = new Intent(MainActivity4.this, notification.class);
                startActivity(intent);
            }
        });

        // Botón para guardar y pasar a MainActivity5
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtener valores seleccionados en los Spinners
                String timeLimit = spinnerTimeLimit.getSelectedItem().toString();
                String notificationTime = spinnerNotificationTime.getSelectedItem().toString();

                // Enviar datos a MainActivity5
                Intent intent = new Intent(MainActivity4.this, MainActivity5.class);
                intent.putExtra("TIME_LIMIT", timeLimit);
                intent.putExtra("NOTIFICATION_TIME", notificationTime);
                startActivity(intent);

                // No se cierra la actividad actual; solo se pasa a MainActivity5
                // Eliminar el finish() para no cerrar esta actividad automáticamente
            }
        });
    }
}
