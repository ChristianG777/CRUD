package com.example.crudformulario

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crudformulario.FeedReaderContract.FeedEntry
import com.example.crudformulario.ui.theme.CrudFormularioTheme

data class PersonaModel(val id: Long, val nombre: String, val apellido: String)

class MainActivity : ComponentActivity() {
    private lateinit var dbHelper: FeedReaderDbHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbHelper = FeedReaderDbHelper(this)
        enableEdgeToEdge()
        setContent {
            CrudFormularioTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CrudScreen(dbHelper = dbHelper, modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }

    override fun onDestroy() {
        dbHelper.close()
        super.onDestroy()
    }
}

@Composable
fun CrudScreen(dbHelper: FeedReaderDbHelper, modifier: Modifier = Modifier) {
    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var selectedId by remember { mutableStateOf<Long?>(null) }
    var mensajeFeedback by remember { mutableStateOf("Completa el formulario para registrar.") }

    // Función para leer registros de SQLite
    fun obtenerPersonas(): List<PersonaModel> {
        val lista = mutableListOf<PersonaModel>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            FeedEntry.TABLE_NAME,
            arrayOf(FeedEntry._ID, FeedEntry.COLUMN_NAME_NOMBRE, FeedEntry.COLUMN_NAME_APELLIDO),
            null, null, null, null, "${FeedEntry._ID} DESC"
        )
        cursor.use {
            while (it.moveToNext()) {
                val id = it.getLong(0)
                val nom = it.getString(1)
                val ap = it.getString(2)
                lista.add(PersonaModel(id, nom, ap))
            }
        }
        return lista
    }

    var listaPersonas by remember { mutableStateOf(obtenerPersonas()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "CRUD SQLite - Android",
            fontSize = 20.sp,
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = apellido,
            onValueChange = { apellido = it },
            label = { Text("Apellido") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = {
                if (nombre.isNotBlank() && apellido.isNotBlank()) {
                    val db = dbHelper.writableDatabase
                    val values = ContentValues().apply {
                        put(FeedEntry.COLUMN_NAME_NOMBRE, nombre)
                        put(FeedEntry.COLUMN_NAME_APELLIDO, apellido)
                    }

                    if (selectedId == null) {
                        // CREATE
                        db.insert(FeedEntry.TABLE_NAME, null, values)
                        mensajeFeedback = "¡Guardado en SQLite!"
                    } else {
                        // UPDATE
                        db.update(
                            FeedEntry.TABLE_NAME,
                            values,
                            "${FeedEntry._ID} = ?",
                            arrayOf(selectedId.toString())
                        )
                        mensajeFeedback = "¡Actualizado en SQLite!"
                        selectedId = null
                    }
                    nombre = ""
                    apellido = ""
                    listaPersonas = obtenerPersonas()
                } else {
                    mensajeFeedback = "Por favor, llena ambos campos."
                }
            }) {
                Text(if (selectedId == null) "Guardar" else "Actualizar")
            }

            Button(
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                onClick = {
                    nombre = ""
                    apellido = ""
                    selectedId = null
                    mensajeFeedback = "Formulario limpiado."
                }
            ) {
                Text("Limpiar")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(text = mensajeFeedback, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)

        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "Registros en Base de Datos:", fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(listaPersonas) { _, persona ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            nombre = persona.nombre
                            apellido = persona.apellido
                            selectedId = persona.id
                            mensajeFeedback = "Editando registro #${persona.id}"
                        },
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "#${persona.id} - ${persona.nombre} ${persona.apellido}", fontSize = 16.sp)
                            Text(text = "Toca para editar", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                        }

                        Button(
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            onClick = {
                                val db = dbHelper.writableDatabase
                                db.delete(FeedEntry.TABLE_NAME, "${FeedEntry._ID} = ?", arrayOf(persona.id.toString()))
                                if (selectedId == persona.id) {
                                    nombre = ""
                                    apellido = ""
                                    selectedId = null
                                }
                                listaPersonas = obtenerPersonas()
                                mensajeFeedback = "Registro eliminado."
                            }
                        ) {
                            Text("Eliminar", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}