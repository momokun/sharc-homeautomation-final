package gs.momokun.homeautomationx;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by ElmoTan on 12/9/2016.
 */

public class ChangeElectronicNameAdapter {

    private Activity activity;
    private Context context;
    private View view;
    private int pos;

    private EditText etName;

    private SharedPreferences sp;

    private String nameSPKey[] = {"electronicName1","electronicName2","electronicName3","electronicName4","electronicName5"};


    private AlertDialog ad;

    public ChangeElectronicNameAdapter(Activity activity, Context context, View view, int ePos) {
        this.activity = activity;
        this.context = context;
        this.view = view;
        this.pos = ePos;
    }

    public void declaration(){
        sp = PreferenceManager.getDefaultSharedPreferences(context);

        etName = (EditText) view.findViewById(R.id.editTextDialogUserInput);
        etName.setSingleLine(true);
    }

    public void dialogBuilder(){
        declaration();
        AlertDialog.Builder adb = new AlertDialog.Builder(context)
                .setView(view)
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ad.dismiss();
                    }
                });

        ad = adb.create();
        ad.show();
        Button ok = ad.getButton(DialogInterface.BUTTON_POSITIVE);
        ok.setOnClickListener(new ChangeElectronicNameAdapter.CustomValidationListener(ad));

        Button cancel = ad.getButton(DialogInterface.BUTTON_NEGATIVE);
        cancel.setOnClickListener(new ChangeElectronicNameAdapter.CustomValidationListenerExit(ad));
    }

    private AlertDialog confirmAd;
    private class CustomValidationListenerExit implements View.OnClickListener{
        private final Dialog DIALOG;

        CustomValidationListenerExit(Dialog dialog) {
            this.DIALOG = dialog;
        }

        @Override
        public void onClick(View view) {
            Log.d("EtName",etName.getText().toString());
            String inputValue = etName.getText().toString();

            if(inputValue.length()>0) {

                AlertDialog.Builder confirmAdb = new AlertDialog.Builder(context);

                confirmAdb.setTitle("Confirmation");
                confirmAdb.setMessage("Are you sure want to cancel?");
                confirmAdb.setCancelable(false);
                confirmAdb.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        confirmAd.dismiss();
                        ad.dismiss();
                    }
                });
                confirmAdb.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        confirmAd.dismiss();
                    }
                });
                confirmAd = confirmAdb.create();
                confirmAd.show();

            }else{
                ad.dismiss();
            }
        }
    }

    private class CustomValidationListener implements View.OnClickListener {
        private final Dialog DIALOG;

        CustomValidationListener(Dialog dialog) {
            this.DIALOG = dialog;
        }

        @Override
        public void onClick(View v) {
            Log.d("EtName",etName.getText().toString());
            String inputValue = etName.getText().toString();

            if(inputValue.length()>0){
                if(inputValue.length()>2 && inputValue.length()<15){
                    sp.edit().putString(nameSPKey[pos],inputValue).apply();
                    ad.dismiss();
                }else{
                    Toast.makeText(activity, "New name must be between 3 to 14 character", Toast.LENGTH_LONG).show();
                }
            }else{
                Toast.makeText(activity, "Can't be empty", Toast.LENGTH_SHORT).show();
            }

        }
    }
}
