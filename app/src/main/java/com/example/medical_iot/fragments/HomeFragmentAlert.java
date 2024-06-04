package com.example.medical_iot.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.medical_iot.R;

//--------------------------SOURCES--------------------------------------------------//
//https://openclassrooms.com/fr/courses/8150246-developpez-votre-premiere-application-android?archived-source=4517166
//https://youtube.com/playlist?list=PLMS9Cy4Enq5JnwAxe6Ao74qSTxxXjiw7N&si=yZT4TEG9P1kJ26ob
//----------------------------------------------------------------------------------//

public class HomeFragmentAlert extends Fragment
{
    //NOTE : ceci est un fragment qui contient la vue sur les alertes visuelles (bannière rouge)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.item_alert_notification, container,false);
    }
}
