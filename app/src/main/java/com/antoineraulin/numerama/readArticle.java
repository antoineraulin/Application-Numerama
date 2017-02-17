package com.antoineraulin.numerama;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;
import com.google.android.youtube.player.YouTubePlayerView;
import com.squareup.picasso.Picasso;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xml.sax.InputSource;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.google.android.youtube.player.YouTubeBaseActivity;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import static java.security.AccessController.getContext;

public class readArticle extends AppCompatActivity {

    private int ytfragmentCount = 0;
    private YouTubePlayer YPlayer;
    private SwipeRefreshLayout swipeContainer;
    private String data;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_article);
         data = getIntent().getExtras().getString("link");
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swypelayout);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new RetrieveFeedTask().execute(data);
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(R.color.colorPrimary,
                R.color.colorPrimaryDark );

        swipeContainer.setRefreshing(true);
        new RetrieveFeedTask().execute(data);

    }

    class RetrieveFeedTask extends AsyncTask<String, Void, String> {

        private Exception exception;

        protected String doInBackground(String... urls) {
            String html =  "hello" ;
            try {
                System.out.println("begin of the truc");

                HttpClient httpclient = new DefaultHttpClient(); // Create HTTP Client
                HttpGet httpget = new HttpGet(urls[0]); // Set the action you want to do
                HttpResponse response = httpclient.execute(httpget); // Executeit
                HttpEntity entity = response.getEntity();
                InputStream is = entity.getContent(); // Create an InputStream with the response
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"), 8);
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) // Read line by line
                    sb.append(line + "\n");

                String resString = sb.toString();
                Document doc = Jsoup.parse(resString);
                Elements elements = doc.select("p[class=chapo]");
                System.out.println(elements.html());
                String chapo = elements.html();
                Elements h1element = doc.select("h1[itemprop=headline]");
                String headline = h1element.html();
                Elements spanelement = doc.select("span[itemprop=name]");
                String author = spanelement.html();
                Elements articleelement = doc.select("article");


                is.close();
                return headline+"##AAA##"+author+"##AAA##"+chapo+"##AAA##"+articleelement.html();
                //Pattern pattern = Pattern.compile("<article>(.*?)</article>");
                //Matcher matcher = pattern.matcher(resString);
                //while (matcher.find()) {
                //    System.out.println(matcher.group(1));
                //    html = matcher.group(1);
                //    return matcher.group(1);

                //}
            } catch (Exception e) {
                System.out.println("error = " + e);
                return null;
            }
        }

        protected void onPostExecute(String feed) {
            System.out.println("feed = "+ feed);
            String[] parts = feed.split("##AAA##");
            TextView headlineTextView = (TextView) findViewById(R.id.headlinehea);
            headlineTextView.setText(parts[0].replace("&nbsp;"," "));
            TextView authorTextView = (TextView) findViewById(R.id.author);
            if(parts[1].isEmpty()){
                authorTextView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dpToPixel(0)));
            }else{
                System.out.println("pars 1 = '"+parts[1]+"'");
            authorTextView.setText("par "+parts[1]);}
            TextView chapoTextView = (TextView) findViewById(R.id.chapo);
            chapoTextView.setText(parts[2].replace("&nbsp;"," "));
            Document ArticleDoc = Jsoup.parse(parts[3]);
            System.out.println(parts[3]);
            String paragraphe[] = new String[300];
            Elements articleP = ArticleDoc.select("p");
            int time = 0;
            for(Element p : articleP){
                paragraphe[time] = p.html();
                System.out.println("paragraphe : "+p.html()); //get all elements inside
                time++;
            }
            int time2 = 0;
            boolean stop = false;
            while(time2 <= paragraphe.length && paragraphe[time2] != null && !stop){
                /*Pattern pattern = Pattern.compile("<a(.*?)>");
                Matcher matcher = pattern.matcher(paragraphe[time2]);
                while (matcher.find()) {
                    System.out.println("found : "+matcher.group(1));
                    paragraphe[time2] = paragraphe[time2].replace(matcher.group(1), "");
                    paragraphe[time2] = paragraphe[time2].replace("<a>", "");
                    paragraphe[time2] = paragraphe[time2].replace("</a>", "");
                }*/
                if(paragraphe[time2].matches("(Partager sur les réseaux sociaux|&nbsp;|Wed|Thurs|Fri).*")) stop = true;
                else{
                    if(paragraphe[time2].contains("<img")){
                        Pattern pattern2 = Pattern.compile("src=\"(.*?)\"");
                        Matcher matcher2 = pattern2.matcher(paragraphe[time2]);
                        while (matcher2.find()) {
                            String url = "http:" + matcher2.group(1);
                            System.out.println("found : "+url);
                            createImageView(matcher2.group(1));
                        }
                    }else if(paragraphe[time2].matches("(<iframe width=\"500\").*")){
                        Pattern pattern2 = Pattern.compile("src=\"(.*?)\"");
                        Matcher matcher2 = pattern2.matcher(paragraphe[time2]);
                        while (matcher2.find()) {
                            String url = "http:" + matcher2.group(1);
                            System.out.println("found : "+url);
                            createyoutubefragment(matcher2.group(1));
                        }
                    }else{
                        if (time2 != 0) createTextView(Html.fromHtml(paragraphe[time2]));
                    }

                        }

                time2++;
            }
            swipeContainer.setRefreshing(false);
        }


    }
    private void createTextView(Spanned text){
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.ScrollLinear);
        TextView textView = new TextView(this);
        Linkify.addLinks(textView, Linkify.WEB_URLS);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setText(text);
        textView.setPadding(dpToPixel(16), 0, dpToPixel(16), dpToPixel(16));
        linearLayout.addView(textView);
        System.out.println("textview added with value :"+text);
    }
    private void createImageView(String url){
        url = "http:"+url;
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.ScrollLinear);
        ImageView imageView = new ImageView(this);
        imageView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dpToPixel(200)));
        Picasso.with(imageView.getContext()).load(url).centerCrop().fit().into(imageView);
        imageView.setPadding(dpToPixel(16), 0, dpToPixel(16), dpToPixel(16));

        linearLayout.addView(imageView);
        System.out.println("imageview added with image from url : "+url);
    }

    private void createyoutubefragment(String url){
        int thefragmentname = R.id.youtube_fragment;
        int thelayoutename = R.layout.youtubelayout;
        if(ytfragmentCount == 0){
            thefragmentname = R.id.youtube_fragment;
            thelayoutename = R.layout.youtubelayout;
            ytfragmentCount++;
        }else if(ytfragmentCount == 1){
            thefragmentname = R.id.youtube_fragment2;
            thelayoutename = R.layout.youtubelayout2;
            ytfragmentCount++;
        }
        else if(ytfragmentCount == 2){
            thefragmentname = R.id.youtube_fragment3;
            thelayoutename = R.layout.youtubelayout3;
            ytfragmentCount++;
        }
        else if(ytfragmentCount == 3){
            thefragmentname = R.id.youtube_fragment4;
            thelayoutename = R.layout.youtubelayout4;
            ytfragmentCount++;
        }
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.ScrollLinear);
        LayoutInflater inflater = (LayoutInflater)      this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View childLayout = inflater.inflate(thelayoutename,
                (ViewGroup) findViewById(thefragmentname));
        childLayout.setPadding(0, 0, 0, dpToPixel(16));
        linearLayout.addView(childLayout);
        Pattern pattern2 = Pattern.compile("embed/(.*?)?feature");
        Matcher matcher2 = pattern2.matcher(url);
        while (matcher2.find()) {
            final String goodurl = matcher2.group(1).replace("?", "");
        YouTubePlayerSupportFragment youTubePlayerFragment = (YouTubePlayerSupportFragment) this.getSupportFragmentManager()
                .findFragmentById(thefragmentname);
        youTubePlayerFragment.initialize("AIzaSyD1kge49hUJDDW4nX25FsYJD7OKivy3MPU", new YouTubePlayer.OnInitializedListener() {


            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                if (!b) {
                    YPlayer = youTubePlayer;
                    YPlayer.cueVideo(goodurl);
                }
            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

            }
        });}
    }
    private int dpToPixel(int dp){
        final float scale = this.getResources().getDisplayMetrics().density;
        int pixels = (int) (dp * scale + 0.5f);
        return pixels;
    }
    private void showErrorAlert(){
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage("Oops, Une Erreur est survenue lors de l'affichage de cet article. Veuillez m'excusez pour la gêne occasionée.");

        builder1.setPositiveButton(
                "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent=new Intent(readArticle.this,MainActivity.class);
                        startActivity(intent);
                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.setCanceledOnTouchOutside(true);
        alert11.setOnCancelListener(
                new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        Intent intent=new Intent(readArticle.this,MainActivity.class);
                        startActivity(intent);
                    }
                }
        );
        alert11.show();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_comm) {
            Intent intent=new Intent(this,commentaire.class);
            intent.putExtra("link", data+"#commentaires");
            intent.putExtra("originalLink", data);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
