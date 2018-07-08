package ro.lockdowncode.eyedread;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class NFCResultActivity extends AppCompatActivity {

    public static final String KEY_FIRST_NAME = "FIRST_NAME";
    public static final String KEY_LAST_NAME = "LAST_NAME";
    public static final String KEY_GENDER = "GENDER" ;
    public static final String KEY_STATE = "STATE";
    public static final String KEY_NATION = "NATION" ;
    public static final String KEY_EXP_DATE = "EXPIRY_DATE";
    public static final String KEY_BDAY = "BIRTH_DATE";
    public static final String KEY_PASS_NUM ="PASSPORT_NUMBER" ;
    public static final String KEY_PASS_CODE = "PASSPORT_CODE";
    public static final String KEY_CNP = "CNP" ;
    public static final String KEY_PHOTO = "PHOTO" ;


    private String secondaryID,primaryID,gender,issuingState,nationality,expDate,birthDate,passNumber,passCode,cnp;
    Bitmap facePhoto;

    TextView infodump;
    ImageView face;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfcresult);
        getIntentData();
        infodump = findViewById(R.id.infodump);
        face = findViewById(R.id.facephoto);
        face.setImageBitmap(facePhoto);

        infodump.setText(secondaryID+ "\n" + primaryID+ "\n" +gender+ "\n" +issuingState+ "\n" +nationality+ "\n" +expDate+ "\n" +birthDate+ "\n" +passCode+ "\n" +passNumber+ "\n" +cnp+ "\n");
    }

    private void getIntentData() {

        Intent intent = getIntent();


        secondaryID =intent.getStringExtra(NFCResultActivity.KEY_FIRST_NAME);
        primaryID =intent.getStringExtra(NFCResultActivity.KEY_LAST_NAME);
        gender =intent.getStringExtra(NFCResultActivity.KEY_GENDER);
        issuingState =intent.getStringExtra(NFCResultActivity.KEY_STATE);
        nationality =intent.getStringExtra(NFCResultActivity.KEY_NATION);
        expDate = intent.getStringExtra(NFCResultActivity.KEY_EXP_DATE);
        birthDate = intent.getStringExtra(NFCResultActivity.KEY_BDAY);
        passNumber =intent.getStringExtra(NFCResultActivity.KEY_PASS_NUM);
        passCode = intent.getStringExtra(NFCResultActivity.KEY_PASS_CODE);
        cnp = intent.getStringExtra(NFCResultActivity.KEY_CNP);
        facePhoto = intent.getParcelableExtra(KEY_PHOTO);
    }

}
