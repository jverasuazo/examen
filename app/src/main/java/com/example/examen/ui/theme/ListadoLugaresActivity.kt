package com.example.examen.ui.theme

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.examen.MainActivity
import com.example.examen.R
import com.example.examen.db.AppDatabase
import com.example.examen.db.Lugar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ListadoLugaresActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch (Dispatchers.IO){
            val lugarDao = AppDatabase.getInstance(this@ListadoLugaresActivity).lugarDao()
            val cantRegistros = lugarDao.contar()
            //Se insertan estos registros si no hay nada
            if (cantRegistros < 1){
                lugarDao.insertar(Lugar(0, "7 tazas", false))
            }
        }


        setContent {
            ListaLugaresUI()
        }
    }
}

@Composable
fun ListaLugaresUI() {
    val contexto = LocalContext.current
    val (lugares, setLugares) = remember { mutableStateOf(emptyList<Lugar>()) }
    val textoboton = LocalContext.current.resources.getString(R.string.agregar)
    val inicio = LocalContext.current.resources.getString(R.string.inicio)

    LaunchedEffect(lugares) {
        withContext(Dispatchers.IO) {
            val dao = AppDatabase.getInstance(contexto).lugarDao()
            setLugares(dao.findAll())
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment =Alignment.CenterHorizontally
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().weight(1f)
        ) {
            items(lugares) { lugar ->
                LugarItemUI(lugar) {
                    setLugares(emptyList<Lugar>())
                }
            }
        }
        Spacer(modifier = Modifier.width(20.dp))

        Button(onClick = {
            //Aqui voy a volver al home
            val intent = Intent(contexto, MainActivity::class.java)
            contexto.startActivity(intent)
        }) {
            Text(inicio)
        }

        Spacer(modifier = Modifier.width(20.dp))
        //Boton para ir a home
        Button(onClick = {
            //Ir a capturar fotografia
            val intent = Intent(contexto, CamaraActivity::class.java)
            contexto.startActivity(intent)
        }) {
            Text(textoboton)
        }
    }
}

@Composable
fun LugarItemUI(lugar:Lugar, onSave:() -> Unit ={} ){
    val contexto = LocalContext.current
    val alcanceCorrutina = rememberCoroutineScope()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp, horizontal = 20.dp)
    ) {
        if (lugar.visitado) {
            //Recurso drawable para imagen de lugar visitado
            Image(
                painter = painterResource(id = R.drawable.check),
                contentDescription = "Icono para visitado",
                modifier = Modifier.clickable {
                    alcanceCorrutina.launch(Dispatchers.IO) {
                        val dao = AppDatabase.getInstance(contexto).lugarDao()
                        lugar.visitado = false
                        dao.actualizar(lugar)
                        onSave()
                    }
                }
            )
        } else {
            Icon(
                Icons.Filled.AddCircle,
                contentDescription = "lugar por visitar",
                modifier = Modifier.clickable {
                    alcanceCorrutina.launch(Dispatchers.IO) {
                        val dao = AppDatabase.getInstance(contexto).lugarDao()
                        lugar.visitado = true
                        dao.actualizar(lugar)
                        onSave()
                    }
                }
            )
        }
        Spacer(modifier = Modifier.width(20.dp))
        Image(
            painter = painterResource(id = R.drawable.tazas),
            contentDescription = "foto 7 tazas"

        )

        Spacer(modifier = Modifier.width(20.dp))

        Text(
            text = lugar.lugar,
            modifier = Modifier.weight(2f)
        )

        Spacer(modifier = Modifier.width(20.dp))

        Button(onClick = {
            val intent =Intent(contexto, UbicacionActivity::class.java)
            contexto.startActivity(intent)
        }) {
            Text("Detalles")
        }


        Icon(
            Icons.Filled.Delete,
            contentDescription = "Eliminar lugar",
            modifier = Modifier.clickable {
                alcanceCorrutina.launch(Dispatchers.IO) {
                    val dao = AppDatabase.getInstance(contexto).lugarDao()
                    dao.eliminar(lugar)
                    onSave()
                }
            }
        )
    }
}

//Utilicé las vistas previas para ver como avanzaban los cambios
@Preview(showBackground = true)
@Composable
fun LugarItemUIPreview(){
    val lugar = Lugar(1, "7 Tazas", true)
    LugarItemUI(lugar)
}

@Preview(showBackground = true)
@Composable
fun LugarItemUIPreview2(){
    val lugar2 = Lugar(2, "Segundo lugar", false)
    LugarItemUI(lugar2 )
}