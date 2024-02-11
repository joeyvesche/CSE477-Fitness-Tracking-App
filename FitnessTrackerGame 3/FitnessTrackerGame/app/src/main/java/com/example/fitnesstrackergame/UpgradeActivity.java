package com.example.fitnesstrackergame;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class UpgradeActivity extends AppCompatActivity {

    TextView mCash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upgrade);

        mCash = (TextView)findViewById(R.id.cashReserve1);
        SharedPreferences sharedPreferences = getSharedPreferences("localData", Context.MODE_PRIVATE);
        String cash = sharedPreferences.getString("Cash", "1000");
        mCash.setText(cash);
        Button backButton = (Button)findViewById(R.id.backButton);
        /**
         * Return to the game activity with current state of cash reserve
         * and storing the state from the main activity
         */
        backButton.setOnClickListener(
                view -> {
                    saveData();
                    Intent intent = new Intent(view.getContext(), GameActivity.class);
                    startActivity(intent);
                });
        /**
         * Subtract from cash reserve each time the weight is upgraded
         */
        Button weightUpgrade = (Button)findViewById(R.id.weightUpgrade);
        weightUpgrade.setOnClickListener(
                view -> {
                    int cashAmount = Integer.parseInt(mCash.getText().toString());
                    int cost = Integer.parseInt(weightUpgrade.getText().toString().replace("$", ""));
                    int result = cashAmount - cost;
                    if(result < 0)
                        result = 0;
                    mCash.setText(Integer.toString(result));
                });
    }

    private void saveData() {
        // Save data using SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("localData", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("Cash", mCash.getText().toString());

        editor.apply();

        SharedPreferences loginPreferences = getSharedPreferences("loginPreferences", Context.MODE_PRIVATE);
        String username = loginPreferences.getString("username", "");
        String password = loginPreferences.getString("password", "");
        SharedPreferences gamedata = getSharedPreferences("localData", Context.MODE_PRIVATE);
        String cash = gamedata.getString("Cash", "1000");
        new SendDataToServerTask().execute(username, password, cash);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        saveData();
    }
}