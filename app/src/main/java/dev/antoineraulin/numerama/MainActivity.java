package dev.antoineraulin.numerama;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.net.Uri;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.apache.commons.lang3.StringEscapeUtils;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeContainer;

    private List<MyObject> articles = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.content_main);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getFeed();
            }
        });
        swipeContainer.setColorSchemeResources(R.color.colorPrimary,
                R.color.colorPrimaryDark);
        toolbar.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Context context = v.getContext();
                recyclerView.setLayoutManager(new LinearLayoutManagerWithSmoothScroller(context));
                recyclerView.smoothScrollToPosition(0);
                Snackbar snackbar = Snackbar
                        .make(v, "Revenu en haut", Snackbar.LENGTH_LONG);

                snackbar.show();
            }
        });
        swipeContainer.setRefreshing(true);
        getFeed();
        // ATTENTION: This was auto-generated to handle app links.
        Intent appLinkIntent = getIntent();
        String appLinkAction = appLinkIntent.getAction();
        Uri appLinkData = appLinkIntent.getData();
        handleIntent(getIntent());


    }
    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////:PRIVATE FUNCTION:///////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        String appLinkAction = intent.getAction();
        Uri appLinkData = intent.getData();
        Log.d("URI intent", String.valueOf(appLinkData));
        try{
        if(String.valueOf(appLinkData).contains("numerama.com")){
            Context context = getApplicationContext();
            Intent theIntent=new Intent(getApplicationContext(),ArticleActivity.class);
            theIntent.putExtra("link", appLinkData.toString());
            theIntent.putExtra("isIntent", true);
            context.startActivity(intent);
        }}catch (Error e){
            Log.e("HandleIntent", String.valueOf(e));
        }
    }

    private void getFeed(){
        articles.clear();
        Ion.with(getApplicationContext())
                .load("http://www.numerama.com/feed/")
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        Log.d("html", result);
                        try {
                            Log.e("info", "started");
                            Pattern patternitem = Pattern.compile("<item>([\\s\\S]*?)<\\/item>");
                            Matcher matcheritem = patternitem.matcher(result);
                            final List<String> itemMatches = new ArrayList<>();
                            while (matcheritem.find()) {
                                itemMatches.add(matcheritem.group(1));
                            }
                            for (int i = 0;i<itemMatches.size();i++){
                                String itemContent = itemMatches.get(i);
                                String title = Html.fromHtml(getStringBetween(itemContent, "<title>", "</title>")).toString();
                                Log.d("title", title);
                                String link = getStringBetween(itemContent, "<link>", "</link>");
                                Log.d("link", link);
                                String pubDate = getStringBetween(itemContent, "<pubDate>","</pubDate>");
                                Log.d("publication date", pubDate);
                                String author = getStringBetween(itemContent, "<dc:creator><![CDATA[","]]></dc:creator>");
                                String descriptionTag = getStringBetween(itemContent, "<description>", "</description>");
                                String imageUrl = getStringBetween(descriptionTag, "src=\"", "\" class");
                                Log.d("imageUrl", imageUrl);
                                String description = Html.fromHtml(getStringBetween(descriptionTag, "/></p>", "<a hre")).toString();
                                Log.d("description", description);
                                Pattern patterncat = Pattern.compile("<category>(.*?)<\\/category>");
                                Matcher matchercat = patterncat.matcher(itemContent);
                                final List<String> catMatches = new ArrayList<>();
                                while (matchercat.find()) {
                                    catMatches.add(matchercat.group(1));
                                }
                                String cat = getStringBetween(catMatches.get(0),"<![CDATA[", "]]>");





                                articles.add(new MyObject(title,imageUrl,link, getTimeElapsed(pubDate)[0], getTimeElapsed(pubDate)[1]+" par "+author, description, cat));
                            }
                            recycle();
                            swipeContainer.setRefreshing(false);

                        }catch (Exception error){
                            Log.e("error", String.valueOf(error));
                        }
                    }
                });
    }

    private void recycle(){
        Log.d("info", "start recycling");
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        recyclerView.setAdapter(new MyAdapter(articles));
    }
    private String[] getTimeElapsed(String pubdate){
        Date startDate = new Date(pubdate);
        Date date2 = new Date();
        long difference = date2.getTime() - startDate.getTime();
        difference = difference / 1000;
        difference = difference / 60;
        double resultat;
        String strResutlat;
        double hour;
        double day;
        String unite = "minutes";
        double x = difference - Math.floor(difference);
        if (x >= 0.5){
            resultat = Math.floor(difference);
            resultat++;
            strResutlat = String.valueOf(resultat);
            strResutlat = strResutlat.replace(".0", "");
        }else{
            resultat = Math.floor(difference);
            strResutlat = String.valueOf(resultat);
            strResutlat = strResutlat.replace(".0", "");
        }
        if(resultat <= 1) unite = "minute";
        if(resultat >= 60){
            unite = "heures";
            hour = resultat / 60;
            double y = hour - Math.floor(hour);
            if (y >= 0.5){
                hour = Math.floor(hour);
                hour++;
                strResutlat = String.valueOf(hour);
                strResutlat = strResutlat.replace(".0", "");
            }else{
                hour = Math.floor(hour);
                strResutlat = String.valueOf(hour);
                strResutlat = strResutlat.replace(".0", "");
            }
            if(hour <= 1) unite = "heure";

        }
        if(resultat >= 60){
            unite = "heures";
            hour = resultat / 60;
            double y = hour - Math.floor(hour);
            if (y >= 0.5){
                hour = Math.floor(hour);
                hour++;
                strResutlat = String.valueOf(hour);
                strResutlat = strResutlat.replace(".0", "");
            }else{
                hour = Math.floor(hour);
                strResutlat = String.valueOf(hour);
                strResutlat = strResutlat.replace(".0", "");
            }
            if(hour <= 1) unite = "heure";

        }
        if(resultat >= 1440){
            unite = "jours";
            day = resultat / 1440;
            double z = day - Math.floor(day);
            if (z >= 0.5){
                day = Math.floor(day);
                day++;
                strResutlat = String.valueOf(day);
                strResutlat = strResutlat.replace(".0", "");
            }else{
                day = Math.floor(day);
                strResutlat = String.valueOf(day);
                strResutlat = strResutlat.replace(".0", "");
            }
            if(day <= 1) unite = "jour";

        }
        return new String[]{strResutlat, unite};
    }

    private boolean appInstalledOrNot(String uri) {
        PackageManager pm = getPackageManager();
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
        }

        return false;
    }
    public String getStringBetween(String s, String first, String second){
        return s.substring(s.indexOf(first) + first.length(), s.indexOf(second));
    }
    /////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////:PUBLIC CLASS:////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////

    public class LinearLayoutManagerWithSmoothScroller extends LinearLayoutManager {

        public LinearLayoutManagerWithSmoothScroller(Context context) {
            super(context, VERTICAL, false);
        }

        public LinearLayoutManagerWithSmoothScroller(Context context, int orientation, boolean reverseLayout) {
            super(context, orientation, reverseLayout);
        }

        @Override
        public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state,
                                           int position) {
            RecyclerView.SmoothScroller smoothScroller = new TopSnappedSmoothScroller(recyclerView.getContext());
            smoothScroller.setTargetPosition(position);
            startSmoothScroll(smoothScroller);
        }

        private class TopSnappedSmoothScroller extends LinearSmoothScroller {
            public TopSnappedSmoothScroller(Context context) {
                super(context);

            }

            @Override
            public PointF computeScrollVectorForPosition(int targetPosition) {
                return LinearLayoutManagerWithSmoothScroller.this
                        .computeScrollVectorForPosition(targetPosition);
            }

            @Override
            protected int getVerticalSnapPreference() {
                return SNAP_TO_START;
            }
        }
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////:OVERRIDE FUNCTION://////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.frandroid) {
            boolean isAppInstalled = appInstalledOrNot("com.frandroid.app");
            if(isAppInstalled) {
                Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.frandroid.app");
                if (launchIntent != null) {
                    startActivity(launchIntent);
                }
            }else{
                Uri uri = Uri.parse("http://frandroid.com");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
            // Handle the camera action
        } else if (id == R.id.humanoid) {
            Uri uri = Uri.parse("http://humanoid.fr");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
        else if(id == R.id.youtube){
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/channel/UCAz-755tH3m8_BwaluzdJwQ")));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }



}
