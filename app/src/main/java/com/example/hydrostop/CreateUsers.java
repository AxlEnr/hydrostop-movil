package com.example.hydrostop;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
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
    private EditText etNombre, etApellido, etEmail, etTelefono, etEdad, etShowers, etPassword;
    private AutoCompleteTextView autoCompleteGender;
    private Button btnRegister;
    private Button btnDeleteUser, btnActivateUser;
    private OkHttpClient client;
    private SharedPreferences sharedPreferences;

    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_users);
        client = new OkHttpClient();
        btnDeleteUser = findViewById(R.id.btn_delete_user);
        btnActivateUser = findViewById(R.id.btn_activate_user);
        btnDeleteUser.setOnClickListener(v -> showUserSelectionDialog());
        btnActivateUser.setOnClickListener(v -> showUserSelectionDialog2());
        sharedPreferences = getSharedPreferences("HydroStopPrefs", MODE_PRIVATE);

        // Inicialización de vistas
        initViews();

        // Configurar el menú desplegable para género
        setupGenderDropdown();

        // Configurar listeners
        setupListeners();
    }

    private void showUserSelectionDialog() {
        // Mostrar diálogo de carga mientras se obtienen los usuarios
        AlertDialog loadingDialog = new AlertDialog.Builder(this)
                .setTitle("Cargando usuarios")
                .setMessage("Obteniendo lista de usuarios...")
                .setCancelable(false)
                .create();
        loadingDialog.show();

        String apiUrl = getString(R.string.api_url);
        String url = apiUrl + "users/";

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    loadingDialog.dismiss();
                    Toast.makeText(CreateUsers.this, "Error al cargar usuarios", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseData = response.body().string();
                        JSONArray usersArray = new JSONArray(responseData);

                        runOnUiThread(() -> {
                            loadingDialog.dismiss();
                            showUsersListDialog(usersArray);
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                        runOnUiThread(() -> {
                            loadingDialog.dismiss();
                            Toast.makeText(CreateUsers.this, "Error al procesar usuarios", Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    runOnUiThread(() -> {
                        loadingDialog.dismiss();
                        Toast.makeText(CreateUsers.this, "Error al obtener usuarios", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void showUserSelectionDialog2() {
        // Mostrar diálogo de carga mientras se obtienen los usuarios
        AlertDialog loadingDialog = new AlertDialog.Builder(this)
                .setTitle("Cargando usuarios")
                .setMessage("Obteniendo lista de usuarios...")
                .setCancelable(false)
                .create();
        loadingDialog.show();

        String apiUrl = getString(R.string.api_url);
        String url = apiUrl + "users2/";

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    loadingDialog.dismiss();
                    Toast.makeText(CreateUsers.this, "Error al cargar usuarios", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseData = response.body().string();
                        JSONArray usersArray = new JSONArray(responseData);

                        runOnUiThread(() -> {
                            loadingDialog.dismiss();
                            showUsersListDialog2(usersArray);
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                        runOnUiThread(() -> {
                            loadingDialog.dismiss();
                            Toast.makeText(CreateUsers.this, "Error al procesar usuarios", Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    runOnUiThread(() -> {
                        loadingDialog.dismiss();
                        Toast.makeText(CreateUsers.this, "Error al obtener usuarios", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void showUsersListDialog2(JSONArray usersArray) {
        if (usersArray.length() == 0) {
            Toast.makeText(this, "No hay usuarios registrados", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Crear arrays para nombres e IDs
            String[] userNames = new String[usersArray.length()];
            final int[] userIds = new int[usersArray.length()];

            for (int i = 0; i < usersArray.length(); i++) {
                JSONObject user = usersArray.getJSONObject(i);
                userNames[i] = user.getString("first_name") + " " + user.getString("last_name") +
                        " (" + user.getString("email") + ")";
                userIds[i] = user.getInt("id");
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Seleccione usuario a reactivar");
            builder.setItems(userNames, (dialog, which) -> {
                int selectedUserId = userIds[which];
                String selectedUserName = userNames[which];
                showDeleteConfirmation2(selectedUserId, selectedUserName);
            });
            builder.setNegativeButton("Cancelar", null);
            builder.show();

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al mostrar usuarios", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteConfirmation2(int userId, String userName) {
        new AlertDialog.Builder(this)
                .setTitle("Confirmar reactivación")
                .setMessage("¿Está seguro de volver a activar al usuario " + userName + "?")
                .setPositiveButton("Activar", (dialog, which) -> deleteUser2(userId))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void deleteUser2(int userId) {
        // Mostrar diálogo de progreso
        AlertDialog progressDialog = new AlertDialog.Builder(this)
                .setTitle("Activando usuario")
                .setMessage("Por favor espere...")
                .setCancelable(false)
                .create();
        progressDialog.show();

        String apiUrl = getString(R.string.api_url);
        String url = apiUrl + "users/activate/" + userId + "/";
        String token = sharedPreferences.getString("access_token", null);

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token)
                .put(RequestBody.create("", MediaType.parse("application/json")))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(CreateUsers.this, "Error al activar usuario", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    if (response.isSuccessful()) {
                        Toast.makeText(CreateUsers.this, "Usuario activado exitosamente", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(CreateUsers.this, "Error al activar usuario", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }



    private void showUsersListDialog(JSONArray usersArray) {
        if (usersArray.length() == 0) {
            Toast.makeText(this, "No hay usuarios registrados", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Crear arrays para nombres e IDs
            String[] userNames = new String[usersArray.length()];
            final int[] userIds = new int[usersArray.length()];

            for (int i = 0; i < usersArray.length(); i++) {
                JSONObject user = usersArray.getJSONObject(i);
                userNames[i] = user.getString("first_name") + " " + user.getString("last_name") +
                        " (" + user.getString("email") + ")";
                userIds[i] = user.getInt("id");
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Seleccione usuario a eliminar");
            builder.setItems(userNames, (dialog, which) -> {
                int selectedUserId = userIds[which];
                String selectedUserName = userNames[which];
                showDeleteConfirmation(selectedUserId, selectedUserName);
            });
            builder.setNegativeButton("Cancelar", null);
            builder.show();

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al mostrar usuarios", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteConfirmation(int userId, String userName) {
        new AlertDialog.Builder(this)
                .setTitle("Confirmar eliminación")
                .setMessage("¿Está seguro de eliminar al usuario " + userName + "?")
                .setPositiveButton("Eliminar", (dialog, which) -> deleteUser(userId))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void deleteUser(int userId) {
        // Mostrar diálogo de progreso
        AlertDialog progressDialog = new AlertDialog.Builder(this)
                .setTitle("Eliminando usuario")
                .setMessage("Por favor espere...")
                .setCancelable(false)
                .create();
        progressDialog.show();

        String apiUrl = getString(R.string.api_url);
        String url = apiUrl + "users/deactivate/" + userId + "/";
        String token = sharedPreferences.getString("access_token", null);

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token)
                .put(RequestBody.create("", MediaType.parse("application/json")))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(CreateUsers.this, "Error al eliminar usuario", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    if (response.isSuccessful()) {
                        Toast.makeText(CreateUsers.this, "Usuario eliminado exitosamente", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(CreateUsers.this, "Error al eliminar usuario", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
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

        InputFilter[] filters = new InputFilter[] {
                new InputFilter.LengthFilter(15), // Máximo 15 caracteres (lada internacional)
                new DigitsKeyListener() // Solo permite dígitos
        };
        etTelefono.setFilters(filters);
        etTelefono.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() < 10) {
                    etTelefono.setError("El teléfono debe tener al menos 10 dígitos");
                } else {
                    etTelefono.setError(null);
                }
            }
        });
    }
    private void setupGenderDropdown() {
        // Definir opciones de género (solo Masculino y Femenino)
        String[] genderOptions = new String[]{"Masculino", "Femenino", "Otro"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                genderOptions
        );

        autoCompleteGender.setAdapter(adapter);
        autoCompleteGender.setThreshold(0); // Mostrar todas las opciones inmediatamente
        autoCompleteGender.setKeyListener(null); // Bloquear entrada de texto manual

        // Mostrar dropdown al hacer clic
        autoCompleteGender.setOnClickListener(v -> autoCompleteGender.showDropDown());
        autoCompleteGender.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) autoCompleteGender.showDropDown();
        });
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

        String gender = autoCompleteGender.getText().toString();
        if (!gender.equals("Masculino") && !gender.equals("Femenino") && !gender.equals("Otro")) {
            autoCompleteGender.setError("Seleccione Masculino o Femenino u Otro");
            autoCompleteGender.requestFocus();
            autoCompleteGender.showDropDown();
            return false;
        }

        // Validación del teléfono
        String phone = etTelefono.getText().toString();
        if (phone.length() < 10) {
            etTelefono.setError("El teléfono debe tener al menos 10 dígitos");
            etTelefono.requestFocus();
            return false;
        }

        if (phone.length() > 15) {
            etTelefono.setError("Máximo 15 dígitos (con lada internacional)");
            etTelefono.requestFocus();
            return false;
        }

        return true;
    }

    private void registerUser() {
        String apiUrl = getString(R.string.api_url);
        String url = apiUrl + "users/signup/";
        String token = sharedPreferences.getString("access_token", null);

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

            // Convertir género seleccionado a valor numérico
            String gender = autoCompleteGender.getText().toString();
            if (gender.equals("Masculino")) {
                jsonBody.put("genre", 0);
            } else if (gender.equals("Femenino")) {
                jsonBody.put("genre", 1);
            } else if (gender.equals("Otro")) {
                jsonBody.put("genre", 2);
            }

            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json; charset=utf-8"),
                    jsonBody.toString()
            );

            Request.Builder requestBuilder = new Request.Builder()
                    .url(url)
                    .post(body);

            // Solo agregar token si existe (para cuando admin crea usuarios)
            if (token != null) {
                requestBuilder.addHeader("Authorization", "Bearer " + token);
            }

            Request request = requestBuilder.build();

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
        return etNombre.getText().toString().substring(0, 3).toLowerCase().trim() +
                etApellido.getText().toString().substring(0, 3).toLowerCase().trim() +
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