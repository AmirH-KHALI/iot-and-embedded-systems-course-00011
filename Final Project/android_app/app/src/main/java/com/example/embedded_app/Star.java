package com.example.embedded_app;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class Star{
    public int starID;
    public float RA;
    public float DEC;
    public String spectrum;
    public Star(int id, float r , float d,String s){
        starID = id;
        RA = r;
        DEC = d;
        spectrum = s;
    }
    public String ShowName(){
        return starID + ". " + spectrum;
    }
}
