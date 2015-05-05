package com.bbw.mandrew.basicbweather; /**
 * Created by Maximillian on 3/25/2015.
 */
import android.app.Activity;
import android.content.SharedPreferences;

public class CityPreference {

    SharedPreferences prefs;

    public CityPreference(Activity activity){
        prefs = activity.getPreferences(Activity.MODE_PRIVATE);
    }

    // If the user has not chosen a city yet, return
    // Oxford, MS as the default city
    String getCity(){
        return prefs.getString("city", "38655, us");
    }

    void setCity(String city){
        prefs.edit().putString("city", city).commit();
    }

}