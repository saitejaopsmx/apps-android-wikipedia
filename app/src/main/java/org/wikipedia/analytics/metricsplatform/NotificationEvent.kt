package org.wikipedia.analytics.metricsplatform

import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import org.wikipedia.Constants
import org.wikipedia.WikipediaApp
import org.wikipedia.notifications.NotificationPollBroadcastReceiver
import org.wikipedia.notifications.db.Notification

class NotificationInteractionEvent(
    private val notification_id: Int,
    private val notification_wiki: String,
    private val notification_type: String,
    private val action_rank: Int,
    private val action_icon: String,
    private val selection_token: String,
    private val incoming_only: Boolean,
    private val device_level_enabled: Boolean,
) {
    companion object : MetricsEvent() {
        private const val ACTION_INCOMING = -1
        private const val ACTION_READ_AND_ARCHIVED = 0
        private const val ACTION_CLICKED = 10
        private const val ACTION_DISMISSED = 11
        const val ACTION_PRIMARY = 1
        const val ACTION_SECONDARY = 2
        const val ACTION_LINK_CLICKED = 3

        private fun logClicked(intent: Intent) {
            submit(
                NotificationInteractionEvent(
                    intent.getLongExtra(Constants.INTENT_EXTRA_NOTIFICATION_ID, 0).toInt(),
                    WikipediaApp.instance.wikiSite.dbName(),
                    intent.getStringExtra(Constants.INTENT_EXTRA_NOTIFICATION_TYPE).orEmpty(),
                    NotificationInteractionEvent.ACTION_CLICKED,
                    "",
                    "",
                    incoming_only = false,
                    device_level_enabled = true
                )
            )
        }

        private fun logDismissed(intent: Intent) {
            submit(
                NotificationInteractionEvent(
                    intent.getLongExtra(Constants.INTENT_EXTRA_NOTIFICATION_ID, 0).toInt(),
                    WikipediaApp.instance.wikiSite.dbName(),
                    intent.getStringExtra(Constants.INTENT_EXTRA_NOTIFICATION_TYPE).orEmpty(),
                    NotificationInteractionEvent.ACTION_DISMISSED,
                    "",
                    "",
                    incoming_only = false,
                    device_level_enabled = true
                )
            )
        }

        fun logMarkRead(notification: Notification, selectionToken: Long?) {
            submit(
                NotificationInteractionEvent(
                    notification.id.toInt(),
                    notification.wiki,
                    notification.type,
                    NotificationInteractionEvent.ACTION_READ_AND_ARCHIVED,
                    "",
                    selectionToken?.toString()
                        ?: "",
                    incoming_only = false,
                    device_level_enabled = true
                )
            )
        }

        fun logIncoming(notification: Notification, type: String?) {
            submit(
                NotificationInteractionEvent(
                    notification.id.toInt(),
                    notification.wiki,
                    type ?: notification.type,
                    NotificationInteractionEvent.ACTION_INCOMING,
                    "",
                    "",
                    incoming_only = true,
                    device_level_enabled = NotificationManagerCompat.from(
                        WikipediaApp.instance
                    ).areNotificationsEnabled()
                )
            )
        }

        fun logAction(notification: Notification, index: Int, link: Notification.Link) {
            submit(
                NotificationInteractionEvent(
                    notification.id.toInt(), notification.wiki, notification.type, index,
                    link.icon(), "", incoming_only = false, device_level_enabled = true
                )
            )
        }

        fun processIntent(intent: Intent) {
            if (!intent.hasExtra(Constants.INTENT_EXTRA_NOTIFICATION_ID)) {
                return
            }
            if (NotificationPollBroadcastReceiver.ACTION_CANCEL == intent.action) {
                logDismissed(intent)
            } else {
                logClicked(intent)
            }
        }

        fun submit(notificationEvent: NotificationInteractionEvent) {
            submitEvent(
                "notification_interaction",
                mapOf(
                    "notification_id" to notificationEvent.notification_id,
                    "notification_wiki" to notificationEvent.notification_wiki,
                    "notification_type" to notificationEvent.notification_type,
                    "action_rank" to notificationEvent.action_rank,
                    "action_icon" to notificationEvent.action_icon,
                    "selection_token" to notificationEvent.selection_token,
                    "incoming_only" to notificationEvent.incoming_only,
                    "device_level_enabled" to notificationEvent.device_level_enabled
                )
            )
        }
    }
}