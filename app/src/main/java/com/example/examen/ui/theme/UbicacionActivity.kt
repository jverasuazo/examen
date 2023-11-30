package com.example.examen.ui.theme

import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModel
import com.example.examen.MainActivity
import com.example.examen.R
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class AppVM2: ViewModel(){
    val latitud  = mutableStateOf(0.0)
    val longitud = mutableStateOf(0.0)

    var permisosUbicacionOk:() -> Unit = {}
}

class UbicacionActivity : ComponentActivity() {
    val appVM2:AppVM2 by viewModels()

    val lanzadorPermisos = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ){
        if (
            (it[android.Manifest.permission.ACCESS_FINE_LOCATION]?: false) or
            (it[android.Manifest.permission.ACCESS_COARSE_LOCATION]?: false)
        )else{
            Log.v("lanzadorPermisos callback", "Se denegaron los permisos")
        }
        appVM2.permisosUbicacionOk()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppUI(appVM2, lanzadorPermisos)
        }
    }
}

class FaltaPermisosException(mensaje:String): Exception(mensaje)

fun conseguirUbicacion(contexto: Context, onSuccess:(ubicacion: Location) -> Unit){
    try{
        val servicio = LocationServices.getFusedLocationProviderClient(contexto)
        val tarea    = servicio.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            null
        )
        tarea.addOnSuccessListener {
            onSuccess(it)
        }
    }catch(se:SecurityException){
        throw FaltaPermisosException("Sin permisos de ubicacion")
    }
}

@Composable
fun AppUI(appVM2:AppVM2, lanzadorPermisos:ActivityResultLauncher<Array<String>>){
    val contexto = LocalContext.current
    val textoboton = LocalContext.current.resources.getString(R.string.ubicacion)

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = {
            val intent = Intent(contexto, MainActivity::class.java)
            contexto.startActivity(intent)
        }) {
            Text("Home")
        }
        Spacer(modifier = Modifier.width(20.dp))

        Button(onClick = {
            appVM2.permisosUbicacionOk = {
                conseguirUbicacion(contexto) {
                    appVM2.latitud.value = it.latitude
                    appVM2.longitud.value = it.longitude
                }
            }

            lanzadorPermisos.launch(
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )

        }) {
            Text(textoboton)
        }

        Spacer(Modifier.height(20.dp))
        Text("Valor noche: 100USD")
        Spacer(Modifier.height(20.dp))
        Text("Traslado: 20USD")
        Spacer(Modifier.height(100.dp))

        AndroidView(
            factory = {
                MapView(it).apply{
                    setTileSource(TileSourceFactory.MAPNIK)
                    org.osmdroid.config.Configuration.getInstance().userAgentValue = contexto.packageName
                    controller.setZoom(15.0)
                }
            }, update ={
                it.overlays.removeIf { true }
                it.invalidate()

                val geoPoint = GeoPoint(appVM2.latitud.value, appVM2.longitud.value)
                it.controller.animateTo(geoPoint)

                val marcador = Marker(it)
                marcador.position = geoPoint
                marcador.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                it.overlays.add(marcador)
            }
        )
    }
}