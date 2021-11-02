package com.example.tp2_grupo4.HttpClient;

import android.app.IntentService;
import android.content.Intent;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpClient_GET extends IntentService
{
    private Exception mException=null;

    public HttpClient_GET() {
        super("HttpClient_GET");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        String uri = intent.getExtras().getString("uri");
        String datosJson = intent.getExtras().getString("jsonData");
        String receiver = intent.getExtras().getString("receiver");

        ejecutarGET(uri, datosJson, receiver);
    }

    protected void ejecutarGET(String uri, String datosJson, String receiver)
    {
        String result = GET(uri, datosJson);

        Intent i = new Intent("com.example.intentservice.intent.action." + receiver);
        i.putExtra("datosJson", result);

        sendBroadcast(i);
    }

    private String GET (String uri, String jsonData)
    {
        HttpURLConnection urlConnection = null;
        try
        {
            URL mUrl = new URL(uri);

            urlConnection = (HttpURLConnection) mUrl.openConnection();

            urlConnection.setRequestMethod("GET");

            if(jsonData != null && jsonData.length() > 0)
            {
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                urlConnection.setRequestProperty("Content-Type", "application/json");
                DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream ());
                wr.writeBytes(jsonData);

                wr.flush();
                wr.close();
            }

            urlConnection.connect();

            int responseCode = urlConnection.getResponseCode();

            String result = "";

            if ( responseCode == HttpURLConnection.HTTP_OK)
            {
                InputStreamReader inputStreamReader = new InputStreamReader((urlConnection.getInputStream()));
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
