package com.example.medical_iot.repository;
//--------------SOURCES--------------------//
//repository principe : https://www.youtube.com/watch?v=7cEqDV_c94k
//conversion kotlin en java : ChatGPT
//
//----------------------------------------//

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class WaitingDataRepository
{
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

    public synchronized void addAlerte(String p_alerte)
    {
        alertes.add(p_alerte);
        Log.d("ALERTES", "ajout de l'alerte dans la liste d'attente");
    }

    public synchronized List<String> getAlerte()
    {
        return new ArrayList<>(alertes);
    }

    public synchronized void suppAlerte(int p_num)
    {
        alertes.remove(p_num);
        Log.d("ALERTES", "suppression de l'alerte acquitté de la liste d'attente");
    }

    public synchronized int getNombreAlerte(){return alertes.size();}
}
