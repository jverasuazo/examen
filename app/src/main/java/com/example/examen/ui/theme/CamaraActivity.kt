package com.example.examen.ui.theme

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.examen.MainActivity
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.time.LocalDateTime

enum class Pantalla{
    FORM,
    CAMARA
}

class AppVM : ViewModel(){
    val pantallaActual = mutableStateOf(Pantalla.FORM)
    var onPermisoCamaraOk:() -> Unit = {}
}

class FormRegistroVM : ViewModel(){
    //val fotos: Any
    val fotos = mutableStateListOf<Uri>()
    val nombre = mutableStateOf("")
    val foto = mutableStateOf<Uri?>(null)
}

class CamaraActivity : ComponentActivity() {
    val camaraVM: AppVM by viewModels()
    val FormRegistroVM: FormRegistroVM by viewModels()

    lateinit var cameraController: LifecycleCameraController

    val lanzadorPermisos = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ){
        if( it[android.Manifest.permission.CAMERA]?:false ){
            //aca ejecuto lo que quiera hacer con la camara
            camaraVM.onPermisoCamaraOk()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        cameraController = LifecycleCameraController(this        )
        cameraController.bindToLifecycle(this)
        cameraController.cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        setContent {
            AppUI(lanzadorPermisos, cameraController)
        }
    }
}

@Composable
fun AppUI(
    lanzadorPermisos: ActivityResultLauncher<Array<String>>,
    cameraController: LifecycleCameraController
){
    val appVM:AppVM = viewModel()

    when(appVM.pantallaActual.value){
        Pantalla.FORM -> {
            PantallaFormUI()
        }
        Pantalla.CAMARA -> {
            PantallaCamaraUI(lanzadorPermisos, cameraController)
        }
    }
}

fun uri2imageBitmap(uri: Uri, contexto: Context) = BitmapFactory.decodeStream(
    contexto.contentResolver.openInputStream(uri)
).asImageBitmap()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaFormUI(){
    val contexto = LocalContext.current
    val appVM:AppVM = viewModel()
    val formRegistroVM:FormRegistroVM = viewModel()
    val ciudadIngresada = formRegistroVM.nombre.value

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        OutlinedTextField(
            value = formRegistroVM.nombre.value,
            onValueChange = { formRegistroVM.nombre.value = it },
            label = { Text("Lugar") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = formRegistroVM.nombre.value,
            onValueChange = { formRegistroVM.nombre.value = it },
            label = { Text("Imagen referencial") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = formRegistroVM.nombre.value,
            onValueChange = { formRegistroVM.nombre.value = it },
            label = { Text("Latitud") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = formRegistroVM.nombre.value,
            onValueChange = { formRegistroVM.nombre.value = it },
            label = { Text("Orden") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = formRegistroVM.nombre.value,
            onValueChange = { formRegistroVM.nombre.value = it },
            label = { Text("Costo Alojamiento") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = formRegistroVM.nombre.value,
            onValueChange = { formRegistroVM.nombre.value = it },
            label = { Text("Costo Traslados") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = formRegistroVM.nombre.value,
            onValueChange = { formRegistroVM.nombre.value = it },
            label = { Text("Comentarios") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )

        Button(onClick = {
            appVM.pantallaActual.value = Pantalla.CAMARA

        }) {
            Text("Guardar y Tomar foto")
        }

        //Variable Fotos al inicio de la clase
        for (fotoUri in formRegistroVM.fotos) {
            Image(
                painter = BitmapPainter(uri2imageBitmap(fotoUri, contexto)),
                contentDescription = "Imagen capturada desde CameraX",
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable {
                    //Abrir la foto en pantalla completa al hacer clic
                    //AbrirImagenEnPantallaCompleta(fotoUri)
                }
            )
        }
    }
}

fun generarNombreSegunFechaHastaSegundo():String = LocalDateTime
    .now().toString().replace(Regex("[T:.-]"),"").substring(0, 14)


fun crearArchivoImagenPrivada(contexto: Context): File = File(
    contexto.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
    "${generarNombreSegunFechaHastaSegundo()}.jpg"
)


fun capturarFotografia(
    cameraController: LifecycleCameraController,
    archivo: File,
    contexto: Context,
    onImagenGurdada: (uri: Uri) -> Unit
){

    val opciones = ImageCapture.OutputFileOptions.Builder(archivo).build()
    cameraController.takePicture(
        opciones,
        ContextCompat.getMainExecutor(contexto),
        object: ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                outputFileResults.savedUri?.let {
                    guardarImagenEnMediaStore(contexto, it)
                    onImagenGurdada( it )
                }
            }

            override fun onError(exception: ImageCaptureException) {
                Log.e("capturarFotografia::OnImageSavedCallback::onError", exception.message?:"Error")
            }

        }
    )
}

//Funcion para guardar fotografia en la galeria
fun guardarImagenEnMediaStore(context: Context, uri: Uri) {
    val contentResolver: ContentResolver = context.contentResolver
    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, generarNombreSegunFechaHastaSegundo() + ".jpg")
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        put(MediaStore.Images.Media.IS_PENDING, 1)
    }

    val contentUri: Uri? = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

    try {
        if (contentUri != null) {
            val outputStream: OutputStream? = contentResolver.openOutputStream(contentUri)
            outputStream?.use {
                val inputStream = context.contentResolver.openInputStream(uri)
                inputStream?.use { input ->
                    input.copyTo(outputStream)
                }
            }

            contentValues.clear()
            contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
            contentResolver.update(contentUri, contentValues, null, null)
        }
    } catch (e: IOException) {
        Log.e("guardarImagenEnMediaStore", "Error al guardar la imagen en MediaStore", e)
    }
}

@Composable
fun PantallaCamaraUI(
    lanzadorPermisos: ActivityResultLauncher<Array<String>>,
    cameraController: LifecycleCameraController
){
    val contexto = LocalContext.current
    val formRegistroVM:FormRegistroVM = viewModel()
    val appVM:AppVM = viewModel()


    lanzadorPermisos.launch(arrayOf(android.Manifest.permission.CAMERA))

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = {
            PreviewView(it).apply{
                controller = cameraController
            }
        }
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = {
            capturarFotografia(
                cameraController,
                crearArchivoImagenPrivada(contexto),
                contexto
            )/*{
            formRegistroVM.foto.value = it
            appVM.pantallaActual.value = Pantalla.FORM
        }*/

            { nuevaFotoUri ->
                formRegistroVM.fotos.add(nuevaFotoUri)
                formRegistroVM.foto.value = nuevaFotoUri
                appVM.pantallaActual.value = Pantalla.FORM
            }

        }) {
            Text("Capturar foto")
        }

        Spacer(modifier = Modifier.width(20.dp))

        Button(onClick = {
            val intent = Intent(contexto, MainActivity::class.java)
            contexto.startActivity(intent)
        }) {
            Text("Home")
        }
    }
}