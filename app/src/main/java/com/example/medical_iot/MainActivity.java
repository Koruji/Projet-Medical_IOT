package com.example.medical_iot;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.window.OnBackInvokedDispatcher;

import com.example.medical_iot.fragments.HomeFragment;
import com.example.medical_iot.fragments.HomeFragmentAlert;
import com.example.medical_iot.model.ArchiveDataModel;

import java.sql.Connection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity
{

    Handler openLogin;
    SharedPreferences login;

    ConnectionToMySQL baseDeDonnee;

    Connection connect;

    String messageBD;

    String messageWIFI;

    ScheduledExecutorService executorWIFI;

    ExecutorService executorBD;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        //__________________________________________________________creation de la page menu
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("MainActivity", "onCreate: activite cree");

        //injection du fragment de choix historique des alertes sur le layout prévu sur la page home
        FragmentTransaction transactionIntoLayout = getSupportFragmentManager().beginTransaction();

        //--------------Test connexion au WIFI----------------------------------------------------------------------------//
        checkConnexionToWIFIThread();

        //--------------Test connexion a la BD----------------------------------------------------------------------------//
        baseDeDonnee = new ConnectionToMySQL();
        connectToMedicalBD();

        //----------------------------execution première de la page login-----------------------------------------//
        openLogin = new Handler();
        login = getSharedPreferences("VerifOuvertureLogin", MODE_PRIVATE);
        boolean openLoginExecute = login.getBoolean("loginOpen", false);

        if(!openLoginExecute)
        {
            openLogin.post(new Runnable()
            {
                @Override
                public void run()
                {
                    startActivity(new Intent(MainActivity.this, LoginSession.class));
                    Log.d("MainActivity", "ouverture 1ere de la page login");

                    //indique que la page de login a été vu au moins une fois
                    SharedPreferences.Editor edit = login.edit();
                    edit.putBoolean("loginOpen", true);
                    edit.apply();
                }
            });
        }

        //----------------------Bouton de deconnexion--------------------------------------------//
        Button deconnexionButton = findViewById(R.id.deconnexion_button);
        deconnexionButton.setOnClickListener(v -> {
            closeApplication();
        });

        //----------------------recuperation de la donnée de l'acquittement
        Intent intent = getIntent();
        if(intent != null)
        {
            ArchiveDataModel archiveData = intent.getParcelableExtra("archive");
            if (archiveData != null)
            {
                //--------------------------envoi vers le home fragment pour transfert vers le archive adapter
                Bundle newBundle = new Bundle();
                newBundle.putParcelable("archive", archiveData);
                HomeFragment fragmentMenu = new HomeFragment();
                fragmentMenu.setArguments(newBundle);
                //on remplace le layout par ce qu'on a préparé dans le home fragment
                transactionIntoLayout.replace(R.id.archive_container_home, fragmentMenu); //penser a faire new
            }
        }

        //ajout de l'affichage d'une alerte
        transactionIntoLayout.replace(R.id.notification_zone, new HomeFragmentAlert());

        transactionIntoLayout.addToBackStack(null); //permet de ne pas avoir de retour
        transactionIntoLayout.commit();

        Log.d("MainActivity", "onCreate: composants injectes OK");

        //controle du bouton back
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed()
            {
                //je fais rien pour ne plus avoir la possibilité d'utiliser le bouton back
                Log.d("MainActivity", "handleOnBackPressed: non tu peux pas sortir de l'appli comme ca");
            }
        });
    }

    //code lors de la fermeture de l'application
    protected void closeApplication()
    {
        //avant de fermer l'application il est essentiel de remettre à 0 le passage par la page de login
        //cela permettra à la réouverture de remontrer la page de login
        SharedPreferences.Editor edit = login.edit();
        edit.putBoolean("loginOpen", false);
        edit.apply();
        //on appel finish() qui appel onDestroy()
        finishAffinity();
    }
    protected void onDestroy() {
        super.onDestroy();
        Log.d("SYSTEM", "onDestroy appelé");

        //arrêt des tâches en arrière plan pour éviter qu'elles ne continuent à la fermeture
        //de l'activité principale, elles se relanceront à la cration d'une nouvelle main activity
        if(executorBD != null && !executorBD.isShutdown())
        {
            executorBD.shutdownNow();
            Log.d("MainActivity", "je ferme la 1re tache en arrière plan");
        }

        if(executorWIFI != null && !executorWIFI.isShutdown())
        {
            executorWIFI.shutdownNow();
            Log.d("MainActivity", "je ferme la 2eme tache en arrière plan");
        }
        Intent stop = new Intent(this, MainActivity.class);
        stopService(stop);
    }



    //méthode pour ouvrir la 2ème activité donc la page d'acquittement suite au clique sur
    public void openAckPage(View view)
    {
        startActivity(new Intent(this, AckPage.class));
        finish();
        Log.d("AckPage", "openAckPage: j'ouvre la nouvelle activite");
    }

    //méthode de connexion à la base de donnée
    public void connectToMedicalBD()
    {
        //se fait en arrière plan grâce au ExecutorService qui gère des thread exécutant des tâches en arrière plan
        executorBD = Executors.newSingleThreadExecutor();
        //dans ce cas présent il s'agit d'un thread unique qui sera réalisé en arrière plan
        //en fonction de la file d'attente des threads déjà en cours (au nombre de 0 dans notre cas)
        executorBD.execute(() -> {
            //bloc pour surveiller les exceptions et erreur
            try
            {
                connect = baseDeDonnee.getConnection();
                //on lance la méthode de la classe ConnectionToMySQL qui permet de se connecter à la BD
                Log.d("CONNEXION", "passage par le choix du message");
                //en fonction de la valeur retournée on initialise un message
                if(connect == null)
                {
                    messageBD = "Erreur de connexion a la base de donnée!";
                }
                else
                {
                    messageBD = "Connexion REUSSIE !";
                }
            }
            catch (Exception e)
            {
                //en cas de problème système/code on retourne la nature de l'erreur
                Log.d("CONNEXION", "erreur pour le message");
                throw new RuntimeException(e);
            }

            //suite à la phase de connection on affiche le message sur l'interface utilisateur
            //permet de lancer une modification sur un autre thread (donc le thread de l'interface utilisateur)
            //même si l'on est dans un autre thread (donc le thread arrière plan executorBD)
            runOnUiThread(() -> {
                //bloc pour surveiller les exceptions et erreur
                try
                {
                    Thread.sleep(1000);
                    Log.d("CONNEXION", "affichage ok");
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                    Log.d("CONNEXION", "affichage non possible");
                }
                //code pour afficher le message utilisateur de la connection BD
                Toast.makeText(this, messageBD, Toast.LENGTH_SHORT).show();
                Log.d("CONNEXION", "je suis passé par le toast");
            });
        });
    }

    public void checkConnexionToWIFIThread()
    {
        //on fait un thread en arrière plan comme la méthode précédente mais avec un timer
        executorWIFI = Executors.newScheduledThreadPool(1);
        executorWIFI.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try
                {
                    checkConnexionToWIFI();
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }

                runOnUiThread(() ->
                {
                    try
                    {
                        Thread.sleep(1000);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                    TextView wifiText = findViewById(R.id.wifi_info_home_page);
                    wifiText.setText(messageWIFI);
                });
            }
        }, 0, 10, TimeUnit.SECONDS);
    }

    public void checkConnexionToWIFI()
    {
        //on recupere le manager de connexion
        ConnectivityManager connectManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        //on recupere les informations de connexion WIFI
        NetworkInfo networkInfo = connectManager.getActiveNetworkInfo();
        Log.d("CONNEXION", "je regarde la connexion WIFI");

        if(networkInfo != null && networkInfo.isConnected() && networkInfo.getType() == ConnectivityManager.TYPE_WIFI)
        {
            //en vérifiant que le tel est bien connecté à un réseau WIFI, on récupère les informations du WIFI
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo infoDuWifi = wifiManager.getConnectionInfo();

            messageWIFI = "WIFI : " + infoDuWifi.getSSID() + "; Statut : " + networkInfo.getState();
        }
        else if(networkInfo != null && networkInfo.isConnected() && networkInfo.getType() != ConnectivityManager.TYPE_WIFI)
        {
            messageWIFI = "Réseau NON WIFI" + "; Statut : " + networkInfo.getState();
        }
        else
        {
            messageWIFI = "Pas de connexion WIFI";
        }
    }
}