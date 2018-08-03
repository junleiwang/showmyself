package com.example.wv.bluetooth;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class SendMessageActivity extends AppCompatActivity implements View.OnClickListener{

    private TextView tvName,tvAddress;
    private Button btnSend;
    private EditText etMessage;

    private String name,address,message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_message);

        Intent intent = getIntent();

        name = intent.getStringExtra("bluetoothname");
        address = intent.getStringExtra("mac");

        setupView();
    }

    private void setupView() {

        tvName = (TextView) findViewById(R.id.tv_name);
        tvAddress = (TextView) findViewById(R.id.tv_macAddress);
        tvName.setText(name);
        tvAddress.setText(address);

        btnSend = (Button) findViewById(R.id.btn_send);
        etMessage = (EditText) findViewById(R.id.et_message);
        message = etMessage.getText().toString().trim();

        btnSend.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {

        switch(v.getId()){
            case R.id.btn_send:






                break;

            default:
                break;


        }

    }
}
