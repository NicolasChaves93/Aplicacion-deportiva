package com.entrenaapp.mobile.data.session;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.entrenaapp.mobile.data.remote.model.AuthResponse;

import org.json.JSONObject;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.nio.charset.StandardCharsets;

public class SessionManager {
    private static final String TAG = "SessionManager";
    private static final String PREFS_NAME = "entrenaapp_session";

    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_NOMBRE = "nombre";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_ROL = "rol";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = crearPrefsCifradas(context.getApplicationContext());
    }

    private SharedPreferences crearPrefsCifradas(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            return EncryptedSharedPreferences.create(
                    context,
                    PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            Log.e(TAG, "No fue posible crear el almacenamiento cifrado de sesion", e);
            throw new IllegalStateException("No fue posible inicializar la sesion segura", e);
        }
    }

    // Guarda la sesion tras un login exitoso contra la API
    public void guardarSesion(AuthResponse auth) {
        prefs.edit()
                .putString(KEY_TOKEN, auth.getToken())
                .putString(KEY_USER_ID, auth.getId())
                .putString(KEY_NOMBRE, auth.getNombre())
                .putString(KEY_EMAIL, auth.getEmail())
                .putString(KEY_ROL, auth.getRol())
                .apply();
    }

    // Indica si hay una sesion cacheada con un token vigente (no expirado)
    public boolean haySesionActiva() {
        String token = getToken();
        return token != null && !tokenExpirado(token);
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public String getNombre() {
        return prefs.getString(KEY_NOMBRE, null);
    }

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, null);
    }

    public String getRol() {
        return prefs.getString(KEY_ROL, null);
    }

    public void cerrarSesion() {
        prefs.edit().clear().apply();
    }

    // Lee el claim "exp" del JWT (sin validar la firma: solo para decidir si mostrar
    // la sesion cacheada u obligar a re-loguear; el servidor sigue validando la firma
    // en cada llamada protegida).
    private boolean tokenExpirado(String token) {
        try {
            String[] partes = token.split("\\.");
            if (partes.length < 2) {
                return true;
            }
            byte[] payloadBytes = Base64.decode(partes[1], Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
            JSONObject payload = new JSONObject(new String(payloadBytes, StandardCharsets.UTF_8));
            long expSeconds = payload.optLong("exp", 0);
            return expSeconds == 0 || (System.currentTimeMillis() / 1000) >= expSeconds;
        } catch (Exception e) {
            Log.e(TAG, "No fue posible interpretar el token guardado", e);
            return true;
        }
    }
}
