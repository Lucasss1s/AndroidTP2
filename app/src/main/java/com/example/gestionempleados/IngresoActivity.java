package com.example.gestionempleados;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gestionempleados.databinding.ActivityIngresoBinding;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IngresoActivity extends AppCompatActivity {

    ActivityIngresoBinding binding;
    BaseDatosSQLite baseDatosSQLite;
    ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityIngresoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        baseDatosSQLite = new BaseDatosSQLite(this);
        executorService = Executors.newSingleThreadExecutor();

        baseDatosSQLite.crearAdminDefecto();

        binding.ingresoBoton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = binding.ingresoEmail.getText().toString();
                String contraseña = binding.ingresoContrasena.getText().toString();

                if (email.equals("") || contraseña.equals("")) {
                    Toast.makeText(IngresoActivity.this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
                } else {
                    executorService.execute(() -> {
                        Boolean verificarDatos = baseDatosSQLite.verificarEmailContraseña(email, contraseña);

                        runOnUiThread(() -> {
                            if (verificarDatos) {
                                executorService.execute(() -> {
                                    String rol = baseDatosSQLite.obtenerRol(email, contraseña);

                                    runOnUiThread(() -> {
                                        if (rol != null) {
                                            if (rol.equals("Admin")) {
                                                Intent intent = new Intent(IngresoActivity.this, AdminActivity.class);
                                                startActivity(intent);
                                            } else {
                                                Intent intent = new Intent(IngresoActivity.this, MainActivity.class);
                                                intent.putExtra("email_usuario", email);
                                                startActivity(intent);
                                            }
                                            Toast.makeText(IngresoActivity.this, "Ingreso con éxito", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(IngresoActivity.this, "Error al obtener rol", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                });
                            } else {
                                Toast.makeText(IngresoActivity.this, "Email o contraseña invalidos", Toast.LENGTH_SHORT).show();
                            }
                        });
                    });
                }
            }
        });

        binding.redirijirRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), RegistroActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
