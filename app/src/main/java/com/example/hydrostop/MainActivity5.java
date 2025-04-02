package com.example.hydrostop;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity5 extends AppCompatActivity {

    EditText editNewPassword, editConfirmPassword;
    Button btnSavePassword;
    SharedPreferences sharedPreferences;
    private TextView textUser;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main5);

        // Vinculación con la UI
        editNewPassword = findViewById(R.id.edit_new_password);
        editConfirmPassword = findViewById(R.id.edit_confirm_password);
        btnSavePassword = findViewById(R.id.btn_save_password);
        textUser = findViewById(R.id.text_user);

        sharedPreferences = getSharedPreferences("HydroStopPrefs", MODE_PRIVATE);
        loadUserData();
        // Botón para guardar la contraseña
        btnSavePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newPassword = editNewPassword.getText().toString();
                String confirmPassword = editConfirmPassword.getText().toString();

                if (newPassword.equals(confirmPassword) && !newPassword.isEmpty()) {
                    Toast.makeText(MainActivity5.this, "Contraseña guardada", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity5.this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void loadUserData() {
        String first_name = sharedPreferences.getString("first_name", "Usuario");
        String role = sharedPreferences.getString("role", "user");
        textUser.setText(first_name + " (" + role + ")");
    }
}
