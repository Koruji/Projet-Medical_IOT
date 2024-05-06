package com.example.medical_iot;

import android.os.StrictMode;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Objects;

//implementation d'une classe pour se connecter à la BD
//TO DO : rajouter les elements de connection + tester

public class ConnectionToMySQL
{
    protected static String baseDonnee  = "medicaliotv3";
    protected static String ip = "192.168.0.6";
    protected static String port = "3306";
    protected static String username = "application";
    protected static String password = "PH69+";

    public Connection getConnection()
    {
        Connection connect = null;
        try
        {
            Class.forName("com.mysql.cj.jdbc.Driver");
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            String connectionString = "jdbc:mysql://"+ip+ ":" + port + "/" + baseDonnee;
            Log.d("CONNEXION", "c'est ok j'essaye de me co a la BD");
            connect = DriverManager.getConnection(connectionString, username, password);
        }
        catch (Exception ex)
        {
            Log.e("ERROR", Objects.requireNonNull(ex.getMessage()));
            Log.d("CONNEXION", "la BD veut pas se log");
        }
        return connect;
    }
}
