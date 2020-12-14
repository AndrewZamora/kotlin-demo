package com.example.kotlin_demo

import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import org.w3c.dom.Text

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        val webView = findViewById<WebView>(R.id.webView)

        webView.webViewClient = WebViewClient()
        webView.loadUrl("http://10.0.2.2:5500")
        webView.settings.javaScriptEnabled = true

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
//            val textDisplayedValue = findViewById<TextView>(R.id.textDisplayedValue)
//            val originalValue: Int = textDisplayedValue.text.toString().toInt()
//            val newValue: Int = originalValue * 2
//            textDisplayedValue.text = newValue.toString()
//            Snackbar.make(view, "Value $originalValue changed to $newValue", Snackbar.LENGTH_LONG).show()
            sendDataToWebView(webView)
        }


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
}