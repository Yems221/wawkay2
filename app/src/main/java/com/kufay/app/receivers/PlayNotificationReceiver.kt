package com.kufay.app.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.kufay.app.data.repository.NotificationRepository
import com.kufay.app.service.TTSService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * BroadcastReceiver qui gère le clic sur le bouton "Écouter"
 * dans les notifications Kufay
 */
@AndroidEntryPoint
class PlayNotificationReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationRepository: NotificationRepository

    @Inject
    lateinit var ttsService: TTSService

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "com.kufay.app.ACTION_PLAY_NOTIFICATION") {
            val notificationId = intent.getLongExtra("notification_id", -1L)

            if (notificationId != -1L) {
                Log.d("KUFAY_PLAY", "▶ Bouton play cliqué pour notification ID: $notificationId")

                // Récupérer la notification et la lire
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val notification = notificationRepository.getNotificationById(notificationId)

                        if (notification != null) {
                            Log.d("KUFAY_PLAY", "📢 Lecture de la notification: ${notification.title}")

                            // Lire la notification via TTS
                            ttsService.speakNotification(notification, isRecognizedPattern = true)
                        } else {
                            Log.e("KUFAY_PLAY", "❌ Notification introuvable (ID: $notificationId)")
                        }
                    } catch (e: Exception) {
                        Log.e("KUFAY_PLAY", "❌ Erreur lors de la lecture: ${e.message}", e)
                    }
                }
            }
        }
    }
}