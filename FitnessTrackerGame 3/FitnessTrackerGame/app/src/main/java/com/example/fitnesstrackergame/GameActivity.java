package com.example.fitnesstrackergame;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class GameActivity extends AppCompatActivity {

    private TextView mCash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        mCash = (TextView)findViewById(R.id.cashReserve);
        SharedPreferences sharedPreferences = getSharedPreferences("localData", Context.MODE_PRIVATE);
        String cash = sharedPreferences.getString("Cash", "1000");
        mCash.setText(cash);

        Button fitnessButton = (Button)findViewById(R.id.fitnessButton);
        /**
         * Move to the main activity with current state
         */
        fitnessButton.setOnClickListener(
                view -> {
                    saveData();
                    Intent intent = new Intent(view.getContext(), MainActivity.class);
                    startActivity(intent);
                });
        /**
         * Move to the upgrade activity with current state
         */
        Button upgradeButton = (Button)findViewById(R.id.upgradeButton1);
        upgradeButton.setOnClickListener(
                view -> {
                    saveData();
                    Intent intent = new Intent(view.getContext(), UpgradeActivity.class);
                    startActivity(intent);
                });
        /**
         * Each time the gym is clicked on, add a specified amount to the cash reserve
         */
        ImageView gymImage = (ImageView)findViewById(R.id.gymImage);
        gymImage.setOnClickListener(
                view -> {
                    int cashAmount = Integer.parseInt(mCash.getText().toString());
                    mCash.setText(Integer.toString(cashAmount + 10));
                });
    }

    private void saveData() {
        // Save data using SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("localData", Context.MODE_PRIVATE);
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

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString("Cash", mCash.getText().toString());
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mCash.setText(savedInstanceState.getString("Cash"));
    }


}