package com.devst.appprueba;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.Locale;

public class Login extends AppCompatActivity {

    // Coordenadas por defecto (Plaza de Armas, Santiago) si no hay ubicación real
    static final double LAT_DEFECTO = -33.4378;
    static final double LNG_DEFECTO = -70.6505;

    //Creación de Variables Encapsuladas
    Button btnLinterna;
    Button btnSegundavista;
    Button btnUbicacion;
    Button btnMapa;
    Button btnConfig;
    Button btnFormulario;
    TextView tvUbicacion;

    //Creación de variables para la Linterna
    CameraManager cameraManager;
    String idCamara;
    boolean linternaEncendida = false;

    //Variables para Ubicación
    LocationManager locationManager;
    double latitud = LAT_DEFECTO;
    double longitud = LNG_DEFECTO;
    boolean ubicacionObtenida = false;

    //Permisos: ahora se manejan con la API moderna (registerForActivityResult)
    private final ActivityResultLauncher<String> permisoCamara =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), concedido -> {
                if (concedido) {
                    cambiarLinterna();
                } else {
                    Toast.makeText(this,
                            "Sin permiso de cámara no se puede usar la linterna",
                            Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<String[]> permisoUbicacion =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), resultado -> {
                boolean fina = Boolean.TRUE.equals(resultado.get(Manifest.permission.ACCESS_FINE_LOCATION));
                boolean aprox = Boolean.TRUE.equals(resultado.get(Manifest.permission.ACCESS_COARSE_LOCATION));
                if (fina || aprox) {
                    obtenerUbicacion();
                } else {
                    Toast.makeText(this,
                            "Sin permiso de ubicación no se puede obtener tu posición",
                            Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        Utilidades.aplicarInsets(findViewById(R.id.rootLogin));

        //Conexión del XML a Java
        btnLinterna = findViewById(R.id.btnLinterna);
        btnSegundavista = findViewById(R.id.btnSegundavista);
        btnUbicacion = findViewById(R.id.btnUbicacion);
        btnMapa = findViewById(R.id.btnMapa);
        btnConfig = findViewById(R.id.btnConfig);
        btnFormulario = findViewById(R.id.btnFormulario);
        tvUbicacion = findViewById(R.id.tvUbicacion);

        //Preparación para la Linterna y la Ubicación
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        buscarCamaraConFlash();

        //Creamos los Eventos

        // ---------- INTENT EXPLÍCITO 1: Login -> Segundavista (con datos extra) ----------
        btnSegundavista.setOnClickListener(view -> {
            Intent segunda_vista = new Intent(Login.this, Segundavista.class);
            segunda_vista.putExtra("nombre", ubicacionObtenida ? "Mi ubicación" : "Plaza de Armas, Santiago");
            segunda_vista.putExtra("lat", latitud);
            segunda_vista.putExtra("lng", longitud);
            startActivity(segunda_vista);
        });

        // ---------- INTENT EXPLÍCITO 2: Login -> ConfigActivity ----------
        btnConfig.setOnClickListener(view ->
                startActivity(new Intent(Login.this, ConfigActivity.class)));

        // ---------- Navegación hacia el formulario (el Explícito 3 es Form -> Confirm) ----------
        btnFormulario.setOnClickListener(view ->
                startActivity(new Intent(Login.this, FormActivity.class)));

        // Evento Flash (cámara)
        btnLinterna.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                permisoCamara.launch(Manifest.permission.CAMERA);
                return;
            }
            cambiarLinterna();
        });

        // Evento Ubicación
        btnUbicacion.setOnClickListener(view -> obtenerUbicacion());

        // ---------- INTENT IMPLÍCITO 1: Google Maps (geo:) ----------
        btnMapa.setOnClickListener(view -> {
            // Validación: primero hay que tener una ubicación
            if (!ubicacionObtenida) {
                Toast.makeText(this,
                        "Primero presiona \"Obtener mi Ubicación\"",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            String coords = String.format(Locale.US, "%.6f,%.6f", latitud, longitud);
            Uri uri = Uri.parse("geo:" + coords + "?q=" + coords + "(" + Uri.encode("Mi ubicación") + ")");
            Utilidades.abrirIntent(this, new Intent(Intent.ACTION_VIEW, uri),
                    "No hay una app de mapas instalada");
        });
    }

    // ------------------------------------------------------------------
    // LINTERNA
    // ------------------------------------------------------------------

    // Busca la primera cámara que tenga flash (la antigua usaba siempre la [0])
    private void buscarCamaraConFlash() {
        try {
            for (String id : cameraManager.getCameraIdList()) {
                Boolean tieneFlash = cameraManager.getCameraCharacteristics(id)
                        .get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                if (Boolean.TRUE.equals(tieneFlash)) {
                    idCamara = id;
                    return;
                }
            }
        } catch (CameraAccessException e) {
            Toast.makeText(this, "Error al acceder a la cámara", Toast.LENGTH_SHORT).show();
        }
    }

    //Método activación linterna
    public void cambiarLinterna() {
        if (idCamara == null) {
            Toast.makeText(this,
                    "Este dispositivo no tiene flash disponible",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            linternaEncendida = !linternaEncendida;
            cameraManager.setTorchMode(idCamara, linternaEncendida);

            if (linternaEncendida) {
                btnLinterna.setText("🔦 APAGAR LINTERNA");
            } else {
                btnLinterna.setText("🔦 ENCENDER LINTERNA");
            }
        } catch (CameraAccessException e) {
            linternaEncendida = !linternaEncendida; // revertir el estado
            Toast.makeText(this, "No se puede usar la linterna", Toast.LENGTH_SHORT).show();
        }
    }

    // ------------------------------------------------------------------
    // UBICACIÓN
    // ------------------------------------------------------------------

    @SuppressLint("MissingPermission") // el permiso se verifica justo antes
    private void obtenerUbicacion() {
        boolean fina = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean aprox = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (!fina && !aprox) {
            permisoUbicacion.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION});
            return;
        }

        // 1) Intentar con la última ubicación conocida (la más precisa disponible)
        Location mejor = null;
        for (String proveedor : locationManager.getProviders(true)) {
            try {
                Location l = locationManager.getLastKnownLocation(proveedor);
                if (l != null && (mejor == null || l.getAccuracy() < mejor.getAccuracy())) {
                    mejor = l;
                }
            } catch (SecurityException ignorada) {
                // p. ej. GPS sin permiso de ubicación precisa: se prueba con el siguiente proveedor
            }
        }
        if (mejor != null) {
            mostrarUbicacion(mejor);
            return;
        }

        // 2) Si no hay ninguna guardada, pedir una ubicación actual
        String proveedor = null;
        if (locationManager.isProviderEnabled(LocationManager.FUSED_PROVIDER)) {
            proveedor = LocationManager.FUSED_PROVIDER;
        } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            proveedor = LocationManager.NETWORK_PROVIDER;
        } else if (fina && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            proveedor = LocationManager.GPS_PROVIDER;
        }

        if (proveedor == null) {
            tvUbicacion.setText("📍 Activa la ubicación del dispositivo e intenta de nuevo");
            return;
        }

        tvUbicacion.setText("Buscando ubicación...");
        try {
            locationManager.getCurrentLocation(proveedor, null,
                    ContextCompat.getMainExecutor(this), location -> {
                        if (location != null) {
                            mostrarUbicacion(location);
                        } else {
                            tvUbicacion.setText("No se pudo obtener la ubicación. Intenta de nuevo");
                        }
                    });
        } catch (SecurityException e) {
            tvUbicacion.setText("No se pudo obtener la ubicación (falta permiso)");
        }
    }

    private void mostrarUbicacion(Location location) {
        latitud = location.getLatitude();
        longitud = location.getLongitude();
        ubicacionObtenida = true;
        tvUbicacion.setText(String.format(Locale.US,
                "📍 Latitud: %.5f\nLongitud: %.5f", latitud, longitud));
    }
}
