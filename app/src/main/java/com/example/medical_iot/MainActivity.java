package com.example.medical_iot;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.medical_iot.backgroundServices.ListenUDPservice;
import com.example.medical_iot.backgroundServices.UDPShortService;
import com.example.medical_iot.fragments.HomeFragment;
import com.example.medical_iot.fragments.HomeFragmentAlert;
import com.example.medical_iot.model.ArchiveDataModel;
import com.example.medical_iot.repository.ArchiveRepository;
import com.example.medical_iot.repository.WaitingDataRepository;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

//-------------------------SOURCES--------------------------------//
//https://www.javatpoint.com/android-image-switcher --> pour la possibilité de faire un tableau de drawable
//https://www.youtube.com/watch?v=didDyJkt-zQ --> bouton back
//https://openclassrooms.com/fr/courses/4568596-construisez-une-interface-utilisateur-flexible-et-adaptative/4568603-comprenez-les-differents-moyens-de-naviguer-sur-une-application
//https://androidcorpo.com/persistance-des-donnees/149-sharedpreferences --> sharedPreferences
//----------------------------------------------------------------//
public class MainActivity extends AppCompatActivity
{
    //_________________________________________________ATTRIBUTS_______________________________________________________//
    //----pour la page de login
    private SharedPreferences login;
    private ArchiveRepository repository;

    //----pour la vérification de la connexion WIFI
    private String messageWIFI;
    private ScheduledExecutorService executorWIFI;

    //----pour le serveur UDP
    private UDPShortService serveurEnvoi;
    private ExecutorService executorServerSend;
    private String receptionAlerte;
    private boolean saveState;

    //----utilisé par la page menu
    private FragmentTransaction transactionIntoLayout;

    //----pour les pictogrammes
    private TextView switchWifi;
    private final int[] switchWifiImage = {R.drawable.ic_wifi_error, R.drawable.ic_wifi};
    private TextView switchSave;
    private final int save = R.drawable.ic_validation;

    //----pour la connexion à la base de données (NON RETENUE)
    /*ConnectionToMySQL baseDeDonnee;
    Connection connect;
    String messageBD;
    ExecutorService executorBD;
    ConnectionToMySQL baseDeDonnee;*/

    //_____________________________________________EXECUTION PRINCIPALE__________________________________________________//
    @SuppressLint({"SetTextI18n", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        //------------Creation de la page menu (MainActivity)-------------------------------------------------------------//
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("MainActivity", "onCreate: activite cree");

        //----------------------------Execution première de la page login-------------------------------------------------//
        //----pour la page de login
        Handler openLogin = new Handler();
        //création d'un sharedPreference qui stocke en mémoire si l'utilisateur a déjà renseigné des informations sur la
        //page de login
        login = getSharedPreferences("VerifOuvertureLogin", MODE_PRIVATE);
        boolean openLoginExecute = login.getBoolean("loginOpen", false);

        //si c'est le premier passage alors la page de login est ouverte
        if(!openLoginExecute)
        {
            openLogin.post(() -> {
                startActivity(new Intent(MainActivity.this, LoginSession.class));
                Log.d("MainActivity", "ouverture 1ere de la page login");

                //indique que la page de login a été vu au moins une fois
                SharedPreferences.Editor edit = login.edit();
                edit.putBoolean("loginOpen", true);
                edit.apply();
            });
        }

        //-------------Initialisation du manager de fragment qui implémente les fragments à la page de menu---------------//
        transactionIntoLayout = getSupportFragmentManager().beginTransaction();

        //--------------Test connexion au WIFI----------------------------------------------------------------------------//
        checkConnexionToWIFIThread();

        //----------------Test de connexion à la Base de données (NON RETENU)---------------------------------------------//
        /*baseDeDonnee = new ConnectionToMySQL();
        connectToMedicalBD();*/

        //---------------Réception des données d'alerte-------------------------------------------------------------------//
        //démarrage de l'écoute constante du serveur UDP pour la récéption des alertes
        Intent serviceUDP = new Intent(this, ListenUDPservice.class);
        serviceUDP.putExtra("appelCommandeNotif", "Démarrage du service UDP");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceUDP);
        }

        receptionData();

        //----------------------Récuperation de la donnée de l'acquittement et traitement---------------------------------------//
        Intent intent = getIntent();
        if(intent != null)
        {
            ArchiveDataModel dataAckPage = intent.getParcelableExtra("archive");
            if (dataAckPage != null)
            {
                FragmentTransaction transactionNotificationAlert = getSupportFragmentManager().beginTransaction();

                //création de la donnée à envoyer vers le module de communication + envoi
                sendBDrequest(dataAckPage);

                //envoi de la variable "archive" vers le home fragment pour transfert vers le archive adapter
                Bundle newBundle = new Bundle();
                newBundle.putParcelable("archive", dataAckPage);
                HomeFragment fragmentMenu = new HomeFragment();
                repository = fragmentMenu.getRepository();
                fragmentMenu.setArguments(newBundle);

                //on remplace le layout par ce qu'on a préparé dans le home fragment
                transactionNotificationAlert.replace(R.id.archive_container_home, fragmentMenu);
                transactionNotificationAlert.addToBackStack(null); //permet de ne pas avoir de retour
                transactionNotificationAlert.commit();
            }
        }
        Log.d("MainActivity", "onCreate: composants injectes OK");

