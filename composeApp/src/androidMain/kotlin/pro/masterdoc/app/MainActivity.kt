package pro.masterdoc.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.arkivanov.decompose.defaultComponentContext
import pro.masterdoc.app.di.createRootComponent
import space.kodio.core.Kodio
import space.kodio.core.initialize
import space.kodio.core.onRequestPermissionsResult

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Kodio.initialize(this)
        enableEdgeToEdge()

        val root = createRootComponent(defaultComponentContext())

        setContent {
            App(rootComponent = root)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String?>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Kodio.onRequestPermissionsResult(requestCode, grantResults)
    }
}
