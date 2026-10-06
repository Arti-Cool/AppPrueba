package com.devst.appprueba;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Funciones compartidas por todas las Activities.
 */
public class Utilidades {

    /**
     * Lanza un Intent implícito de forma segura.
     * Si no existe ninguna app que lo maneje, muestra un mensaje en vez de cerrar la app.
     * (Se usa try/catch en lugar de resolveActivity() porque desde Android 11 esa
     * consulta requiere declarar <queries> en el manifest.)
     */
    public static void abrirIntent(Activity activity, Intent intent, String mensajeError) {
        try {
            activity.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(activity, mensajeError, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Con EdgeToEdge la vista queda debajo de la barra de estado y de navegación.
     * Esto agrega el espacio necesario para que no se tape el contenido.
     */
    public static void aplicarInsets(View vistaRaiz) {
        final int izq = vistaRaiz.getPaddingLeft();
        final int arr = vistaRaiz.getPaddingTop();
        final int der = vistaRaiz.getPaddingRight();
        final int aba = vistaRaiz.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(vistaRaiz, (v, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(izq + barras.left, arr + barras.top, der + barras.right, aba + barras.bottom);
            return insets;
        });
    }
}