        //----------------------Bouton de déconnexion--------------------------------------------------------------------//
        Button deconnexionButton = findViewById(R.id.deconnexion_button);
        //si le bouton de déconnexion est cliqué, alors l'identifiant renseigné est déconnecté et l'application se ferme
        deconnexionButton.setOnClickListener(v -> closeApplication());

        //------------------------Paramétrage du bouton retour-----------------------------------------------------------------//
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed()
            {
                //je fais rien pour ne plus avoir la possibilité d'utiliser le bouton back
                Log.d("MainActivity", "handleOnBackPressed: interdit de sortir de l'application par le bouton retour");
            }
        });
    }


    //_______________________________________METHODES DU MAINACTIVITY()_______________________________________________________//

    private BroadcastReceiver udpReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(ListenUDPservice.ACTION_UDP_BROADCAST.equals(intent.getAction()))
            {
                //récupération des alertes et traitement visuel
                receptionData();
            }
        }
    };

    protected void onStart()
    {
        super.onStart();
        IntentFilter filter = new IntentFilter(ListenUDPservice.ACTION_UDP_BROADCAST);
        LocalBroadcastManager.getInstance(this).registerReceiver(udpReceiver, filter);
    }

    protected void onStop()
    {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(udpReceiver);
    }

    //------METHODE : onDestroy
    //------FONCTION : cette méthode est utilisé par défaut par l'application lors de la fermeture/mise en fin de processus d'une
    //---------------- d'une activité. On peut y rajouter des éléments. Dans le cas présent, la fermeture de toutes les tâches en
    //---------------- arrière plan sont ajoutées
    //------RETOUR : aucun
    protected void onDestroy() {
        super.onDestroy();
        Log.d("SYSTEM", "onDestroy appelé");

        //arrêt des tâches en arrière plan pour éviter qu'elles ne continuent à la fermeture
        //de l'activité principale, elles se relanceront à la cration d'une nouvelle main activity
        if(executorWIFI != null && !executorWIFI.isShutdown())
        {
            executorWIFI.shutdownNow();
            Log.d("MainActivity", "je ferme la 1re tache en arrière plan");
        }

        if(executorServerSend != null && !executorServerSend.isShutdown())
        {
            executorServerSend.shutdownNow();
            Log.d("MainActivity", "je ferme la 2eme tache en arrière plan");
        }

        //effacement de toutes les données d'archive
        //repository.clearData();
    }

    //___________________________________________________________________________________________________________________________//
    //------METHODE : closeApplication
    //------FONCTION : cette méthode remet à zéro le passage par la page de login. Cela signifie qu'après l'appel du destroy et
    //---------------- de la fermeture de l'application, une fois que l'on retourne sur cette dernière, la page de login réapparait.
    //------RETOUR : aucun
    protected void closeApplication()
    {
        //avant de fermer l'application il est essentiel de remettre à 0 le passage par la page de login
        //cela permettra à la réouverture de remontrer la page de login
        SharedPreferences.Editor edit = login.edit();
        edit.putBoolean("loginOpen", false);
        edit.apply();
        Log.d("MainActivity", "closeApplication appelé");

        //mise en arrêt du service en arrière plan
        Intent stop = new Intent(this, MainActivity.class);
        stopService(stop);
    }

    //___________________________________________________________________________________________________________________________//
    //------METHODE : openAckPage
    //------FONCTION : ouvre l'activité qui permet d'acquitter une alerte. Cette méthode est lancé lors du clique sur la notification
    //---------------- d'alerte. Elle permet aussi de transférer les données d'alerte reçues.
    //------RETOUR : aucun
    public void openAckPage(View view)
    {
        if(receptionAlerte != null)
        {
            //préparation de la donnée a transférer
            Intent transfertToAckPage = new Intent(this, AckPage.class);
            transfertToAckPage.putExtra("donneeAlerte", receptionAlerte);
            transfertToAckPage.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            //lance l'activité AckPage
            startActivity(transfertToAckPage);
            //ferme l'activité principale MainActivity qui correspond au menu
            finish();
            Log.d("AckPage", "openAckPage: j'ouvre la nouvelle activite");
        }
        else {
            Log.d("MainActivity", "l'alerte reçu est non valide ");
        }
    }

    //___________________________________________________________________________________________________________________________//
    //------METHODE : checkConnexionToWIFI
    //------FONCTION : permet d'identifier si le smartphone est connecté au réseau du projet
    //------RETOUR : aucun
    public void checkConnexionToWIFI() throws IOException {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        switchWifi = findViewById(R.id.switch_wifi_home_page);
        if(activeNetworkInfo != null && activeNetworkInfo.isConnected())
        {
            InetAddress ipAdresseReseau = InetAddress.getByName("192.168.0.3"); //renseigner l'adresse IP du téléphone
            if(ipAdresseReseau.isReachable(2000))
            {
                messageWIFI = "Connecté au réseau";
            }
            else
            {
                messageWIFI = "Aucune connexion au réseau";
            }
        }
    }

    //___________________________________________________________________________________________________________________________//
    //------METHODE : checkConnexionToWIFIthread
    //------FONCTION : permet d'exécuter une tâche en arrière plan qui détermine la nature de la connexion internet,
    //---------------- et la renseigne à l'utilisateur via la page de menu de l'application
    //------RETOUR : aucun
    public void checkConnexionToWIFIThread()
    {
        //on fait un thread en arrière plan comme la méthode précédente mais avec un timer
        executorWIFI = Executors.newScheduledThreadPool(1);
        Log.d("CONNEXION", "Thread de WIFI est lancé");
        executorWIFI.scheduleAtFixedRate(() -> {
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
                if(Objects.equals(messageWIFI, "Connecté au réseau"))
                {
                    switchWifi.setCompoundDrawablesWithIntrinsicBounds(switchWifiImage[1],0,0,0);
                }
                else
                {
                    switchWifi.setCompoundDrawablesWithIntrinsicBounds(switchWifiImage[0],0,0,0);
                }
            });
        }, 0, 10, TimeUnit.SECONDS);
    }

    //___________________________________________________________________________________________________________________________//
    //------METHODE : receptionData (MODIFIER)
    //------FONCTION : permet de récupérer l'alerte n°1 stockée dans le repository des alertes
    //------RETOUR : aucun
    public void receptionData()
    {
        //1re vérification : s'il n'y a rien dans la liste des alertes alors ne fait rien
        if(!WaitingDataRepository.getInstance().getAlerte().isEmpty())
        {
            receptionAlerte = WaitingDataRepository.getInstance().getAlerte().get(0);
            if(!Objects.equals(receptionAlerte, ""))
            {

                Log.d("CONNEXION", "le layout d'alerte a été injecté");
            }
            else
            {
                //2eme vérification : supression de l'alerte n°1 si cette dernière n'a pas de données
                WaitingDataRepository.getInstance().suppAlerte(0);
                receptionAlerte = null;
            }
        }
        //Log.d("CONNEXION", "aucune alerte valide en cours");
    }

    //___________________________________________________________________________________________________________________________//
    //------METHODE : showAlert (ancienne partie de la méthode receptionData)
    //------FONCTION : affiche la bannière lors d'une notification d'alerte
    //------RETOUR : aucun
    public void showAlert()
    {
        //indique le remplacement de la zone bleu par la bannière rouge d'alerte
        transactionIntoLayout.replace(R.id.notification_zone, new HomeFragmentAlert());
        transactionIntoLayout.addToBackStack(null);
        //injecte la bannière
        transactionIntoLayout.commit();
    }

    //___________________________________________________________________________________________________________________________//
    //------METHODE : sendBDrequest
    //------FONCTION : permet de créer la requête SQL nécessaire à l'envoi de données vers le module de communication et l'envoi vers
    //---------------- le module
    //------RETOUR : aucun
    private void sendBDrequest(ArchiveDataModel p_archive) {
        String requeteSQL = "INSERT INTO rapport_incident (deplacement_surveillant, heure_acquittement, acquittement_surveillant, espace_commentaire)" +
                " VALUES ('" + p_archive.getDeplacement_surveillant() + "', '" + p_archive.getHeure_acquittement() + "', '" + p_archive.getAcquittement_surveillant() + "', '"
                + p_archive.getEspace_commentaire() + "');";

        executorServerSend = Executors.newSingleThreadExecutor();
        //etat de sauvegarde a modifier
        switchSave = findViewById(R.id.save_statue);

        Log.d("CONNEXION", "Thread de serveur pour envoi est lancé");
        executorServerSend.execute(() -> {
            try {
                serveurEnvoi = new UDPShortService();
                saveState = serveurEnvoi.envoiAcquittement(requeteSQL);
                Log.d("CONNEXION", "requête SQL envoyé");
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
            /*if (saveState)
            {
                switchSave.setCompoundDrawablesWithIntrinsicBounds(save, 0, 0, 0);
            }*/
        });
    }

    //___________________________________________________________________________________________________________________________//
    //------METHODE : connectToMedicalBD (NON RETENUE)
    //------FONCTION : devait permettre de se connecter à la base de donnée, en arrière plan, pour y écrire les données d'acquittement
    //------RETOUR : aucun
    /*public void connectToMedicalBD()
    {
        //se fait en arrière plan grâce au ExecutorService qui gère des thread exécutant des tâches en arrière plan
        executorBD = Executors.newSingleThreadExecutor();
        //dans ce cas présent il s'agit d'un thread unique qui sera réalisé en arrière plan
        //en fonction de la file d'attente des threads déjà en cours (au nombre de 0 dans notre cas)
        executorBD.execute(() -> {
            //bloc pour surveiller les exceptions et erreur
            try
            {
                connect = baseDeDonnee.getConnect();
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
                //throw new RuntimeException(e);
                e.printStackTrace();
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
    }*/
}