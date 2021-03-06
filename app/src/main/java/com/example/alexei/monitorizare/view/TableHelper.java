package com.example.alexei.monitorizare.view;

import android.content.Context;

import com.example.alexei.monitorizare.database.inOutmodel.InOut;

import java.util.List;

public class TableHelper {

    Context mContext;
    private String[] headers ={"Data","Primit","Cheltuit"};
    private String [][]  inOutArray;

    public TableHelper(Context context){
        this.mContext = context;

    }
    public String[] getHeaders()
    {
        return headers;
    }

    public String[][] getData(List<InOut> listInOutData)
    {
        InOut inOutData;

        inOutArray = new String[listInOutData.size()][3];

        for (int i=0;i<listInOutData.size();i++) {

            inOutData=listInOutData.get(i);

            //inOutArray[i][0]=String.valueOf(inOutData.ID);
            inOutArray[i][0]=String.valueOf(inOutData.DATE);
            inOutArray[i][1]=String.valueOf(inOutData.INPUT);
            inOutArray[i][2]=String.valueOf(inOutData.OUTPUT);
        }

        return inOutArray;
    }



}
