package com.example.medical_iot;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.medical_iot.model.ArchiveDataModel;
import com.example.medical_iot.repository.WaitingDataRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
//------------------------------------SOURCES--------------------------//
//https://www.youtube.com/watch?v=J7m5Gq-Kiqs --> boîte de dialogue
//https://openclassrooms.com/fr/courses/4568596-construisez-une-interface-utilisateur-flexible-et-adaptative/4568603-comprenez-les-differents-moyens-de-naviguer-sur-une-application
//https://www.youtube.com/watch?v=IR4OSBb__iQ --> liaison entre les activités
//--------------------------------------------------------------------//

public class AckPage extends AppCompatActivity {

    //_________________________________________________ATTRIBUTS_______________________________________________________//
    //----données de l'alerte
    private TextView alertZone; //zone de l'alerte en cours
    private TextView numberOfRoom; //numéro de la chambre concernée par l'alerte

    //----données d'alerte et d'acquittement
    private ArchiveDataModel archive; //objet contenant toutes les données à transmettre
    public String donneeRecuAlerte; //donnee transféré de la page menu vers la page acquittement

    //----données de l'activité AckPage
    private CheckBox checkAckMovement; //zone de validation de déplacement
    private CheckBox checkAckConfirmation; //zone de validation d'acquittement
    private CardView confirmationAckZone; //représente la zone contenant la validation de déplacement et d'acquittement
    private LinearLayout zoneComment; //layout de la zone de commentaire

    //----boîte de dialogue
    private Dialog ackConfirmationDialog; //boite de dialogue
    private Button confirmationACK; //bouton OUI de la boite de dialogue
    private Button cancelACK; //bouton NON de la boite de dialogue

