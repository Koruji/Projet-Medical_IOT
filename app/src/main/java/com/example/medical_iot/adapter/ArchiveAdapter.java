package com.example.medical_iot.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medical_iot.ArchiveRepository;
import com.example.medical_iot.R;
import com.example.medical_iot.model.ArchiveDataModel;

import java.util.ArrayList;
import java.util.List;


//class sur la création et les interactions des icones d'archive
public class ArchiveAdapter extends RecyclerView.Adapter<ArchiveAdapter.ViewHolder>
{

    private final ArchiveRepository archiveDataList;

    public ArchiveAdapter(ArchiveRepository archiveDataListImplement) {
        this.archiveDataList = archiveDataListImplement;
    }

    @NonNull
    @Override
    //permet d'injecter un composant
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View viewArchive = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_archive_icon, parent, false);

        return new ViewHolder(viewArchive);
    }

    @Override
    //met à jour l'état de chaque composants qui vont être injectés
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        if(ArchiveRepository.Singleton.archiveList != null)
        {
            //recuperation des informations nécessaires aux archives
            ArchiveDataModel currentArchive = ArchiveRepository.Singleton.archiveList.get(position);

            //application des données récupérées
            holder.ID_chambre.setText(String.valueOf(currentArchive.getID_chambre()));
            holder.heure_acquittement.setText(currentArchive.getHeure_acquittement());
        }
    }

    @Override
    //nombre d'item que l'on veut afficher de manière dynamique
    public int getItemCount()
    {
        return ArchiveRepository.Singleton.archiveList.size();
    }

    //on y range tous les composants à contrôler
    static class ViewHolder extends RecyclerView.ViewHolder //vu qu'il s'agit d'un composant maître d'Android (contrôle des composants par class + Android)
    {
        public CardView iconArchive;
        public TextView ID_chambre;
        public TextView heure_acquittement;

        public ViewHolder(View view) { //peut être changer pour mettre le texte et non la boite
            super(view);
            iconArchive = view.findViewById(R.id.background_icon_history);
            ID_chambre = view.findViewById(R.id.data_room_concerned);
            heure_acquittement = view.findViewById(R.id.data_room_concerned_hour);
        }
    }
}
