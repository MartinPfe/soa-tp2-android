package com.example.tp2_grupo4.ui.metrics;

import com.example.tp2_grupo4.data.DbRepository;

import java.util.List;

public class MetricsPresenter implements Metrics.Presenter{
    private MetricsActivity activity;
    private DbRepository db;

    public MetricsPresenter(MetricsActivity activity){
        this.activity = activity;
        db = new DbRepository(this.activity);
    }

    @Override
    public List<String> getCountriesMoreVisitedReport(){
        List<String> arrayResult = db.getCountryMoreVisited();
        return arrayResult;
    }

    @Override
    public List<String> getCountriesLessInfectedReport(){
        List<String> arrayResult = db.getCountriesLessInfected();
        return arrayResult;
    }
}
