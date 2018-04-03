package com.example.alexei.monitorizare.view;

import android.content.Context;
import android.os.Environment;

import com.example.alexei.monitorizare.database.DataBaseAccess;
import com.example.alexei.monitorizare.database.DataBaseHelper;
import com.example.alexei.monitorizare.database.inOutmodel.InOut;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TableHelper {

    private final boolean fromExternalSource = false;
    Context mContext;
    private String[] headers ={"Nr.crt","Data","Primit","Cheltuit","Final"};
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

        inOutArray = new String[listInOutData.size()][5];

        for (int i=0;i<listInOutData.size();i++) {

            inOutData=listInOutData.get(i);

            inOutArray[i][0]=String.valueOf(inOutData.ID);
            inOutArray[i][1]=String.valueOf(inOutData.DATE);
            inOutArray[i][2]=String.valueOf(inOutData.INPUT);
            inOutArray[i][3]=String.valueOf(inOutData.OUTPUT);
            inOutArray[i][4]=String.valueOf(inOutData.DIFFERENCE);

        }

        return inOutArray;
    }



}
