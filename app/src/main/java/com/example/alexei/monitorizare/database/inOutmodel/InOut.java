package com.example.alexei.monitorizare.database.inOutmodel;

/**
 * Created by alexei.andrusceac on 21.03.2018.
 */

public class InOut  {
    public static final String INOUTTABLE = "InOutTable";

    //InOutTable
    public static final String ID = "input_ID";
    public static final String DATE = "Date";
    public static final String INPUT = "Input";
    public static final String OUTPUT = "Output";
    public static final String DIFFERENCE = "Difference";
    public static final String INPUTTOTAL = "InputTotal";
    public static final String OUTPUTTOTAL = "OutputTotal";

    private int id;
    private String date;
    private int input;
    private int output;
    private int difference;
    private int inputTotal;
    private int outputTotal;

    public static final String CREATE_TABLE =
            "create table if not exists " + INOUTTABLE + " ( " + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + DATE + " text not null, "
            + INPUT + " int, "
            + OUTPUT + " int, "
            + DIFFERENCE + "text, "
            + INPUTTOTAL + " text, "
            + OUTPUTTOTAL + " text);";

public InOut(){}
public InOut(int id, String date,int input,int output, int difference,int inputTotal,int outputTotal)
{
        this.id = id;
        this.date = date;
        this.input= input;
        this.output = output;
        this.difference = difference;
        this.inputTotal = inputTotal;
        this.outputTotal = outputTotal;

}
public int getId(){return id;}
public String getDate(){return date;}
public int getInput(){return input;}
public int getOutput(){return output;}
public int getDifference(){return difference;}
public int getInputTotal(){return inputTotal;}
public int getOutputTotal(){return outputTotal;}

    public void setId(int id){ this.id = id;}
    public void setDate(String date){this.date = date;}
    public void setInput(int input){ this.input = input;}
    public void setOutput(int output){this.output = output;}
    public void setDifference(int diff){this.difference = diff;}
    public void setInputTotal(int inTotal){this.inputTotal = inTotal;}
    public void setOutputTotal(int outTotal){this.outputTotal = outTotal;}



}
