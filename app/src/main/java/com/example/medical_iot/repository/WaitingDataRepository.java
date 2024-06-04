package com.example.medical_iot.repository;
//--------------SOURCES--------------------//
//repository principe : https://www.youtube.com/watch?v=7cEqDV_c94k
//conversion kotlin en java : ChatGPT
//----------------------------------------//

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class WaitingDataRepository
{
    //NOTE : le but de cette classe est de permettre un stockage en arrière plan de toutes les alertes non acquittées
    //------- et d'utiliser cette liste pour traiter ces alertes dans l'ordre d'arrivée

    //-------------------------------------ATTRIBUTS---------------------------------------------------//
    private static WaitingDataRepository instance;
    private final List<String> alertes = new ArrayList<>();

    //----------------------------------CONSTRUCTEUR---------------------------------------------------//
    private WaitingDataRepository() {}

    //----------------------------------METHODES-------------------------------------------------------//
    public static synchronized WaitingDataRepository getInstance()
    {
        if (instance == null)
        {
            instance = new WaitingDataRepository();
        }
        return instance;
    }

    //___________________________________________________________________________________________________________________________//
    //------METHODE : addAlerte
    //------FONCTION : permet d'ajouter une alerte reçue à la liste d'attente
    //------RETOUR : aucun
    public synchronized void addAlerte(String p_alerte)
    {
        alertes.add(p_alerte);
        Log.d("ALERTES", "ajout de l'alerte dans la liste d'attente");
    }

    //___________________________________________________________________________________________________________________________//
    //------METHODE : getAlerte
    //------FONCTION : retourne la liste entière des alertes en attente de traitement
    //------RETOUR : liste de chaine de caractère
    public synchronized List<String> getAlerte()
    {
        return new ArrayList<>(alertes);
    }

    //___________________________________________________________________________________________________________________________//
    //------METHODE : suppAlerte
    //------FONCTION : supprime une alerte non traité au rang p_num
    //------RETOUR : aucun
    public synchronized void suppAlerte(int p_num)
    {
        alertes.remove(p_num);
        Log.d("ALERTES", "suppression de l'alerte acquitté de la liste d'attente");
    }

    //___________________________________________________________________________________________________________________________//
    //------METHODE : getNombreAlerte
    //------FONCTION : retourne le nombre d'alertes en attente de traitement
    //------RETOUR : entier
    public synchronized int getNombreAlerte(){return alertes.size();}
}
