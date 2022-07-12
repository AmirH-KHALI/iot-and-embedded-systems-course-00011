package com.example.embedded_app;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;


import java.util.ArrayList;

public class StarAdapter extends RecyclerView.Adapter<StarAdapter.MyViewHolder> {
    public ArrayList<Star> mGameList;
    private Activity mActivity;
    public View mView;
    EditText ra, dec;

    public StarAdapter(ArrayList<Star> gl,Activity a) {
        mGameList = gl;
        mActivity = a;
        ra = a.findViewById(R.id.RATEXT);
        dec = a.findViewById(R.id.DECTEXT);
    }

    @NonNull
    @Override
    public StarAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from( viewGroup.getContext() )
                .inflate( R.layout.user_item, viewGroup, false );
        return new MyViewHolder( view );
    }
    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView,RATextView,DECTextView;
        CardView all;
        public MyViewHolder(View view) {
            super( view );
            mView=view;
            nameTextView=view.findViewById( R.id.TextNameUser );
            RATextView=view.findViewById( R.id.RATEXT );
            DECTextView=view.findViewById( R.id.DECTEXT );
            all = view.findViewById(R.id.Card_Game_Item);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull StarAdapter.MyViewHolder holder, int position) {
        holder.nameTextView.setText(mGameList.get(position).ShowName());
        holder.RATextView.setText(mGameList.get(position).RA+"");
        holder.DECTextView.setText(mGameList.get(position).DEC+"");
        holder.all.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                float r = Float.parseFloat( holder.RATextView.getText()+"");
                float d = Float.parseFloat( holder.DECTextView.getText()+"");

                MainActivity.SetTextBoxes(r,d);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mGameList.size();
    }

}
