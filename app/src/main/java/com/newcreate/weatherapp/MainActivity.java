package com.newcreate.weatherapp;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity {

    TextView cityTextView,tempTextView,windTextView,conditionTextView,humidityTextView,minTempTextView,maxTempTextView,pressureTextView;
    EditText editText;
    Button button;
    HttpURLConnection urlConnection;
    InputStream in;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu); //your file name
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        final EditText newCity = new EditText(this);

        if(item.getItemId() == R.id.changeCity) {
            new AlertDialog.Builder(this)
                    .setTitle("Change City")
                    .setMessage("Enter City Name !")
                    .setView(newCity)
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton){
                            try
                            {
                                String encodedCityName = URLEncoder.encode(newCity.getText().toString(), "UTF-8");
                                new DownloadTask().execute("http://api.openweathermap.org/data/2.5/weather?q=" + encodedCityName + "&units=metric&{$$API}");
                                Toast.makeText(MainActivity.this, "City Changed", Toast.LENGTH_SHORT).show();
                            }catch (Exception e)  {e.printStackTrace();}
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            Toast.makeText(MainActivity.this, "", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .show();
        }

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cityTextView = findViewById(R.id.cityTextView);
        tempTextView = findViewById(R.id.tempTextView);
        windTextView = findViewById(R.id.windTextView);
        conditionTextView = findViewById(R.id.conditionTextView);
        humidityTextView  = findViewById(R.id.humidityTextView);
        minTempTextView   = findViewById(R.id.minTempTextView);
        maxTempTextView   = findViewById(R.id.maxTempTextView);
        pressureTextView  = findViewById(R.id.pressureTextView);


        DownloadTask downloadTask = new DownloadTask();
        downloadTask.execute("http://api.openweathermap.org/data/2.5/weather?q=jaipur&units=metric&appid{$$API}");

    }


    public class DownloadTask extends AsyncTask<String,Void,String>
    {

        @Override
        protected String doInBackground(String... urls)
        {
            String result = "";
            URL url;
            urlConnection = null;

            try
            {

                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();

                while (data != -1)
                {
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }

                return result;

            }
            catch (Exception e)
            {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Could not find weather :(", Toast.LENGTH_SHORT).show();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s)
        {
            super.onPostExecute(s);

            try
            {
                JSONObject jsonObject = new JSONObject(s);

                JSONObject  main_info = jsonObject.getJSONObject("main");
                JSONObject  wind_info = jsonObject.getJSONObject("wind");
                JSONObject  sys_info = jsonObject.getJSONObject("sys");

                String maxTemp = main_info.getString("temp_max");
                String minTemp = main_info.getString("temp_min");
                String humidity = main_info.getString("humidity");
                String pressure = main_info.getString("pressure");
                String temp = main_info.getString("temp");

                String speed = wind_info.getString("speed");

                String country = sys_info.getString("country");
                String city = jsonObject.getString("name");

                tempTextView.setText(temp+ " C");
                windTextView.setText(speed+" mps");
                humidityTextView.setText(humidity+"%");
                pressureTextView.setText(pressure +" hpa");
                minTempTextView.setText(minTemp +" C");
                maxTempTextView.setText(maxTemp +" C");
                cityTextView.setText(city+", "+country);


                String message = null;
                JSONArray  weather_info = jsonObject.getJSONArray("weather");

                String description = null;
                for (int i=0; i < weather_info.length(); i++) {
                    JSONObject jsonPart = weather_info.getJSONObject(i);

                    String main = jsonPart.getString("main");
                    description = jsonPart.getString("description");

                    message = main;
                }
                conditionTextView.setText(message + " ("+description+")");


            }
            catch (Exception e)
            {
                Toast.makeText(getApplicationContext(), "Could not find weather :(", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

            try
            {
                urlConnection.disconnect();
                in.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}