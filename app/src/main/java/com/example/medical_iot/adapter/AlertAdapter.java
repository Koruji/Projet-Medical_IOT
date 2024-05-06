package com.example.medical_iot.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medical_iot.AckPage;
import com.example.medical_iot.MainActivity;
import com.example.medical_iot.R;

//class sur la création et les interactions des alertes sur la page d'accueil
public class AlertAdapter extends RecyclerView.Adapter<AlertAdapter.ViewHolder>
{

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View viewAlert = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_alert_notification, parent, false);

        return new ViewHolder(viewAlert);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        //
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    class ViewHolder extends RecyclerView.ViewHolder
    {
        public TextView iconAlert;

        public ViewHolder(View view)
        {
            super(view);
            iconAlert=view.findViewById(R.id.home_alert_begin_text);
        }
    }
}
