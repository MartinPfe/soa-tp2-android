package com.example.tp2_grupo4.ui.login;

import android.app.Activity;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tp2_grupo4.HttpClient.HttpCliente_POST;
import com.example.tp2_grupo4.MainActivity;
import com.example.tp2_grupo4.R;
import com.example.tp2_grupo4.data.DbRepository;


import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private LoginViewModel loginViewModel;
    public IntentFilter filtroRegistro;
    public IntentFilter filtroLogin;
    private ReceptorOperacionRegistro receiverRegistro = new ReceptorOperacionRegistro();
    private ReceptorOperacionLogin receiverLogin = new ReceptorOperacionLogin();

    DbRepository db;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginViewModel = new ViewModelProvider(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);

        final EditText usernameEditText = findViewById(R.id.username);
        final EditText passwordEditText = findViewById(R.id.password);
        final EditText nameEditText = findViewById(R.id.name);
        final EditText lastNameEditText = findViewById(R.id.lastname);
        final EditText dniEditText = findViewById(R.id.dni);

        db = new DbRepository(this);

        configurarBroadcastReceiver();

        final Button loginButton = findViewById(R.id.login);
        final Button registerButton = findViewById(R.id.register);
        final ProgressBar loadingProgressBar = findViewById(R.id.loading);

        loginViewModel.getLoginFormState().observe(this, new Observer<LoginFormState>() {
            @Override
            public void onChanged(@Nullable LoginFormState loginFormState) {
                if (loginFormState == null) {
                    return;
                }
                loginButton.setEnabled(loginFormState.isDataValid());
                if (loginFormState.getUsernameError() != null) {
                    usernameEditText.setError(getString(loginFormState.getUsernameError()));
                }
                if (loginFormState.getPasswordError() != null) {
                    passwordEditText.setError(getString(loginFormState.getPasswordError()));
                }
            }
        });

        loginViewModel.getLoginResult().observe(this, new Observer<LoginResult>() {
            @Override
            public void onChanged(@Nullable LoginResult loginResult) {
                if (loginResult == null) {
                    return;
                }
                loadingProgressBar.setVisibility(View.GONE);
                if (loginResult.getError() != null) {
                    showLoginFailed(loginResult.getError());
                }
                if (loginResult.getSuccess() != null) {
                    updateUiWithUser(loginResult.getSuccess());
                }
                setResult(Activity.RESULT_OK);

                //Complete and destroy login activity once successful
                finish();
            }
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.loginDataChanged(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    loginViewModel.login(usernameEditText.getText().toString(),
                            passwordEditText.getText().toString());
                }
                return false;
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject objRegister = new JSONObject();
                try {
                    objRegister.put("email", usernameEditText.getText().toString());
                    objRegister.put("password", passwordEditText.getText().toString());

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //TODO: Cambiar por variables de entorno
                String registerUri = "http://so-unlam.net.ar/api/api/login";

                Intent i = new Intent(LoginActivity.this, HttpCliente_POST.class);

                i.putExtra("uri", registerUri);
                i.putExtra("jsonData", objRegister.toString());
                i.putExtra("receiver", "RESPUESTA_LOGIN");

                Log.e("1", "Antes de llamar");

                startService(i);
            }
        });


        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject objRegister = new JSONObject();
                try {
                    objRegister.put("email", usernameEditText.getText().toString());
                    objRegister.put("password", passwordEditText.getText().toString());
                    objRegister.put("name", nameEditText.getText().toString());
                    objRegister.put("lastname", lastNameEditText.getText().toString());
                    objRegister.put("dni", dniEditText.getText().toString());

                    //TODO: Cambiar por variables de entorno
                    objRegister.put("commission", 2900);
                    objRegister.put("group", 4);
                    objRegister.put("env", "PROD");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //TODO: Cambiar por variables de entorno
                String registerUri = "http://so-unlam.net.ar/api/api/register";

                Intent i = new Intent(LoginActivity.this, HttpCliente_POST.class);

                i.putExtra("uri", registerUri);
                i.putExtra("jsonData", objRegister.toString());
                i.putExtra("receiver", "RESPUESTA_REGISTRO");

                Log.e("1", "Antes de llamar");

                startService(i);

            }
        });
    }

    private void updateUiWithUser(LoggedInUserView model) {
        String welcome = getString(R.string.welcome) + model.getDisplayName();
        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }

    private void configurarBroadcastReceiver() {
        filtroRegistro = new IntentFilter("com.example.intentservice.intent.action.RESPUESTA_REGISTRO");
        filtroRegistro.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(receiverRegistro, filtroRegistro);

        filtroLogin = new IntentFilter("com.example.intentservice.intent.action.RESPUESTA_LOGIN");
        filtroLogin.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(receiverLogin, filtroLogin);
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

                        if(!db.existUser(findViewById(R.id.username).toString())){
                            db.insertUser(findViewById(R.id.username).toString(), refreshToken,token);
                        }
                        else{
                            db.updateLoggedUser(findViewById(R.id.username).toString(), refreshToken,token);
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

                        if(!db.existUser(findViewById(R.id.username).toString())){
                            db.insertUser(findViewById(R.id.username).toString(), refreshToken,token);
                        }
                        else{
                            db.updateLoggedUser(findViewById(R.id.username).toString(), refreshToken,token);
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

