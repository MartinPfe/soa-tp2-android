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

//Clase que genera un hiloencargado de emitir peticiones POST al Servidor y recibir su respuesta
public class HttpCliente_POST extends IntentService
{

    private Exception mException=null;

    public HttpCliente_POST() {
        super("HttpCliente_POST");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        Log.e("1", "onHandleIntent");
        String uri = intent.getExtras().getString("uri");
        String datosJson = intent.getExtras().getString("jsonData");
        String receiver = intent.getExtras().getString("receiver");

        ejecutarPost(uri, datosJson, receiver);
    }

    protected void ejecutarPost(String uri, String datosJson, String receiver)
    {
        Log.e("1", "ejecutarPost");

        String result = POST(uri, datosJson);

        Intent i = new Intent("com.example.intentservice.intent.action." + receiver);
        i.putExtra("datosJson", result);

        sendBroadcast(i);
    }

    //Metodo que le envia una peticion POST al servidor solicitandole modificar el estado de un led
    //con un valor determinado
    private String POST (String uri, String jsonData)
    {
        Log.e("1", "POST");
        HttpURLConnection urlConnection = null;
        try
        {
            //Se alamacena la URI del request del servicio web
            URL mUrl = new URL(uri);

            //Se arma el request con el formato correcto
            urlConnection = (HttpURLConnection) mUrl.openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            //urlConnection.setRequestProperty("Content-Length", "" + jsonData.getBytes().length);

            //Se crea un paquete JSON que indica el estado(encendido o apagado) del led que se desea
            //modificar. Este paquete JSON se escribe en el campo body del mensaje POST
            DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream ());

            JSONObject obj = new JSONObject();

            wr.writeBytes(jsonData);

            wr.flush();
            wr.close();

            //se envia el request al Servidor
            urlConnection.connect();

            //Se obtiene la respuesta que envio el Servidor ante el request
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
            System.out.println(e);
        }
        return resultString;
    }

}
