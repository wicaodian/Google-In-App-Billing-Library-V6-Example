package com.memo.iapsample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button btn_nonconsumeable,btn_consumeable,btn_subscription;
    Intent intent = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init(){
        btn_nonconsumeable = this.findViewById(R.id.btn_nonconsumeable);
        btn_consumeable = this.findViewById(R.id.btn_consumeable);
        btn_subscription = this.findViewById(R.id.btn_subscription);

        btn_nonconsumeable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent = new Intent(MainActivity.this, NonConsumeable.class);
                startActivity(intent);
            }
        });

        btn_consumeable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                intent = new Intent(MainActivity.this, Consumeable.class);
                startActivity(intent);
            }
        });

        btn_subscription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                intent = new Intent(MainActivity.this, Subscription.class);
                startActivity(intent);
            }
        });
    }
}