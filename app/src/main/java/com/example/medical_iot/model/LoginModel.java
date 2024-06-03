package com.example.medical_iot.model;

import android.content.Context;
import android.content.Intent;

import com.example.medical_iot.LoginSession;
import com.example.medical_iot.backgroundServices.UDPShortService;

public class LoginModel
{
    //_________________________________________________ATTRIBUTS_______________________________________________________//
    private static LoginModel instance;
    private String login_surveillant;
    private String mdp_surveillant;
    private Context context;

    //--------------------------------------------------METHODES-------------------------------------------------------//
    public static synchronized LoginModel getInstance() {
        if (instance == null) {
            instance = new LoginModel();
        }
        return instance;
    }

    public void setLoginModel(String p_login, String p_mdp)
    {
        this.login_surveillant = p_login;
        this.mdp_surveillant = p_mdp;
    }

    public String getLogin_surveillant() {
        return login_surveillant;
    }

    public String getMdp_surveillant() {
        return mdp_surveillant;
    }
}
