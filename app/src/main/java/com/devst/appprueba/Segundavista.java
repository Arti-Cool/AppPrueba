package com.devst.appprueba;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.Locale;

/**
 * Pantalla de detalle. Recibe datos por putExtra (Explícito 1) y
 * lanza 4 intents implícitos: web, marcador, correo y calendario.
 */
public class Segundavista extends AppCompatActivity {

    TextView tvNombre, tvCoordenadas;
    EditText etUrl, etTelefono, etCorreo;
    Button btnWeb, btnLlamar, btnCorreo, btnCalendario, btnVolver;

    String nombre;
    double lat, lng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_segundavista);
        Utilidades.aplicarInsets(findViewById(R.id.rootSegunda));

        tvNombre = findViewById(R.id.tvNombre);
        tvCoordenadas = findViewById(R.id.tvCoordenadas);
        etUrl = findViewById(R.id.etUrl);
        etTelefono = findViewById(R.id.etTelefono);
        etCorreo = findViewById(R.id.etCorreo);
        btnWeb = findViewById(R.id.btnWeb);
        btnLlamar = findViewById(R.id.btnLlamar);
        btnCorreo = findViewById(R.id.btnCorreo);
        btnCalendario = findViewById(R.id.btnCalendario);
        btnVolver = findViewById(R.id.btnVolver);

        // Recibir los extras enviados desde Login
        nombre = getIntent().getStringExtra("nombre");
        if (nombre == null) nombre = "Lugar sin nombre";
        lat = getIntent().getDoubleExtra("lat", Login.LAT_DEFECTO);
        lng = getIntent().getDoubleExtra("lng", Login.LNG_DEFECTO);

        tvNombre.setText("📍 " + nombre);
        tvCoordenadas.setText(String.format(Locale.US, "Lat: %.5f  |  Lng: %.5f", lat, lng));

        btnWeb.setOnClickListener(v -> abrirWeb());
        btnLlamar.setOnClickListener(v -> abrirMarcador());
        btnCorreo.setOnClickListener(v -> enviarCorreo());
        btnCalendario.setOnClickListener(v -> agregarAlCalendario());
        btnVolver.setOnClickListener(v -> finish());
    }

    // ---------- INTENT IMPLÍCITO 2: página web (ACTION_VIEW + https://) ----------
    private void abrirWeb() {
        String url = etUrl.getText().toString().trim();
        if (url.isEmpty()) {
            etUrl.setError("Ingresa una dirección web");
            return;
        }
        // Si el usuario escribió "santotomas.cl", se completa con https://
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }
        if (!Patterns.WEB_URL.matcher(url).matches()) {
            etUrl.setError("Dirección web no válida");
            return;
        }
        Utilidades.abrirIntent(this, new Intent(Intent.ACTION_VIEW, Uri.parse(url)),
                "No hay un navegador instalado");
    }

    // ---------- INTENT IMPLÍCITO 3: marcador telefónico (ACTION_DIAL + tel:) ----------
    // ACTION_DIAL solo abre el marcador: no necesita el permiso CALL_PHONE
    private void abrirMarcador() {
        String telefono = etTelefono.getText().toString().replaceAll("[\\s-]", "");
        if (!telefono.matches("^\\+?[0-9]{8,12}$")) {
            etTelefono.setError("Teléfono no válido (8 a 12 dígitos, puede iniciar con +)");
            return;
        }
        Utilidades.abrirIntent(this, new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + telefono)),
                "No hay una app de teléfono disponible");
    }

    // ---------- INTENT IMPLÍCITO 4: correo (ACTION_SENDTO + mailto:) ----------
    private void enviarCorreo() {
        String correo = etCorreo.getText().toString().trim();
        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            etCorreo.setError("Correo no válido");
            return;
        }
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // solo apps de correo
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{correo});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Consulta sobre: " + nombre);
        intent.putExtra(Intent.EXTRA_TEXT, "Hola, quisiera más información sobre " + nombre + ".");
        Utilidades.abrirIntent(this, intent, "No hay una app de correo instalada");
    }

    // ---------- INTENT IMPLÍCITO 5: calendario (ACTION_INSERT + Events.CONTENT_URI) ----------
    private void agregarAlCalendario() {
        // Evento de ejemplo: mañana a las 10:00, con una hora de duración
        Calendar inicio = Calendar.getInstance();
        inicio.add(Calendar.DAY_OF_YEAR, 1);
        inicio.set(Calendar.HOUR_OF_DAY, 10);
        inicio.set(Calendar.MINUTE, 0);
        inicio.set(Calendar.SECOND, 0);
        Calendar fin = (Calendar) inicio.clone();
        fin.add(Calendar.HOUR_OF_DAY, 1);

        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.setData(CalendarContract.Events.CONTENT_URI);
        intent.putExtra(CalendarContract.Events.TITLE, "Visita: " + nombre);
        intent.putExtra(CalendarContract.Events.EVENT_LOCATION, nombre);
        intent.putExtra(CalendarContract.Events.DESCRIPTION,
                String.format(Locale.US, "Coordenadas: %.5f, %.5f", lat, lng));
        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, inicio.getTimeInMillis());
        intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, fin.getTimeInMillis());
        Utilidades.abrirIntent(this, intent, "No hay una app de calendario instalada");
    }
}
