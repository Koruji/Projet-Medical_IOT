package com.example.medical_iot.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medical_iot.repository.ArchiveRepository;
import com.example.medical_iot.R;
import com.example.medical_iot.adapter.ArchiveAdapter;
import com.example.medical_iot.model.ArchiveDataModel;

public class HomeFragment extends Fragment
{
    //on créé le repository
    private ArchiveRepository repository = new ArchiveRepository();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        //permet d'injecter sur le layout de la page home le fragment avec le choix de l'archive
        View viewArchive = inflater.inflate(R.layout.fragment_home, container, false);

        //on fait une liste d'archive
        ArchiveDataModel newArchiveElement;

        //------------recuperation de la donnee a stocker dans la liste
        Bundle bundleFromMainActivity = getArguments();
        if (bundleFromMainActivity != null)
        {
            newArchiveElement = bundleFromMainActivity.getParcelable("archive");
            repository.updateData(newArchiveElement);
        }

        //on récupère le recycler view associé au patron d'une archive dans fragment_home
        RecyclerView archiveRecyclerView = viewArchive.findViewById(R.id.recycler_icon_archive);
        archiveRecyclerView.setAdapter(new ArchiveAdapter(repository));

        return viewArchive;
    }

    public ArchiveRepository getRepository()
    {
        return repository;
    }

}
