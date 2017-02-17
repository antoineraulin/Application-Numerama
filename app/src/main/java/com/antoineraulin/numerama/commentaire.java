package com.antoineraulin.numerama;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class commentaire extends AppCompatActivity {
    private WebView wv1;
    ProgressBar simpleProgressBar;
    String originalData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commentaire);
        try {
            String data = getIntent().getExtras().getString("link");
            originalData = getIntent().getExtras().getString("originalLink");
            System.out.println("data = " + data);
            simpleProgressBar = (ProgressBar) findViewById(R.id.progressBar3);
            wv1 = (WebView) findViewById(R.id.webview);
            wv1.setWebViewClient(new MyBrowser());
            wv1.getSettings().setLoadsImagesAutomatically(true);
            wv1.getSettings().setJavaScriptEnabled(true);
            wv1.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
            simpleProgressBar.setVisibility(VISIBLE);
            wv1.loadUrl(data);
        }catch (Exception error){
            System.out.println("ERROR "+ error);
        }
    }
    private class MyBrowser extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            simpleProgressBar.setVisibility(VISIBLE);
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url)
        {
            simpleProgressBar.setVisibility(INVISIBLE);
            // hide element by class name
            System.out.println("hiding...Please Wait");
            wv1.loadUrl("javascript:(function() { " +
                    "document.getElementById('close-comments').style.display='none';})()");
            wv1.loadUrl("javascript:(function() { " +
                    "document.getElementById('full-discussion').style.display='none';})()");

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_close) {
            Intent intent=new Intent(this,readArticle.class);
            intent.putExtra("link", originalData);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
