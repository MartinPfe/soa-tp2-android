package com.example.tp2_grupo4;

import static android.content.Context.SENSOR_SERVICE;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.BatteryManager;
import android.util.Log;
import android.widget.Toast;

import com.example.tp2_grupo4.data.DbRepository;
import com.example.tp2_grupo4.data.model.Country;
import com.example.tp2_grupo4.data.model.User;
import com.example.tp2_grupo4.services.EventsService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.ThreadLocalRandom;

public class MainPresenter implements Main.Presenter, SensorEventListener{

    private MainActivity activity;

    private SensorManager sensor;
    private final static float ACC = 15;
    private final static float LIGHT = 20000;

    private boolean IsGetRandomCountryRunning = false;

    private String covid19Uri = "https://api.covid19api.com/";

    User loggedInUser;
    DbRepository db;

    public Country currentCountry;

    private ReceptorOperacionTraerPais receiverPaises = new ReceptorOperacionTraerPais();
    private ReceptorOperacionTraerCasos receiverCasos = new ReceptorOperacionTraerCasos();
    private ReceptorBateria receiverBateria = new ReceptorBateria();

    public MainPresenter(MainActivity activity) {
        this.activity = activity;

        initialize();
    }

    public String getCovid19Uri() {
        return covid19Uri;
    }

    public void registerSensor()
    {
        sensor.registerListener(this, sensor.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        sensor.registerListener(this, sensor.getDefaultSensor(Sensor.TYPE_LIGHT), SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void unregisterSensor()
    {
        sensor.unregisterListener(this, sensor.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
        sensor.unregisterListener(this, sensor.getDefaultSensor(Sensor.TYPE_LIGHT));
    }

    protected String getLoggedUserEmail(){

        return loggedInUser.email != null ? loggedInUser.email : "martin.pfe@gmail.com";
    }

    protected String getRandomCountriesUri(){
        IsGetRandomCountryRunning = true;
        String countriesUri = getCovid19Uri() + "countries";

        return countriesUri;
    }

    protected String getCountryCovidStats(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -2);
        String yesterdayDate = dateFormat.format(cal.getTime());

        String from = yesterdayDate + "T00:00:00Z";
        String to = yesterdayDate + "T00:00:01Z";

        String covidCasesUri = covid19Uri +"country/"  + currentCountry.getSlug() +"?from="+ from +"&to=" + to ;
        return covidCasesUri;
    }

    public ReceptorOperacionTraerPais getReceiverPaises() {
        return receiverPaises;
    }

    public ReceptorOperacionTraerCasos getReceiverCasos() {
        return receiverCasos;
    }

    public ReceptorBateria getReceiverBateria() {
        return receiverBateria;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {

    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        int sensorType = event.sensor.getType();

        float[] values = event.values;

        synchronized (this) {
            Log.d("sensor", event.sensor.getName());

            if (sensorType == Sensor.TYPE_ACCELEROMETER) {
                if ((Math.abs(values[0]) > ACC || Math.abs(values[1]) > ACC || Math.abs(values[2]) > ACC)) {
                    if (!IsGetRandomCountryRunning) {
                        activity.getRandomCountry();
                    }
                }
            }

            if (sensorType == Sensor.TYPE_LIGHT) {
                if (values[0] > LIGHT) {
                    activity.IrAMetricas();
                }
            }
        }

    }

    @Override
    public void initialize() {
        db = new DbRepository(this.activity);
        loggedInUser = db.getLoggedUser();
        sensor = (SensorManager) this.activity.getSystemService(SENSOR_SERVICE);

        registerEvent("Pantallas", "Un usuario entro a la activity principal");
    }

    public class ReceptorBateria extends BroadcastReceiver
    {
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            float battery = level * 100 / (float)scale;
            activity.batteryLvlTextView.setText(String.valueOf(battery) + "%");
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

                    currentCountry = new Country(countryName, countrySlug);

                    activity.displayCountryInfoText.setText(currentCountry.getName());

                    activity.getCountryCovidStats();

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
                IsGetRandomCountryRunning = false;

                String datosJsonString = intent.getStringExtra("datosJson");

                if(datosJsonString != "NO_OK")
                {
                    JSONArray countriesArray = new JSONArray(datosJsonString);
                    JSONObject countryJson = countriesArray.getJSONObject(0);

                    String activeCases = countryJson.getString("Active");

                    db.insertCountryInfection(loggedInUser.userId, currentCountry.getName(), Integer.parseInt(activeCases));

                    registerEvent("ConsultaCovid", "Se obtuvo la respuesta de " + currentCountry.getName() + " con " + activeCases + " casos.");

                    activity.covidCasesTextView.setText(activeCases);
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


    protected void registerEvent(String type, String description){
        Intent i = new Intent(activity, EventsService.class);

        i.putExtra("type", type);
        i.putExtra("description", description);

        activity.startService(i);
    }

}
