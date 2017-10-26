package dev.antoineraulin.numerama;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;


/**
 * Created by antoineraulin on 14/02/2017.
 */

public class MyViewHolder extends RecyclerView.ViewHolder{

    private TextView textViewView;
    private ImageView imageView;
    private TextView dateTextView;
    private TextView descriptionTextView;
    private LinearLayout descriptionLinearLayout;
    private TextView catTextView;

    public MyViewHolder(View itemView) {
        super(itemView);

        textViewView = (TextView) itemView.findViewById(R.id.text);
        dateTextView = (TextView) itemView.findViewById(R.id.time);
        imageView = (ImageView) itemView.findViewById(R.id.image);
        descriptionTextView = (TextView) itemView.findViewById(R.id.description);
        descriptionLinearLayout = (LinearLayout) itemView.findViewById(R.id.descLa);
        catTextView = (TextView) itemView.findViewById(R.id.cat);

    }

    public void bind(final MyObject myObject){
        textViewView.setText(myObject.getText());
        catTextView.setText(myObject.getCat());
        descriptionTextView.setText(myObject.getDescription());
        dateTextView.setText(", Il y a "+myObject.getDate()+" "+myObject.getUnite());
        Log.e("picasso", "url : '"+myObject.getImageUrl()+"'");
        Ion.with(imageView)
                .placeholder(R.mipmap.nblack)
                .load(myObject.getImageUrl());
        textViewView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Context context = v.getContext();
                Intent intent=new Intent(v.getContext(),ArticleActivity.class);
                intent.putExtra("link", myObject.getLink());
                intent.putExtra("title", myObject.getText());
                intent.putExtra("image", myObject.getImageUrl());
                context.startActivity(intent);
            }
        });
        descriptionTextView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Context context = v.getContext();
                Intent intent=new Intent(v.getContext(),ArticleActivity.class);
                intent.putExtra("link", myObject.getLink());
                intent.putExtra("title", myObject.getText());
                intent.putExtra("image", myObject.getImageUrl());
                context.startActivity(intent);
            }
        });
        imageView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Context context = v.getContext();
                Intent intent=new Intent(v.getContext(),ArticleActivity.class);
                intent.putExtra("link", myObject.getLink());
                intent.putExtra("title", myObject.getText());
                intent.putExtra("image", myObject.getImageUrl());
                context.startActivity(intent);
            }
        });
    }


}