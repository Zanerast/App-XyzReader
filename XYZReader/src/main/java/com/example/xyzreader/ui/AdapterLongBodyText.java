package com.example.xyzreader.ui;

import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.xyzreader.R;

import java.util.ArrayList;

public class AdapterLongBodyText extends RecyclerView.Adapter<AdapterLongBodyText.ViewHolder>{

    private ArrayList<String> longText;

    public AdapterLongBodyText(ArrayList<String> inputText)  {
        longText = inputText;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == 0) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_body_author, parent, false);
        } else if (viewType == 1){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_body_date, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_body_text, parent, false);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return longText.size();
    }

    @Override
    public int getItemViewType(int position) {

        if (position == 0) {
            return 0;
        } else if (position == 1){
            return 1;
        } else {
            return 2;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);

            textView = (TextView) itemView.findViewById(R.id.tv_long_text);
            textView.setTypeface(Typeface.createFromAsset(itemView.getResources().getAssets(), "Rosario-Regular.ttf"));
        }

        public void bind(int position) {
            textView.setText(longText.get(position));
        }
    }

    public class ViewHolderAuthor extends RecyclerView.ViewHolder {

        TextView textView;

        public ViewHolderAuthor(View itemView) {
            super(itemView);

            textView = (TextView) itemView.findViewById(R.id.tv_long_text);
            textView.setTypeface(Typeface.createFromAsset(itemView.getResources().getAssets(), "Rosario-Regular.ttf"));
        }

        public void bind(int position) {
            textView.setText(longText.get(position));
        }
    }

}
