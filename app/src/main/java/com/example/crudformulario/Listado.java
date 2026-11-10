package com.example.crudformulario;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class Listado extends AppCompatActivity {
    private final String[] cabecera = {"Id", "Nombre", "Apellido"};
    private final ArrayList<String[]> datos = new ArrayList<>();
    private FeedReaderDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listado);

        dbHelper = new FeedReaderDbHelper(this);
        TableLayout tblistado = findViewById(R.id.tblistado);
        TextView txtvacio = findViewById(R.id.txtvacio);

        TraerDatos();

        if (datos.isEmpty()) {
            txtvacio.setVisibility(View.VISIBLE);
        } else {
            txtvacio.setVisibility(View.GONE);
            DynamicTable creaTabla = new DynamicTable(tblistado, this);
            creaTabla.setCabecera(cabecera);
            creaTabla.setDatos(datos);
            creaTabla.crearCabecera();
            creaTabla.crearFilas();
        }
    }

    @Override
    protected void onDestroy() {
        if (dbHelper != null) {
            dbHelper.close();
        }
        super.onDestroy();
    }

    /** Lee todas las filas de la tabla, ordenadas por apellido, y las guarda en "datos". */
    private void TraerDatos() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = {
                BaseColumns._ID,
                FeedReaderContract.FeedEntry.COLUMN_NAME_NOMBRE,
                FeedReaderContract.FeedEntry.COLUMN_NAME_APELLIDO
        };
        String sortOrder = FeedReaderContract.FeedEntry.COLUMN_NAME_APELLIDO + " ASC";

        try (Cursor cursor = db.query(
                FeedReaderContract.FeedEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                sortOrder
        )) {
            while (cursor.moveToNext()) {
                String[] fila = new String[3];
                fila[0] = String.valueOf(cursor.getLong(
                        cursor.getColumnIndexOrThrow(FeedReaderContract.FeedEntry._ID)));
                fila[1] = cursor.getString(
                        cursor.getColumnIndexOrThrow(FeedReaderContract.FeedEntry.COLUMN_NAME_NOMBRE));
                fila[2] = cursor.getString(
                        cursor.getColumnIndexOrThrow(FeedReaderContract.FeedEntry.COLUMN_NAME_APELLIDO));
                datos.add(fila);
            }
        }
    }

    /** Vuelve a la pantalla anterior (MainActivity) sin crear una nueva copia de ella. */
    public void Regresar(View vista) {
        finish();
    }
}