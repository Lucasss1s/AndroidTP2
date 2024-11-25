package com.example.gestionempleados;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.gestionempleados.databinding.ActivityRegistroBinding;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RegistroActivity extends AppCompatActivity {

    ActivityRegistroBinding binding;
    BaseDatosSQLite baseDatosSQLite;
    ExecutorService executorService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegistroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        baseDatosSQLite = new BaseDatosSQLite(this);
        executorService = Executors.newSingleThreadExecutor();

        binding.registroBoton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = binding.registroEmail.getText().toString();
                String contraseña = binding.registroContrasena.getText().toString();
                String confirmar = binding.registroConfirmar.getText().toString();

                if (email.equals("") || contraseña.equals("") || confirmar.equals("")){
                    Toast.makeText(RegistroActivity.this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
                }else {
                    if (contraseña.equals(confirmar)) {
                        executorService.execute(() -> {
                            Boolean verificarEmailUsuario = baseDatosSQLite.verificarEmail(email);

                            runOnUiThread(() -> {
                                if (!verificarEmailUsuario) {
                                    executorService.execute(() -> {
                                        Boolean insertar = baseDatosSQLite.insertData(email, contraseña);

                                        runOnUiThread(() -> {
                                            if (insertar) {
                                                Toast.makeText(RegistroActivity.this, "Registro completado", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(getApplicationContext(), IngresoActivity.class);
                                                startActivity(intent);
                                            } else {
                                                Toast.makeText(RegistroActivity.this, "Fallo el registro", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    });
                                } else {
                                    Toast.makeText(RegistroActivity.this, "El usuario ya existe, presione Ingresar", Toast.LENGTH_SHORT).show();
                                }
                            });
                        });
                    } else {
                        Toast.makeText(RegistroActivity.this, "Contraseña invalida", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        binding.redirijirIngreso.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), IngresoActivity.class);
                startActivity(intent);
            }
        });
    }

    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}