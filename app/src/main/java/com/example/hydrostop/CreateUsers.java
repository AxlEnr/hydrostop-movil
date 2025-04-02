package com.example.hydrostop;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CreateUsers extends AppCompatActivity {

    // Definición de variables
    private EditText etNombre, etApellido, etEmail, etTelefono, etEdad, etShowers, etPassword;
    private AutoCompleteTextView autoCompleteGender;
    private Button btnRegister;

    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_users);

        // Inicialización de vistas
        initViews();

        // Configurar el menú desplegable para género
        setupGenderDropdown();

        // Configurar listeners
        setupListeners();
    }

    private void initViews() {
        etNombre = findViewById(R.id.edit_first_name);
        etApellido = findViewById(R.id.edit_last_name);
        etEmail = findViewById(R.id.edit_email);
        etTelefono = findViewById(R.id.edit_phone);
        etEdad = findViewById(R.id.edit_age);
        etShowers = findViewById(R.id.edit_showers_per_week);
        etPassword = findViewById(R.id.edit_password);
        autoCompleteGender = findViewById(R.id.ACVWgenre);
        btnRegister = findViewById(R.id.btn_register);
    }
    private void setupGenderDropdown() {
        // Obtener opciones de género desde resources
        String[] genderOptions = getResources().getStringArray(R.array.gender_options);

        // Crear adaptador para el AutoCompleteTextView
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                genderOptions
        );

        autoCompleteGender.setAdapter(adapter);
        autoCompleteGender.setThreshold(1); // Mostrar sugerencias al escribir 1 carácter
    }

    private void setupListeners() {
        // Listener para el botón de registro
        btnRegister.setOnClickListener(v -> {
            if (validateFields()) {
                registerUser();
            }
        });

        // Listeners para la barra de navegación
        findViewById(R.id.nav_home).setOnClickListener(v ->
                startActivity(new Intent(this, MainActivity2.class)));

        findViewById(R.id.nav_settings).setOnClickListener(v ->
                startActivity(new Intent(this, MainActivity4.class)));

        findViewById(R.id.nav_profile).setOnClickListener(v ->
                startActivity(new Intent(this, CreateUsers.class)));

        findViewById(R.id.nav_notification).setOnClickListener(v ->
                startActivity(new Intent(this, notification.class)));
    }

    private boolean validateFields() {
        if (
                etNombre.getText().toString().isEmpty() ||
                etApellido.getText().toString().isEmpty() ||
                etEmail.getText().toString().isEmpty() ||
                etTelefono.getText().toString().isEmpty() ||
                etEdad.getText().toString().isEmpty() ||
                etShowers.getText().toString().isEmpty() ||
                etPassword.getText().toString().isEmpty() ||
                autoCompleteGender.getText().toString().isEmpty()) {

            Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (etPassword.getText().toString().length() < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void registerUser() {
        String apiUrl = getString(R.string.api_url);
        String url = apiUrl + "users/signup/";

        OkHttpClient client = new OkHttpClient();
        JSONObject jsonBody = new JSONObject();

        try {
            // Construir el objeto JSON con los datos del usuario
            jsonBody.put("username", generateUsername());
            jsonBody.put("first_name", etNombre.getText().toString());
            jsonBody.put("last_name", etApellido.getText().toString());
            jsonBody.put("email", etEmail.getText().toString());
            jsonBody.put("password", etPassword.getText().toString());
            jsonBody.put("age", Integer.parseInt(etEdad.getText().toString()));
            jsonBody.put("phone_number", etTelefono.getText().toString());
            jsonBody.put("showers_per_week", Integer.parseInt(etShowers.getText().toString()));
            jsonBody.put("gender", autoCompleteGender.getText().toString());

            // Crear la solicitud HTTP
            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json; charset=utf-8"),
                    jsonBody.toString()
            );

            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();

            // Enviar la solicitud
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() ->
                            Toast.makeText(CreateUsers.this, "Error de conexión: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    runOnUiThread(() -> {
                        if (response.isSuccessful()) {
                            handleSuccessResponse();
                        } else {
                            handleErrorResponse(response);
                        }
                    });
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() ->
                    Toast.makeText(CreateUsers.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private String generateUsername() {
        // Generar un nombre de usuario combinando nombre, apellido y últimos 4 dígitos del teléfono
        String phone = etTelefono.getText().toString();
        String phoneSuffix = phone.length() > 4 ? phone.substring(phone.length() - 4) : phone;
        return etNombre.getText().toString().toLowerCase() +
                etApellido.getText().toString().toLowerCase() +
                phoneSuffix;
    }

    private void handleSuccessResponse() {
        Toast.makeText(this, "Usuario registrado exitosamente", Toast.LENGTH_SHORT).show();
        // Limpiar campos después del registro exitoso
        clearFields();
        // Redirigir a la pantalla principal
        startActivity(new Intent(this, MainActivity2.class));
        finish();
    }

    private void handleErrorResponse(Response response) {
        try {
            String errorBody = response.body() != null ? response.body().string() : "Error desconocido";
            Toast.makeText(this, "Error en el registro: " + errorBody, Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(this, "Error procesando la respuesta", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearFields() {
        etNombre.setText("");
        etApellido.setText("");
        etEmail.setText("");
        etTelefono.setText("");
        etEdad.setText("");
        etShowers.setText("");
        etPassword.setText("");
        autoCompleteGender.setText("");
    }
}