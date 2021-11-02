package com.example.tp2_grupo4.HttpClient;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static android.webkit.ConsoleMessage.MessageLevel.LOG;

public class HttpCliente_POST extends IntentService
{
    private Exception mException=null;

    public HttpCliente_POST() {
        super("HttpCliente_POST");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        String uri = intent.getExtras().getString("uri");
        String datosJson = intent.getExtras().getString("jsonData");
        String receiver = intent.getExtras().getString("receiver");
        String token = intent.getExtras().getString("token");

        ejecutarPost(uri, datosJson, receiver, token);
    }

    protected void ejecutarPost(String uri, String datosJson, String receiver, String token)
    {
        String result = POST(uri, datosJson, token);

        Intent i = new Intent("com.example.intentservice.intent.action." + receiver);
        i.putExtra("datosJson", result);

        sendBroadcast(i);
    }


    private String POST (String uri, String jsonData, String token)
    {
        HttpURLConnection urlConnection = null;
        try
        {
            URL mUrl = new URL(uri);

            urlConnection = (HttpURLConnection) mUrl.openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json");

            if(token != null && token.length() > 0)
            {
                urlConnection.setRequestProperty("Authorization", "Bearer " + token);
            }

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
