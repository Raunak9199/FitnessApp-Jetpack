package com.nexusbihar.fitnessapp.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ExitConfirmationDialog(
    modifier: Modifier = Modifier,
    onConfirmExit: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Exit App")
        },
        text = {
            Text(text = "Are you sure you want to exit the app?")
        },
        confirmButton = {
            TextButton(onClick = onConfirmExit) {
                Text("Yes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("No")
            }
        }
    )
}
