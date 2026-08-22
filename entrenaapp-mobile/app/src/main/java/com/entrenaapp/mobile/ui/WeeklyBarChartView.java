package com.entrenaapp.mobile.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.entrenaapp.mobile.R;

import java.util.Collections;
import java.util.List;

/**
 * Grafico de barras minimalista dibujado a mano (sin dependencias externas)
 * para mostrar la carga semanal de entrenamientos en el Home.
 */
public class WeeklyBarChartView extends View {

    private List<Integer> valores = Collections.emptyList();
    private List<String> etiquetas = Collections.emptyList();

    private final Paint paintBarra = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintTextoValor = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintTextoEtiqueta = new Paint(Paint.ANTI_ALIAS_FLAG);

    public WeeklyBarChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paintBarra.setColor(ContextCompat.getColor(context, R.color.brand_secondary));
        paintTextoValor.setColor(ContextCompat.getColor(context, R.color.brand_primary));
        paintTextoValor.setTextSize(spToPx(11));
        paintTextoValor.setTextAlign(Paint.Align.CENTER);
        paintTextoValor.setFakeBoldText(true);
        paintTextoEtiqueta.setColor(ContextCompat.getColor(context, R.color.brand_on_surface_variant));
        paintTextoEtiqueta.setTextSize(spToPx(10));
        paintTextoEtiqueta.setTextAlign(Paint.Align.CENTER);
    }

    public void setDatos(List<Integer> valores, List<String> etiquetas) {
        this.valores = valores;
        this.etiquetas = etiquetas;
        invalidate();
    }

    private float spToPx(float sp) {
        return sp * getResources().getDisplayMetrics().scaledDensity;
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (valores.isEmpty()) {
            return;
        }

        int ancho = getWidth();
        int alto = getHeight();
        float espacioTextoValor = spToPx(16);
        float espacioTextoEtiqueta = spToPx(18);
        float alturaDisponibleBarras = alto - espacioTextoValor - espacioTextoEtiqueta;
        if (alturaDisponibleBarras <= 0) {
            return;
        }

        int maximo = 1;
        for (int v : valores) {
            maximo = Math.max(maximo, v);
        }

        int cantidad = valores.size();
        float anchoSlot = (float) ancho / cantidad;
        float anchoBarra = anchoSlot * 0.4f;
        float radio = dpToPx(4);

        for (int i = 0; i < cantidad; i++) {
            int valor = valores.get(i);
            float centroX = anchoSlot * i + anchoSlot / 2f;
            float alturaBarra = valor == 0 ? dpToPx(2) : (valor / (float) maximo) * alturaDisponibleBarras;
            float top = espacioTextoValor + (alturaDisponibleBarras - alturaBarra);
            float bottom = espacioTextoValor + alturaDisponibleBarras;

            canvas.drawRoundRect(centroX - anchoBarra / 2f, top, centroX + anchoBarra / 2f, bottom, radio, radio, paintBarra);
            canvas.drawText(String.valueOf(valor), centroX, top - dpToPx(4), paintTextoValor);
            canvas.drawText(etiquetas.get(i), centroX, bottom + espacioTextoEtiqueta * 0.75f, paintTextoEtiqueta);
        }
    }
}
