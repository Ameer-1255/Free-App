package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.StorageStats
import com.example.data.model.VideoItem
import com.example.ui.theme.DarkOutline
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun VideoDetailsDialog(
  video: VideoItem,
  onDismiss: () -> Unit,
  onPlay: (VideoItem) -> Unit,
  onShare: (VideoItem) -> Unit,
  onRename: (VideoItem, String) -> Unit,
  onDelete: (VideoItem) -> Unit
) {
  var isRenaming by remember { mutableStateOf(false) }
  var newTitle by remember { mutableStateOf(video.title) }

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = DarkSurface,
    titleContentColor = TextPrimary,
    textContentColor = TextSecondary,
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Info,
          contentDescription = null,
          tint = NeonCyan
        )
        Text(
          text = "Video Details",
          fontWeight = FontWeight.Bold,
          fontSize = 18.sp
        )
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        if (isRenaming) {
          OutlinedTextField(
            value = newTitle,
            onValueChange = { newTitle = it },
            label = { Text("Video Title") },
            modifier = Modifier.fillMaxWidth()
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
          ) {
            TextButton(onClick = { isRenaming = false }) {
              Text("Cancel")
            }
            Button(
              onClick = {
                onRename(video, newTitle)
                isRenaming = false
              },
              colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
            ) {
              Text("Save", color = Color.Black)
            }
          }
        } else {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = video.title,
              fontWeight = FontWeight.SemiBold,
              fontSize = 15.sp,
              color = TextPrimary,
              modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { isRenaming = true }) {
              Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Rename",
                tint = NeonCyan,
                modifier = Modifier.size(18.dp)
              )
            }
          }
        }

        HorizontalDivider(color = DarkOutline.copy(alpha = 0.5f))

        DetailRow(label = "Platform", value = video.platform)
        DetailRow(label = "Quality / Resolution", value = video.quality)
        DetailRow(label = "File Size", value = StorageStats.formatBytes(video.fileSizeBytes))
        DetailRow(label = "Duration", value = "${video.durationSeconds / 60}m ${video.durationSeconds % 60}s")
        DetailRow(label = "MIME Type", value = video.mimeType)
        DetailRow(
          label = "Saved On",
          value = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(video.timestamp))
        )

        if (video.localFilePath.isNotEmpty()) {
          Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = video.localFilePath.takeLast(40),
                color = TextSecondary,
                fontSize = 11.sp
              )
            }
          }
        }
      }
    },
    confirmButton = {
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(bottom = 8.dp)
      ) {
        OutlinedButton(
          onClick = {
            onDelete(video)
            onDismiss()
          },
          colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
          modifier = Modifier.testTag("delete_video_button")
        ) {
          Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Delete")
        }

        Button(
          onClick = {
            onDismiss()
            onPlay(video)
          },
          colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
          modifier = Modifier.testTag("play_from_details_button")
        ) {
          Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Play Video", color = Color.Black, fontWeight = FontWeight.Bold)
        }
      }
    },
    dismissButton = {
      TextButton(onClick = { onShare(video) }) {
        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Share")
      }
    }
  )
}

@Composable
private fun DetailRow(label: String, value: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = label,
      color = TextMuted,
      fontSize = 13.sp
    )
    Text(
      text = value,
      color = TextPrimary,
      fontWeight = FontWeight.Medium,
      fontSize = 13.sp
    )
  }
}
