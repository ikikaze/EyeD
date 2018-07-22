package ro.lockdowncode.eyedread.UI;

import android.content.Context;
import android.hardware.Camera;
import android.support.v7.widget.AppCompatCheckBox;
import android.util.AttributeSet;
import android.widget.CompoundButton;

import com.google.android.cameraview.CameraView;

import ro.lockdowncode.eyedread.R;

public class FlashButton extends AppCompatCheckBox {

    static private final int OFF = -1;
    static private final int AUTO = 0;
    static private final int TORCH = 1;
    private int state;
    private CameraView mCameraView;
    private CameraSource mCameraSource;

    public FlashButton(Context context) {
        super(context);
        init();
    }

    public FlashButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FlashButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init()
    {
        state = AUTO;
        updateBtn();

        setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            // checkbox status is changed from uncheck to checked.
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                switch (state)
                {
                    case OFF:
                        state = AUTO;
                        break;
                    case AUTO:
                        state = TORCH;
                        break;
                    case TORCH:
                        state = OFF;
                        break;
                }

                if(mCameraSource != null)
                    setCameraSourceFlash();
                if(mCameraView !=null)
                    setCameraViewFlash();
                updateBtn();
            }
        });

    }

    private void updateBtn()
    {
        int btnDrawable = R.drawable.flash_auto;
        switch (state)
        {
            case OFF:
                btnDrawable = R.drawable.flash_off;
                break;
            case AUTO:
                btnDrawable = R.drawable.flash_auto;
                break;
            case TORCH:
                btnDrawable = R.drawable.flash_on;
                break;
        }
        setButtonDrawable(btnDrawable);

    }
    public int getState()
    {
        return state;
    }

    public void setState(int state)
    {
        this.state = state;
        updateBtn();
    }

    public void setCameraView(CameraView cameraView) {
        this.mCameraView = cameraView;
    }

    public void setCameraSource(CameraSource cameraSource)
    {
        mCameraSource = cameraSource;
    }

    private void setCameraSourceFlash() {

        switch (state)
        {
            case OFF:
                mCameraSource.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                break;
            case AUTO:
                mCameraSource.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                break;
            case TORCH:
                mCameraSource.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                break;
        }

    }

    private void setCameraViewFlash() {
        switch (state)
        {
            case OFF:
                mCameraView.setFlash(CameraView.FLASH_OFF);
                break;
            case AUTO:
                mCameraView.setFlash(CameraView.FLASH_AUTO);
                break;
            case TORCH:
                mCameraView.setFlash(CameraView.FLASH_TORCH);
                break;
        }

    }
}
