package com.example.crudformulario;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Construye por código las filas y celdas de un TableLayout:
 * una fila de cabecera y una fila por cada registro recibido.
 */
public class DynamicTable {
    private static final int COLOR_CABECERA = Color.parseColor("#1A2142");
    private static final int COLOR_FILA_PAR = Color.parseColor("#FFFFFF");
    private static final int COLOR_FILA_IMPAR = Color.parseColor("#F8F2B5");

    private final TableLayout tabla;
    private final Context contexto;
    private String[] cabecera;
    private ArrayList<String[]> datos;
    private TableRow fila;
    private TextView celda;

    public DynamicTable(TableLayout tabla, Context contexto) {
        this.tabla = tabla;
        this.contexto = contexto;
    }

    public void setCabecera(String[] cabecera) {
        this.cabecera = cabecera;
    }

    public void setDatos(ArrayList<String[]> datos) {
        this.datos = datos;
    }

    private void nuevaFila() {
        fila = new TableRow(contexto);
    }

    private void nuevaCelda() {
        celda = new TextView(contexto);
        celda.setGravity(Gravity.CENTER);
        celda.setPadding(24, 16, 24, 16);
        celda.setTextSize(14);
    }

    public void crearCabecera() {
        nuevaFila();
        for (String titulo : cabecera) {
            nuevaCelda();
            celda.setText(titulo);
            celda.setTextSize(18);
            celda.setTypeface(null, Typeface.BOLD);
            celda.setTextColor(Color.WHITE);
            celda.setBackgroundColor(COLOR_CABECERA);
            fila.addView(celda, parametrosCelda());
        }
        tabla.addView(fila);
    }

    public void crearFilas() {
        int numeroFila = 0;
        for (String[] datosfila : datos) {
            nuevaFila();
            int colorFondo = (numeroFila % 2 == 0) ? COLOR_FILA_PAR : COLOR_FILA_IMPAR;
            for (String dato : datosfila) {
                nuevaCelda();
                celda.setText(dato);
                celda.setTextSize(16);
                celda.setTextColor(Color.BLACK);
                celda.setBackgroundColor(colorFondo);
                fila.addView(celda, parametrosCelda());
            }
            tabla.addView(fila);
            numeroFila++;
        }
    }

    private TableRow.LayoutParams parametrosCelda() {
        TableRow.LayoutParams parametros = new TableRow.LayoutParams();
        parametros.setMargins(1, 1, 1, 1);
        parametros.weight = 1;
        return parametros;
    }
}