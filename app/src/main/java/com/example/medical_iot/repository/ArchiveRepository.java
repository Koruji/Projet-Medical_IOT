package com.example.medical_iot.repository;

import com.example.medical_iot.model.ArchiveDataModel;

import java.util.ArrayList;

//classe pour stocker localement les données des archives d'une nuit

public class ArchiveRepository
{
    //NOTE : le but de cette classe est de permettre un stockage en arrière plan de toutes les alertes déjà acquittées
    //------- et d'utiliser cette liste pour pouvoir les afficher sur le menu

    //-----------------------------------------METHODES--------------------------------------------------------------------------//
    //___________________________________________________________________________________________________________________________//
    //------METHODE : Singleton
    //------FONCTION : instancie une liste d'objet de type ArchiveDataModel
    //------RETOUR : aucun
    public static class Singleton //structure qui permet de ne pas créer à chaque fois l'objet
    {
        public static ArrayList<ArchiveDataModel> archiveList  = new ArrayList<>();
    }

    //___________________________________________________________________________________________________________________________//
    //------METHODE : updateData
    //------FONCTION : permet d'ajouter un objet de type ArchiveDataModel a la liste instancié
    //------RETOUR : aucun
    public void updateData(ArchiveDataModel newData) //met a jour les données
    {
        Singleton.archiveList.add(newData);
    }

}
