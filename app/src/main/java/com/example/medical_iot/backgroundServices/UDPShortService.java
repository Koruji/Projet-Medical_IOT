package com.example.medical_iot.backgroundServices;

import static java.lang.Thread.sleep;

import android.content.Intent;
import android.util.Log;

import com.example.medical_iot.model.LoginModel;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;

//------------------SOURCES--------------------------//
//https://www.jmdoudoux.fr/java/dej/chap-net.htm
//http://tvaira.free.fr/dev/android/android-udp.html

//--------------------------------------------------//

public class UDPShortService {
    //-------------------Serveur permettant l'envoi des données id et acquittement---------------------------------------//

    //---------------------------------------------------METHODES-----------------------------------------------------------------//

    //___________________________________________________________________________________________________________________________//
    //------METHODE : envoiId
    //------FONCTION : envoi vers le module de communication les logins renseignés par l'utilisateur / attente d'accusé de réception pour
    //---------------- valider les logins
    //------RETOUR : booléen
    public boolean envoiId() throws UnknownHostException {
        // Récupération des identifiants sur l'activité AckPage
        LoginModel loginModel = LoginModel.getInstance();
        String login_surveillant = loginModel.getLogin_surveillant();
        String mdp_surveillant = loginModel.getMdp_surveillant();

        // Initialisation de l'adresse d'envoi (adresse de la BD)
        InetAddress adresseIPEnvoi = InetAddress.getByName("192.168.0.9");

        // Allocation d'espace pour recevoir les accusés de réception
        byte[] recep = new byte[3];

        // Valeur à retourner pour indiquer la validation ou non des identifiants
        boolean validation = false;

        DatagramSocket socketEnvoi = null;
        DatagramSocket socketReception = null;

        try {
            // Création du socket pour l'envoi
            socketEnvoi = new DatagramSocket();
            Log.d("UDP", "port local pour la reception est " + 6000);

            // Préparation de la requête SQL de demande d'identifiant à envoyer
            String requeteSQL = String.format(
                    "SELECT login_surveillant, mdp_surveillant " +
                            "FROM surveillant " +
                            "WHERE login_surveillant = '%s' AND mdp_surveillant = '%s';",
                    login_surveillant, mdp_surveillant);

            // Préparation de la taille de la donnée du paquet à envoyer
            byte[] envoi = requeteSQL.getBytes();

            DatagramPacket paquetEnvoye = new DatagramPacket(envoi, envoi.length, adresseIPEnvoi, 12345);
            // Envoi du paquet contenant les identifiants renseignés par l'utilisateur
            socketEnvoi.send(paquetEnvoye);
            Log.d("UDP", "paquetEnvoye pour identifiant");

            // Fermeture du socket d'envoi
            socketEnvoi.close();
            Log.d("UDP", "fermeture socket d'envoi");

            // Ouverture d'un nouveau socket pour la réception sur le port attribué localement
            socketReception = new DatagramSocket(6000);

            // Préparation de réception de la donnée d'acquittement
            DatagramPacket paquetRecu = new DatagramPacket(recep, recep.length);

            // Réception du paquet
            Log.d("UDP", "en attente de réception de la validation des identifiants");
            socketReception.receive(paquetRecu);
            Log.d("UDP", "donnée d'état des identifiants reçus");
            String donneeRecu = new String(paquetRecu.getData(), 0, paquetRecu.getLength()).trim();
            Log.d("UDP", "donnée recu " + donneeRecu);

            if ("oui".equals(donneeRecu)) {
                validation = true;
                Log.d("UDP", "logins validés");
            } else {
                Log.d("UDP", "logins invalides");
            }

        } catch (IOException e) {
            Log.d("UDP", "erreur connexion login", e);
        } finally {
            if (socketEnvoi != null && !socketEnvoi.isClosed()) {
                socketEnvoi.close();
            }
            if (socketReception != null && !socketReception.isClosed()) {
                socketReception.close();
                Log.d("UDP", "fermeture socket de réception");
            }
        }

        return validation;
    }

