package com.example.hydrostop;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class notification extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    private TextView textUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notification);

        textUser = findViewById(R.id.text_user);
        sharedPreferences = getSharedPreferences("HydroStopPrefs", MODE_PRIVATE);
        loadUserData();

        // Configurar los listeners para los íconos de la barra de navegación
        findViewById(R.id.nav_home).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Enviar al MainActivity2
                Intent intent = new Intent(notification.this, MainActivity2.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.nav_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Enviar al MainActivity4 (Configuración)
                Intent intent = new Intent(notification.this, MainActivity4.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.nav_profile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Enviar al CreateUserActivity (Formulario para crear usuarios)
                Intent intent = new Intent(notification.this, CreateUsers.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.nav_notification).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Enviar a la pantalla de notificaciones
                Intent intent = new Intent(notification.this, notification.class);
                startActivity(intent);
            }
        });
    }

    private void loadUserData() {
        String first_name = sharedPreferences.getString("first_name", "Usuario");
        String role = sharedPreferences.getString("role", "user");
        textUser.setText(first_name + " (" + role + ")");
    }

}