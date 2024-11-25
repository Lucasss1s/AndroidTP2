package com.example.gestionempleados;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    BaseDatosSQLite baseDatosSQLite;
    private String emailUsuario;
    ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        baseDatosSQLite = new BaseDatosSQLite(this);
        executorService = Executors.newSingleThreadExecutor();

        Button cerrarSesionBoton = findViewById(R.id.cerrar_sesion_empleado);
        Button registrarEntrada = findViewById(R.id.registrar_entrada);
        Button finalizarAsistencia = findViewById(R.id.finalizar_asistencia);
        Button verHistorial = findViewById(R.id.ver_historial);
        Button revertirAsistencia= findViewById(R.id.revertir_asistencia);

        emailUsuario = getIntent().getStringExtra("email_usuario");


        cerrarSesionBoton.setOnClickListener(v -> {
            new AlertDialog.Builder(MainActivity.this)
                .setTitle("Cerrar Sesión")
                .setMessage("¿Está seguro que desea cerrar sesión?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    baseDatosSQLite.cerrarSesion(MainActivity.this);
                    Intent intent = new Intent(MainActivity.this, IngresoActivity.class);
                    startActivity(intent);
                    finish();
            })
               .setNegativeButton("No", null)
               .show();
        });

        registrarEntrada.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Registrar Entrada");
            builder.setMessage("Ingrese la descripción de la tarea:");

            final EditText input = new EditText(MainActivity.this);
            input.setHint("Descripción de la tarea");
            builder.setView(input);

            builder.setPositiveButton("Registrar", (dialog, which) -> {
                String descripcion = input.getText().toString().trim();
                if (descripcion.isEmpty()) {
                    Toast.makeText(MainActivity.this, "La descripción no puede estar vacía.", Toast.LENGTH_SHORT).show();
                    return;
                }

                executorService.execute(() -> {
                    boolean resultado = baseDatosSQLite.registrarEntrada(emailUsuario, descripcion);

                    runOnUiThread(() -> {
                        if (resultado) {
                            Toast.makeText(MainActivity.this, "Entrada registrada con éxito.", Toast.LENGTH_SHORT).show();
                            revertirAsistencia.setEnabled(true);
                        } else {
                            Toast.makeText(MainActivity.this, "Ya tienes una entrada registrada hoy.", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            });

            builder.setNegativeButton("Cancelar", null);
            builder.show();
        });

        finalizarAsistencia.setOnClickListener(v -> {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Confirmar acción")
                    .setMessage("¿Estás seguro de que deseas finalizar tu asistencia de hoy? Esta acción no se puede deshacer.")
                    .setPositiveButton("Sí, finalizar", (dialog, which) -> {

                        executorService.execute(() -> {
                            boolean resultado = baseDatosSQLite.registrarSalida(emailUsuario);

                            runOnUiThread(() -> {
                                if (resultado) {
                                    Toast.makeText(MainActivity.this, "Asistencia finalizada con éxito.", Toast.LENGTH_SHORT).show();
                                    revertirAsistencia.setEnabled(false);
                                } else {
                                    Toast.makeText(MainActivity.this, "No tienes una asistencia activa para hoy.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        });
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });

        revertirAsistencia.setOnClickListener(v -> {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Confirmar acción")
                    .setMessage("¿Estás seguro de que deseas revertir la asistencia de hoy? Esta acción no se puede deshacer.")
                    .setPositiveButton("Sí, revertir", (dialog, which) -> {

                        executorService.execute(() -> {
                            boolean resultado = baseDatosSQLite.revertirAsistenciaHoy(emailUsuario);

                            runOnUiThread(() -> {
                                if (resultado) {
                                    Toast.makeText(MainActivity.this, "Asistencia revertida con éxito.", Toast.LENGTH_SHORT).show();
                                    revertirAsistencia.setEnabled(false);
                                } else {
                                    Toast.makeText(MainActivity.this, "No tienes una asistencia activa para hoy.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        });
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });

        verHistorial.setOnClickListener(v -> {
            ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("Cargando historial...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            executorService.execute(() -> {

                Cursor cursor = baseDatosSQLite.obtenerHistorialAsistencias(emailUsuario);
                List<String> historial = new ArrayList<>();
                while (cursor.moveToNext()) {
                    String fecha = cursor.getString(0);
                    String entrada = cursor.getString(1);
                    String salida = cursor.getString(2);
                    String descripcion = cursor.getString(3);
                    historial.add(fecha + " - Entrada: " + entrada + " - Salida: " + salida + " - " + descripcion);
                }
                cursor.close();

                runOnUiThread(() -> {
                    progressDialog.dismiss();

                    View popupView = getLayoutInflater().inflate(R.layout.historial_popup, null);
                    ListView listaHistorial = popupView.findViewById(R.id.lista_historial);
                    Button cerrarHistorial = popupView.findViewById(R.id.cerrar_historial);

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, historial);
                    listaHistorial.setAdapter(adapter);

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setView(popupView);
                    AlertDialog dialog = builder.create();

                    cerrarHistorial.setOnClickListener(view -> dialog.dismiss());

                    dialog.show();
                });
            });
        });


    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
