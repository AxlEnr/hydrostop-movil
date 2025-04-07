package com.example.hydrostop;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.List;
import java.util.Locale;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

public class notification extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    private TextView textUser;
    private TextView showerInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notification);

        textUser = findViewById(R.id.text_user);
        sharedPreferences = getSharedPreferences("HydroStopPrefs", MODE_PRIVATE);
        loadUserData();
        fetchShowerHistory();

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

    private void fetchShowerHistory() {
        String apiUrl = getString(R.string.api_url);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(apiUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ShowerHistoryService service = retrofit.create(ShowerHistoryService.class);
        service.getShowerHistory().enqueue(new Callback<List<ShowerHistory>>() {
            @Override
            public void onResponse(Call<List<ShowerHistory>> call, Response<List<ShowerHistory>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ShowerHistory> showerHistoryList = response.body();
                    updateShowerList(showerHistoryList);
                }
            }

            @Override
            public void onFailure(Call<List<ShowerHistory>> call, Throwable t) {
                Toast.makeText(notification.this, "Error al obtener datos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateShowerList(List<ShowerHistory> showerHistoryList) {
        LinearLayout showerContainer = findViewById(R.id.shower_container);
        showerContainer.removeAllViews(); // Limpiar datos previos

        for (ShowerHistory shower : showerHistoryList) {
            View showerView = getLayoutInflater().inflate(R.layout.item_shower, showerContainer, false);

            TextView userName = showerView.findViewById(R.id.user_name);
            TextView showerName = showerView.findViewById(R.id.shower_name);
            TextView startTime = showerView.findViewById(R.id.start_time);
            TextView endTime = showerView.findViewById(R.id.end_time);
            TextView duration = showerView.findViewById(R.id.duration);
            TextView status = showerView.findViewById(R.id.shower_status);

            userName.setText("Usuario: " + shower.getUserName());
            showerName.setText("Regadera: " + shower.getShowerName());
            startTime.setText("Inicio: " + formatDate(shower.getStartTime()));
            endTime.setText("Fin: " + formatDate(shower.getEndTime()));
            duration.setText("Duración: " + (shower.getDurationSeconds() / 60) + " min");

            if (shower.isCompleted()) {
                status.setText("Estado: Finalizado");
                status.setTextColor(Color.RED);
            } else {
                status.setText("Estado: En Uso");
                status.setTextColor(Color.GREEN);
            }

            showerContainer.addView(showerView);
        }
    }

    // Función para formatear fecha a un formato legible
    private String formatDate(String dateStr) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            Date date = inputFormat.parse(dateStr);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return dateStr; // En caso de error, mostrar sin formateo
        }
    }


    private final Handler handler = new Handler();
    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            fetchShowerHistory();
            handler.postDelayed(this, 5000); // Actualiza cada 5 segundos
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        handler.postDelayed(runnable, 0);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable);
    }

}