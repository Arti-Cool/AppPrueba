package com.devst.appprueba;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

/**
 * Explícito 2: pantalla de ajustes interna con Toolbar y botón "Atrás".
 */
public class ConfigActivity extends AppCompatActivity {

    private static final String PREFS = "ajustes";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_config);
        Utilidades.aplicarInsets(findViewById(R.id.rootConfig));

        // Toolbar con flecha de retroceso
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Configuración");
        }

        // Ajustes guardados en SharedPreferences
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        SwitchCompat swNotificaciones = findViewById(R.id.swNotificaciones);
        SwitchCompat swSonido = findViewById(R.id.swSonido);

        swNotificaciones.setChecked(prefs.getBoolean("notificaciones", true));
        swSonido.setChecked(prefs.getBoolean("sonido", true));

        swNotificaciones.setOnCheckedChangeListener((b, activo) ->
                prefs.edit().putBoolean("notificaciones", activo).apply());
        swSonido.setOnCheckedChangeListener((b, activo) ->
                prefs.edit().putBoolean("sonido", activo).apply());
    }

    // Se ejecuta al tocar la flecha "Atrás" de la Toolbar
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
