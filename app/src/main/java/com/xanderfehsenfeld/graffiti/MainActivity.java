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
import android.widget.ToggleButton;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    boolean readMode = true;
    NfcAdapter myAdapter;
    PendingIntent pendingIntent;
    IntentFilter[] intentFiltersArray;
    String[][] techListsArray;
    EditText et;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        et = (EditText) findViewById( R.id.graffiti);
        resolveHint();
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

    private void resolveHint(){
        if (readMode) {
            et.setHint(R.string.read_default);
            et.setClickable(false);
        } else {
            et.setHint(R.string.write_hint);
            et.setClickable(true);
        }
    }

    public void toggle(View view){
        readMode = !readMode;
        et.setText("");
        resolveHint();
    }

    public void onNewIntent(Intent intent) {
        /* rebeccas github : rebecca-watson */

        if (readMode) {
            String s = new String();

            Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            Parcelable[] data = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (data != null) {
                try {
                    for (int i = 0; i < data.length; i++) {
                        NdefRecord[] recs = ((NdefMessage) data[i]).getRecords();
                        for (int j = 0; j < recs.length; j++) {
                            if (recs[j].getTnf() == NdefRecord.TNF_WELL_KNOWN &&
                                    Arrays.equals(recs[j].getType(), NdefRecord.RTD_TEXT)) {
                                byte[] payload = recs[j].getPayload();
                                String textEncoding = ((payload[0] & 0200) == 0) ? "UTF-8" : "UTF-16";
                                int langCodeLen = payload[0] & 0077;

                                s += new String(payload, langCodeLen + 1, payload.length - langCodeLen - 1, textEncoding);
                            }
                        }
                    }
                } catch (Exception e) {

                }
            }

            EditText et = (EditText) findViewById(R.id.graffiti);
            et.setText(s);

        } else {
            Toast toast = Toast.makeText(MainActivity.this, "detected!", Toast.LENGTH_SHORT);
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



}
