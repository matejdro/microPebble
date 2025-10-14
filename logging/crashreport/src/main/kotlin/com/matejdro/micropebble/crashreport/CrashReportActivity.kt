package com.matejdro.micropebble.crashreport

import android.content.ClipData
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.matejdro.micropebble.logging.FileLoggingControllerImpl
import com.matejdro.micropebble.ui.debugging.PreviewTheme
import com.matejdro.micropebble.ui.theme.MicroPebbleTheme
import kotlinx.coroutines.launch

/**
 * Activity that shows encountered errors.
 *
 * This is intentionally its own activity so it's independent of the app - if navigation system
 * on the MainActivity starts crashing for some reason, this will still work.
 */
class CrashReportActivity : ComponentActivity() {
   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      val errorText = intent?.getStringExtra(EXTRA_TEXT).orEmpty()
      val extraData = FileLoggingControllerImpl(this).getDeviceInfo()

      val fullData = "$extraData\n\n$errorText"

      setContent {
         MicroPebbleTheme {
            Surface(Modifier.fillMaxSize()) {
               val clipboard = LocalClipboard.current
               val coroutineScope = rememberCoroutineScope()

               val appName = getApplicationInfo().loadLabel(packageManager).toString()

               Column(
                  verticalArrangement = Arrangement.spacedBy(16.dp),
                  modifier = Modifier
                     .padding(16.dp)
                     .verticalScroll(rememberScrollState())
               ) {
                  ErrorReport(
                     errorText = fullData,
                     appName = appName,
                     copy = {
                        coroutineScope.launch {
                           clipboard.setClipEntry(
                              ClipEntry(
                                 ClipData.newPlainText(
                                    getString(R.string.error_title, appName),
                                    fullData
                                 )
                              )
                           )
                        }
                     },
                     share = {
                        val sendIntent: Intent = Intent().apply {
                           this.action = Intent.ACTION_SEND
                           this.putExtra(Intent.EXTRA_TEXT, fullData)
                           this.type = "text/plain"
                        }

                        val shareIntent = Intent.createChooser(sendIntent, getString(R.string.stacktrace))
                        startActivity(shareIntent)
                     },
                     openGithubIssue = {
                        val searchIntent = Intent().apply {
                           action = Intent.ACTION_VIEW
                           data = "https://github.com/matejdro/microPebble/issues/new".toUri()
                        }

                        startActivity(searchIntent)
                     }
                  )
               }
            }
         }
      }
   }

   companion object {
      const val EXTRA_TEXT = "text"
   }
}

@Composable
private fun ColumnScope.ErrorReport(
   errorText: String,
   appName: String,
   copy: () -> Unit,
   share: () -> Unit,
   openGithubIssue: () -> Unit,
) {
   Text(stringResource(R.string.has_encountered_an_error, appName), style = MaterialTheme.typography.headlineMedium)

   Button(onClick = copy) {
      Row(verticalAlignment = Alignment.CenterVertically) {
         Icon(
            painterResource(R.drawable.copy),
            contentDescription = null,
            modifier = Modifier.padding(end = 8.dp)
         )

         Text(stringResource(R.string.copy_info))
      }
   }

   Button(onClick = share) {
      Row(verticalAlignment = Alignment.CenterVertically) {
         Icon(
            painterResource(R.drawable.share),
            contentDescription = null,
            modifier = Modifier.padding(end = 8.dp)
         )

         Text(stringResource(R.string.share_info))
      }
   }

   Button(onClick = openGithubIssue) {
      Row(verticalAlignment = Alignment.CenterVertically) {
         Icon(
            painterResource(R.drawable.report_issue),
            contentDescription = null,
            modifier = Modifier.padding(end = 8.dp)
         )

         Text(stringResource(R.string.open_github_issue))
      }
   }

   Text(stringResource(R.string.info), style = MaterialTheme.typography.headlineSmall)
   TextField(value = errorText, onValueChange = {}, readOnly = true)
}

@Preview
@Composable
private fun CrashReportPreview() {
   val errorText = "java.lang.Exception..."

   PreviewTheme {
      Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(16.dp)) {
         ErrorReport(errorText, "microPebble", {}, {}, {})
      }
   }
}
