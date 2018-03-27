package com.example.alexei.monitorizare.view;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.content.ContentValues;
public class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {


    private ClickListener clickListener;
    private GestureDetector gestureDetector;

    public RecyclerTouchListener(Context context,final RecyclerView recyclerView,final ClickListener clickListener)
    {
        this.clickListener = clickListener;
        gestureDetector = new GestureDetector(context,new GestureDetector.SimpleOnGestureListener(){
           @Override
           public boolean onSingleTapUp(MotionEvent motionEvent)
           {return true;}

           @Override
            public void onLongPress(MotionEvent motionEvent)
           {
               View child = recyclerView.findChildViewUnder(motionEvent.getX(),motionEvent.getY());
               if(child !=null && clickListener !=null)
               {
                    clickListener.onLongClick(child, recyclerView.getChildAdapterPosition(child));
               }

           }

        });
    }
    @Override
    public boolean onInterceptTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {
        View child = recyclerView.findChildViewUnder(motionEvent.getX(), motionEvent.getY());
        if (child != null && clickListener != null && gestureDetector.onTouchEvent(motionEvent)) {
            clickListener.onClick(child, recyclerView.getChildAdapterPosition(child));
        }

        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

    public interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }
}
