package org.techtown.photo

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import net.gotev.uploadservice.BuildConfig
import net.gotev.uploadservice.UploadServiceConfig
import net.gotev.uploadservice.UploadServiceConfig.retryPolicy
import net.gotev.uploadservice.data.RetryPolicyConfig
import net.gotev.uploadservice.observer.request.GlobalRequestObserver


/**
 * Base Application class
 *
 * 1. 파일 업로드 라이브러리를 위한 초기화
 *
 */
class App : Application() {

    companion object {
        const val notificationChannelID = "UploadServiceChannel"
    }

    // Customize the notification channel as you wish. This is only for a bare minimum example
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(
                notificationChannelID,
                "Upload Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onCreate() {
        super.onCreate()

        // Config 초기화
        UploadServiceConfig.initialize(
            context = this,
            defaultNotificationChannel = notificationChannelID,
            debug = BuildConfig.DEBUG
        )

        // 알림 채널 설정
        createNotificationChannel()

        // 재시도 정책
        retryPolicy = RetryPolicyConfig(1, 10, 2, 3)

        // 옵저버 설정
        GlobalRequestObserver(this, GlobalUploadObserver())

    }

}