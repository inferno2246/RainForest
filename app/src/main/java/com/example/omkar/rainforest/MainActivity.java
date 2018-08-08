package com.example.omkar.rainforest;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String> {
    private static final int LOADER_NUMBER = 22;


    static TextView weatherInfo;
    TextView x_view;
    TextView y_view;
    EditText x_edit;
    EditText y_edit;
    Button submit_button;
    String cord_x;
    String cord_y;
    String f_coord;
    String textEntered=null,lat=null,longi=null;
    String result = null;

    int session = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        weatherInfo = findViewById(R.id.weather_info);
        x_view = findViewById(R.id.X_View);
        y_view = findViewById(R.id.Y_view);
        x_edit = findViewById(R.id.x_edittext);
        y_edit = findViewById(R.id.y_edittext);
        submit_button = findViewById(R.id.submit_button);

        getSupportLoaderManager().initLoader(LOADER_NUMBER, null, this);


        submit_button.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        weatherInfo.setText("");
                        cord_x=x_edit.getText().toString();
                        cord_y=y_edit.getText().toString();
                        f_coord=cord_x+","+cord_y;
                        CallLoader();
                    }
                }
        );

        Intent intentThatStartedThisActivity = getIntent();
        if (intentThatStartedThisActivity.hasExtra(Intent.EXTRA_TEXT)) {
            session++;
            textEntered = intentThatStartedThisActivity.getStringExtra(Intent.EXTRA_TEXT);
            lat = textEntered.substring(textEntered.indexOf('(')+1,textEntered.indexOf(','));
            longi = textEntered.substring(textEntered.indexOf(',')+1,textEntered.indexOf(')'));
            x_edit.setText(lat);
            y_edit.setText(longi);

        }




    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemClicked = item.getItemId();
        if(itemClicked == R.id.map_navigator){
            Context context = MainActivity.this;
            Class mapActivity = MapActivity.class;
            Intent intent = new Intent(context,mapActivity);
            startActivity(intent);

            if(session>0){
                MainActivity.this.finish();
            }
            return true;
        }
        return true;

    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public Loader<String> onCreateLoader(int i, final Bundle bundle) {


        return new AsyncTaskLoader<String>(this) {

            @Override
            protected void onStartLoading() {
                if(bundle==null){
                    return;
                }
                forceLoad();
            }

            @Override
            public String loadInBackground() {
                String GET_COORD = bundle.getString("SEARCH_QUERY");

                if(GET_COORD == null){
                    return null;
                }

                try {
                    Document doc = Jsoup.connect("https://weather.com/en-IN/weather/today/l/"+GET_COORD).get();
                    for (Element row : doc.select("header[class=loc-container]")) {
                        result=GET_COORD+" \nLocation: ";
                        result = result+row.text()+" ";

                    }
                    for (Element row : doc.select("div[class=today_nowcard-section today_nowcard-condition]")) {
                        result = result +"\n\nWeather Data:\n"+ (row.text());

                    }

                    for (Element row : doc.select("div[class=today_nowcard-sidecar component panel]")) {
                        result = result +"\n\n"+ (row.text());

                    }

                    return result;

                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }

            }
        };
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String s) {
        if(s==null){
            weatherInfo.append("NO DATA FOUND CHECK YOUR INTERNET CONNECTION OR THE COORDINATES");
        }else{
            weatherInfo.append(s);
        }
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {

    }

    public void CallLoader(){
        Bundle queryBundle = new Bundle();
        queryBundle.putString("SEARCH_QUERY",f_coord);

        LoaderManager loaderManager = getSupportLoaderManager();

        Loader<String> searchLoader = loaderManager.getLoader(LOADER_NUMBER);


        if(searchLoader==null){
            loaderManager.initLoader(LOADER_NUMBER,queryBundle, this);

        }else{
            loaderManager.restartLoader(LOADER_NUMBER,queryBundle, this);

        }
    }

}