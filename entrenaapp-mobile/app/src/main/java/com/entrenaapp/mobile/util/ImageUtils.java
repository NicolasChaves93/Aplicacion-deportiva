package com.entrenaapp.mobile.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;

import androidx.exifinterface.media.ExifInterface;

import java.io.IOException;

/**
 * Las fotos tomadas con la camara del sistema quedan en el archivo con la
 * orientacion "cruda" del sensor mas un tag EXIF que indica como rotarlas.
 * BitmapFactory e ImageView.setImageURI no leen ese tag, asi que sin este
 * paso las fotos de perfil salen de lado en algunos dispositivos.
 */
public final class ImageUtils {

    private static final String TAG = "ImageUtils";

    private ImageUtils() {
        // impide crear objetos de esta clase
    }

    public static Bitmap cargarBitmapRotado(String path, int maxAncho, int maxAlto) {
        BitmapFactory.Options opciones = new BitmapFactory.Options();
        opciones.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, opciones);

        opciones.inSampleSize = calcularInSampleSize(opciones, maxAncho, maxAlto);
        opciones.inJustDecodeBounds = false;

        Bitmap bitmap = BitmapFactory.decodeFile(path, opciones);
        if (bitmap == null) {
            return null;
        }

        int rotacion = obtenerRotacionExif(path);
        if (rotacion == 0) {
            return bitmap;
        }

        Matrix matrix = new Matrix();
        matrix.postRotate(rotacion);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private static int calcularInSampleSize(BitmapFactory.Options opciones, int maxAncho, int maxAlto) {
        int alto = opciones.outHeight;
        int ancho = opciones.outWidth;
        int inSampleSize = 1;

        if (alto > maxAlto || ancho > maxAncho) {
            int mitadAlto = alto / 2;
            int mitadAncho = ancho / 2;
            while ((mitadAlto / inSampleSize) >= maxAlto && (mitadAncho / inSampleSize) >= maxAncho) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    private static int obtenerRotacionExif(String path) {
        try {
            ExifInterface exif = new ExifInterface(path);
            int orientacion = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientacion) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return 90;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return 180;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return 270;
                default:
                    return 0;
            }
        } catch (IOException e) {
            Log.e(TAG, "No fue posible leer la orientacion EXIF de " + path, e);
            return 0;
        }
    }
}
