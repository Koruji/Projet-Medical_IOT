package com.example.medical_iot;


import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.medical_iot.model.ArchiveDataModel;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class AckPage extends AppCompatActivity {

    private ArchiveDataModel archive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //-----------------------CREATION DE L ACTIVITE--------------------------------------------------//
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ack_page);

        Log.d("AckPage", "onCreate: page ack cree");

        //---------------------DECLARATION + PARAMETRAGE VARIABLES--------------------------------------//
        archive = new ArchiveDataModel();
        Dialog ackConfirmationDialog = new Dialog(this); //instanciation de la boite de dialogue

        CheckBox checkAckMovement = findViewById(R.id.ack_checkbox_presence);
        CheckBox checkAckConfirmation = findViewById(R.id.ack_checkbox_acceptance);
        EditText ackComment = findViewById(R.id.comment_zone_ack);
        //EditText ackName = findViewById(R.id.identification_zone_ack_name);
        //EditText ackSurname = findViewById(R.id.identification_zone_ack_surname);
        TextView numberOfRoom = findViewById(R.id.data_room_concerned);

        LinearLayout zoneComment = findViewById(R.id.ack_comment_zone);
        //LinearLayout identificationZone = findViewById(R.id.ack_identification);
        CardView confirmationAckZone = findViewById(R.id.ack_checkbox_acceptance_cardview);

        //----------------------MISE EN PLACE DES INFORMATIONS D ALERTE RECUP----------------------------//
        numberOfRoom.setText(String.valueOf(archive.getID_chambre())); //fonctionne
        //a remplacer par ce que l'on recoit avec la BD

        //------------------------PARAMETRAGE BOUTON BACK------------------------------------------------//
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                //ajout de rien car on veut que le bouton retour soit désactivé lors de l'acquittement
                Log.d("AckPage", "handleOnBackPressed: tu peux pas");
            }
        });

        //----------------------MECANIQUE DE VALIDATION (ACQUITTEMENT)-----------------------------------//
        //temps que le déplacement n'est pas validé, les autres éléments de la page sont inacessibles
        checkAckMovement.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    confirmationAckZone.setVisibility(View.VISIBLE);
                    zoneComment.setVisibility(View.VISIBLE);
                    //identificationZone.setVisibility(View.VISIBLE);

                }
            }
        });

        //clic sur le bouton d'acquittement
        checkAckConfirmation.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onClick(View view) {
                //on initialise le contenu de la boite de dialogue avec notre layout xml
                ackConfirmationDialog.setContentView(R.layout.custom_confirmation_ack_box);

                //on indique que cela doit apparaitre juste au dessus de l'activité donc page d'acquittement (vu groupé)
                ackConfirmationDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                ackConfirmationDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.custom_confirmation_ack_background));

                //on interdit à l'app de fermer la boite de dialogue en cas de clic en dehors de la zone
                ackConfirmationDialog.setCancelable(false);

                //on recupere les 2 boutons de notre boite de dialogue + le texte d'affiché
                Button confirmationACK = ackConfirmationDialog.findViewById(R.id.ack_confirmation_button_yes);
                Button cancelACK = ackConfirmationDialog.findViewById(R.id.ack_confirmation_button_no);
                TextView ackDialogText = ackConfirmationDialog.findViewById(R.id.ack_confirmation_popup_text);

                //---------------VERIFICATION DE RENSEIGNEMENT------------------------------------------------------//
                if (TextUtils.isEmpty(ackComment.getText())) { //changement du texte
                    ackDialogText.setText(R.string.ack_confirmation_without_comment);
                }

                /*if (TextUtils.isEmpty(ackName.getText())) {
                        checkAckConfirmation.setChecked(false);
                        ackName.setError("A remplir");
                        ackName.requestFocus();
                        return;
                }

                if(TextUtils.isEmpty(ackSurname.getText())) {
                    checkAckConfirmation.setChecked(false);
                    ackSurname.setError("A remplir");
                    ackSurname.requestFocus();
                    return;
                }

                if (!TextUtils.isEmpty(ackName.getText()) || !TextUtils.isEmpty(ackSurname.getText()))*/
                //-------------------AFFICHAGE BOITE DIALOGUE-------------------------------------------------------//
                    boolean checked = ((CheckBox) view).isChecked();
                    if (checked) {
                        Log.d("AckPage", "onClick: je montre la boite de dialogue");
                        ackConfirmationDialog.show();
                    }

                //-----------------------CONDITIONS LIES A LA BOITE DE DIALOGUE-------------------------------------------//
                //on initialise les actions qui leurs sont associées (ANCIENNE SYNTAXE)
                //confirmationACK.setOnClickListener(new View.OnClickListener() {
                //                    @Override
                //                    public void onClick(View view) {
                //

                cancelACK.setOnClickListener(v -> {
                    Log.d("AckPage", "onClick: ta clique sur NON");
                    checkAckConfirmation.setChecked(false);
                    ackConfirmationDialog.cancel();
                });

                confirmationACK.setOnClickListener(v -> {
                    //--------------ENSEMBLE DES DONNEES RECUPEREES (archive) APRES FERMETURE BOITE DIALOGUE------------//
                    String comment = ackComment.getText().toString().trim();
                    //String name = ackName.getText().toString().trim();
                    //String surname = ackSurname.getText().toString().trim();

                    archive.setDeplacement_surveillant(checkAckMovement.isChecked());
                    archive.setAcquittement_surveillant(checkAckMovement.isChecked());
                    //archive.setNom_surveillant(name);
                    //archive.setPrenom_surveillant(surname);
                    archive.setEspace_commentaire(comment);

                    //recuperation de l'heure
                    Date date = new Date();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.FRANCE);
                    String hour = dateFormat.format(date);
                    archive.setHeure_acquittement(hour);

                    //preparation du transfert de donnees a la fermeture de l'activité
                    Intent transfertToHomePage = new Intent(AckPage.this, MainActivity.class);
                    transfertToHomePage.putExtra("archive", archive);

                    //---------------------------------------------------------------------------------------------------//
                    Log.d("AckPage", "onClick: ta clique sur OUI");
                    ackConfirmationDialog.dismiss(); //ferme la boite de dialogue

                    transfertToHomePage.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(transfertToHomePage);
                    Log.d("MainActivity", "onClick: et que retour au menu");

                    finish(); //fermer l'activité actuelle pour éviter de l'avoir en arrière plan
                    Log.d("AckPage", "onClick: si t la c'est que la page s'est fermé");
                });


            }
        });
    };
}