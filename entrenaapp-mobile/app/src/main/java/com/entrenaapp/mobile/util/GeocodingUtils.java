package com.entrenaapp.mobile.util;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Traduce coordenadas GPS a una direccion legible (reverse geocoding). Es un
 * best-effort: no todos los dispositivos tienen el backend de Geocoder
 * disponible y requiere red, asi que el callback siempre recibe null si no
 * se pudo resolver, y quien llama debe mostrar lat/lng como respaldo.
 */
public final class GeocodingUtils {

    private static final String TAG = "GeocodingUtils";
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    public interface DireccionCallback {
        void onResultado(@androidx.annotation.Nullable String direccion);
    }

    private GeocodingUtils() {
        // impide crear objetos de esta clase
    }

    public static void obtenerDireccion(Context context, double lat, double lng, DireccionCallback callback) {
        EXECUTOR.execute(() -> {
            String direccion = resolverDireccion(context, lat, lng);
            MAIN_HANDLER.post(() -> callback.onResultado(direccion));
        });
    }

    @SuppressWarnings("deprecation")
    private static String resolverDireccion(Context context, double lat, double lng) {
        try {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            List<Address> resultados = geocoder.getFromLocation(lat, lng, 1);
            if (resultados == null || resultados.isEmpty()) {
                return null;
            }
            return formatearDireccion(resultados.get(0));
        } catch (IOException | IllegalArgumentException e) {
            Log.w(TAG, "No fue posible resolver la direccion para " + lat + "," + lng, e);
            return null;
        }
    }

    private static String formatearDireccion(Address address) {
        StringBuilder sb = new StringBuilder();
        if (address.getThoroughfare() != null) {
            sb.append(address.getThoroughfare());
        }
        if (address.getSubThoroughfare() != null) {
            sb.append(" ").append(address.getSubThoroughfare());
        }
        if (address.getLocality() != null) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(address.getLocality());
        }
        return sb.length() > 0 ? sb.toString().trim() : address.getAddressLine(0);
    }
}
