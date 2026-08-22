package com.entrenaapp.mobile.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;

/**
 * Verifica y observa el estado de conexion a internet. Se usa para decidir
 * si una accion se guarda "solo local" (y se sincronizara despues) y para
 * el indicador en linea/sin conexion de la barra superior.
 */
public final class ConnectivityObserver {

    public interface Callback {
        void onCambio(boolean conectado);
    }

    private ConnectivityObserver() {
        // impide crear objetos de esta clase
    }

    public static boolean hayConexion(Context context) {
        ConnectivityManager manager = (ConnectivityManager)
                context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager == null) {
            return false;
        }
        Network red = manager.getActiveNetwork();
        if (red == null) {
            return false;
        }
        NetworkCapabilities capacidades = manager.getNetworkCapabilities(red);
        return capacidades != null
                && capacidades.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                && capacidades.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
    }

    /**
     * Registra un callback que se dispara cada vez que cambia la
     * conectividad de la red por defecto del sistema (la que de verdad usa
     * la app para salir a internet). Devuelve el
     * ConnectivityManager.NetworkCallback para que el llamador lo
     * desregistre en su onStop/onDestroy.
     *
     * Usa registerDefaultNetworkCallback en vez de registerNetworkCallback
     * con un NetworkRequest generico: ese segundo modo escucha *cualquier*
     * red que matchee las capacidades pedidas, y al perder la red activa
     * puede tardar en avisar (o directamente no avisar) si el sistema tarda
     * en confirmar que no hay otra red disponible. registerDefaultNetworkCallback
     * sigue puntualmente a la red que el sistema esta usando ahora mismo.
     */
    public static ConnectivityManager.NetworkCallback observar(Context context, Callback callback) {
        ConnectivityManager manager = (ConnectivityManager)
                context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        ConnectivityManager.NetworkCallback callbackReal = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                callback.onCambio(true);
            }

            @Override
            public void onLost(Network network) {
                callback.onCambio(false);
            }

            @Override
            public void onCapabilitiesChanged(Network network, NetworkCapabilities capabilities) {
                callback.onCambio(capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED));
            }
        };

        if (manager != null) {
            manager.registerDefaultNetworkCallback(callbackReal);
        }
        return callbackReal;
    }

    public static void dejarDeObservar(Context context, ConnectivityManager.NetworkCallback callback) {
        ConnectivityManager manager = (ConnectivityManager)
                context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager != null && callback != null) {
            try {
                manager.unregisterNetworkCallback(callback);
            } catch (IllegalArgumentException ignored) {
                // ya estaba desregistrado
            }
        }
    }
}
