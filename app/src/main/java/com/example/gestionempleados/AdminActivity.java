package com.example.gestionempleados;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdminActivity extends AppCompatActivity {

    BaseDatosSQLite baseDatosSQLite;
    ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        baseDatosSQLite = new BaseDatosSQLite(this);
        executorService = Executors.newSingleThreadExecutor();

        Button cerrarSesionBoton = findViewById(R.id.cerrar_sesion_admin);
        Button crearUsuarioBoton = findViewById(R.id.crear_usuario_btn);
        Button actualizarUsuarioBoton = findViewById(R.id.actualizar_usuario_btn);
        Button eliminarUsuarioBoton = findViewById(R.id.eliminar_usuario_btn);
        Button mostrarUsuarios = findViewById(R.id.mostrar_usuarios);


        cerrarSesionBoton.setOnClickListener(v -> {
            new AlertDialog.Builder(AdminActivity.this)
                    .setTitle("Cerrar Sesión")
                    .setMessage("¿Está seguro que desea cerrar sesión?")
                    .setPositiveButton("Sí", (dialog, which) -> {
                        baseDatosSQLite.cerrarSesion(AdminActivity.this);
                        startActivity(new Intent(AdminActivity.this, IngresoActivity.class));
                        finish();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        mostrarUsuarios.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ExecutorService executor = Executors.newSingleThreadExecutor();
                Handler handler = new Handler(Looper.getMainLooper());

                executor.execute(() -> {
                    Cursor cursor = baseDatosSQLite.obtenerTodosUsuarios();
                    List<String> usuarios = new ArrayList<>();
                    while (cursor.moveToNext()) {
                        String email = cursor.getString(0);
                        String rol = cursor.getString(1);
                        usuarios.add(email + " - Rol: " + rol);
                    }
                    cursor.close();

                    handler.post(() -> {
                        View popupView = getLayoutInflater().inflate(R.layout.usuarios_popup, null);

                        ListView listaUsuarios = popupView.findViewById(R.id.lista_usuarios);
                        Button cerrarUsuarios = popupView.findViewById(R.id.cerrar_usuarios);

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(AdminActivity.this, android.R.layout.simple_list_item_1, usuarios);
                        listaUsuarios.setAdapter(adapter);

                        AlertDialog.Builder builder = new AlertDialog.Builder(AdminActivity.this);
                        builder.setView(popupView);
                        AlertDialog dialog = builder.create();

                        cerrarUsuarios.setOnClickListener(view -> dialog.dismiss());

                        dialog.show();
                    });
                });
            }
        });


        crearUsuarioBoton.setOnClickListener(v -> crearUsuario());

        actualizarUsuarioBoton.setOnClickListener(v -> actualizarUsuario());

        eliminarUsuarioBoton.setOnClickListener(v -> eliminarUsuario());
    }


    private void crearUsuario() {
        View popupView = getLayoutInflater().inflate(R.layout.popup_crear_usuario, null);

        EditText popupEmailInput = popupView.findViewById(R.id.popup_email_input);
        EditText popupPasswordInput = popupView.findViewById(R.id.popup_password_input);
        Spinner popupRoleSpinner = popupView.findViewById(R.id.popup_rol_spinner);
        Button popupCrearUsuarioBtn = popupView.findViewById(R.id.popup_crear_usuario_btn);
        Button popupCancelarBtn = popupView.findViewById(R.id.popup_cancelar_btn);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.roles_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        popupRoleSpinner.setAdapter(adapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(AdminActivity.this);
        builder.setView(popupView);
        AlertDialog dialog = builder.create();

        popupCrearUsuarioBtn.setOnClickListener(view -> {
            String email = popupEmailInput.getText().toString();
            String password = popupPasswordInput.getText().toString();
            String rol = popupRoleSpinner.getSelectedItem().toString();

            if (email.isEmpty() || password.isEmpty() || rol.isEmpty()) {
                Toast.makeText(AdminActivity.this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
            } else {
                executorService.execute(() -> {
                    boolean resultado = baseDatosSQLite.crearUsuario(email, password, rol);
                    runOnUiThread(() -> {
                        if (resultado) {
                            Toast.makeText(AdminActivity.this, "Usuario creado", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        } else {
                            Toast.makeText(AdminActivity.this, "Error al crear el usuario", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            }
        });

        popupCancelarBtn.setOnClickListener(view -> dialog.dismiss());

        dialog.show();
    }

    @SuppressLint("Range")
    private void actualizarUsuario() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        View popupView = getLayoutInflater().inflate(R.layout.popup_actualizar_usuario, null);

        EditText buscarEmailInput = popupView.findViewById(R.id.popup_buscar_email_input);
        Button buscarEmailBtn = popupView.findViewById(R.id.popup_buscar_email_btn);
        EditText popupEmailInput = popupView.findViewById(R.id.popup_actualizar_email_input);
        EditText popupPasswordInput = popupView.findViewById(R.id.popup_actualizar_password_input);
        Spinner popupRoleSpinner = popupView.findViewById(R.id.popup_actualizar_rol_spinner);
        Button popupActualizarUsuarioBtn = popupView.findViewById(R.id.popup_actualizar_usuario_btn);
        Button popupCancelarBtn = popupView.findViewById(R.id.popup_actualizar_cancelar_btn);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.roles_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        popupRoleSpinner.setAdapter(adapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(AdminActivity.this);
        builder.setView(popupView);
        AlertDialog dialog = builder.create();

        popupEmailInput.setVisibility(View.GONE);
        popupPasswordInput.setVisibility(View.GONE);
        popupRoleSpinner.setVisibility(View.GONE);
        popupActualizarUsuarioBtn.setVisibility(View.GONE);

        buscarEmailBtn.setOnClickListener(view -> {
            String email = buscarEmailInput.getText().toString();

            if (email.isEmpty()) {
                Toast.makeText(AdminActivity.this, "Por favor, ingrese un email", Toast.LENGTH_SHORT).show();
                return;
            }

            executorService.execute(() -> {
                Cursor cursor = baseDatosSQLite.obtenerUsuarioPorEmail(email);
                if (cursor != null && cursor.moveToFirst()) {
                    runOnUiThread(() -> {
                        popupEmailInput.setVisibility(View.VISIBLE);
                        popupPasswordInput.setVisibility(View.VISIBLE);
                        popupRoleSpinner.setVisibility(View.VISIBLE);
                        popupActualizarUsuarioBtn.setVisibility(View.VISIBLE);

                        popupEmailInput.setText(cursor.getString(cursor.getColumnIndex("email")));
                        popupPasswordInput.setText(cursor.getString(cursor.getColumnIndex("contraseña")));

                        String rolActual = cursor.getString(cursor.getColumnIndex("rol"));
                        int position = adapter.getPosition(rolActual);
                        popupRoleSpinner.setSelection(position);

                        cursor.close();
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(AdminActivity.this, "Usuario no encontrado", Toast.LENGTH_SHORT).show());
                }
            });
        });

        popupActualizarUsuarioBtn.setOnClickListener(view -> {
            String emailActual = buscarEmailInput.getText().toString();
            String nuevoEmail = popupEmailInput.getText().toString();
            String nuevaPassword = popupPasswordInput.getText().toString();
            String nuevoRole = popupRoleSpinner.getSelectedItem().toString();

            if (nuevoEmail.isEmpty() || nuevaPassword.isEmpty() || nuevoRole.isEmpty()) {
                Toast.makeText(AdminActivity.this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
                return;
            }

            executorService.execute(() -> {
                if (!baseDatosSQLite.esEmailUnico(nuevoEmail) && !nuevoEmail.equals(emailActual)) {
                    runOnUiThread(() -> Toast.makeText(AdminActivity.this, "El nuevo email ya está en uso", Toast.LENGTH_SHORT).show());
                    return;
                }

                boolean actualizado = baseDatosSQLite.actualizarUsuario(emailActual, nuevoEmail, nuevaPassword, nuevoRole);
                runOnUiThread(() -> {
                    if (actualizado) {
                        Toast.makeText(AdminActivity.this, "Usuario actualizado", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(AdminActivity.this, "Error al actualizar el usuario", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });

        popupCancelarBtn.setOnClickListener(view -> dialog.dismiss());

        dialog.show();
    }



    @SuppressLint("Range")
    private void eliminarUsuario() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        View popupView = getLayoutInflater().inflate(R.layout.popup_eliminar_usuario, null);

        EditText emailInputPopup = popupView.findViewById(R.id.popup_eliminar_email_input);
        Button eliminarUsuarioBtn = popupView.findViewById(R.id.popup_eliminar_usuario_btn);
        Button cancelarBtn = popupView.findViewById(R.id.popup_eliminar_cancelar_btn);

        AlertDialog.Builder builder = new AlertDialog.Builder(AdminActivity.this);
        builder.setView(popupView);
        AlertDialog dialog = builder.create();

        eliminarUsuarioBtn.setOnClickListener(view -> {
            String email = emailInputPopup.getText().toString();

            if (email.isEmpty()) {
                Toast.makeText(AdminActivity.this, "El email es obligatorio", Toast.LENGTH_SHORT).show();
                return;
            }

            executorService.execute(() -> {
                boolean eliminado = baseDatosSQLite.eliminarUsuario(email);
                runOnUiThread(() -> {
                    if (eliminado) {
                        Toast.makeText(AdminActivity.this, "Usuario eliminado", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(AdminActivity.this, "Error al eliminar el usuario", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });

        cancelarBtn.setOnClickListener(view -> dialog.dismiss());

        dialog.show();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!executorService.isShutdown()) {
            executorService.shutdown();
        }
    }



}
