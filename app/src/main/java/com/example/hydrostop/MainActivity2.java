package com.example.hydrostop;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MainActivity2 extends AppCompatActivity {
    private OkHttpClient client;
    private TextView textUser;
    private SharedPreferences sharedPreferences;
    private Button btnAddShower, btnRefresh, btnConfigTime;
    private List<String> foundDevices = new ArrayList<>();
    private static final int REQUEST_TIME_CONFIG = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        client = new OkHttpClient();
        textUser = findViewById(R.id.text_user);
        sharedPreferences = getSharedPreferences("HydroStopPrefs", MODE_PRIVATE);

        // Configurar botones
        btnAddShower = findViewById(R.id.btn_add_shower);
        btnRefresh = findViewById(R.id.btn_refresh);

        btnAddShower.setOnClickListener(v -> scanNetworkForShowers());
        btnRefresh.setOnClickListener(v -> loadAllShowers());
        btnConfigTime = findViewById(R.id.btnconfig_time);

        btnConfigTime.setOnClickListener(v -> {
            try {
                LinearLayout showersContainer = findViewById(R.id.showers_container);

                if (showersContainer == null) {
                    Log.e("HydroStop", "showers_container es NULL. Verifica que el ID en el XML sea correcto.");
                    Toast.makeText(MainActivity2.this, "Error: showers_container no encontrado.", Toast.LENGTH_SHORT).show();
                    return;
                }

                Log.d("HydroStop", "showers_container tiene " + showersContainer.getChildCount() + " hijos.");

                if (showersContainer.getChildCount() > 0) {
                    Intent intent = new Intent(MainActivity2.this, MainActivity3.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(MainActivity2.this, "No hay regaderas registradas para configurar.", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Log.e("HydroStop", "Error en btnConfigTime: " + e.getMessage(), e);
                Toast.makeText(MainActivity2.this, "Se produjo un error. Revisa Logcat.", Toast.LENGTH_SHORT).show();
            }
        });



        loadUserData();
        setupNavigation();
        loadAllShowers(); // Cargar regaderas al iniciar
    }


    private void scanNetworkForShowers() {
        btnAddShower.setText("Escaneando...");
        btnAddShower.setEnabled(false);

        String networkPrefix = getNetworkPrefix();
        foundDevices.clear();

        // Mostrar diálogo de progreso
        AlertDialog progressDialog = new AlertDialog.Builder(this)
                .setTitle("Escaneando red")
                .setMessage("Buscando dispositivos en la red local...")
                .setCancelable(false)
                .create();
        progressDialog.show();

        // Ejecutar escaneo en segundo plano
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            scanNetworkDevices(networkPrefix, 1, 254);

            runOnUiThread(() -> {
                progressDialog.dismiss();
                btnAddShower.setText("Agregar regadera");
                btnAddShower.setEnabled(true);
                showFoundDevicesDialog();
            });
        });
    }

    private String getNetworkPrefix() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();

        String ip = String.format("%d.%d.%d.%d",
                (ipAddress & 0xff),
                (ipAddress >> 8 & 0xff),
                (ipAddress >> 16 & 0xff),
                (ipAddress >> 24 & 0xff));

        String[] parts = ip.split("\\.");
        return parts[0] + "." + parts[1] + "." + parts[2] + ".";
    }

    private void scanNetworkDevices(String networkPrefix, int start, int end) {
        ExecutorService executor = Executors.newFixedThreadPool(20);

        for (int i = start; i <= end; i++) {
            String host = networkPrefix + i;
            executor.execute(() -> {
                try {
                    Socket socket = new Socket();
                    socket.connect(new InetSocketAddress(host, 80), 500); // Puerto común
                    socket.close();
                    synchronized (foundDevices) {
                        foundDevices.add(host);
                    }
                } catch (IOException e) {
                    // No responde, continuar
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(2, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void showFoundDevicesDialog() {
        if (foundDevices.isEmpty()) {
            Toast.makeText(this, "No se encontraron dispositivos", Toast.LENGTH_SHORT).show();
            return;
        }

        CharSequence[] devicesArray = foundDevices.toArray(new CharSequence[0]);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Dispositivos encontrados");
        builder.setItems(devicesArray, (dialog, which) -> {
            String selectedIp = foundDevices.get(which);
            addNewShower(selectedIp);
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void addNewShower(String ipAddress) {
        // Mostrar diálogo para ingresar nombre de regadera
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Nombre de la regadera");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String showerName = input.getText().toString();
            if (!showerName.isEmpty()) {
                createShowerOnBackend(showerName, ipAddress);
            } else {
                Toast.makeText(this, "Debes ingresar un nombre", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void createShowerOnBackend(String showerName, String ipAddress) {
        // Primero verificar si ya existe una regadera con este nombre o IP
        String apiUrl = getString(R.string.api_url);
        String checkUrl = apiUrl + "showers/check?name=" + showerName + "&ip=" + ipAddress;

        Request checkRequest = new Request.Builder()
                .url(checkUrl)
                .build();

        client.newCall(checkRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(MainActivity2.this, "Error al verificar regadera", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseData = response.body().string();
                        JSONObject json = new JSONObject(responseData);

                        boolean nameExists = json.getBoolean("name_exists");
                        boolean ipExists = json.getBoolean("ip_exists");

                        runOnUiThread(() -> {
                            if (nameExists) {
                                Toast.makeText(MainActivity2.this, "Ya existe una regadera con este nombre", Toast.LENGTH_LONG).show();
                            } else if (ipExists) {
                                Toast.makeText(MainActivity2.this, "Ya existe una regadera con esta IP", Toast.LENGTH_LONG).show();
                            } else {
                                // Si no existe, proceder a crear
                                proceedWithShowerCreation(showerName, ipAddress);
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void proceedWithShowerCreation(String showerName, String ipAddress) {
        String apiUrl = getString(R.string.api_url);
        String url = apiUrl + "showers/create";

        RequestBody formBody = new FormBody.Builder()
                .add("name", showerName)
                .add("ip_address", ipAddress)
                .add("time", "300")  // 5 minutos por defecto
                .add("alert_time", "60")  // 1 minuto por defecto
                .add("status", "0")  // Sin uso inicialmente
                .add("available", "1")  // Disponible
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(MainActivity2.this, "Error al crear regadera", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity2.this, "Regadera creada: " + showerName, Toast.LENGTH_SHORT).show();
                        loadAllShowers(); // Recargar todas las regaderas
                    });
                }
            }
        });
    }

    private void loadAllShowers() {
        String apiUrl = getString(R.string.api_url);
        String url = apiUrl + "showers";

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(MainActivity2.this, "Error al cargar regaderas", Toast.LENGTH_SHORT).show());
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
        TextView showerTime = showerView.findViewById(R.id.shower_time);
        TextView showerAlert = showerView.findViewById(R.id.shower_alert);
        TextView showerStatus = showerView.findViewById(R.id.shower_status);
        Button btnDelete = showerView.findViewById(R.id.btn_delete_shower);

        // Configurar datos
        showerName.setText(shower.getString("name"));
        showerTime.setText("Tiempo: " + formatTime(shower.getInt("time")));
        showerAlert.setText("Alerta: " + formatTime(shower.getInt("alert_time")) + " antes");

        // Configurar estado
        int status = shower.getInt("status");
        int available = shower.getInt("available");
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

        // Configurar botones
        final int showerId = shower.getInt("id");

        btnDelete.setOnClickListener(v -> {
            try {
                showDeleteConfirmation(showerId, shower.getString("name"), showerView);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });

        container.addView(showerView);
    }

    private void showDeleteConfirmation(int showerId, String showerName, View showerView) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar regadera")
                .setMessage("¿Eliminar la regadera " + showerName + "?")
                .setPositiveButton("Eliminar", (dialog, which) -> deleteShower(showerId, showerView))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void deleteShower(int showerId, View showerView) {
        String apiUrl = getString(R.string.api_url);
        String url = apiUrl + "showers/delete/" + showerId + "/";
        String token = sharedPreferences.getString("access_token", null);

        Request request = new Request.Builder()
                .url(url)
                .delete()
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(MainActivity2.this, "Error al eliminar", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(MainActivity2.this, "Regadera eliminada", Toast.LENGTH_SHORT).show();
                        ((LinearLayout)showerView.getParent()).removeView(showerView);
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
        return seconds < 60 ? seconds + " segundos" : (seconds / 60) + " minutos";
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TIME_CONFIG && resultCode == RESULT_OK) {
            loadAllShowers(); // Recargar después de editar
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