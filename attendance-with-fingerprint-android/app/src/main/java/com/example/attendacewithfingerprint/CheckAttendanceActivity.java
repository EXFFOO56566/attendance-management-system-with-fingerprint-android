package com.example.attendacewithfingerprint;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.attendacewithfingerprint.database.DBManager;

import java.util.ArrayList;
import java.util.HashMap;

public class CheckAttendanceActivity extends BaseActivity {

    ListAdapter adapter;
    ListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Request window feature action bar
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_attendance);

        // check if empty database
        if (dbManager.checkIfEmpty("DATAATTENDANCE")) {
            Toast.makeText(CheckAttendanceActivity.this, getString(R.string.data_attendance_empty), Toast.LENGTH_LONG).show();
        }
        //check if not empty database
        else {
            //open database sqlite
            dbManager = new DBManager(this);
            dbManager.open();

            ArrayList<HashMap<String, String>> userList = dbManager.getAttendance();
            lv = findViewById(R.id.listView);
            adapter = new SimpleAdapter(
                    this, userList, R.layout.list_row, new String[]{"name", "date", "time", "type", "location"},
                    new int[]{R.id.name, R.id.date, R.id.time, R.id.type, R.id.location});
            lv.setAdapter(adapter);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list_attendance, menu);


        boolean checkData = dbManager.checkIfEmpty("DATAATTENDANCE");

        // Will show icon
        if (checkData) {
            menu.findItem(R.id.delete).setVisible(false);
        } else {
            menu.findItem(R.id.delete).setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.delete) {
            deleteDialog();
        } else if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return true;
    }

    // Show dialog when user click delete all
    private void deleteDialog() {
        AlertDialog builder = new AlertDialog.Builder(CheckAttendanceActivity.this)
                .setTitle(getString(R.string.delete_all_data))
                .setMessage(getString(R.string.are_you_sure_delete_all))
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int i) {
                        deleteAllData();
                        dialog.dismiss();
                    }

                })
                .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int i) {
                        dialog.dismiss();
                    }
                })
                .create();

        builder.show();
    }

    // Function delete all
    public void deleteAllData() {
        dbManager.deleteAllAttendance();
        lv.setVisibility(View.GONE);
        Toast.makeText(CheckAttendanceActivity.this, getString(R.string.data_attendance_empty), Toast.LENGTH_LONG).show();
    }

}
