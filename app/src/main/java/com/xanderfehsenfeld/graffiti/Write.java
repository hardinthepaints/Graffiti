package com.xanderfehsenfeld.graffiti;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcF;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class Write extends AppCompatActivity {
    /* write to nfc with set amount of characters */
    NfcAdapter myAdapter;
    PendingIntent pendingIntent;
    IntentFilter[] intentFiltersArray;
    String[][] techListsArray;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);


        /* Copied from Matthew's code */
        myAdapter = NfcAdapter.getDefaultAdapter(this);


        pendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            ndef.addDataType("*/*");    /* Handles all MIME based dispatches.
                                       You should specify only the ones that you need. */
        }
        catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("fail", e);
        }
        IntentFilter td = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        intentFiltersArray = new IntentFilter[] {
                ndef, td
        };

        techListsArray = new String[][] { new String[] { NfcF.class.getName() } };
    }


        /* write to nfc with set amount of characters */
        private void write(){
            EditText editText = (EditText) findViewById(R.id.graffiti);
            String message = editText.getText().toString().trim();


            /* check is not hint  or empty */
            if (!message.equals(null) && !message.equals("") && !message.equals( editText.getHint().toString() )){

                /* write to nfc */


            } else {
                /* alert user to make not empty */
            }
        }

    public void goToRead(View view){
        startActivity(new Intent(Write.this, Read.class));
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        myAdapter.disableForegroundDispatch(this);
    }
    @Override
    public void onResume() {
        super.onResume();
        myAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techListsArray);
    }

    private NdefRecord createRecord(String text) throws UnsupportedEncodingException{
        //create message with standard
        String lang = "en";
        byte[] textBytes = text.getBytes();
        byte[] langBytes = lang.getBytes("US-ASCII");
        int langLength = langBytes.length;
        int textLength = textBytes.length;

        byte[] payload = new byte[1 + langLength + textLength];
        payload[0] = (byte)langLength;

        //copy langbytes and textbytes to payload
        System.arraycopy(langBytes, 0, payload, 1, langLength);
        System.arraycopy(textBytes, 0, payload, 1 + langLength, textLength);

        NdefRecord record = new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], payload);
        return record;



    }

    public void onNewIntent(Intent intent) {
        Toast toast = Toast.makeText(Write.this, "detected!", Toast.LENGTH_SHORT);
        toast.show();
        EditText et = (EditText) findViewById(R.id.graffiti);
        String s = et.getText().toString().trim();

        Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        NdefRecord[] records = new NdefRecord[0];
        try {
            records = new NdefRecord[]{ createRecord(s) };
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        NdefMessage message = new NdefMessage(records);
        Ndef ndef = Ndef.get(tagFromIntent);
        try {
            ndef.connect();
            ndef.writeNdefMessage(message);
            ndef.close();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (FormatException e) {
            e.printStackTrace();
        }


    }
}
