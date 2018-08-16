package ro.lockdowncode.eyedread;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.TagLostException;
import android.nfc.tech.IsoDep;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import net.sf.scuba.smartcards.CardFileInputStream;
import net.sf.scuba.smartcards.CardService;

import org.jmrtd.BACKey;
import org.jmrtd.BACKeySpec;
import org.jmrtd.PassportService;
import org.jmrtd.lds.COMFile;
import org.jmrtd.lds.CardAccessFile;
import org.jmrtd.lds.DG1File;
import org.jmrtd.lds.DG2File;
import org.jmrtd.lds.FaceImageInfo;
import org.jmrtd.lds.FaceInfo;
import org.jmrtd.lds.LDS;
import org.jmrtd.lds.LDSFile;
import org.jmrtd.lds.MRZInfo;
import org.jmrtd.lds.PACEInfo;
import org.jmrtd.lds.SODFile;
import org.spongycastle.jce.provider.BouncyCastleProvider;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class NFCActivity extends AppCompatActivity {




    static final String TAG = "NFCActivity";

    TextView txt;
    ProgressBar loadingbar;
    String passNumber, passExpDate, passBDay;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc);
        txt = findViewById(R.id.tmpTxt);
        loadingbar = findViewById(R.id.progressBar);

        loadingbar.setVisibility(View.INVISIBLE);


        Security.insertProviderAt(new BouncyCastleProvider(), 1);

        Intent intent = getIntent();
        passNumber = intent.getStringExtra("Number");
        passBDay = intent.getStringExtra("Bday");
        passExpDate = intent.getStringExtra("ExpDate");
    }



    @Override
    public void onNewIntent(Intent intent) {
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
            Tag tag = intent.getExtras().getParcelable(NfcAdapter.EXTRA_TAG);

            if (Arrays.asList(tag.getTechList()).contains("android.nfc.tech.IsoDep")) {

                if (passNumber != null && !passNumber.isEmpty()
                        && passExpDate != null && !passExpDate.isEmpty()
                        && passBDay != null && !passBDay.isEmpty()) {
                    BACKeySpec bacKey = new BACKey(passNumber, passBDay, passExpDate);
                    new ReadTask(IsoDep.get(tag), bacKey).execute();
                    //mainLayout.setVisibility(View.GONE);
                    //loadingLayout.setVisibility(View.VISIBLE);
                } else {
                    Snackbar.make(txt, "BAD INPUT DATA", Snackbar.LENGTH_SHORT).show();
                }
                //txt.setText("FOUND IT BOIII");

            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        NfcAdapter adapter = NfcAdapter.getDefaultAdapter(this);
        if (adapter != null) {
            Intent intent = new Intent(getApplicationContext(), this.getClass());
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            String[][] filter = new String[][]{new String[]{"android.nfc.tech.IsoDep"}};
            adapter.enableForegroundDispatch(this, pendingIntent, null, filter);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();

        NfcAdapter adapter = NfcAdapter.getDefaultAdapter(this);
        if (adapter != null) {
            adapter.disableForegroundDispatch(this);
        }
    }



    private class ReadTask extends AsyncTask<Void, Void, Exception> {

        private IsoDep isoDep;
        private BACKeySpec bacKey;

        public ReadTask(IsoDep isoDep, BACKeySpec bacKey) {
            this.isoDep = isoDep;
            this.bacKey = bacKey;
        }

        private COMFile comFile;
        private SODFile sodFile;
        private DG1File dg1File;
        private DG2File dg2File;

        private Bitmap bitmap;

        @Override
        protected Exception doInBackground(Void... params) {
            try {

                loadingbar.setVisibility(View.VISIBLE);
                txt.setText("STAI HO CA MA INCARC, NU MA MISCA");

                CardService cardService = CardService.getInstance(isoDep);
                //cardService.open();


                PassportService service = new PassportService(cardService);
                service.open();


                boolean paceSucceeded = false;
                try {
                    CardAccessFile cardAccessFile = new CardAccessFile(service.getInputStream(PassportService.EF_CARD_ACCESS));
                    Collection<PACEInfo> paceInfos = cardAccessFile.getPACEInfos();
                    if (paceInfos != null && paceInfos.size() > 0) {
                        PACEInfo paceInfo = paceInfos.iterator().next();
                        service.doPACE(bacKey, paceInfo.getObjectIdentifier(), PACEInfo.toParameterSpec(paceInfo.getParameterId()));
                        paceSucceeded = true;
                    } else {
                        paceSucceeded = true;
                    }
                } catch (Exception e) {
                    Log.w(TAG, e);
                }

                service.sendSelectApplet(paceSucceeded);

                if (!paceSucceeded) {
                    try {
                        service.getInputStream(PassportService.EF_COM).read();
                    } catch (Exception e) {
                        service.doBAC(bacKey);
                    }
                }

                LDS lds = new LDS();

//                if(!isoDep.isConnected() || !service.isOpen())
//                    throw new TagLostException();
//                CardFileInputStream comIn = service.getInputStream(PassportService.EF_COM);
//                lds.add(PassportService.EF_COM, comIn, comIn.getLength());
//                comFile = lds.getCOMFile();
//
//                if(!isoDep.isConnected() || !service.isOpen())
//                    throw new TagLostException();
//                CardFileInputStream sodIn = service.getInputStream(PassportService.EF_SOD);
//                lds.add(PassportService.EF_SOD, sodIn, sodIn.getLength());
//                sodFile = lds.getSODFile();


                if(!isoDep.isConnected() || !service.isOpen())
                    throw new TagLostException();

                CardFileInputStream dg1In = service.getInputStream(PassportService.EF_DG1);
                lds.add(PassportService.EF_DG1, dg1In, dg1In.getLength());
                dg1File = lds.getDG1File();



                if(!isoDep.isConnected() || !service.isOpen())
                    throw new TagLostException();
                CardFileInputStream dg2In = service.getInputStream(PassportService.EF_DG2);
                lds.add(PassportService.EF_DG2, dg2In, dg2In.getLength());
                dg2File = lds.getDG2File();

                List<FaceImageInfo> allFaceImageInfos = new ArrayList<>();
                List<FaceInfo> faceInfos = dg2File.getFaceInfos();
                for (FaceInfo faceInfo : faceInfos) {
                    allFaceImageInfos.addAll(faceInfo.getFaceImageInfos());
                }

                if (!allFaceImageInfos.isEmpty()) {
                    FaceImageInfo faceImageInfo = allFaceImageInfos.iterator().next();

                    int imageLength = faceImageInfo.getImageLength();
                    DataInputStream dataInputStream = new DataInputStream(faceImageInfo.getImageInputStream());
                    byte[] buffer = new byte[imageLength];
                    dataInputStream.readFully(buffer, 0, imageLength);
                    InputStream inputStream = new ByteArrayInputStream(buffer, 0, imageLength);

                    bitmap = ImageUtil.decodeImage(
                            NFCActivity.this, faceImageInfo.getMimeType(), inputStream);

                }

            } catch (Exception e) {
                return e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Exception result) {
            //mainLayout.setVisibility(View.VISIBLE);
            //loadingLayout.setVisibility(View.GONE);
            if(result !=null) {
                Toast.makeText(NFCActivity.this, result.getLocalizedMessage(), Toast.LENGTH_SHORT);
                Log.d("NFCResult", result.getClass().toString());
                txt.setText(result.getClass().toString());
            }




           if (result == null) {
//
                Intent intent;
                if (getCallingActivity() != null) {
                    intent = new Intent();
                } else {
                    intent = new Intent(NFCActivity.this, NFCResultActivity.class);
                }
//
//
//
//                intent.putExtra(ResultActivity.KEY_FIRST_NAME, mrzInfo.getSecondaryIdentifier().replace("<", ""));
//                intent.putExtra(ResultActivity.KEY_LAST_NAME, mrzInfo.getPrimaryIdentifier().replace("<", ""));
//                intent.putExtra(ResultActivity.KEY_GENDER, mrzInfo.getGender().toString());
//                intent.putExtra(ResultActivity.KEY_STATE, mrzInfo.getIssuingState());
//                intent.putExtra(ResultActivity.KEY_NATIONALITY, mrzInfo.getNationality());
//
             MRZInfo mrzInfo = dg1File.getMRZInfo();

             String secondaryID = mrzInfo.getSecondaryIdentifier().replace("<", " ");
             String primaryID = mrzInfo.getPrimaryIdentifier().replace("<"," ");
             String gender = mrzInfo.getGender().toString();
             String issuingState = mrzInfo.getIssuingState();
             String nationality = mrzInfo.getNationality();
             String expDate = mrzInfo.getDateOfExpiry();
             String birthDate = mrzInfo.getDateOfBirth();
             String passCode = mrzInfo.getDocumentCode();
             String passNumber = mrzInfo.getDocumentNumber();
             String cnp = mrzInfo.getPersonalNumber();

             intent.putExtra(NFCResultActivity.KEY_FIRST_NAME,secondaryID);
             intent.putExtra(NFCResultActivity.KEY_LAST_NAME,primaryID);
             intent.putExtra(NFCResultActivity.KEY_GENDER,gender);
             intent.putExtra(NFCResultActivity.KEY_STATE,issuingState);
             intent.putExtra(NFCResultActivity.KEY_NATION,nationality);
             intent.putExtra(NFCResultActivity.KEY_EXP_DATE,expDate);
             intent.putExtra(NFCResultActivity.KEY_BDAY,birthDate);
             intent.putExtra(NFCResultActivity.KEY_PASS_NUM,passNumber);
             intent.putExtra(NFCResultActivity.KEY_PASS_CODE,passCode);
             intent.putExtra(NFCResultActivity.KEY_CNP,cnp);




              if (bitmap != null) {
                    double ratio = 320.0 / bitmap.getHeight();
                    int targetHeight = (int) (bitmap.getHeight() * ratio);
                    int targetWidth = (int) (bitmap.getWidth() * ratio);

                    intent.putExtra(NFCResultActivity.KEY_PHOTO,
                            Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, false));
                }

//                if (getCallingActivity() != null) {
//                    setResult(Activity.RESULT_OK, intent);
//                    finish();
//                } else {
                    startActivity(intent);
//                }

            }
        }

    }

}


