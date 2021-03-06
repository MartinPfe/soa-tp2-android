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

public class LoginActivity extends AppCompatActivity implements Login.View {

    private LoginPresenter presenter;

    private LoginViewModel loginViewModel;
    public IntentFilter filtroRegistro;
    public IntentFilter filtroLogin;

    EditText usernameEditText;
    EditText passwordEditText;
    EditText nameEditText;
    EditText lastNameEditText;
    EditText dniEditText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        presenter = new LoginPresenter(this);

        setContentView(R.layout.activity_login);
        loginViewModel = new ViewModelProvider(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);

        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        nameEditText = findViewById(R.id.name);
        lastNameEditText = findViewById(R.id.lastname);
        dniEditText = findViewById(R.id.dni);


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

                String loginUri = getString(R.string.loginUri);;

                Intent i = new Intent(LoginActivity.this, HttpCliente_POST.class);

                i.putExtra("uri", loginUri);
                i.putExtra("jsonData", objRegister.toString());
                i.putExtra("receiver", "RESPUESTA_LOGIN");

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

                    objRegister.put("commission", getString(R.string.comission));
                    objRegister.put("group", getString(R.string.group));
                    objRegister.put("env", getString(R.string.env));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                String registerUri = getString(R.string.registerUri);

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
        registerReceiver(presenter.getReceiverRegistro(), filtroRegistro);

        filtroLogin = new IntentFilter("com.example.intentservice.intent.action.RESPUESTA_LOGIN");
        filtroLogin.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(presenter.getReceiverLogin(), filtroLogin);
    }

}

