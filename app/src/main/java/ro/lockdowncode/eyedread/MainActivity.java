package ro.lockdowncode.eyedread;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button btnSearch;
    private Button btnID;
    private Button btnPass;
    private Button btnLicense;
    private Button btnConnect;

    //CAMEL CASE EVERYTHING
    //COMMENTS ARE NOT STARTED WITH # , STOP THINKING PYTHON

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //initialize buttons
        initButtons();


    }

    private void initButtons() {
        btnConnect = findViewById(R.id.btnConnect);
        btnSearch = findViewById(R.id.btnSearch);
        btnID = findViewById(R.id.btnID);
        btnPass = findViewById(R.id.btnPass);
        btnLicense = findViewById(R.id.btnLicense);
    }


    public void btnsClicked(View view) {
        int id = view.getId();

        Intent intent = null;
        switch (id) {
            case R.id.btnID:
                break;
            case R.id.btnLicense:
                intent = new Intent(this, LicenseActivity.class);
                break;
            case R.id.btnPass:
                break;
            case R.id.btnSearch:
                break;
            case R.id.btnConnect:
                break;
        }
        if (intent != null)
            startActivity(intent);
    }
}
