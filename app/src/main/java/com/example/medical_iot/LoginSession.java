package com.example.medical_iot;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;

public class LoginSession extends AppCompatActivity {

    Button validationLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_session);

        //------------Recuperation données de la page login--------------------------//
        EditText ID = findViewById(R.id.identification_zone_ack_id);
        EditText mdp = findViewById(R.id.identification_zone_ack_password);
        validationLogin = findViewById(R.id.identification_zone_ack_id_button);

        //--------------Verification renseignements------------------------------------//
        //écoute du bouton de login pour se connecter
        validationLogin.setOnClickListener(view -> {
            if (TextUtils.isEmpty(ID.getText())) {
                ID.setError("A remplir");
                ID.requestFocus();
            }

            if(TextUtils.isEmpty(mdp.getText())) {
                mdp.setError("A remplir");
                mdp.requestFocus();
                return;
            }

            if (!TextUtils.isEmpty(ID.getText()) || !TextUtils.isEmpty(mdp.getText()))
            {
                startActivity(new Intent(LoginSession.this, MainActivity.class));
                finish();
            }
        });
    }

}