package com.example.tp2_grupo4;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.tp2_grupo4.HttpClient.HttpCliente_POST;
import com.example.tp2_grupo4.services.EventsService;
import com.example.tp2_grupo4.ui.login.LoginActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        registerEvent("Login", "Un usuario inicio sesion");
    }

    protected void registerEvent(String type, String description){
        Intent i = new Intent(MainActivity.this, EventsService.class);

        i.putExtra("email", getLoggedUserEmail());
        i.putExtra("type", type);
        i.putExtra("description", description);

        startService(i);
    }

    protected String getLoggedUserEmail(){
        //TODO: Obtenerlo desde la base
        return "martin.pfe@gmail.com";
    }
}