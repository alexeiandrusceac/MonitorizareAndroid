package com.example.alexei.monitorizare.view;

import android.content.Context;
import android.provider.ContactsContract;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.alexei.monitorizare.R;
import com.example.alexei.monitorizare.database.inOutmodel.InOut;

import java.util.List;

public class InOutAdapter extends RecyclerView.Adapter<InOutAdapter.MyViewHolder> {

    private Context context;
    private List<InOut> dataList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView dateView;
        public TextView inputView;
        public TextView outputView;

        public MyViewHolder(View view) {
            super(view);
            dateView = view.findViewById(R.id.dateText);
            inputView = view.findViewById(R.id.inputText);
            outputView = view.findViewById(R.id.outputText);
        }
    }


    public InOutAdapter(Context context, List<InOut> dataList) {
        this.context = context;
        this.dataList = dataList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_monitorizare_main, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        InOut inOut = dataList.get(position);

        holder.dateView.setText(String.valueOf(inOut.DATE));

        // Displaying dot from HTML character code
        holder.inputView.setText(String.valueOf(inOut.INPUT));

        // Formatting and displaying timestamp
        holder.outputView.setText(String.valueOf(inOut.OUTPUT));
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

}
