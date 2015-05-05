package com.bbw.mandrew.basicbweather;

import java.util.*;
import java.util.Locale;
import java.text.DateFormat;
import android.os.Bundle;
import android.os.Handler;
import android.graphics.Typeface;
//import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.Fragment;
import org.json.JSONObject;


public class WeatherFragment extends Fragment {
    Typeface weatherFont;
    ImageView precip;
    ImageView top;
    ImageView bot;
    ImageView shoes;
    TextView cityField;
    TextView updatedField;
    TextView detailsField;
    TextView currentTemperatureField;
    TextView weatherIcon;

    Handler handler;

    public WeatherFragment(){
        handler = new Handler();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_weather, container, false);
        cityField = (TextView)rootView.findViewById(R.id.city_field);
        updatedField = (TextView)rootView.findViewById(R.id.updated_field);
        detailsField = (TextView)rootView.findViewById(R.id.details_field);
        currentTemperatureField = (TextView)rootView.findViewById(R.id.current_temperature_field);
        weatherIcon = (TextView)rootView.findViewById(R.id.weather_icon);
        precip = (ImageView)rootView.findViewById(R.id.precip);
        top = (ImageView)rootView.findViewById(R.id.top);
        bot = (ImageView)rootView.findViewById(R.id.bot);
        shoes = (ImageView)rootView.findViewById(R.id.shoes);
        weatherIcon.setTypeface(weatherFont);
        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        weatherFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/weather.ttf");
        String currentCity = new CityPreference(getActivity()).getCity();
        updateWeatherData(currentCity);
        Log.d("Check city", currentCity);
    }

    private void updateWeatherData(final String city){
        new Thread(){
            public void run(){
                final JSONObject json = RemoteFetch.getJSON(getActivity(), city);
                //Log.d("Check city", json.toString());
                if(json == null){
                    handler.post(new Runnable(){
                        public void run(){
                            Toast.makeText(getActivity(),
                                    getActivity().getString(R.string.place_not_found),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    handler.post(new Runnable(){
                        public void run(){
                            renderWeather(json);
                        }
                    });
                }
            }
        }.start();
    }

    private void renderWeather(JSONObject json){
        try {
            cityField.setText(json.getString("name").toUpperCase(Locale.US) +
                    ", " + json.getJSONObject("sys").getString("country"));

            JSONObject details = json.getJSONArray("weather").getJSONObject(0);
            JSONObject main = json.getJSONObject("main");
            detailsField.setText(
                    details.getString("description").toUpperCase(Locale.US) +
                            "\n" + "Humidity: " + main.getString("humidity") + "%" +
                            "\n" + "Pressure: " + main.getString("pressure") + " hPa");

            currentTemperatureField.setText(
                    String.format("%.2f", main.getDouble("temp"))+ " " + (char) 0x00B0 +"F");

            DateFormat df = DateFormat.getDateTimeInstance();
            String updatedOn = df.format(new Date(json.getLong("dt")*1000));
            updatedField.setText("Last update: " + updatedOn);

            setWeatherIcon(details.getInt("id"),
                    json.getJSONObject("sys").getLong("sunrise") * 1000,
                    json.getJSONObject("sys").getLong("sunset") * 1000);

            setPrecip(details.getInt("id"));
            setTop(main.getDouble("temp"));
            setBot(main.getDouble("temp"));
            setShoes(main.getDouble("temp"),details.getInt("id"));

        }catch(Exception e){
            Log.e("SimpleWeather", "One or more fields not found in the JSON data");
        }
    }

    private void setPrecip(int id)
    {
        if (id <800||id>804)
            precip.setImageResource(R.drawable.umbrella);
    }
    private void setTop(double temp)
    {
        if (temp<50)
            top.setImageResource(R.drawable.jacket);
        else if (temp<70)
            top.setImageResource(R.drawable.flannel);
        else
            top.setImageResource(R.drawable.tank);
    }

    private void setBot(double temp)
    {
        if (temp<50)
            bot.setImageResource(R.drawable.sweats);
        else if (temp<65)
            bot.setImageResource(R.drawable.leggings);
        else
            bot.setImageResource(R.drawable.norts);
    }

    private void setShoes(double temp, int precip)
    {
        if (precip <800||precip>804)
            shoes.setImageResource(R.drawable.boots);
        else if (temp<50)
            shoes.setImageResource(R.drawable.uggs);
        else if (temp<75)
            shoes.setImageResource(R.drawable.tennis);
        else
            shoes.setImageResource(R.drawable.chacos);
    }

    private void setWeatherIcon(int actualId, long sunrise, long sunset){
        int id = actualId / 100;
        String icon = "";
        if(actualId == 800){
            long currentTime = new Date().getTime();
            if(currentTime>=sunrise && currentTime<sunset) {
                icon = getActivity().getString(R.string.weather_sunny);
            } else {
                icon = getActivity().getString(R.string.weather_clear_night);
            }
        } else {
            switch(id) {
                case 2 : icon = getActivity().getString(R.string.weather_thunder);
                    break;
                case 3 : icon = getActivity().getString(R.string.weather_drizzle);
                    break;
                case 7 : icon = getActivity().getString(R.string.weather_foggy);
                    break;
                case 8 : icon = getActivity().getString(R.string.weather_cloudy);
                    break;
                case 6 : icon = getActivity().getString(R.string.weather_snowy);
                    break;
                case 5 : icon = getActivity().getString(R.string.weather_rainy);
                    break;
            }
        }
        weatherIcon.setText(icon);
    }

    public void changeCity(String city)
    {
        updateWeatherData(city);
    }
}