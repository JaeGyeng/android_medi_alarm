package com.example.jaegyengjo.medicine_alarm.Info;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.jaegyengjo.medicine_alarm.R;

import java.util.ArrayList;

public class InfoActivity extends AppCompatActivity {
    private ListView myListView;
    MedicineData mydb;
    ArrayAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.medic_info);

        mydb = new MedicineData(this);
        ArrayList array_list = mydb.getAllMovies();

        mAdapter =
                new ArrayAdapter(this, android.R.layout.simple_list_item_1, array_list);

        myListView = (ListView) findViewById(R.id.listView1);
        myListView.setAdapter(mAdapter);

        myListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                int id = arg2 + 1;
                Bundle dataBundle = new Bundle();
                dataBundle.putInt("id", id);
                Intent intent = new Intent(getApplicationContext(), inputInfo.class);
                intent.putExtras(dataBundle);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAdapter.clear();
        mAdapter.addAll(mydb.getAllMovies());
        mAdapter.notifyDataSetChanged();
    }

    public void onClick(View target) {
        Bundle bundle = new Bundle();
        bundle.putInt("id", 0);
        Intent intent = new Intent(getApplicationContext(), inputInfo.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }
}