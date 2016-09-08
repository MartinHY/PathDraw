package com.martin.pathdraw;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.button).setOnClickListener(onClickListener);
        findViewById(R.id.button2).setOnClickListener(onClickListener);
        findViewById(R.id.button3).setOnClickListener(onClickListener);
    }


    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = null;
            switch (v.getId()) {
                case R.id.button:
                    intent = new Intent(MainActivity.this, NormalAct.class);
                    break;
                case R.id.button2:
                    intent = new Intent(MainActivity.this, FillAct.class);
                    break;
                case R.id.button3:
                    intent = new Intent(MainActivity.this, AfterFillAct.class);
                    break;
            }
            if (intent != null)
                MainActivity.this.startActivity(intent);
        }
    };
}
