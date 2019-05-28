package com.example.jaegyengjo.medicine_alarm.Main;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.example.jaegyengjo.medicine_alarm.Info.InfoActivity;
import com.example.jaegyengjo.medicine_alarm.Alarm.AlarmActivity;
import com.example.jaegyengjo.medicine_alarm.R;

import java.util.Calendar;


public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Button button1 = (Button)findViewById(R.id.btnAlarm1);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(getApplicationContext(), "알림이 설정되었습니다.", Toast.LENGTH_SHORT).show();

                new AlarmHATT(getApplicationContext()).Alarm();

            }
        });

        Button button2 = (Button)findViewById(R.id.stop);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "알림이 중지되었습니다.", Toast.LENGTH_SHORT).show();

                new AlarmHATT(getApplicationContext()).Alarm().cancel();
            }
        });

        Button button3 = (Button)findViewById(R.id.newMedic);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AlarmActivity.class);
                startActivity(intent);
            }
        });


        Button button4 = (Button)findViewById(R.id.medicInfo);
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, InfoActivity.class);
                startActivity(intent);
            }
        });





    }
    public class AlarmHATT {
        private Context context;

        public AlarmHATT(Context context) {
            this.context= context;
        }

        public PendingIntent Alarm() {

            long period = 1000 * 5;
            long after = 1000 * 5;
            long t = SystemClock.elapsedRealtime();
            AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(MainActivity.this, BroadcastD.class);

            PendingIntent sender = PendingIntent.getBroadcast(MainActivity.this, 0, intent, 0);

            Calendar calendar = Calendar.getInstance();
            //알람시간 calendar에 set해주기

            //알람 예약
            am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, t + after, period, sender);

            return sender;
        }
    }





}



