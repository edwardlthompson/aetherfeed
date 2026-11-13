package org.aetherfeed.app.notify

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.runBlocking
import org.aetherfeed.app.MainActivity
import org.aetherfeed.app.R
import org.aetherfeed.app.di.NotifyEntryPoint

/** Home-screen unread count — backed by [LibraryUnreadTotal], FOSS AppWidget (no Glance). */
class UnreadWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        appWidgetIds.forEach { id ->
            publish(context, appWidgetManager, id)
        }
    }

    companion object {
        internal fun publish(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
        ) {
            val count =
                runBlocking {
                    val library =
                        EntryPointAccessors.fromApplication(
                            context.applicationContext,
                            NotifyEntryPoint::class.java,
                        ).libraryRepository()
                    LibraryUnreadTotal(library) { defaultNotificationChannels(context) }.current()
                }
            val views =
                RemoteViews(context.packageName, R.layout.unread_widget).apply {
                    setTextViewText(R.id.unread_count, context.getString(R.string.unread_total, count))
                    setOnClickPendingIntent(
                        R.id.unread_widget_root,
                        android.app.PendingIntent.getActivity(
                            context,
                            0,
                            android.content.Intent(context, MainActivity::class.java),
                            android.app.PendingIntent.FLAG_UPDATE_CURRENT or
                                android.app.PendingIntent.FLAG_IMMUTABLE,
                        ),
                    )
                }
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
