package com.faunadb.app;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private Handler handler;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.textView_content);

        String endpoint = getResources().getString(R.string.fauna_endpoint);
        textView.setText(getResources().getString(R.string.fetching_msg, endpoint));

        handler = new Handler();
        handler.postDelayed(new InspectFauna(), 2500);
    }

    class InspectFauna implements Runnable {

        @Override
        public void run() {
            InspectFaunaDBTask job = new InspectFaunaDBTask(MainActivity.this, resources -> {
                StringBuilder sb = new StringBuilder();
                sb.append("<h1>Fauna Resources</h1>");
                sb.append("<dl>");

                for(FaunaResource r: resources) {
                    sb.append("<dt>").append(r.getName()).append(":</dt>");
                    sb.append("<dd><ol>");
                    for(String row: r.getContent())
                        sb.append("<li>").append(row).append("</li>");
                    sb.append("</ol></dd>");
                }

                sb.append("</dl>");

                Log.i("FAUNA", String.format("Result HTML : %s", sb));

                textView.setText(Html.fromHtml(sb.toString(), Html.FROM_HTML_MODE_COMPACT));
            });

            job.execute("classes", "indexes", "databases");
        }
    }


}
