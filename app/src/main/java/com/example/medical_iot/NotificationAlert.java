package com.example.medical_iot;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.medical_iot.repository.WaitingDataRepository;
//-------------------SOURCES------------------------------------------------------------//
//https://devstory.net/10427/android-notification
//https://developer.android.com/develop/ui/views/notifications/build-notification?hl=fr
//-------------------------------------------------------------------------------------//

public class NotificationAlert
{
    //-----------------------------------------ATTRIBUTS------------------------------------------------------------------------//
    public static final String CHANNEL_ID = "NotificationCountAlertes"; //définition du nom du channel de notification

    //-----------------------------------------METHODES--------------------------------------------------------------------------//
    //___________________________________________________________________________________________________________________________//
    //------METHODE : createNotification
    //------FONCTION : permet de créer une instance de notification qui indiquera le nombre d'alertes a traiter
    //------RETOUR : aucun
    public static void createNotification(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Alertes à traiter : ";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);

            //Instancie une séquence de vibration
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 500, 500, 500});

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    //___________________________________________________________________________________________________________________________//
    //------METHODE : sendNotification
    //------FONCTION : permet d'envoyer une notification sur le téléphone mobile avec les paramètres définis dans createNotification
    //------RETOUR : aucun
    public static void sendNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.logomedical_iot)
                .setContentTitle("Alertes à traiter : ")
                .setContentText("Il reste " + String.valueOf(WaitingDataRepository.getInstance().getNombreAlerte()) + " alertes à traiter.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVibrate(new long[]{0, 500, 500, 500})
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        if (notificationManager != null) {
            notificationManager.notify(1, builder.build());
        }
    }
}
