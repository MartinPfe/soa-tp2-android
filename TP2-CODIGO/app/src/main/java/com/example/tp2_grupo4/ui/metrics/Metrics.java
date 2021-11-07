package com.example.tp2_grupo4.ui.metrics;

import java.util.List;

public class Metrics {
    interface View{

    }

    interface Presenter{
        List<String> getCountriesMoreVisitedReport();
        List<String> getCountriesLessInfectedReport();
    }
}
