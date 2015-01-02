package es.reprogramador.networknotifier;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class NetworkNotifier extends Activity {

    // This Activity only starts the service.
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            Intent s = new Intent(this, NetworkNotifierService.class);
            if (s != null) {
                startService(s);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        this.finish();
    }

}
