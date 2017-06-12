package gs.momokun.homeautomationx;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


public class SplashScreen extends AppCompatActivity {
    // Splash screen timer
    static int SPLASH_TIME_OUT = 3000;
    BluetoothAdapter btAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        ImageButton splashLogo = (ImageButton) findViewById(R.id.splash_logo);
        final TextView splashText = (TextView) findViewById(R.id.splash_text);
        splashLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                splashText.setText("SHARC - Smart Home Automation Electronic Controller");
            }
        });

        BluetoothStateChecker();

    }

    //check if bluetooth module is on
    private void BluetoothStateChecker(){
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if(btAdapter!=null){
            if(btAdapter.isEnabled()){
                new Handler().postDelayed(new Runnable() {

            /*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             */

                    @Override
                    public void run() {
                        // This method will be executed once the timer is over
                        // Start your app main activity

                        Intent i = new Intent(SplashScreen.this, MainActivity.class);
                        startActivity(i);

                        // close this activity
                        finish();
                    }
                }, SPLASH_TIME_OUT);
            }else{
                Intent btRequestEnable = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(btRequestEnable,1);
            }
        }else{
            Toast.makeText(this, "No Bluetooth on this device, please check your device.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        if(requestCode==1)
        {
            if(btAdapter.isEnabled()){
                new Handler().postDelayed(new Runnable() {

            /*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             */

                    @Override
                    public void run() {
                        // This method will be executed once the timer is over
                        // Start your app main activity

                        Intent i = new Intent(SplashScreen.this, MainActivity.class);
                        startActivity(i);

                        // close this activity
                        finish();
                    }
                }, SPLASH_TIME_OUT);
            }else{
                BluetoothStateChecker();
            }
        }
    }

}