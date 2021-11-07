package com.example.tp2_grupo4;

public class Main {
    interface View {
        void IrAMetricas();
        void getRandomCountry();
        void getCountryCovidStats();
        void registerEvent(String type, String description);
        void configurarBroadcastReceiver();
    }

    interface Presenter {
        void initialize();
    }
}