    //_____________________________________________EXECUTION PRINCIPALE__________________________________________________//
    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //-----------------------Création de l'activité------------------------------------------------------------------//
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ack_page);

        Log.d("AckPage", "onCreate: page ack cree");

        //-----------------------Mise en place des informations de l'alerte-----------------------------------------------//
        alertData();

        //-----------------------Mécanique de validation------------------------------------------------------------------//
        //initialisation des attributs
        checkAckMovement = findViewById(R.id.ack_checkbox_presence);
        confirmationAckZone = findViewById(R.id.ack_checkbox_acceptance_cardview);
        zoneComment = findViewById(R.id.ack_comment_zone);
        checkAckConfirmation = findViewById(R.id.ack_checkbox_acceptance);

        //tant que le déplacement n'est pas validé, les autres éléments de la page sont inacessibles
        checkAckMovement.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked) {
                //quand le déplacement est validé, on a accès à l'autre partie de la page d'acquittement
                confirmationAckZone.setVisibility(View.VISIBLE);
                zoneComment.setVisibility(View.VISIBLE);
            }
        });

        //quand on clique sur la validation d'acquittement une boite de dialogue est créée
        checkAckConfirmation.setOnClickListener(view -> {
            boolean checked = ((CheckBox) view).isChecked();
            //on affiche la boite de dialogue
            if (checked) {
                Log.d("AckPage", "onClick: je montre la boite de dialogue");
                createDialogBox();
            }

            //si l'on clique sur le NON de la boite de dialogue
            cancelACK.setOnClickListener(v -> {
                Log.d("AckPage", "onClick: NON");

                //on décoche la case de validation d'acquittement et on ferme la boîte de dialogue
                checkAckConfirmation.setChecked(false);
                ackConfirmationDialog.cancel();
            });

            //si l'on clique sur le OUI de la boite de dialogue
            confirmationACK.setOnClickListener(v -> {
                //fermeture de la boîte de dialogue
                Log.d("AckPage", "onClick: OUI");
                ackConfirmationDialog.dismiss();

                //récupération de toutes les données liées à l'acquittement
                dataToSend();

                //envoi des données vers le mainActivity pour un archivage
                Intent transfertToHomePage = new Intent(AckPage.this, MainActivity.class);
                transfertToHomePage.putExtra("archive", archive);
                transfertToHomePage.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(transfertToHomePage);
                Log.d("MainActivity", "onClick: retour au menu");

                //on supprime l'alerte de la liste d'attente
                WaitingDataRepository.getInstance().suppAlerte(0);
                Log.d("CONNEXION", WaitingDataRepository.getInstance().getAlerte().toString());

                //fermeture de l'activité AckPage
                finish();
                Log.d("AckPage", "onClick: fermeture de l'activité");
            });
        });


        //------------------------Paramétrage du bouton retour-----------------------------------------------------------------//
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                //ajout de rien car on veut que le bouton retour soit désactivé lors de l'acquittement
                Log.d("AckPage", "handleOnBackPressed: interdit de sortir de l'application par le bouton retour");
            }
        });
    }

    //___________________________________________________________________________________________________________________________//
    //------METHODE : alertData
    //------FONCTION : récupère les données de l'alerte de l'activité MainActivity et la traite
    //------RETOUR : aucun
    private void alertData() {
        archive = new ArchiveDataModel();
        Intent intent = getIntent();

        if (intent != null) {
            donneeRecuAlerte = intent.getStringExtra("donneeAlerte");
        }
        String[] donnees = donneeRecuAlerte.split(" ");
        numberOfRoom = findViewById(R.id.number_room_concerned);
        alertZone = findViewById(R.id.zone_room_concerned);

        //récupération de toutes les données de l'alerte nécessaires
        archive.setID_chambre(Integer.parseInt(donnees[0]));
        numberOfRoom.setText(String.valueOf(archive.getID_chambre()));
        alertZone.setText(donnees[2]);
    }

    //___________________________________________________________________________________________________________________________//
    //------METHODE : createDialogBox
    //------FONCTION : créer une boite de dialogue a choix après un clic sur "Valider l'acquittement"
    //------RETOUR : aucun
    @SuppressLint("UseCompatLoadingForDrawables")
    private void createDialogBox() {
        //instanciation de la boite de dialogue
        ackConfirmationDialog = new Dialog(this);

        //on initialise le contenu de la boite de dialogue avec notre layout xml
        ackConfirmationDialog.setContentView(R.layout.custom_confirmation_ack_box);

        //on récupère les 2 boutons de notre boite de dialogue + la zone de commentaire
        confirmationACK = ackConfirmationDialog.findViewById(R.id.ack_confirmation_button_yes);
        cancelACK = ackConfirmationDialog.findViewById(R.id.ack_confirmation_button_no);
        EditText ackComment = findViewById(R.id.comment_zone_ack);

        //s'il n'y pas de commentaires dans la zone commentaire on change de message pour la boîte de dialogue
        //texte de la boite de dialogue
        /*TextView boxText = findViewById(R.id.ack_confirmation_popup_text);
        if (TextUtils.isEmpty(ackComment.getText())) { //changement du texte
            boxText.setText(R.string.ack_confirmation_without_comment);
        }*/

        //on indique la dialogBox doit apparaitre juste au dessus de l'activité AckPage (vu groupé)
        ackConfirmationDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ackConfirmationDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.custom_confirmation_ack_background));
        }

        //on interdit à l'application de fermer la boite de dialogue en cas de clique en dehors de la zone
        ackConfirmationDialog.setCancelable(false);

        //on montre la boite de dialogue
        ackConfirmationDialog.show();
    }

    //___________________________________________________________________________________________________________________________//
    //------METHODE : dataToSend
    //------FONCTION : récupère les données a stocker dans l'attribut "archive"
    //------RETOUR : void
    public void dataToSend() {
        //récupération de la zone commentaire
        //zone de commentaire (récupération données)
        EditText ackComment = findViewById(R.id.comment_zone_ack);
        String comment = ackComment.getText().toString().trim();
        archive.setEspace_commentaire(comment);

        //récupération de la valeur de déplacement et d'acquittement
        archive.setDeplacement_surveillant(checkAckMovement.isChecked());
        archive.setAcquittement_surveillant(checkAckMovement.isChecked());

        //recuperation de l'heure
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.FRANCE);
        String hour = dateFormat.format(date);
        archive.setHeure_acquittement(hour);
    }
}