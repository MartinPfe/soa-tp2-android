package com.example.tp2_grupo4.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.tp2_grupo4.HttpClient.HttpCliente_POST;
import com.example.tp2_grupo4.R;
import com.example.tp2_grupo4.data.DbRepository;
import com.example.tp2_grupo4.data.model.User;
import com.example.tp2_grupo4.helpers.InternetHelper;
import com.example.tp2_grupo4.ui.login.LoginActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class EventsService extends IntentService
{
    private Exception mException=null;

    public EventsService() {
        super("EventsService");
    }
    private DbRepository db = new DbRepository(this);

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String type = intent.getExtras().getString("type");
        String description = intent.getExtras().getString("description");

        //Como este proceso se realiza en background, no mostramos un mensaje a proposito
        if(!InternetHelper.isOnline())
        {
            return;
        }

        HttpCliente_POST client = new HttpCliente_POST();

        JSONObject objEvent = new JSONObject();
        try {
            objEvent.put("type_events", type);
            objEvent.put("description", description);
            objEvent.put("env", getString(R.string.env));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        client.ejecutarPost(getString(R.string.eventsUri), objEvent.toString(), "", obtenerToken());
    }

    protected String obtenerToken()
    {
        User user = db.getLoggedUser();
        String userRefreshToken = user.refreshToken;
        String token = user.accessToken;

        Date date = new Date();
        Boolean tokenVencido = false;

        //Lo hacemos con un minuto menos por si justo llegamos a los 15 mientras procemos lo que queda
          if(TimeUnit.MILLISECONDS.toMinutes(Math.abs(date.getTime() - user.lastRefresh)) > 14){
            tokenVencido = true;
        }

        if(tokenVencido) {

            String response = PUT(getString(R.string.refreshTokenUri), "", userRefreshToken);

            if (response != "NO_OK") {
                try {
                    JSONObject responseJson = new JSONObject(response);

                    token = responseJson.getString("token");
                    String refreshToken = responseJson.getString("token_refresh");

                    db.updateLoggedUser(user.email,refreshToken,token);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        return token;
    }

    //Esto correspondería tenerlo en un httpClient distintos. Para hacerlo mas rápido lo dejo acá por el momento
    private String PUT (String uri, String jsonData, String refreshToken)
    {
        HttpURLConnection urlConnection = null;
        try
        {
            URL mUrl = new URL(uri);

            urlConnection = (HttpURLConnection) mUrl.openConnection();
            urlConnection.setRequestMethod("PUT");
            urlConnection.setRequestProperty("Content-Type", "application/json");

            urlConnection.setRequestProperty("Authorization", "Bearer " + refreshToken);

            DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream ());

            wr.writeBytes(jsonData);

            wr.flush();
            wr.close();

            urlConnection.connect();

            int responseCode = urlConnection.getResponseCode();

            String result = "";

            if ( responseCode == HttpURLConnection.HTTP_OK)
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

        }
        catch (Exception e)
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
