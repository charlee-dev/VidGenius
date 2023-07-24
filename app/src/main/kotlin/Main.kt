import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.ExperimentalComposeApi
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.charleex.autoytvid.ui.features.RootContent
import com.charleex.autoytvid.ui.initKoin

@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@ExperimentalComposeApi
fun main() = application {
    initKoin()

    val windowState = rememberWindowState()
    windowState.apply {
        size = DpSize(1200.dp, 800.dp)
        position = WindowPosition(
            alignment = Alignment.Center,
        )
    }

    Window(
        onCloseRequest = ::exitApplication,
        state = windowState,
        title = "Auto Yt Vid"
    ) {
        RootContent(
            window = window,
            modifier = Modifier
                .fillMaxSize()
        )
    }
}