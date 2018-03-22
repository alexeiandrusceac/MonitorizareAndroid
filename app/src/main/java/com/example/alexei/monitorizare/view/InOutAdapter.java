package com.example.alexei.monitorizare.view;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.example.alexei.monitorizare.R;
import com.example.alexei.monitorizare.database.inOutmodel.InOut;

import org.w3c.dom.Text;

import java.util.List;

/**
 * Created by alexei.andrusceac on 22.03.2018.
 */

public class InOutAdapter extends RecyclerView.Adapter<InOutAdapter.MyViewHolder> {

    private Context context;
    private List<InOut> listOfData;


    public class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView id;
        public EditText date;
        public EditText input;
        public EditText output;
        public TextView difference;
        public TextView inputTotal;
        public TextView outputTotal;

        public MyViewHolder(View view) {
            super(view);
            id = view.findViewById(R.id.idText);
            date = view.findViewById(R.id.dateText);
            input = view.findViewById(R.id.inputText);
            output = view.findViewById(R.id.outputText);
            difference = view.findViewById(R.id.differenceText);
        }
    }
        public InOutAdapter(Context context,List<InOut> listOfData){
            this.context = context;
            this.listOfData = listOfData;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType)
        {
            View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.data_activity_monitorizare,viewGroup,false);
            return new MyViewHolder(itemView);
        }
        @Override
        public void onBindViewHolder(MyViewHolder myViewHolder, int position)
        {
            InOut data = listOfData.get(position);
            myViewHolder.id.setText(data.getId());
            myViewHolder.date.setText(data.getDate());
            myViewHolder.input.setText(data.getInput());
            myViewHolder.output.setText(data.getOutput());
            myViewHolder.difference.setText(data.getDifference());
            myViewHolder.inputTotal.setText(data.getInputTotal());
            myViewHolder.outputTotal.setText(data.getOutputTotal());

        }
        @Override
        public int getItemCount (){
            return listOfData.size();

        }

    }

