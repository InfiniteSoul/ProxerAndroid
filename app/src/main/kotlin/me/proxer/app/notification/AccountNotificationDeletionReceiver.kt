package me.proxer.app.notification

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.reactivex.schedulers.Schedulers
import me.proxer.app.MainApplication.Companion.api
import me.proxer.app.util.data.StorageHelper
import me.proxer.app.util.extension.buildSingle
import me.proxer.library.enums.NotificationFilter
import java.util.*

/**
 * @author Ruben Gees
 */
class AccountNotificationDeletionReceiver : BroadcastReceiver() {

    companion object {
        fun getPendingIntent(context: Context): PendingIntent = PendingIntent
                .getBroadcast(context, 0, Intent(context, AccountNotificationDeletionReceiver::class.java), 0)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        StorageHelper.lastNewsDate = Date()

        api.notifications().notifications()
                .limit(Int.MAX_VALUE)
                .markAsRead(true)
                .filter(NotificationFilter.UNREAD)
                .buildSingle()
                .subscribeOn(Schedulers.io())
                .subscribe({}, {})
    }
}
