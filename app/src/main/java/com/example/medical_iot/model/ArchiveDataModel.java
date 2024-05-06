package com.example.medical_iot.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArchiveDataModel implements Parcelable
{
    //mes variables qui correspondent aux attributs présents dans la BD
    private final int ID_chambre;
    private boolean deplacement_surveillant;
    private boolean acquittement_surveillant;
    private String heure_acquittement;
    private String nom_surveillant;
    private String prenom_surveillant;
    private String espace_commentaire;

    //constructeur par défaut (en attendant pas de constructeur avec param j'attend la BD)

    public ArchiveDataModel() {
        this.ID_chambre = 0;
        this.deplacement_surveillant = false;
        this.acquittement_surveillant = false;
        this.heure_acquittement = "00:00";
        this.nom_surveillant = " ";
        this.prenom_surveillant = " ";
        this.espace_commentaire = " ";
    }

    //constructeur avec paramètre (EN ATTENTE)
    public ArchiveDataModel(int ID_chambre, boolean deplacement_surveillant, boolean acquittement_surveillant, String heure_acquittement, String nom_surveillant, String prenom_surveillant, String espace_commentaire) {
        this.ID_chambre = ID_chambre;
        this.deplacement_surveillant = deplacement_surveillant;
        this.acquittement_surveillant = acquittement_surveillant;
        this.nom_surveillant = nom_surveillant;
        this.prenom_surveillant = prenom_surveillant;
        this.espace_commentaire = espace_commentaire;

        if(validateHourFormat(heure_acquittement))
        {
            this.heure_acquittement = heure_acquittement;
        }
        else
        {
            throw new IllegalArgumentException("format d'horaire illégal");
        }
    }

    private boolean validateHourFormat(String hour)
    {
        // Expression régulière pour vérifier le format HH:mm
        String heurePattern = "^([01]\\d|2[0-3]):([0-5]\\d)$";
        Pattern pattern = Pattern.compile(heurePattern);
        Matcher matcher = pattern.matcher(hour);
        return matcher.matches();
    }

    //----------Implémentation Parcelable--LECTURE------------------------------------------------//
    protected ArchiveDataModel(Parcel in) {
        ID_chambre = in.readInt();
        deplacement_surveillant = in.readByte() != 0;
        acquittement_surveillant = in.readByte() != 0;
        heure_acquittement = in.readString();
        nom_surveillant = in.readString();
        prenom_surveillant = in.readString();
        espace_commentaire = in.readString();
    }

    public static final Creator<ArchiveDataModel> CREATOR = new Creator<ArchiveDataModel>() {
        @Override
        public ArchiveDataModel createFromParcel(Parcel in) {
            return new ArchiveDataModel(in);
        }

        @Override
        public ArchiveDataModel[] newArray(int size) {
            return new ArchiveDataModel[size];
        }
    };

    //---------------Ensemble de getter et setter-------------------//
    public int getID_chambre()
    {
        return ID_chambre;
    }

    public boolean getDeplacement_surveillant()
    {
        return deplacement_surveillant;
    }

    public void setDeplacement_surveillant(boolean deplacement_surveillant)
    {
        this.deplacement_surveillant = deplacement_surveillant;
    }

    public boolean getAcquittement_surveillant()
    {
        return acquittement_surveillant;
    }

    public void setAcquittement_surveillant(boolean acquittement_surveillant)
    {
        this.acquittement_surveillant = acquittement_surveillant;
    }

    public String getHeure_acquittement()
    {
        return heure_acquittement;
    }

    public void setHeure_acquittement(String heure_acquittement)
    {
        if(validateHourFormat(heure_acquittement))
        {
            this.heure_acquittement = heure_acquittement;
        }
        else
        {
            throw new IllegalArgumentException("format d'horaire illégal");
        }
    }

    public String getNom_surveillant()
    {
        return nom_surveillant;
    }

    public void setNom_surveillant(String nom_surveillant)
    {
        this.nom_surveillant = nom_surveillant;
    }

    public String getPrenom_surveillant()
    {
        return prenom_surveillant;
    }

    public void setPrenom_surveillant(String prenom_surveillant)
    {
        this.prenom_surveillant = prenom_surveillant;
    }

    public String getEspace_commentaire()
    {
        return espace_commentaire;
    }

    public void setEspace_commentaire(String espace_commentaire)
    {
        this.espace_commentaire = espace_commentaire;
    }

    //----------Implémentation Parcelable--ECRITURE------------------------------------------------//
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i)
    {
        parcel.writeInt(ID_chambre);
        parcel.writeInt(deplacement_surveillant ? 1 : 0);
        parcel.writeInt(acquittement_surveillant ? 1 : 0);
        parcel.writeString(heure_acquittement);
        parcel.writeString(nom_surveillant);
        parcel.writeString(prenom_surveillant);
        parcel.writeString(espace_commentaire);
    }
}

