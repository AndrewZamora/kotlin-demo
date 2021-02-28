package com.example.kotlin_demo

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.media.MediaBrowserServiceCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    @SuppressLint("JavascriptInterface")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        val webView = findViewById<WebView>(R.id.webView)
        test = webView
        webView.webViewClient = WebViewClient()

        webView.settings.javaScriptEnabled = true
        webView.addJavascriptInterface(JSBridge(this@MainActivity),"JSBridge")
        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
//            val textDisplayedValue = findViewById<TextView>(R.id.textDisplayedValue)
//            val originalValue: Int = textDisplayedValue.text.toString().toInt()
//            val newValue: Int = originalValue * 2
//            textDisplayedValue.text = newValue.toString()
//            Snackbar.make(view, "Value $originalValue changed to $newValue", Snackbar.LENGTH_LONG).show()
            sendDataToWebView(webView)
        }

        findViewById<Button>(R.id.button3).setOnClickListener { view->
//            showDefaultNotification("Test Content", "A fake notification.", "Default")
            showNotificationWithCustomTemplate("Kotlin-Demo", "test", "Default")
        }

        webView.webViewClient = object : WebViewClient() {

//            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
//                val url = request?.url.toString()
//                if(!url.contains("5500")) {
//                    webView.stopLoading()
//                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
//                    startActivity(browserIntent)
//                } else {
//                    return super.shouldOverrideUrlLoading(view, request)
//                }
//                return true
//            }
        }
         webView.loadUrl("http://10.0.2.2:5500")
//        Handle Notification Channel
        createNotificationChannel("Default")

    }

    override fun onNewIntent(intent: Intent?) {
        val url = intent?.getStringExtra("URL")
        if(url != null) {
            var webView = findViewById<WebView>(R.id.webView)
            webView.loadUrl(url)
        }
        super.onNewIntent(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Send data to webView through function updateFromNative.
     */
    private fun sendDataToWebView(webView: WebView){
        webView.evaluateJavascript("javascript: updateFromNative('')",null)
    }

    override fun onBackPressed() {
        val webView = findViewById<WebView>(R.id.webView)
            if (webView.canGoBack()) {
                webView.goBack()
            } else {
                super.onBackPressed()
            }
    }

    private fun createNotificationChannel(channelId: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

    }

    private fun getBitmapFromUrl(test:String): Bitmap? {
        return try {
            val connection = URL(test).openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input = connection.inputStream
            BitmapFactory.decodeStream(input)
        } catch (e: IOException) {
            null
        }
    }

    private fun showDefaultNotification(textContent: String, textTitle: String, channelId: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP )
        intent.putExtra("URL", "https://www.google.com")
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(textContent)
                .setContentText(textTitle)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
        // Not the best way to create a unique ID but a unique ID is needed to prevent the last notification from being overwritten
        val uniqueId = Random(System.currentTimeMillis()).nextInt(1000)
        notificationManager.notify(uniqueId/* ID of notification */, notificationBuilder.build())
    }

    private fun showNotificationWithCustomTemplate(textContent: String, textTitle: String, channelId: String) {
        val self = this
        val intent = Intent(this, MainActivity::class.java)
        CoroutineScope(IO).launch {
            val image = async { getBitmapFromUrl("https://upload.wikimedia.org/wikipedia/commons/thumb/7/73/001_Tacos_de_carnitas%2C_carne_asada_y_al_pastor.jpg/330px-001_Tacos_de_carnitas%2C_carne_asada_y_al_pastor.jpg")}.await()
            // https://developer.android.com/training/notify-user/custom-notification#kotlin
            // RemoteViews doesn't support constraint layouts see: https://developer.android.com/reference/android/widget/RemoteViews
            val notificationLayout = RemoteViews(packageName, R.layout.custom_notification_small)
            val notificationLayoutExpanded = RemoteViews(packageName, R.layout.custom_notification_large)
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP )
            val notificationBuilder = NotificationCompat.Builder(self, channelId)
            notificationBuilder
                    .setSmallIcon(R.mipmap.ic_launcher)
//                    .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                    .setCustomContentView(notificationLayout)
//                    .setCustomBigContentView(notificationLayoutExpanded)
            // Add custom text
            notificationLayout.setTextViewText(R.id.custom_notification_small_text, textContent)
            notificationLayoutExpanded.setTextViewText(R.id.custom_notification_large_text, textContent)
            // Add custom image
            notificationLayoutExpanded.setImageViewBitmap(R.id.custom_notification_large_image,image)
            notificationLayout.setImageViewBitmap(R.id.custom_notification_large_image,image)
            // Not the best way to create a unique ID but a unique ID is needed to prevent the last notification from being overwritten
            val uniqueId = Random(System.currentTimeMillis()).nextInt(1000)
            notificationManager.notify(uniqueId/* ID of notification */, notificationBuilder.build())
        }


    }

    companion object {
        lateinit var test: WebView
        fun UpdateAudioState(state:String) {
        // Changes on the webview have to happen on the same thread as the UI
        // https://stackoverflow.com/questions/22607657/webview-methods-on-same-thread-error
           test.post(Runnable {
               test.evaluateJavascript("javascript: updateAudioState('$state')",null)
           })
        }
    }

}

