package com.antoineraulin.numerama;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import static android.support.v4.content.ContextCompat.startActivity;

/**
 * Created by antoineraulin on 14/02/2017.
 */

public class MyViewHolder extends RecyclerView.ViewHolder{

    private TextView textViewView;
    private ImageView imageView;
    private TextView dateTextView;

    //itemView est la vue correspondante à 1 cellule
    public MyViewHolder(View itemView) {
        super(itemView);

        //c'est ici que l'on fait nos findView

        textViewView = (TextView) itemView.findViewById(R.id.text);
        dateTextView = (TextView) itemView.findViewById(R.id.time);
        imageView = (ImageView) itemView.findViewById(R.id.image);
    }

    //puis ajouter une fonction pour remplir la cellule en fonction d'un MyObject
    public void bind(final MyObject myObject){
        textViewView.setText(myObject.getText());
        dateTextView.setText("Il y a "+myObject.getDate()+" "+myObject.getUnite());
        Picasso.with(imageView.getContext()).load(myObject.getImageUrl()).centerCrop().fit().into(imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Context context = v.getContext();
                Intent intent=new Intent(v.getContext(),readArticle.class);
                intent.putExtra("link", myObject.getLink());
                context.startActivity(intent);
            }
        });
    }
}