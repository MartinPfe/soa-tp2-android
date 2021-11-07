package com.example.tp2_grupo4;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.tp2_grupo4.HttpClient.HttpClient_GET;
import com.example.tp2_grupo4.services.EventsService;
import com.example.tp2_grupo4.ui.metrics.MetricsActivity;

public class MainActivity extends AppCompatActivity implements Main.View {

    private MainPresenter presenter;

    public TextView displayCountryInfoText;
    public TextView covidCasesTextView;
    public TextView batteryLvlTextView;

    @Override
    protected void onStop(){
        super.onStop();
        presenter.unregisterSensor();
    }

    @Override
    protected void onStart(){
        super.onStart();
        presenter.registerSensor();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        presenter = new MainPresenter(this);

        final Button getCountryButton = findViewById(R.id.getCountryButton);
        displayCountryInfoText = findViewById(R.id.displayCountryInfo);
        covidCasesTextView = findViewById(R.id.covidCasesTextView);
        batteryLvlTextView = findViewById(R.id.batteryLvlTextView);

        final Button btnMetrics = findViewById(R.id.btnMetrics);

        configurarBroadcastReceiver();
        registerEvent("Login", "Un usuario inicio sesion");

        getCountryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getRandomCountry();
            }
        });

        btnMetrics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IrAMetricas();
            }
        });
    }

    @Override
    public void IrAMetricas() {
        Intent intent = new Intent(getApplicationContext(), MetricsActivity.class);
        startActivity(intent);
    }

    @Override
    public void getRandomCountry(){
        String countriesUri = presenter.getRandomCountriesUri();

        Intent i = new Intent(MainActivity.this, HttpClient_GET.class);

        i.putExtra("uri", countriesUri);
        i.putExtra("receiver", "RESPUESTA_PAISES");

        startService(i);
    }

    @Override
    public void getCountryCovidStats(){
        String covidCasesUri = presenter.getCountryCovidStats();

        Intent i = new Intent(MainActivity.this, HttpClient_GET.class);

        i.putExtra("uri", covidCasesUri);
        i.putExtra("receiver", "RESPUESTA_CASOS");

        startService(i);
    }

    @Override
    public void registerEvent(String type, String description){
        Intent i = new Intent(MainActivity.this, EventsService.class);

        i.putExtra("email", presenter.getLoggedUserEmail());
        i.putExtra("type", type);
        i.putExtra("description", description);

        startService(i);
    }

    @Override
    public void configurarBroadcastReceiver()
    {
        IntentFilter filtroPaises = new IntentFilter("com.example.intentservice.intent.action.RESPUESTA_PAISES");
        filtroPaises.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(presenter.getReceiverPaises(), filtroPaises);

        IntentFilter filtroCasos = new IntentFilter("com.example.intentservice.intent.action.RESPUESTA_CASOS");
        filtroCasos.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(presenter.getReceiverCasos(), filtroCasos);

        IntentFilter filtroBateria = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        filtroBateria.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(presenter.getReceiverBateria(), filtroBateria);
    }

}