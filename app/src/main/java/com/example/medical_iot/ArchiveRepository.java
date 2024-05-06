package com.example.medical_iot;

import com.example.medical_iot.model.ArchiveDataModel;

import java.util.ArrayList;

//class pour stocker localement les données des archives d'une nuit

public class ArchiveRepository
{
    public static class Singleton //structure qui permet de ne pas créer à chaque fois l'objet
    {
        public static ArrayList<ArchiveDataModel> archiveList  = new ArrayList<>();
    }

    public void updateData(ArchiveDataModel newData) //met a jour les données
    {
        //Singleton.archiveList.clear();
        Singleton.archiveList.add(newData);
    }

    public void clearData() //efface l'ensemble des données après fermeture de l'app
    {
        //a implémenter plus tard
    }

    public void sendDataToDatabase() //permet d'envoyer les données à la BD
    {
        //a implémenter plus tard
    }
}