    //___________________________________________________________________________________________________________________________//
    //------METHODE : envoiAcquittement
    //------FONCTION : envoi vers le module de communication des données d'acquittement / attente d'accusé de réception pour
    //---------------- valider l'enregistrement de ces données dans la base de données
    //------RETOUR : booléen
    public boolean envoiAcquittement(String p_requeteSQL) throws UnknownHostException {
        // Initialisation de l'adresse d'envoi (adresse de la BD)
        InetAddress adresseIPEnvoi = InetAddress.getByName("192.168.0.9");

        // Allocation d'espace pour recevoir les accusés de réception
        byte[] recep = new byte[1024];

        // Valeur à retourner pour indiquer la validation ou non de la sauvegarde
        boolean save = false;

        DatagramSocket socketEnvoi = null;
        DatagramSocket socketReception = null;

        try {
            // Préparation du socket d'envoi
            socketEnvoi = new DatagramSocket();
            socketEnvoi.connect(adresseIPEnvoi, 12345);

            // Préparation de la taille de la donnée du paquet à envoyer
            byte[] envoi = p_requeteSQL.getBytes();
            DatagramPacket paquetEnvoye = new DatagramPacket(envoi, envoi.length, adresseIPEnvoi, 12345);

            // Envoi du paquet vers la BD
            socketEnvoi.send(paquetEnvoye);
            Log.d("UDP", "paquetEnvoye pour acquittement");

            // Fermeture du socket d'envoi
            socketEnvoi.close();

            // Réouverture du socket mais avec le port de réception 6000
            socketReception = new DatagramSocket(6000);

            // Préparation de réception de la donnée d'acquittement
            DatagramPacket paquetRecu = new DatagramPacket(recep, recep.length);

            // Réception de l'accusé de réception de la BD
            Log.d("UDP", "en attente d'accusé de réception");
            socketReception.receive(paquetRecu);
            String donneeRecu = new String(paquetRecu.getData(), 0, paquetRecu.getLength()).trim();
            Log.d("UDP", "reception du message " + donneeRecu);

            if (!donneeRecu.isEmpty()) {
                save = true;
                Log.d("UDP", "donnée bien enregistrée dans la base de donnée");
            } else {
                Log.d("UDP", "donnée pas encore enregistrée dans la base de donnée");
            }

        } catch (IOException e) {
            Log.e("UDP", "Erreur lors de l'envoi/réception", e);
        } finally {
            if (socketEnvoi != null && !socketEnvoi.isClosed()) {
                socketEnvoi.close();
            }
            if (socketReception != null && !socketReception.isClosed()) {
                socketReception.close();
            }
        }

        return save;
    }

    //___________________________________________________________________________________________________________________________//
    //------METHODE : sendCloseApplication
    //------FONCTION : envoi vers le module de communication un message indiquant la fermeture de la session sur Medical_IOT
    //------RETOUR : aucun
    public void sendCloseApplication() throws UnknownHostException {
        // Initialisation de l'adresse d'envoi (adresse de la BD)
        InetAddress adresseIPEnvoi = InetAddress.getByName("192.168.0.9");

        DatagramSocket socketEnvoi = null;
        DatagramSocket socketReception = null;

        try {
            // Préparation du socket d'envoi
            socketEnvoi = new DatagramSocket();
            socketEnvoi.connect(adresseIPEnvoi, 12345);

            String message = "fermetureApp";
            byte[] envoi = message.getBytes();
            DatagramPacket paquetEnvoye = new DatagramPacket(envoi, envoi.length, adresseIPEnvoi, 12345);

            // Envoi du paquet vers la BD
            socketEnvoi.send(paquetEnvoye);
            Log.d("UDP", "la BD a était prévenu de la fermeture de l'application");

            // Fermeture du socket d'envoi
            socketEnvoi.close();
        } catch (IOException e) {
            Log.e("UDP", "Erreur lors de l'envoi/réception", e);
        } finally {
            if (socketEnvoi != null && !socketEnvoi.isClosed()) {
                socketEnvoi.close();
            }
        }
    }
}
