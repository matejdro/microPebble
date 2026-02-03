package com.matejdro.micropebble.appstore.data

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import com.matejdro.micropebble.appstore.api.AppInstallationClient
import com.matejdro.micropebble.appstore.api.AppStatus
import com.matejdro.micropebble.appstore.api.AppstoreSource
import com.matejdro.micropebble.appstore.api.AppstoreSourceService
import com.matejdro.micropebble.appstore.api.updater.AppUpdateFinder
import com.matejdro.micropebble.common.util.toVersionString
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.rebble.libpebblecommon.connection.LockerApi
import io.rebble.libpebblecommon.locker.AppType
import io.rebble.libpebblecommon.locker.LockerWrapper
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

@Inject
@ContributesBinding(AppScope::class)
class AppUpdaterFinderImpl(
   private val context: Context,
   private val lockerApi: LockerApi,
   private val installationClient: AppInstallationClient,
   private val sourceService: AppstoreSourceService,
) : AppUpdateFinder {
   private val channel = NotificationChannel(
      CHANNEL_ID_APP_UPDATE, context.getString(R.string.app_update_channel_name), NotificationManager.IMPORTANCE_DEFAULT
   )

   override suspend fun findAndNotifyUpdates(): Boolean {
      val sources = sourceService.enabledSources.first()
      val apps = combine(
         lockerApi.getLocker(AppType.Watchface, null, Int.MAX_VALUE),
         lockerApi.getLocker(AppType.Watchapp, null, Int.MAX_VALUE),
      ) { watchfaces, watchapps ->
         watchfaces + watchapps
      }.first().mapNotNull { app -> isUpdatable(app, sources)?.let { app to it } }

      if (apps.isNotEmpty()) {
         val notificationManager = context.getSystemService<NotificationManager>() ?: return false
         notificationManager.createNotificationChannel(channel)
         val intent = PendingIntent.getActivity(
            context,
            0,
            Intent(Intent.ACTION_VIEW, "micropebble://watchapps".toUri()),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
         )
         val notification = Notification.Builder(context, CHANNEL_ID_APP_UPDATE).run {
            setContentIntent(intent)
            setAutoCancel(true)
            setOnlyAlertOnce(true)
            setSmallIcon(R.drawable.ic_update)
            if (apps.size == 1) {
               val (app, status) = apps.first()
               setContentTitle(context.getString(R.string.app_has_updates, app.properties.title))
               val text = context.getString(
                  R.string.single_update_text_content,
                  status.fromVersion.toVersionString(),
                  status.toVersion.toVersionString()
               )
               setContentText(text)
            } else {
               configureMultiNotification(apps)
            }
            build()
         }
         notificationManager.notify(UPDATE_AVAILABLE_CODE, notification)
      }
      return true
   }

   private suspend fun isUpdatable(
      app: LockerWrapper,
      sources: List<AppstoreSource>,
   ): AppStatus.Updatable? = installationClient.isAppUpdatable(
      app.properties.id, sources
   ) as? AppStatus.Updatable

   private fun Notification.Builder.configureMultiNotification(apps: List<Pair<LockerWrapper, AppStatus.Updatable>>) {
      setStyle(
         Notification.BigTextStyle().apply {
            val text = buildString {
               for ((app, status) in apps) {
                  val text = context.getString(
                     R.string.update_text_content,
                     app.properties.title,
                     status.fromVersion.toVersionString(),
                     status.toVersion.toVersionString()
                  )
                  appendLine(text)
               }
            }
            setContentText(text)
         }
      )
      setContentTitle(context.resources.getQuantityString(R.plurals.apps_have_updates, apps.size))
   }
}

private const val CHANNEL_ID_APP_UPDATE = "com.matejdro.micropebble.CHANNEL_ID_APP_UPDATE"
const val UPDATE_AVAILABLE_CODE = 120_428_220
