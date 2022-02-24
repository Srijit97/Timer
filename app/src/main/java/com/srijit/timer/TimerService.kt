package com.srijit.timer

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_DEFAULT
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

@SuppressLint("NewApi")
class TimerService: Service() {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val binder = Binder()
    var timeLeft = 0
    var time = 0

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.getIntExtra("time",-1)?.let {
            time = it*60
            val notification = getNotification(it)
            startForeground(121,notification)

            if(it!=-1)
                startTimer(time)
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun getNotificationManager(): NotificationManager{
        return getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    private fun getNotification(time: Int): Notification {
        val manager = getNotificationManager()
        val notificationChannel  = NotificationChannel("CHANNEL_ID","timer_channel",
            IMPORTANCE_DEFAULT)
        manager.createNotificationChannel(notificationChannel)

        return NotificationCompat.Builder(this, "CHANNEL_ID")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(time.toString())
            .setContentIntent(getIntent())
            .setOngoing(true)
            .build()
    }

    fun getIntent(): PendingIntent {

        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.putExtra("timeLeft",timeLeft)
        intent.putExtra("progress",(((time-timeLeft)/time.toDouble())*100).roundToInt())
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

        return PendingIntent.getActivity(
            this,
            150,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }


    private fun startTimer(time: Int) {

        timeLeft = time
        scope.launch {
            while(timeLeft>0){
                delay(1000)
                timeLeft--
                withContext(Dispatchers.Main){
                    val progressIntent = Intent("timer-broadcast")
                    progressIntent.putExtra("timeLeft",timeLeft)
                    progressIntent.putExtra("progress",(((time-timeLeft)/time.toDouble())*100).roundToInt())
                    LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(progressIntent)
                    updateNotification(timeLeft)
                }
                if(timeLeft==1)
                    stopSelf()

            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
        getNotificationManager().cancel(121)
    }

    private fun updateNotification(timeLeft: Int) {
        val notification = NotificationCompat.Builder(this, "CHANNEL_ID")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(timeLeft.toString())
            .setOngoing(true)
            .setContentIntent(getIntent())
            .build()
        getNotificationManager().notify(121,notification)
    }
}