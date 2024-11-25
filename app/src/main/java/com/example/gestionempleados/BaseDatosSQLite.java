package com.example.gestionempleados;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class BaseDatosSQLite extends SQLiteOpenHelper {

    public static final String nombreBaseDatos = "GestionEmpleados.db";

    public BaseDatosSQLite(@Nullable Context context) {
        super(context, "GestionEmpleados.db", null, 3);
    }

    @Override
    public void onCreate(SQLiteDatabase SQLiteDatabase) {
        SQLiteDatabase.execSQL("Create Table allUsuarios(email TEXT primary key, contraseña TEXT, rol TEXT DEFAULT 'Empleado')");
        SQLiteDatabase.execSQL("Create Table asistencias(" +
                "id_asistencia INTEGER primary key AUTOINCREMENT, " +
                "id_usuario TEXT, " +
                "fecha DATE, " +
                "hora_entrada TIME, " +
                "hora_salida TIME, " +
                "descripcion_tarea TEXT, " +
                "estado TEXT DEFAULT 'En curso', " +
                "Foreign key (id_usuario) REFERENCES allUsuarios(email))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase SQLiteDatabase, int oldVersion, int newVersion) {
        SQLiteDatabase.execSQL("Drop Table if exists allUsuarios");
        SQLiteDatabase.execSQL("Drop Table if exists asistencias");
        onCreate(SQLiteDatabase);
    }

    public Boolean insertData(String email, String contraseña){
        SQLiteDatabase SQLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("email", email);
        contentValues .put("contraseña", contraseña);
        long resultado = SQLiteDatabase.insert("allUsuarios", null, contentValues);

        if (resultado == -1){
            return false;
        }else{
            return true;
        }

    }

    public Boolean verificarEmail(String email){
        SQLiteDatabase SQLiteDatabase = this.getWritableDatabase();
        Cursor cursor = SQLiteDatabase.rawQuery("Select * from allUsuarios where email = ?", new String[]{email});

        if (cursor.getCount() > 0){
            return true;
        }else{
            return false;
        }
    }

    public Boolean verificarEmailContraseña(String email, String contraseña){
        SQLiteDatabase SQLiteDatabase = this.getWritableDatabase();
        Cursor cursor = SQLiteDatabase.rawQuery("Select * from allUsuarios where email = ? and contraseña = ?", new String[]{email, contraseña});

        if (cursor.getCount() > 0){
            return true;
        }else{
            return false;
        }
    }

    public void cerrarSesion(Context context) {
        context.getSharedPreferences("SesionUsuario", Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply();
    }

    public void crearAdminDefecto() {
        SQLiteDatabase SQLiteDatabase = this.getWritableDatabase();
        Cursor cursor = SQLiteDatabase.rawQuery("SELECT * FROM allUsuarios WHERE rol = 'Admin'", null);

        if (!cursor.moveToFirst()) {
            ContentValues contentValues = new ContentValues();
            contentValues.put("email", "admin@admin");
            contentValues.put("contraseña", "admin123");
            contentValues.put("rol", "Admin");
            SQLiteDatabase.insert("allUsuarios", null, contentValues);
        }
        cursor.close();
    }

    public String obtenerRol(String email, String contraseña) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT rol FROM allUsuarios WHERE email = ? AND contraseña = ?", new String[]{email, contraseña});

        if (cursor != null && cursor.moveToFirst()) {
            try {
                String rol = cursor.getString(cursor.getColumnIndexOrThrow("rol"));
                cursor.close();
                return rol;
            } catch (IllegalArgumentException e) {
                cursor.close();
                return null;
            }
        } else {
            cursor.close();
            return null;
        }
    }

    public Boolean crearUsuario(String email, String contraseña, String rol) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("email", email);
        contentValues.put("contraseña", contraseña);
        contentValues.put("rol", rol);
        long resultado = db.insert("allUsuarios", null, contentValues);

        return resultado != -1;
    }

    public Cursor obtenerTodosUsuarios() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT email, rol FROM allUsuarios", null);
    }

    public Cursor obtenerUsuarioPorEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM allUsuarios WHERE email = ?", new String[]{email});
    }


    public Boolean actualizarUsuario(String email, String nuevoEmail, String nuevaContraseña, String nuevoRol) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("email", nuevoEmail);
        contentValues.put("contraseña", nuevaContraseña);
        contentValues.put("rol", nuevoRol);

        int resultado = db.update("allUsuarios", contentValues, "email = ?", new String[]{email});

        return resultado > 0;
    }

    public boolean esEmailUnico(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("allUsuarios", null, "email = ?", new String[]{email}, null, null, null);
        boolean isUnique = !cursor.moveToFirst();
        cursor.close();
        return isUnique;
    }



    public Boolean eliminarUsuario(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("asistencias", "id_usuario = ?", new String[]{email});
        int resultado = db.delete("allUsuarios", "email = ?", new String[]{email});
        return resultado > 0;
    }


    public boolean registrarEntrada(String email, String descripcion) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            Cursor cursor = db.rawQuery("SELECT * FROM asistencias WHERE id_usuario = ? AND fecha = date('now')", new String[]{email});
            if (cursor.getCount() > 0) {
                cursor.close();
                return false;
            }
            cursor.close();

            ContentValues contentValues = new ContentValues();
            contentValues.put("id_usuario", email);
            contentValues.put("fecha", new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()));
            contentValues.put("hora_entrada", new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date()));
            contentValues.put("descripcion_tarea", descripcion);
            long resultado = db.insert("asistencias", null, contentValues);

            return resultado != -1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean verificarTablaAsistencias() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='asistencias'", null);
        boolean existe = cursor.getCount() > 0;
        cursor.close();
        return existe;
    }

    public boolean registrarSalida(String email) {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT id_asistencia FROM asistencias WHERE id_usuario = ? AND fecha = date('now') AND estado = 'En curso'", new String[]{email});
        if (cursor.moveToFirst()) {
            String idAsistencia = cursor.getString(0);
            cursor.close();

            ContentValues contentValues = new ContentValues();
            contentValues.put("hora_salida", new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date()));
            contentValues.put("estado", "Finalizado");
            int resultado = db.update("asistencias", contentValues, "id_asistencia = ?", new String[]{idAsistencia});
            return resultado > 0;
        }
        cursor.close();
        return false;
    }

    public Cursor obtenerHistorialAsistencias(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT fecha, hora_entrada, hora_salida, descripcion_tarea " +
                        "FROM asistencias WHERE id_usuario = ? ORDER BY fecha DESC",
                new String[]{email}
        );
    }

    public boolean revertirAsistenciaHoy(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT id_asistencia FROM asistencias WHERE id_usuario = ? AND fecha = date('now') AND estado = 'En curso'",
                new String[]{email}
        );

        if (cursor.moveToFirst()) {
            String idAsistencia = cursor.getString(0); // Obtener el ID de la asistencia
            cursor.close();

            int resultado = db.delete("asistencias", "id_asistencia = ?", new String[]{idAsistencia});
            return resultado > 0;
        }
        cursor.close();
        return false;
    }

    public boolean hayAsistenciaActiva(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT id_asistencia FROM asistencias WHERE id_usuario = ? AND fecha = date('now') AND estado = 'En curso'",
                new String[]{email}
        );

        boolean asistenciaActiva = cursor.moveToFirst();
        cursor.close();
        return asistenciaActiva;
    }












}
