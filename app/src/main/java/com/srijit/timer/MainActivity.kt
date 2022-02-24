package com.srijit.timer

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.AdapterView
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.srijit.timer.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class MainActivity : AppCompatActivity() {

    val viewModel by viewModels<MainViewModel>()

    lateinit var binding: ActivityMainBinding
    var time = 0
    var serviceStartedFlag = false
    lateinit var serviceIntent: Intent
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this
        binding.vm = viewModel

        checkIfLaunchFromNotification()
    }

    private fun checkIfLaunchFromNotification() {
        val timeLeft = intent.getIntExtra("timeLeft", -1)
        if (timeLeft != -1) {
            serviceStartedFlag = true
            viewModel.toggleTimerRunning(true)
        }
    }

    override fun onResume() {
        super.onResume()
        initSpinner()
        initClick()
        LocalBroadcastManager.getInstance(this).registerReceiver(
            localBroadcastManager,
            IntentFilter("timer-broadcast")
        )
    }

    @SuppressLint("NewApi")
    private fun initClick() {
        binding.btTimerToggle.setOnClickListener {
            if (!serviceStartedFlag) {
                if (time != 0) {
                    viewModel.toggleTimerRunning(true)
                    serviceIntent = Intent(this@MainActivity, TimerService::class.java)
                    serviceIntent.putExtra("time", time)
                    startForegroundService(serviceIntent)
                    serviceStartedFlag = true
                }
            } else {

                serviceStartedFlag = false
                viewModel.toggleTimerRunning(false)
                stopService(serviceIntent)
                binding.timerProgress.progress = 0
                binding.tvTimerText.text = ""
            }

        }
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(localBroadcastManager)
    }

    private val localBroadcastManager = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.getIntExtra("timeLeft", -1)?.let {
                if (it != -1)
                    binding.tvTimerText.text = "$it seconds"
            }
            intent?.getIntExtra("progress", -1)?.let {
                Log.d("msg1", it.toString())
                if (it != -1)
                    binding.timerProgress.progress = it
            }

        }

    }

    private fun initSpinner() {
        binding.timeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                Log.d("msg1", position.toString())
                if (position != 0) {
                    time = resources.getStringArray(R.array.time_array)[position].toInt()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

        }
    }
}