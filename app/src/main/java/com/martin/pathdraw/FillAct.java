package com.martin.pathdraw;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.martin.pdmaster.PathDrawingView;

public class FillAct extends AppCompatActivity {
    private PathDrawingView pathDrawingView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fill);
        pathDrawingView = (PathDrawingView) findViewById(R.id.pathdrawing);
        pathDrawingView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pathDrawingView.getSequentialPathAnimator().delay(200).duration(10000).start();
            }
        });
    }
}
