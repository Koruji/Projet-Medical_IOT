package com.example.medical_iot.backgroundServices;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.medical_iot.MainActivity;
import com.example.medical_iot.NotificationAlert;
import com.example.medical_iot.R;
import com.example.medical_iot.repository.WaitingDataRepository;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
//------------SOURCES----------------------------------//
//https://www.youtube.com/watch?v=YZL-_XJSClc --> informations sur Service + code (Philipp Lackner)
//https://devstory.net/10427/android-notification --> notifications
//

public class ListenUDPservice extends Service
{
    //-------------------------------------ATTRIBUTS---------------------------------------------------//
    //----réception
    final static int port = 5000; //port pour recevoir les alertes
    private boolean etatServeurEcoute;
    private DatagramSocket socketListenUDP;

    //----notification
    private static final String channelOfNotification = "listen"; //libellé des notifications créé suite à une alerte
    public static final String ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"; //libellé pour l'arrêt du service

    public static final String ACTION_UDP_BROADCAST = "com.example.medical_iot.UDP_BROADCAST";

    //------------------EXECUTION PRINCIPALE--------------------------------------------------------------------------//
    @Override
    public void onCreate() {
        super.onCreate();
        //création d'une notification système lors de la réception d'une alerte
        createListenUDPNotificationSystem();
    }

    //----------------------------------METHODES-------------------------------------------------------//
    private void startUDPListenerServer()
    {
        //état du socket d'écoute (true -> en écoute / false -> fermé)
        etatServeurEcoute = true;

        //mise en route du service de notification
        NotificationAlert.createNotification(this);

        Log.d("CONNEXION", "socket en écoute");
        try
        {
            //buffer qui permet d'allouer de l'espace pour recevoir la paquet alerte
            byte[] recep = new byte[1024];

            //initialisation d'une variable qui va recevoir le paquet attendu
            DatagramPacket paquetRecu = new DatagramPacket(recep, recep.length);

            //correspond au socket ouvert de notre serveur pour l'écoute
            socketListenUDP = new DatagramSocket(port);
            while(etatServeurEcoute)
            {
                //réception du paquet d'alerte de la part du module de communication
                socketListenUDP.receive(paquetRecu);

                //récupération des données de l'alerte reçu
                String donneeRecu = new String(paquetRecu.getData());

                //message affiché dans le débogueur pour vérifier la provenance du paquet d'alerte
                Log.d("CONNEXION", "Réception du message en provenance de " + paquetRecu.getAddress().getHostAddress() + " au port " + paquetRecu.getPort() + " -> donnée : " + donneeRecu);

                //la donnée du paquet est stockée dans une liste sauvegardé dans le cache de l'application
                WaitingDataRepository.getInstance().addAlerte(donneeRecu);

                //on prévient le main que l'on a reçu une donnée
                Intent broadcastIntent = new Intent(ACTION_UDP_BROADCAST);
                LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);

                //on met à jour et envoi une notif d'alerte
                NotificationAlert.sendNotification(this);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    //Création d'un BoundService qui permet la communication avec plusieurs composants
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //on récupère l'intent du MainActivity pour démarrer le service
        String input = intent.getStringExtra("appelCommandeNotif");

        //création d'un nouvel intent vers le MainActivity
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_MUTABLE);

        //directives pour la création de la notification
        Notification notifSpecification = new NotificationCompat.Builder(this, channelOfNotification)
                .setContentTitle("UDP Serveur")
                .setContentText("L'application est en écoute")
                .setSmallIcon(R.drawable.logomedical_iot)
                .build();
        startForeground(1, notifSpecification);

        //si détection de la volonté de l'arrêt totale du service, tout est arrêté
        if (intent != null && ACTION_STOP_SERVICE.equals(intent.getAction())) {
            stopForeground(true);
            stopSelf();
            return START_NOT_STICKY; //même chose que le 2ème return de cette méthode
        }

        //ajout du démarrage du thread réception --> seul a tourner en arrière plan constamment
        new Thread(this::startUDPListenerServer).start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //indication de la fermeture du socket d'écoute
        etatServeurEcoute = false;
        socketListenUDP.close();
    }

    private void createListenUDPNotificationSystem()
    {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationUDP = new NotificationChannel(
                    channelOfNotification,
                    "ListenUDP Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager managerDeNotif = getSystemService(NotificationManager.class);
            if (managerDeNotif != null)
            {
                managerDeNotif.createNotificationChannel(notificationUDP);
            }
        }
    }
}
