package com.devst.appprueba;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

/**
 * Explícito 3: FormActivity -> ConfirmActivity, esperando un resultado
 * con registerForActivityResult(). Incluye las validaciones del formulario.
 */
public class FormActivity extends AppCompatActivity {

    EditText etNombre, etCorreo, etTelefono, etMensaje;
    Button btnEnviar;
    TextView tvResultado;

    // Recibe la respuesta que devuelve ConfirmActivity
    private final ActivityResultLauncher<Intent> launcherConfirmacion =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String respuesta = result.getData().getStringExtra("respuesta");
                    tvResultado.setText("✅ " + respuesta);
                    limpiarCampos();
                } else {
                    tvResultado.setText("❌ Envío cancelado");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_form);
        Utilidades.aplicarInsets(findViewById(R.id.rootForm));

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Formulario de contacto");
        }

        etNombre = findViewById(R.id.etNombre);
        etCorreo = findViewById(R.id.etCorreo);
        etTelefono = findViewById(R.id.etTelefono);
        etMensaje = findViewById(R.id.etMensaje);
        btnEnviar = findViewById(R.id.btnEnviar);
        tvResultado = findViewById(R.id.tvResultado);

        btnEnviar.setOnClickListener(v -> {
            if (!validarFormulario()) {
                return;
            }
            Intent intent = new Intent(FormActivity.this, ConfirmActivity.class);
            intent.putExtra("nombre", etNombre.getText().toString().trim());
            intent.putExtra("correo", etCorreo.getText().toString().trim());
            intent.putExtra("telefono", etTelefono.getText().toString().trim());
            intent.putExtra("mensaje", etMensaje.getText().toString().trim());
            launcherConfirmacion.launch(intent);
        });
    }

    /** Valida todos los campos; muestra el error en cada uno y enfoca el primero inválido. */
    private boolean validarFormulario() {
        String nombre = etNombre.getText().toString().trim();
        String correo = etCorreo.getText().toString().trim();
        String telefono = etTelefono.getText().toString().replaceAll("[\\s-]", "");
        String mensaje = etMensaje.getText().toString().trim();

        EditText primerError = null;

        if (nombre.isEmpty()) {
            etNombre.setError("Campo obligatorio");
            primerError = etNombre;
        } else if (nombre.length() < 3 || !nombre.matches("^[\\p{L} .'-]+$")) {
            etNombre.setError("Mínimo 3 letras, sin números ni símbolos");
            primerError = etNombre;
        }

        if (correo.isEmpty()) {
            etCorreo.setError("Campo obligatorio");
            if (primerError == null) primerError = etCorreo;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            etCorreo.setError("Correo no válido");
            if (primerError == null) primerError = etCorreo;
        }

        if (telefono.isEmpty()) {
            etTelefono.setError("Campo obligatorio");
            if (primerError == null) primerError = etTelefono;
        } else if (!telefono.matches("^\\+?[0-9]{8,12}$")) {
            etTelefono.setError("Teléfono no válido (8 a 12 dígitos)");
            if (primerError == null) primerError = etTelefono;
        }

        if (mensaje.length() < 10) {
            etMensaje.setError("El mensaje debe tener al menos 10 caracteres");
            if (primerError == null) primerError = etMensaje;
        }

        if (primerError != null) {
            primerError.requestFocus();
            return false;
        }
        return true;
    }

    private void limpiarCampos() {
        etNombre.setText("");
        etCorreo.setText("");
        etTelefono.setText("");
        etMensaje.setText("");
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
