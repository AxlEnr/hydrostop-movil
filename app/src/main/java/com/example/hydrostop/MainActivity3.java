package com.example.hydrostop;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity3 extends AppCompatActivity {

    Spinner spinnerTimeLimit, spinnerNotificationTime;
    Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main3);

        // Vincular elementos del layout
        spinnerTimeLimit = findViewById(R.id.spinner_time_limit);
        spinnerNotificationTime = findViewById(R.id.spinner_notification_time);
        btnSave = findViewById(R.id.btn_save); // Agrega este botón en tu XML

        // Botón para guardar y pasar a MainActivity4
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtener valores seleccionados en los Spinners
                String timeLimit = spinnerTimeLimit.getSelectedItem().toString();
                String notificationTime = spinnerNotificationTime.getSelectedItem().toString();

                // Enviar datos a MainActivity4
                Intent intent = new Intent(MainActivity3.this, MainActivity4.class);
                intent.putExtra("TIME_LIMIT", timeLimit);
                intent.putExtra("NOTIFICATION_TIME", notificationTime);
                startActivity(intent);

                // No se cierra la actividad actual; solo se pasa a MainActivity4
                // Eliminar el finish() para no cerrar esta actividad automáticamente
            }
        });
    }
}
