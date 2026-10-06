package com.devst.appprueba;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

/**
 * Recibe los datos del formulario y devuelve una respuesta (setResult) a FormActivity.
 */
public class ConfirmActivity extends AppCompatActivity {

    private String nombre;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_confirm);
        Utilidades.aplicarInsets(findViewById(R.id.rootConfirm));

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Confirmar datos");
        }

        // Datos recibidos desde FormActivity
        nombre = getIntent().getStringExtra("nombre");
        String correo = getIntent().getStringExtra("correo");
        String telefono = getIntent().getStringExtra("telefono");
        String mensaje = getIntent().getStringExtra("mensaje");

        TextView tvResumen = findViewById(R.id.tvResumen);
        tvResumen.setText("👤 Nombre: " + nombre
                + "\n✉️ Correo: " + correo
                + "\n📞 Teléfono: " + telefono
                + "\n💬 Mensaje: " + mensaje);

        // Confirmar: devuelve RESULT_OK con una respuesta
        findViewById(R.id.btnConfirmar).setOnClickListener(v -> {
            Intent data = new Intent();
            data.putExtra("respuesta", "Solicitud confirmada para " + nombre);
            setResult(RESULT_OK, data);
            finish();
        });

        // Cancelar: devuelve RESULT_CANCELED
        findViewById(R.id.btnCancelar).setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });
    }

    // Flecha "Atrás" de la Toolbar: se considera cancelado
    @Override
    public boolean onSupportNavigateUp() {
        setResult(RESULT_CANCELED);
        finish();
        return true;
    }
}
