package com.example.tp2_grupo4;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tp2_grupo4.HttpClient.HttpClient_GET;
import com.example.tp2_grupo4.data.DbRepository;
import com.example.tp2_grupo4.data.model.Country;
import com.example.tp2_grupo4.data.model.User;
import com.example.tp2_grupo4.services.EventsService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.ThreadLocalRandom;

public class MainActivity extends AppCompatActivity {

    private String covid19Uri = "https://api.covid19api.com/";
    public IntentFilter filtroPaises;
    public IntentFilter filtroCasos;
    public IntentFilter filtroBateria;
    private ReceptorOperacionTraerPais receiverPaises = new ReceptorOperacionTraerPais();
    private ReceptorOperacionTraerCasos receiverCasos = new ReceptorOperacionTraerCasos();
    private ReceptorBateria receiverBateria = new ReceptorBateria();

    User loggedInUser;
    DbRepository db;


    public TextView displayCountryInfoText;
    public TextView covidCasesTextView;
    public TextView batteryLvlTextView;

    public Country currentCountry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

         db = new DbRepository(this);
         loggedInUser = db.getLoggedUser();


        final Button getCountryButton = findViewById(R.id.getCountryButton);
        displayCountryInfoText = findViewById(R.id.displayCountryInfo);
        covidCasesTextView = findViewById(R.id.covidCasesTextView);
        batteryLvlTextView = findViewById(R.id.batteryLvlTextView);

        configurarBroadcastReceiver();

        //registerEvent("Login", "Un usuario inicio sesion");

        getCountryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getRandomCountry();
            }
        });
    }

    protected void getRandomCountry(){
        String countriesUri = covid19Uri + "countries";

        Intent i = new Intent(MainActivity.this, HttpClient_GET.class);

        i.putExtra("uri", countriesUri);
        i.putExtra("receiver", "RESPUESTA_PAISES");

        startService(i);
    }

    protected void getCountryCovidStats(){
        DateFormat df = new DateFormat();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -2);
        String yesterdayDate = dateFormat.format(cal.getTime());

        String from = yesterdayDate + "T00:00:00Z";
        String to = yesterdayDate + "T00:00:01Z";

        String covidCasesUri = covid19Uri +"country/"  + currentCountry.getSlug() +"?from="+ from +"&to=" + to ;

        Intent i = new Intent(MainActivity.this, HttpClient_GET.class);

        i.putExtra("uri", covidCasesUri);
        i.putExtra("receiver", "RESPUESTA_CASOS");

        startService(i);
    }

    protected void registerEvent(String type, String description){
        Intent i = new Intent(MainActivity.this, EventsService.class);

        i.putExtra("email", getLoggedUserEmail());
        i.putExtra("type", type);
        i.putExtra("description", description);

        startService(i);
    }

    protected String getLoggedUserEmail(){
        //TODO: sacarlo cuando tengamos el login activo
//        db.insertUser("noidz@gmail.com", "refreshToken","accessToken");

        return loggedInUser.email != null ? loggedInUser.email : "martin.pfe@gmail.com";
    }

    private void configurarBroadcastReceiver()
    {
        filtroPaises = new IntentFilter("com.example.intentservice.intent.action.RESPUESTA_PAISES");
        filtroPaises.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(receiverPaises, filtroPaises);

        filtroCasos = new IntentFilter("com.example.intentservice.intent.action.RESPUESTA_CASOS");
        filtroCasos.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(receiverCasos, filtroCasos);

        filtroBateria = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        filtroBateria.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(receiverBateria, filtroBateria);
    }

    public class ReceptorBateria extends BroadcastReceiver
    {
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            float battery = level * 100 / (float)scale;
            batteryLvlTextView.setText(String.valueOf(battery) + "%");
        }
    }

    public class ReceptorOperacionTraerPais extends BroadcastReceiver
    {
        public void onReceive(Context context, Intent intent) {
            try {
                String datosJsonString = intent.getStringExtra("datosJson");

                if(datosJsonString != "NO_OK")
                {
                    JSONArray countriesArray = new JSONArray(datosJsonString);

                    countriesArray.length();

                    int randomNum = ThreadLocalRandom.current().nextInt(0, countriesArray.length());

                    JSONObject countryJson = countriesArray.getJSONObject(randomNum);

                    String countryName = countryJson.getString("Country");
                    String countrySlug = countryJson.getString("Slug");
                    int countryInfectedQty = Integer.parseInt(countryJson.getString("Active"));

                    currentCountry = new Country(countryName, countrySlug, countryInfectedQty);

                    displayCountryInfoText.setText(currentCountry.getName());

                    getCountryCovidStats();

                }
                else
                {
                    Toast.makeText(context, "Ocurrió un error llamando a la api del covid", Toast.LENGTH_SHORT).show();
                }
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public class ReceptorOperacionTraerCasos extends BroadcastReceiver
    {
        public void onReceive(Context context, Intent intent) {
            try {
                String datosJsonString = intent.getStringExtra("datosJson");

                if(datosJsonString != "NO_OK")
                {
                    JSONArray countriesArray = new JSONArray(datosJsonString);
                    JSONObject countryJson = countriesArray.getJSONObject(0);

                    String activeCases = countryJson.getString("Active");

                    db.insertCountryInfection(loggedInUser.userId, currentCountry.getName(), Integer.parseInt(activeCases));

                    covidCasesTextView.setText(activeCases);
                }
                else
                {
                    Toast.makeText(context, "Ocurrió un error llamando a la api del covid", Toast.LENGTH_SHORT).show();
                }
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}