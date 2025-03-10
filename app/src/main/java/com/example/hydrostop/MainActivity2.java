package com.example.hydrostop;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity2 extends AppCompatActivity {

    Button btnconfig_time;
    private static final int REQUEST_TIME_CONFIG = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main2);


        btnconfig_time = findViewById(R.id.btnconfig_time);


        btnconfig_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity2.this, MainActivity3.class);
                startActivityForResult(intent, REQUEST_TIME_CONFIG);
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_TIME_CONFIG && resultCode == RESULT_OK) {
            if (data != null) {
                String timeLimit = data.getStringExtra("TIME_LIMIT");
                String notificationTime = data.getStringExtra("NOTIFICATION_TIME");


                btnconfig_time.setText("CONFIGURAR TIEMPO DE USO\n" + timeLimit);
            }
        }
    }
}
