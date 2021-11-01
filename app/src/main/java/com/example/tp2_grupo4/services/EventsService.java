package com.example.tp2_grupo4.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.tp2_grupo4.HttpClient.HttpCliente_POST;
import com.example.tp2_grupo4.ui.login.LoginActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class EventsService extends IntentService
{
    private Exception mException=null;

    private String eventsUri = "http://so-unlam.net.ar/api/api/event";
    private String refreshTokenUri = "http://so-unlam.net.ar/api/api/refresh";

    public EventsService() {
        super("EventsService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String email = intent.getExtras().getString("email");
        String type = intent.getExtras().getString("type");
        String description = intent.getExtras().getString("description");

        Intent i = new Intent(EventsService.this, HttpCliente_POST.class);

        JSONObject objEvent = new JSONObject();
        try {
            objEvent.put("type_events", type);
            objEvent.put("description", description);
            //TODO: Cambiar por variables de entorno
            objEvent.put("env", "TEST");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        i.putExtra("uri", eventsUri);
        i.putExtra("jsonData", objEvent.toString());
        i.putExtra("receiver", "RESPUESTA_EVENTO");
        i.putExtra("token", obtenerToken(email));

        startService(i);
    }

    protected String obtenerToken(String email)
    {
        return "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjE2MzU3MjQ0NDMsInR5cGUiOiJpbmljaWFsIiwidXNlciI6eyJlbWFpbCI6Im1hcnRpbi5wZmVAZ21haWwuY29tIiwiZG5pIjoiMzkxNjY2NjgiLCJncm91cCI6NH19.MWjQjJCdZ-M20-RqJZXvFLCW5BP26Gj0F0BSq7bx0RE";
    }

    private String PUT (String uri, String jsonData, String refreshToken)
    {
        HttpURLConnection urlConnection = null;
        try
        {
            //Se alamacena la URI del request del servicio web
            URL mUrl = new URL(uri);

            urlConnection = (HttpURLConnection) mUrl.openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.setRequestMethod("PUT");
            urlConnection.setRequestProperty("Content-Type", "application/json");

            urlConnection.setRequestProperty("Authorization", "Bearer " + refreshToken);

            DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream ());

            JSONObject obj = new JSONObject();

            wr.writeBytes(jsonData);

            wr.flush();
            wr.close();

            urlConnection.connect();

            int responseCode = urlConnection.getResponseCode();

            String result = "";

            if ( responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED)
            {
                InputStreamReader inputStreamReader = new InputStreamReader((urlConnection.getInputStream()));
                result = convertInputStreamToString(inputStreamReader);
            }
            else if( responseCode == HttpURLConnection.HTTP_BAD_REQUEST)
            {
                InputStreamReader inputStreamReader = new InputStreamReader((urlConnection.getErrorStream()));
                result = convertInputStreamToString(inputStreamReader);
            }
            else
            {
                result = "NO_OK";
            }

            urlConnection.disconnect();

            return result;

        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public String convertInputStreamToString(InputStreamReader inputStreamReader) {
        String resultString = "";
        try {
            BufferedReader r = new BufferedReader(inputStreamReader);
            StringBuilder total = new StringBuilder();
            for (String line; (line = r.readLine()) != null; ) {
                total.append(line).append('\n');
            }

            resultString = total.toString();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        return resultString;
    }

}
