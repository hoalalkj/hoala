package com.yeelight.testnewux;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

public class TemperatureActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temperature);
        String TemperatureString;
        float temp_k;
        double temp_f;
        org.jsoup.nodes.Document document = Jsoup.parse("http://api.openweathermap.org/data/2.5/weather?q=Honolulu,usa&APPID=cbf0ac4717c33ead47cd80623c897eb3");
        try{
            JSONObject jsonObj = new JSONObject (document.select("td.line-content").first().text());
            JSONObject main1 = (JSONObject) jsonObj.get("main");
            temp_k = (float) main1.get("temp");
            temp_f = (temp_k - 273.15) * 1.8 + 32;
            TemperatureString = Double.toString(temp_f);
            ((TextView) findViewById(R.id.textView)).setText(TemperatureString);
        }
        catch(JSONException e){
            System.out.println("JSONException Error");
        }
    }
}
