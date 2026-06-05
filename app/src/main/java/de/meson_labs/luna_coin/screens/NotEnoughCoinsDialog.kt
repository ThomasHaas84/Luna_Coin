package de.meson_labs.luna_coin.screens

import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import de.meson_labs.luna_coin.R

@Composable
fun NotEnoughCoinsDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                if (Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Computer sagt Nein")
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(R.drawable.nein)
                        .crossfade(false)
                        .build(),
                    imageLoader = imageLoader,
                    contentDescription = "Computer sagt Nein",
                    modifier = Modifier
                        .fillMaxWidth()
                        .sizeIn(
                            maxHeight = 360.dp
                        )
                )

                Text(
                    text = "Dafür hast du leider nicht genug Coins.",
                    modifier = Modifier.padding(
                        top = 12.dp
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("OK")
            }
        }
    )
}