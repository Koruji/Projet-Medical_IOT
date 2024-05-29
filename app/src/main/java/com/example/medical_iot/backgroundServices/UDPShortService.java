package com.example.medical_iot.backgroundServices;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

//------------------SOURCES--------------------------//
//https://www.jmdoudoux.fr/java/dej/chap-net.htm
//http://tvaira.free.fr/dev/android/android-udp.html

//--------------------------------------------------//

public class UDPShortService
{
    //-------------------Serveur permettant la réception des données d'alerte + envoi des données id et acquittement---------------------------------------//
    //----envoi
    private DatagramSocket socketSendUDP; //socket pour envoi
    final static int portEnvoi = 12345; //port du destinataire
    private DatagramPacket paquetEnvoye;
    private InetAddress adresseIPEnvoi;

    //----réception d'accusé de réception (ouvert une fois après envoi acquittement)
    final static int portRecep = 5001; //port pour recevoir accusé de réception
    private String donneeRecu; //va recevoir les données des alertes reçus
    private DatagramSocket socketAckRecep; //correspond au socket ouvert de notre serveur pour l'écoute
    private DatagramPacket paquetRecu; //permet de recevoir le paquet reçu sur le port ouvert


    //------------------Méthodes de la classe----------------------------------------------------------------------//
    //----------envoi de la donnée des identifiants
    public void envoiId()
    {
        //a implémenter après tests
    }

    //----------envoi des données acquittements
    public boolean envoiAcquittement(String p_requeteSQL) throws UnknownHostException {
        adresseIPEnvoi = InetAddress.getByName("192.168.0.6");
        byte[] recep = new byte[1024];
        paquetRecu = new DatagramPacket(recep, recep.length);
        boolean save = false;

        try
        {
            socketSendUDP = new DatagramSocket();
            byte[] envoi = p_requeteSQL.getBytes();
            paquetEnvoye = new DatagramPacket(envoi, envoi.length, adresseIPEnvoi, portEnvoi);
            socketSendUDP.send(paquetEnvoye);

            //reception de l'accusé de réception
            socketAckRecep = new DatagramSocket(portRecep);
            socketAckRecep.receive(paquetRecu);
            if(paquetRecu.getData() != null)
            {
                save = true;
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return save;
    }
}
