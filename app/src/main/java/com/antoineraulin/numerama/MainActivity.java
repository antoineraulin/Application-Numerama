package com.antoineraulin.numerama;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import static android.R.attr.category;
import static android.R.attr.name;
import static android.support.v7.widget.LinearSmoothScroller.SNAP_TO_START;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeContainer;


    private List<MyObject> cities = new ArrayList<>();

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
                new RetrieveFeedTask().execute("http://www.numerama.com/feed/");
            }
        });
        swipeContainer.setColorSchemeResources(R.color.colorPrimary,
                R.color.colorPrimaryDark );

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
        new RetrieveFeedTask().execute("http://www.numerama.com/feed/");
        recycle();

    }

    private void recycle(){
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        //définit l'agencement des cellules, ici de façon verticale, comme une ListView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //pour adapter en grille comme une RecyclerView, avec 2 cellules par ligne
        //recyclerView.setLayoutManager(new GridLayoutManager(this,2));

        //puis créer un MyAdapter, lui fournir notre liste de villes.
        //cet adapter servira à remplir notre recyclerview
        recyclerView.setAdapter(new MyAdapter(cities));
    }

    /*private void ajouterVilles() {
        cities.add(new MyObject("Google a examiné les signalements anti-piratage pour un million de sites web","http://www.numerama.com/content/uploads/2016/09/google-anniv-1024x767.jpg"));
        cities.add(new MyObject("L'aéroport Paris Charles-de-Gaulle teste un nouveau système de reconnaissance faciale","http://www.numerama.com/content/uploads/2016/12/aeroport-1024x683.jpg"));
        cities.add(new MyObject("Comment CD Projekt (The Witcher, GOG.com) est devenu un géant","http://www.numerama.com/content/uploads/2015/11/The-Witcher-1024x576.jpg"));
        cities.add(new MyObject("Mars : la Nasa a repéré 3 sites où chercher des traces de vie","http://www.numerama.com/content/uploads/2016/09/eso1509a_-_mars_planet-1024x629.jpg"));
        cities.add(new MyObject("Italie","http://retouralinnocence.com/wp-content/uploads/2013/05/Hotel-en-Italie-pour-les-Vacances2.jpg"));
        cities.add(new MyObject("Russie","http://www.choisir-ma-destination.com/uploads/_large_russie-moscou2.jpg"));
    }*/


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @SuppressWarnings("StatementWithEmptyBody")
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
    class RetrieveFeedTask extends AsyncTask<String, Void, String> {

        private Exception exception;

        protected String doInBackground(String... urls) {
            try {
                cities.clear();

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
                DocumentBuilderFactory dbf =
                        DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                InputSource is2 = new InputSource();
                is2.setCharacterStream(new StringReader(resString));

                Document doc = db.parse(is2);
                NodeList nodes = doc.getElementsByTagName("item");

                // iterate the employees
                for (int i = 0; i < nodes.getLength(); i++) {
                    Element element = (Element) nodes.item(i);

                    NodeList name = element.getElementsByTagName("title");
                    Element line2 = (Element) name.item(0);
                    String title = getCharacterDataFromElement(line2);
                    NodeList nodelink = element.getElementsByTagName("link");
                    Element line6 = (Element) nodelink.item(0);
                    String link = getCharacterDataFromElement(line6);
                    NodeList nodedate = element.getElementsByTagName("pubDate");
                    Element line8 = (Element) nodedate.item(0);
                    String dateDif = getCharacterDataFromElement(line8);
                    NodeList description = element.getElementsByTagName("description");
                    Element line3 = (Element) description.item(0);
                    String descriptionContent = getCharacterDataFromElement(line3);
                    Pattern pattern = Pattern.compile("src=\"(.*?)\" class=");
                    Matcher matcher = pattern.matcher(descriptionContent);
                    while (matcher.find()) {
                        String imageUrl = matcher.group(1);
                        Date date1 = new Date(dateDif);
                        Date date2 = new Date();
                        long difference = date2.getTime() - date1.getTime();
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
                        cities.add(new MyObject(title,imageUrl,link, strResutlat, unite));
                    }
                }

                is.close();
            } catch (Exception e) {
                System.out.println("error = " + e);
                return null;
            }
            return null;
        }

        protected void onPostExecute(String feed) {
            recycle();
            try {
                swipeContainer.setRefreshing(false);
            }catch(Exception e){
                System.out.println("error during refresh");
            }
        }
    }
    public static String getCharacterDataFromElement(Element e) {
        Node child = e.getFirstChild();
        if (child instanceof CharacterData) {
            CharacterData cd = (CharacterData) child;
            return cd.getData();
        }
        return "?";
    }
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

    private boolean appInstalledOrNot(String uri) {
        PackageManager pm = getPackageManager();
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
        }

        return false;
    }

}
