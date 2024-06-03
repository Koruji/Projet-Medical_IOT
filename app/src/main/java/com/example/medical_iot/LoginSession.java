package com.example.medical_iot;

import static java.lang.Thread.sleep;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import com.example.medical_iot.backgroundServices.UDPShortService;
import com.example.medical_iot.model.ArchiveDataModel;
import com.example.medical_iot.model.LoginModel;

import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//-----------------------SOURCES----------------------//
//https://www.youtube.com/watch?v=Gc0sLf91QeM --> obligation de renseignements
//--------------------------------------------------//
public class LoginSession extends AppCompatActivity {

    //_________________________________________________ATTRIBUTS_______________________________________________________//
    private Button validationLogin;
    private UDPShortService udpServer;
    private volatile boolean checkValue = false;
    private ExecutorService executorServer;
    private EditText ID;
    private EditText mdp;

    //_________________________________________________EXECUTION PRINCIPALE_______________________________________________________//
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_session);

        //------------Récupération des données de la page login--------------------------//
        ID = findViewById(R.id.identification_zone_ack_id);
        mdp = findViewById(R.id.identification_zone_ack_password);
        validationLogin = findViewById(R.id.identification_zone_ack_id_button);

        //--------------Vérification des renseignements------------------------------------//
        validationLogin.setOnClickListener(view -> {
            // Récupération des logins pour vérification
            LoginModel loginModel = LoginModel.getInstance();
            loginModel.setLoginModel(ID.getText().toString(), mdp.getText().toString());

            // Conditions d'affichage
            if (TextUtils.isEmpty(ID.getText()) || TextUtils.isEmpty(mdp.getText())) {
                if (TextUtils.isEmpty(ID.getText())) {
                    ID.setError("A remplir");
                    ID.requestFocus();
                }
                if (TextUtils.isEmpty(mdp.getText())) {
                    mdp.setError("A remplir");
                    mdp.requestFocus();
                }
            } else {
                // Lancement de l'envoi en arrière-plan
                startSend();
                Log.d("UDP", "Le lancement des données login a été fait sur LoginSession");
            }
        });
    }

    private void startSend() {
        executorServer = Executors.newSingleThreadExecutor();
        Log.d("UDP", "Thread pour les identifiants ouvert");

        executorServer.execute(() -> {
            try {
                udpServer = new UDPShortService();
                checkValue = udpServer.envoiId();
                Log.d("UDP", "Passage par l'envoi login");

                runOnUiThread(() -> {
                    if (checkValue) {
                        if (!executorServer.isShutdown()) {
                            executorServer.shutdownNow();
                            Log.d("UDP", "Thread pour les identifiants fermé");
                        }
                        Log.d("UDP", "Fermeture de la page login et passage au menu");
                        startActivity(new Intent(LoginSession.this, MainActivity.class));
                        finish();
                    } else {
                        ID.setError("Identifiants erronés");
                        ID.requestFocus();
                        mdp.setError("Identifiants erronés");
                        mdp.requestFocus();
                        Log.d("UDP", "Prévient l'utilisateur des données erronées");
                    }
                });
            } catch (UnknownHostException e) {
                Log.e("UDP", "Erreur de connexion", e);
                runOnUiThread(() -> {
                    ID.setError("Erreur de connexion");
                    ID.requestFocus();
                    mdp.setError("Erreur de connexion");
                    mdp.requestFocus();
                });
            }
        });
    }
}