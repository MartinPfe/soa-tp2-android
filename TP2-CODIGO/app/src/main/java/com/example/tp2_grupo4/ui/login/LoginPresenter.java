package com.example.tp2_grupo4.ui.login;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.tp2_grupo4.MainActivity;
import com.example.tp2_grupo4.R;
import com.example.tp2_grupo4.data.DbRepository;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginPresenter implements Login.Presenter {

    private LoginActivity activity;
    private ReceptorOperacionRegistro receiverRegistro = new ReceptorOperacionRegistro();

    private ReceptorOperacionLogin receiverLogin = new ReceptorOperacionLogin();
    DbRepository db;

    final int commission = 2900;
    final int group = 4;
    final String env = "TEST";
    final String registerUri = "http://so-unlam.net.ar/api/api/register";
    final String loginUri = "http://so-unlam.net.ar/api/api/login";

    public LoginPresenter(LoginActivity activity){
        this.activity = activity;

        db = new DbRepository(this.activity);

    }
    public int getCommission() {
        return commission;
    }

    public int getGroup() {
        return group;
    }

    public String getEnv() {
        return env;
    }

    public String getRegisterUri() {
        return registerUri;
    }

    public String getLoginUri() {
        return loginUri;
    }


    public ReceptorOperacionRegistro getReceiverRegistro() {
        return receiverRegistro;
    }

    public ReceptorOperacionLogin getReceiverLogin() {
        return receiverLogin;
    }

    public class ReceptorOperacionLogin extends BroadcastReceiver
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
                        String refreshToken = datosJson.getString("token_refresh");

                        if(!db.existUser(activity.findViewById(R.id.username).toString())){
                            db.insertUser(activity.findViewById(R.id.username).toString(), refreshToken,token);
                        }
                        else{
                            db.updateLoggedUser(activity.findViewById(R.id.username).toString(), refreshToken,token);
                        }
                        Toast.makeText(context, "Sesión iniciada correctamente", Toast.LENGTH_SHORT).show();

                        Intent mainActivityIntent = new Intent(context, MainActivity.class);
                        context.startActivity(mainActivityIntent);
                    }
                    else
                    {
                        String mensaje = datosJson.getString("msg");
                        Toast.makeText(context, "Ocurrió un error autenticando al usuario: " + mensaje, Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    Toast.makeText(context, "Ocurrió un error autenticando al usuario", Toast.LENGTH_SHORT).show();
                }
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


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
                        String refreshToken = datosJson.getString("token_refresh");

                        if(!db.existUser(activity.findViewById(R.id.username).toString())){
                            db.insertUser(activity.findViewById(R.id.username).toString(), refreshToken,token);
                        }
                        else{
                            db.updateLoggedUser(activity.findViewById(R.id.username).toString(), refreshToken,token);
                        }

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
}
