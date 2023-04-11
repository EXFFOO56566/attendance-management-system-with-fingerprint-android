package com.example.attendacewithfingerprint;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.attendacewithfingerprint.database.DBManager;
import com.example.attendacewithfingerprint.database.DatabaseHelper;
import com.example.attendacewithfingerprint.sharedpreferences.PrefKeys;
import com.example.attendacewithfingerprint.sharedpreferences.PrefUtils;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SettingsActivity extends BaseActivity implements OnClickListener {

    private EditText link, key, name, idNumber;
    private int _id;

    // DB manager
    private DBManager dbManager;

    // Button
    Button add, update;

    // qr code scanner object
    private IntentIntegrator qrScan;

    String isFirstTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        isFirstTime = (String) PrefUtils.getFromPrefs(this, PrefKeys.IS_FIRST_TIME, "");

        link = findViewById(R.id.link);
        key = findViewById(R.id.key);
        name = findViewById(R.id.name);
        idNumber = findViewById(R.id.idNumber);

        TextView title = findViewById(R.id.titleSettings);

        if (isFirstTime.equals("YES")) {
            title.setText(getString(R.string.welcome_settings));
        }

        add = findViewById(R.id.add_record);
        update = findViewById(R.id.update_record);
        Button buttonScan = findViewById(R.id.buttonScan);

        //intializing scan object
        qrScan = new IntentIntegrator(this);

        //attaching onclick listener
        buttonScan.setOnClickListener(this);

        dbManager = new DBManager(this);
        dbManager.open();
        add.setOnClickListener(this);
        update.setOnClickListener(this);

        if (dbManager.checkIfEmpty("DATASABSENT")) {
            add.setVisibility(View.VISIBLE);
        } else if (!dbManager.checkIfEmpty("DATASABSENT")) {
            Cursor cursor = dbManager.getItem();

            if (cursor != null && cursor.moveToFirst()) {
                String linkload = cursor.getString(cursor.getColumnIndex
                        (DatabaseHelper.LINK));
                String keyload = cursor.getString(cursor.getColumnIndex
                        (DatabaseHelper.KEY_API));
                String nameUser = cursor.getString(cursor.getColumnIndex
                        (DatabaseHelper.USER_NAME));

                String idNumberUser = cursor.getString(cursor.getColumnIndex
                        (DatabaseHelper.ID_NUMBER));

                _id = cursor.getInt(cursor.getColumnIndex
                        (DatabaseHelper._ID));

                name.setText(nameUser);
                link.setText(linkload);
                key.setText(keyload);
                idNumber.setText(idNumberUser);

                if (!nameUser.equals(""))
                    disableEditText(name);

                cursor.close();
            }
            update.setVisibility(View.VISIBLE);
        }
    }

    private void disableEditText(EditText editText) {
        editText.setFocusable(false);
        editText.setEnabled(false);
        editText.setKeyListener(null);
        editText.setBackgroundColor(Color.TRANSPARENT);
    }

    @Override
    public void onClick(View v) {

        final String getLink = link.getText().toString();
        final String getKey = key.getText().toString();
        final String getName = name.getText().toString();
        final String getIdNumber = idNumber.getText().toString();

        switch (v.getId()) {
            case R.id.add_record:
            case R.id.update_record:
                if ((getName.trim().equals("")) && (getIdNumber.trim().equals("")) && (getLink.trim().equals("")) && (getKey.trim().equals(""))) {
                    Toast.makeText(getApplicationContext(), getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show();
                } else if (!isValidUrl(getLink)) {
                    Toast.makeText(getApplicationContext(), getString(R.string.url_incorrect_format), Toast.LENGTH_SHORT).show();
                } else {
                    saveAndUpdate(getLink, getKey, getName, getIdNumber);
                }
                break;
            case R.id.buttonScan:
                // zxing QR code
                //initiating the qr code scan
                qrScan.setCameraId(0);
                qrScan.setOrientationLocked(true);
                //Override here
                qrScan.setCaptureActivity(AnyOrientationCaptureActivity.class);
                qrScan.initiateScan();
                break;
        }
    }

    // Getting the scan results
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            //if qrcode has nothing in it
            if (result.getContents() == null) {
                Toast.makeText(this, "Result Not Found or Cancel by User", Toast.LENGTH_LONG).show();
            } else {
                //if qr contains data
                try {
                    //converting the data to json
                    JSONObject obj = new JSONObject(result.getContents());

                    String urlJson = obj.getString("url");
                    String keyJson = obj.getString("key");
                    final String getName = name.getText().toString();
                    final String getIdNumber = idNumber.getText().toString();

                    link.setText(urlJson);
                    key.setText(keyJson);

                    saveAndUpdate(urlJson, keyJson, getName, getIdNumber);


                } catch (JSONException e) {
                    e.printStackTrace();
                    // if control comes here
                    // that means the encoded format not matches
                    // in this case you can display whatever data is available on the qrcode
                    // to a toast
                    Toast.makeText(this, result.getContents(), Toast.LENGTH_LONG).show();
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    // Function save or update settings
    public void saveAndUpdate(String URL, String KEY, String Name, String idNumberUser) {

        if (dbManager.checkIfEmpty("DATASABSENT")) {
            long insertDatabase = dbManager.insert(URL, KEY, Name, idNumberUser);
            // Save it
            if (insertDatabase == -1) {
                Toast.makeText(SettingsActivity.this, getString(R.string.something_error), Toast.LENGTH_LONG).show();
            } else {
                if (isFirstTime.equals("NO")) {
                    Toast.makeText(SettingsActivity.this, getString(R.string.success_saved), Toast.LENGTH_LONG).show();
                } else {
                    PrefUtils.saveToPrefs(this, PrefKeys.IS_FIRST_TIME, "NO");
                    Toast.makeText(SettingsActivity.this, getString(R.string.success_saved), Toast.LENGTH_LONG).show();
                    // Go to main activity
                    Intent goToMainActivity = new Intent(SettingsActivity.this, MainActivity.class);
                    SettingsActivity.this.startActivity(goToMainActivity);
                    SettingsActivity.this.finish();
                }
            }
        } else if (!dbManager.checkIfEmpty("DATASABSENT")) {
            boolean updateDatabse = dbManager.update(_id, URL, KEY, Name, idNumberUser);
            // Update it
            if (!updateDatabse) {
                Toast.makeText(SettingsActivity.this, getString(R.string.something_error), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(SettingsActivity.this, getString(R.string.success_saved), Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * This is used to check the given URL is valid or not.
     *
     * @param url = String url
     * @return true if url is valid, false otherwise.
     */
    private boolean isValidUrl(String url) {
        Pattern p = Patterns.WEB_URL;
        Matcher m = p.matcher(url.toLowerCase());
        return m.matches();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return true;
    }

}