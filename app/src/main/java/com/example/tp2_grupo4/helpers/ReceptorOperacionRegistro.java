package com.example.tp2_grupo4.helpers;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.tp2_grupo4.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class ReceptorOperacionRegistro extends BroadcastReceiver
{
    public void onReceive(Context context, Intent intent) {
        try {
            String datosJsonString = intent.getStringExtra("datosJson");

            if(datosJsonString != "NO_OK")
            {
                JSONObject datosJson = new JSONObject(datosJsonString);
                Boolean success = datosJson.getBoolean("success");

                if(success)
                {
                    String token = datosJson.getString("token");
                    /// //TODO: Guardar el token para futuro usao
                    Toast.makeText(context, "Sesión iniciada correctamente", Toast.LENGTH_SHORT).show();

                    Intent mainActivityIntent = new Intent(context, MainActivity.class);
                    context.startActivity(mainActivityIntent);
                }
                else
                {
                    String mensaje = datosJson.getString("msg");
                    Toast.makeText(context, "Ocurrió un error registrando al usuario: " + mensaje, Toast.LENGTH_SHORT).show();
                }
            }
            else
            {
                Toast.makeText(context, "Ocurrió un error registrando al usuario", Toast.LENGTH_SHORT).show();
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }
}