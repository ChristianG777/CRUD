package com.example.crudformulario

import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.os.Bundle
import android.provider.BaseColumns
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var txtid: EditText
    private lateinit var txtnombre: EditText
    private lateinit var txtapellido: EditText
    private lateinit var dbHelper: FeedReaderDbHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        txtid = findViewById(R.id.txtid)
        txtnombre = findViewById(R.id.nombre)
        txtapellido = findViewById(R.id.apellido)

        dbHelper = FeedReaderDbHelper(this)

        findViewById<View>(R.id.btn_actualizar).setOnClickListener { actualizar() }
        findViewById<View>(R.id.btn_eliminar).setOnClickListener { confirmarEliminacion() }
    }

    // ---------- Navegacion ----------
    fun Listar(vista: View) {
        startActivity(Intent(this, Listado::class.java))
    }

    // ---------- CREATE ----------
    fun Guardar(vista: View) {
        val nombre = txtnombre.text.toString().trim()
        val apellido = txtapellido.text.toString().trim()

        if (nombre.isEmpty() || apellido.isEmpty()) {
            mostrarMensaje("Ingrese nombre y apellido")
            return
        }

        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(FeedReaderContract.FeedEntry.COLUMN_NAME_NOMBRE, nombre)
            put(FeedReaderContract.FeedEntry.COLUMN_NAME_APELLIDO, apellido)
        }
        val newRowId = db.insert(FeedReaderContract.FeedEntry.TABLE_NAME, null, values)

        if (newRowId == -1L) {
            mostrarMensaje("No se pudo guardar el registro")
        } else {
            txtid.setText(newRowId.toString())
            mostrarMensaje("Se guardó el registro con clave: $newRowId")
        }
    }

    // ---------- READ ----------
    fun Buscar(vista: View) {
        val id = txtid.text.toString().trim()
        if (id.isEmpty()) {
            txtid.error = "Ingrese un ID"
            txtid.requestFocus()
            return
        }

        val db = dbHelper.readableDatabase
        val projection = arrayOf(
            BaseColumns._ID,
            FeedReaderContract.FeedEntry.COLUMN_NAME_NOMBRE,
            FeedReaderContract.FeedEntry.COLUMN_NAME_APELLIDO
        )
        val selection = "${BaseColumns._ID} = ?"
        val selectionArgs = arrayOf(id)

        db.query(
            FeedReaderContract.FeedEntry.TABLE_NAME,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            null
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                txtnombre.setText(cursor.getString(cursor.getColumnIndexOrThrow(FeedReaderContract.FeedEntry.COLUMN_NAME_NOMBRE)))
                txtapellido.setText(cursor.getString(cursor.getColumnIndexOrThrow(FeedReaderContract.FeedEntry.COLUMN_NAME_APELLIDO)))
            } else {
                txtnombre.setText("")
                txtapellido.setText("")
                mostrarMensaje("No existe un registro con Id = $id")
            }
        }
    }

    // ---------- UPDATE ----------
    private fun actualizar() {
        val id = leerId() ?: return
        val nombre = txtnombre.text.toString().trim()
        val apellido = txtapellido.text.toString().trim()

        if (nombre.isEmpty() || apellido.isEmpty()) {
            mostrarMensaje("Ingrese nombre y apellido")
            return
        }

        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(FeedReaderContract.FeedEntry.COLUMN_NAME_NOMBRE, nombre)
            put(FeedReaderContract.FeedEntry.COLUMN_NAME_APELLIDO, apellido)
        }
        val selection = "${BaseColumns._ID} = ?"
        val selectionArgs = arrayOf(id)

        val count = db.update(
            FeedReaderContract.FeedEntry.TABLE_NAME,
            values,
            selection,
            selectionArgs
        )

        if (count > 0) {
            mostrarMensaje("Se actualizó $count registro(s)")
            limpiarFormulario()
        } else {
            mostrarMensaje("No se pudo actualizar el registro")
        }
    }

    private fun confirmarEliminacion() {
        val id = leerId() ?: return
        AlertDialog.Builder(this)
            .setTitle("Eliminar registro")
            .setMessage("¿Quieres eliminar el registro #$id?")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Eliminar") { _, _ -> eliminar(id) }
            .show()
    }

    private fun eliminar(id: String) {
        try {
            val deleted = dbHelper.writableDatabase.delete(FeedReaderContract.FeedEntry.TABLE_NAME, "${BaseColumns._ID} = ?", arrayOf(id))
            limpiarFormulario()
            mostrarMensaje(if (deleted > 0) "Registro eliminado" else "El registro ya no existe")
        } catch (error: SQLiteException) {
            mostrarMensaje("No se pudo eliminar el registro")
        }
    }

    private fun leerId(): String? {
        val id = txtid.text.toString().trim()
        try {
            if (id.toLong() > 0) {
                return id
            }
        } catch (error: NumberFormatException) {
            // Error de formato numérico
        }
        txtid.error = "Escribe un ID válido del listado"
        txtid.requestFocus()
        return null
    }

    private fun limpiarFormulario() {
        txtid.setText("")
        txtnombre.setText("")
        txtapellido.setText("")
        txtnombre.error = null
        txtapellido.error = null
    }

    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        dbHelper.close()
        super.onDestroy()
    }
}