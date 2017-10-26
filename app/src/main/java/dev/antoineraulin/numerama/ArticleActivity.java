package dev.antoineraulin.numerama;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.chrisbanes.photoview.PhotoView;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArticleActivity extends AppCompatActivity {

    String backUrl = null;
    String artTitle = null;
    String artUrl = null;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);
         dialog = ProgressDialog.show(ArticleActivity.this, "",
                "Chargement de votre article...", true);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        final int[] avrColor = new int[1];
        setSupportActionBar(toolbar);
        final String title = getIntent().getExtras().getString("title");
        final CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
        final ImageView background = findViewById(R.id.ivBigImage);
        final String backgroundUrl = getIntent().getExtras().getString("image");
        backUrl = backgroundUrl;
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    collapsingToolbarLayout.setTitle(title);
                    Rect rect = new Rect(0, 0, 1, 1);

// You then create a Bitmap and get a canvas to draw into it
                    Bitmap image = Bitmap.createBitmap(rect.width(), rect.height(), Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(image);

//You can get an int value representing an argb color by doing so. Put 1 as alpha by default

//Paint holds information about how to draw shapes
                    Paint paint = new Paint();
                    paint.setColor(avrColor[0]);

// Then draw your shape
                    canvas.drawRect(rect, paint);
                    background.setImageBitmap(image);
                    isShow = true;
                } else if(isShow) {
                    collapsingToolbarLayout.setTitle(" ");//carefull there should a space between double quote otherwise it wont work
                    Ion.with(background)
                            .load(backgroundUrl);
                    isShow = false;
                }
            }
        });

        String url = getIntent().getExtras().getString("link");

        artUrl = url;


        Ion.with(background)
                .load(backgroundUrl);
        Ion.with(this).load(backgroundUrl).withBitmap().asBitmap()
                .setCallback(new FutureCallback<Bitmap>() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onCompleted(Exception e, Bitmap result) {
                        Bitmap bitmap = result;
                        int pixelSpacing = 1;
                        int R = 0; int G = 0; int B = 0;
                        int height = bitmap.getHeight();
                        int width = bitmap.getWidth();
                        int n = 0;
                        int[] pixels = new int[width * height];
                        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
                        for (int i = 0; i < pixels.length; i += pixelSpacing) {
                            int color = pixels[i];
                            R += Color.red(color);
                            G += Color.green(color);
                            B += Color.blue(color);
                            n++;
                        }
                        Window window = getWindow();
                        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                        window.setStatusBarColor(Color.rgb(R / n, G / n, B / n));
                        avrColor[0] = Color.rgb(R / n, G / n, B / n);
                    }
                });
        getArticle(url);

    }


    private void getArticle(final String url){
        Log.e("info", "getArticle() started");
        new Thread(new Runnable() {
            @Override
            public void run() {

                Ion.with(getApplicationContext())
                        .load(url)
                        .asString()
                        .setCallback(new FutureCallback<String>() {
                            @Override
                            public void onCompleted(Exception e, String result) {
                                Log.d("html", result);
                                String startupTitle = null;
                                Document doc = Jsoup.parse(result);
                                String titlee = doc.title();
                                titlee = StringUtils.substringBefore(titlee, " - ");
                                artTitle = titlee;
                                Elements authorElem = doc.select("meta[property=author]");
                                if(authorElem != null){
                                    createTextView(Html.fromHtml("<b> De "+authorElem.toString()+"</b>".replace("&NBSP;", " ")),"#d34e39");
                                }
                                createTitleTextView(titlee, "#e9573f", dpToPixel(7));
                                Element article = doc.select("article").get(0);
                                Elements items = article.select("p,h2,ul,h4,figure,blockquote,.startup-preview");

                                for (Element item : items) {
                                    String data = item.toString();
                                    if(item.hasClass("chapo")){
                                        data = "<b>"+data+"</b>";
                                    }
                                    if(item.toString().startsWith("<h2>")){
                                        data = data.toUpperCase();
                                        if(data.contains("<SPAN")){
                                        String span = getStringBetween(data, "<SPAN", "</SPAN>");
                                        data = data.replace("<SPAN"+span+"</SPAN>", "");}
                                        createTextView(Html.fromHtml(data.replace("&NBSP;", " ")),"#d34e39");
                                    }else if(item.toString().startsWith("<h4>")){
                                        if(data.contains("<span")){
                                            String span = getStringBetween(data, "<span", "</span>");
                                            data = data.replace("<span"+span+"</span>", "");}
                                        createTextView(Html.fromHtml(data.replace("&nbsp;", " ")),"#444444");
                                    }
                                    else if(item.toString().startsWith("<ul") && !item.toString().contains("tags-list")){
                                        Document list = Jsoup.parse(item.toString());
                                        Elements listItems = list.select("li");
                                        for (Element listItem : listItems){
                                            if(!listItem.toString().contains("numerama.com/tag")) createListTextView(Html.fromHtml(listItem.toString()));
                                        }
                                    }
                                    else if(item.toString().startsWith("<div class=\"startup-preview")){
                                        String html = item.toString();
                                        String color = getStringBetween(html, "style=\"background-color:", "\">");
                                        String image = getStringBetween(html, "<img src=\"", "\"> </a>");
                                        String title = getStringBetween(html, "<h4>", "</h4>");
                                        startupTitle = title;
                                        String subtitle = getStringBetween(html, "<p class=\"subtitle\">", "</p>");
                                        String textColor = "#f7f7f7";
                                        if(!isColorDark(Color.parseColor(color))) textColor = "black";
                                        String finalHTML = "<html>\n" +
                                                "    <body style=\"margin: 0px;\">\n" +
                                                "    <div style=\"background-color: "+color+";height: 100%;\">\n" +
                                                "    <img src=\"http:"+image+"\" style=\"height: auto;width: 4em;display: inline-block;top: 15px;bottom: 15px;position: fixed;padding-left: 10px;\">\n" +
                                                "        <div style=\"\n" +
                                                "    display: inline-block;\n" +
                                                "    height: auto;\n" +
                                                "    top: 2px;\n" +
                                                "    bottom: 0px;\n" +
                                                "    position: relative;\n" +
                                                "    left: 87px;\n" +
                                                "    color: "+textColor+";\n" +
                                                "line-height: 1em;\n"+
                                                "    font-family: Arial;\n" +
                                                "\">\n" +
                                                "        <h3>"+title.toUpperCase()+"</h3>\n" +
                                                "            <p style=\"font-size: 0.8em\">"+subtitle+"</p>\n" +
                                                "        </div>\n" +
                                                "    </div>\n" +
                                                "    \n" +
                                                "</body></html>";

                                        createStarpupView(finalHTML, dpToPixel(90));
                                    }
                                    else if(item.toString().startsWith("<blockquote>")){
                                        String quote = Html.fromHtml(item.toString()).toString();
                                        quote = quote.toUpperCase();
                                        if(quote.contains("«")) quote = quote;
                                        else quote = "<i>« "+quote+" »</i>";
                                        createQuoteTextView(Html.fromHtml(quote.replace("&NBSP;", " ")),"#e9573f", dpToPixel(11));
                                    }else if(item.toString().startsWith("<blockquote")){
                                        String quote = Html.fromHtml(item.toString()).toString();
                                        quote = "<i>“ "+quote+" ”</i>";
                                        //createQuoteTextView(Html.fromHtml(quote.replace("&NBSP;", " ")),"#e9573f", dpToPixel(9));
                                    }else if(item.toString().startsWith("<figure") && item.toString().contains("<iframe")){
                                        Pattern pattern2 = Pattern.compile("src=\"(.*?)\"");
                                        Matcher matcher2 = pattern2.matcher(item.toString());
                                        while (matcher2.find()) {
                                            String url = matcher2.group(1);
                                            int viewSize = dpToPixel(230);
                                            if(matcher2.group(1).contains("numerama.com")) {url = "http:" + matcher2.group(1);viewSize = dpToPixel(400);}
                                            System.out.println("found embedded content: "+url);
                                            createFigureEmbeddedContentView(url, viewSize);
                                        }
                                    }
                                    else if(item.toString().startsWith("<figure")){
                                        Pattern pattern2 = Pattern.compile("src=\"(.*?)\"");
                                        Matcher matcher2 = pattern2.matcher(item.toString());
                                        while (matcher2.find()) {
                                            String url = "http:" + matcher2.group(1);
                                            System.out.println("found figure: "+url);
                                            createFigureImageView(matcher2.group(1));
                                        }
                                    }
                                    else if(item.toString().contains("<iframe")){
                                        Pattern pattern2 = Pattern.compile("src=\"(.*?)\"");
                                        Matcher matcher2 = pattern2.matcher(item.toString());
                                        while (matcher2.find()) {
                                            String url = matcher2.group(1);
                                            int viewSize = dpToPixel(200);
                                            if(matcher2.group(1).contains("numerama.com")) {url = "http:" + matcher2.group(1);viewSize = dpToPixel(400);}
                                            System.out.println("found embedded content: "+url);
                                            createEmbeddedContentView(url, viewSize);
                                        }
                                    }
                                    else if(item.toString().contains("<img")){
                                        Pattern pattern2 = Pattern.compile("src=\"(.*?)\"");
                                        Matcher matcher2 = pattern2.matcher(item.toString());
                                        while (matcher2.find()) {
                                            String url = "http:" + matcher2.group(1);
                                            System.out.println("found normal image: "+url);
                                            createImageView(matcher2.group(1));
                                        }
                                    }else{

                                        if(!data.contains("<li") && !data.contains("class=\"subtitle") && !data.contains("<h4>"+startupTitle+"</h4>") && !data.contains("class=\"subtitle") && !data.contains("Partager sur les réseaux sociaux")) createTextView(Html.fromHtml(data.replace("\"//www.numerama", "\"http://www.numerama")));
                                    }
                                    Log.d("items", data);

                                }

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Ion.with(ArticleActivity.this).load(backUrl).withBitmap().asBitmap()
                                                .setCallback(new FutureCallback<Bitmap>() {
                                                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                                                    @Override
                                                    public void onCompleted(Exception e, Bitmap result) {
                                                        Bitmap bitmap = result;
                                                        int pixelSpacing = 1;
                                                        int Red = 0; int G = 0; int B = 0;
                                                        int height = bitmap.getHeight();
                                                        int width = bitmap.getWidth();
                                                        int n = 0;
                                                        int[] pixels = new int[width * height];
                                                        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
                                                        for (int i = 0; i < pixels.length; i += pixelSpacing) {
                                                            int color = pixels[i];
                                                            Red += Color.red(color);
                                                            G += Color.green(color);
                                                            B += Color.blue(color);
                                                            n++;
                                                        }
                                                        LinearLayout linearLayout = findViewById(R.id.NestedViewLinear);
                                                        TextView shareButton = new TextView(ArticleActivity.this);
                                                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                                                LinearLayout.LayoutParams.MATCH_PARENT, dpToPixel(60));

                                                        layoutParams.setMargins(dpToPixel(20),dpToPixel(10), dpToPixel(20), 0);

                                                        shareButton.setPadding(dpToPixel(6), 0, dpToPixel(16), dpToPixel(6));
                                                        shareButton.setText("PARTAGER");
                                                        shareButton.setGravity(Gravity.CENTER);
                                                        shareButton.setTextSize(dpToPixel(8));
                                                        shareButton.setTypeface(null, Typeface.BOLD);
                                                        shareButton.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                                                        if(isColorDark(Color.rgb(Red / n, G / n, B / n))){
                                                            shareButton.setTextColor(Color.WHITE);
                                                        }
                                                        shareButton.setBackgroundColor(Color.rgb(Red / n, G / n, B / n));
                                                        linearLayout.addView(shareButton, layoutParams);
                                                        shareButton.setOnClickListener(new View.OnClickListener() {
                                                            public void onClick(View v) {
                                                                Intent sendIntent = new Intent();
                                                                sendIntent.setAction(Intent.ACTION_SEND);
                                                                sendIntent.putExtra(Intent.EXTRA_TEXT, artTitle + " : "+artUrl);
                                                                sendIntent.setType("text/plain");
                                                                startActivity(Intent.createChooser(sendIntent, "Partager cet article..."));
                                                            }});

                                                    }
                                                });


                                    }
                                });


                            dialog.dismiss();
                            }
                        });


            }
        }).start();
    }

    public String getStringBetween(String s, String first, String second){
        return s.substring(s.indexOf(first) + first.length(), s.indexOf(second));
    }

    private void createTextView(Spanned text){
        LinearLayout linearLayout = findViewById(R.id.NestedViewLinear);
        TextView textView = new TextView(this);
        Linkify.addLinks(textView, Linkify.WEB_URLS);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setText(text);
        textView.setTextAlignment(View.TEXT_ALIGNMENT_INHERIT);
        textView.setPadding(dpToPixel(16), 0, dpToPixel(16), dpToPixel(-27));
        linearLayout.addView(textView);
    }
    private void createListTextView(Spanned text){
        LinearLayout linearLayout = findViewById(R.id.NestedViewLinear);
        TextView textView = new TextView(this);
        Linkify.addLinks(textView, Linkify.WEB_URLS);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setText(text);
        textView.setTextAlignment(View.TEXT_ALIGNMENT_INHERIT);
        textView.setPadding(dpToPixel(16), 0, dpToPixel(16), dpToPixel(-35));
        linearLayout.addView(textView);
    }
    private void createTextView(Spanned text, String hexcolor){
        LinearLayout linearLayout = findViewById(R.id.NestedViewLinear);
        TextView textView = new TextView(this);
        Linkify.addLinks(textView, Linkify.WEB_URLS);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setText(text);
        textView.setTextColor(Color.parseColor(hexcolor));
        textView.setTextAlignment(View.TEXT_ALIGNMENT_INHERIT);
        textView.setPadding(dpToPixel(16), 0, dpToPixel(16), dpToPixel(-27));
        linearLayout.addView(textView);
    }
    private void createQuoteTextView(Spanned text, String hexcolor,int textSize){
        LinearLayout linearLayout = findViewById(R.id.NestedViewLinear);
        TextView textView = new TextView(this);
        Linkify.addLinks(textView, Linkify.WEB_URLS);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setText(text);
        textView.setTextColor(Color.parseColor(hexcolor));
        textView.setGravity(View.TEXT_ALIGNMENT_CENTER);
        textView.setTextSize(textSize);
        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        textView.setPadding(dpToPixel(16), dpToPixel(5), dpToPixel(16), dpToPixel(15));
        linearLayout.addView(textView);
    }

    private void createImageView(String url){
        url = "http:"+url;
        Log.d("image url", url);
        LinearLayout linearLayout = findViewById(R.id.NestedViewLinear);
        PhotoView imageView = new PhotoView(this);
        imageView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dpToPixel(200)));
        Ion.with(imageView)
                .load(url);
        imageView.setPadding(dpToPixel(6), 0, dpToPixel(16), dpToPixel(6));

        linearLayout.addView(imageView);
    }
    private void createFigureImageView(String url){
        url = "http:"+url;
        Log.d("image url", url);
        LinearLayout linearLayout = findViewById(R.id.NestedViewLinear);
        PhotoView imageView = new PhotoView(this);
        imageView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dpToPixel(300)));
        Ion.with(imageView)
                .load(url);
        imageView.setPadding(dpToPixel(0), 0, dpToPixel(0), dpToPixel(10));

        linearLayout.addView(imageView);
    }
    public boolean isColorDark(int color){
        double darkness = 1-(0.299*Color.red(color) + 0.587*Color.green(color) + 0.114*Color.blue(color))/255;
        if(darkness<0.5){
            return false; // It's a light color
        }else{
            return true; // It's a dark color
        }
    }
    private void createEmbeddedContentView(String url, int viewSize){
        Log.d("embeddedContent url", url);
        LinearLayout linearLayout = findViewById(R.id.NestedViewLinear);
        WebView webView = new WebView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                viewSize
        );
        params.setMargins(dpToPixel(16), dpToPixel(10), dpToPixel(16), dpToPixel(5));
        webView.setLayoutParams(params);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(url);

        linearLayout.addView(webView);
    }
    private void createFigureEmbeddedContentView(String url, int viewSize){
        Log.d("FigureEmbeddedContent", url);
        LinearLayout linearLayout = findViewById(R.id.NestedViewLinear);
        WebView webView = new WebView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                viewSize
        );
        params.setMargins(0, 0, 0, dpToPixel(16));
        webView.setLayoutParams(params);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(url);

        linearLayout.addView(webView);
    }
    private void createStarpupView(String html, int viewSize){
        LinearLayout linearLayout = findViewById(R.id.NestedViewLinear);
        WebView webView = new WebView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                viewSize
        );
        params.setMargins(dpToPixel(16), dpToPixel(10), dpToPixel(16), dpToPixel(5));
        webView.setLayoutParams(params);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadData(html, "text/html", "UTF-8");

        linearLayout.addView(webView);
    }

    private void createTitleTextView(String text, String hexcolor, int textSize){
        LinearLayout linearLayout = findViewById(R.id.NestedViewLinear);
        TextView textView = new TextView(this);
        Linkify.addLinks(textView, Linkify.WEB_URLS);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setText(text);
        textView.setTextColor(Color.parseColor(hexcolor));
        textView.setGravity(View.TEXT_ALIGNMENT_CENTER);
        textView.setTextSize(textSize);
        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        textView.setPadding(dpToPixel(16), dpToPixel(0), dpToPixel(16), dpToPixel(15));
        linearLayout.addView(textView);
    }

    private int dpToPixel(int dp){
        final float scale = this.getResources().getDisplayMetrics().density;
        int pixels = (int) (dp * scale + 0.5f);
        return pixels;
    }
}
