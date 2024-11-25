package com.example.gestionempleados;

import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class UsuarioAdapter extends RecyclerView.Adapter<UsuarioAdapter.UsuarioViewHolder> {

    private Cursor cursor;

    // Constructor para recibir el cursor
    public UsuarioAdapter(Cursor cursor) {
        this.cursor = cursor;
    }

    @NonNull
    @Override
    public UsuarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflar la vista para cada ítem
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_usuario, parent, false);
        return new UsuarioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsuarioViewHolder holder, int position) {
        if (cursor != null && cursor.moveToPosition(position)) {
            // Obtener datos del cursor
            String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
            String rol = cursor.getString(cursor.getColumnIndexOrThrow("rol"));

            // Configurar los datos en las vistas
            holder.emailTextView.setText(email);
            holder.roleTextView.setText(rol);
        }
    }

    @Override
    public int getItemCount() {
        return cursor != null ? cursor.getCount() : 0;
    }

    // Clase interna para representar los ViewHolders
    static class UsuarioViewHolder extends RecyclerView.ViewHolder {
        TextView emailTextView;
        TextView roleTextView;

        public UsuarioViewHolder(@NonNull View itemView) {
            super(itemView);
            emailTextView = itemView.findViewById(R.id.campo_email);
            roleTextView = itemView.findViewById(R.id.campo_rol);
        }
    }
}
