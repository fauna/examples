package com.faunadb.app

import androidx.appcompat.app.AppCompatActivity

import android.os.Bundle
import android.os.Handler
import android.text.Html
import android.util.Log
import android.widget.TextView
import java.util.function.Consumer

class MainActivity : AppCompatActivity() {

    private var handler: Handler? = null
    private var textView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textView = findViewById(R.id.textView_content)

        val endpoint = resources.getString(R.string.fauna_endpoint)
        textView!!.text = resources.getString(R.string.fetching_msg, endpoint)

        handler = Handler()
        handler!!.postDelayed(InspectFauna(), 2500)
    }

    internal inner class InspectFauna : Runnable {

        override fun run() {
            val job = InspectFaunaDBTask(this@MainActivity, Consumer { resources ->
                val sb = StringBuilder()
                sb.append("<h1>Fauna Resources</h1>")
                sb.append("<dl>")

                resources.forEach { r ->
                    sb.append("<dt>").append(r.name).append(":</dt>")
                    sb.append("<dd><ol>")
                    for (row in r.content)
                        sb.append("<li>").append(row).append("</li>")
                    sb.append("</ol></dd>")
                }
                sb.append("</dl>")

                Log.i("FAUNA", "Result HTML : $sb")

                textView!!.text = Html.fromHtml(sb.toString(), Html.FROM_HTML_MODE_COMPACT)
            })

            job.execute("classes", "indexes", "databases")
        }
    }


}
