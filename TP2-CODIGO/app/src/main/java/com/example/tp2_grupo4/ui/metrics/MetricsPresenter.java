package com.example.tp2_grupo4.ui.metrics;

import android.content.Intent;

import com.example.tp2_grupo4.data.DbRepository;
import com.example.tp2_grupo4.services.EventsService;

import java.util.List;

public class MetricsPresenter implements Metrics.Presenter{
    private MetricsActivity activity;
    private DbRepository db;

    public MetricsPresenter(MetricsActivity activity){
        this.activity = activity;
        db = new DbRepository(this.activity);

        registerEvent("Pantallas", "Un usuario entro a la activity de métricas");
    }

    @Override
    public List<String> getCountriesMoreVisitedReport(){
        List<String> arrayResult = db.getCountryMoreVisited();
        registerEvent("Reportes", "Se pidió el reporte de paises mas vistos.");
        return arrayResult;
    }

    @Override
    public List<String> getCountriesLessInfectedReport(){
        List<String> arrayResult = db.getCountriesLessInfected();

        registerEvent("Reportes", "Se pidió el reporte de paises con menos infectados.");
        return arrayResult;
    }

    protected void registerEvent(String type, String description){
        Intent i = new Intent(activity, EventsService.class);

        i.putExtra("type", type);
        i.putExtra("description", description);

        activity.startService(i);
    }
}
