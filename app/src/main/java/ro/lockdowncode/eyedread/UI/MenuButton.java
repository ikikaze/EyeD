package ro.lockdowncode.eyedread.UI;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;

import ro.lockdowncode.eyedread.R;



public class MenuButton extends ConstraintLayout {

    private ImageView mImageView;
    private TextView mTextView;
    private int mImgDrawable;
    private int mText;


    public MenuButton(Context context) {
        super(context);
        initializeViews(context);

    }


    public MenuButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeViews(context);
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs,R.styleable.MenuButton,0,0);
        int cnt = attrs.getAttributeCount();

        mImgDrawable = typedArray.getResourceId(R.styleable.MenuButton_menu_button_image,-1);
        mText = typedArray.getResourceId(R.styleable.MenuButton_menu_button_text,-1);
        typedArray.recycle();
    }

    public MenuButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializeViews(context);
       /* TypedArray typedArray = context.obtainStyledAttributes(R.styleable.MenuButton);
        mImgDrawable = typedArray.getResourceId(R.styleable.MenuButton_menu_button_image,-1);
        mText = typedArray.getResourceId(R.styleable.MenuButton_menu_button_text,-1);

        typedArray.recycle();*/

    }

    private void initializeViews(Context context) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.menu_button, this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mImageView =this.findViewById(R.id.cbtnImage);
        mTextView = this.findViewById(R.id.cbtnText);

        mImageView.setImageResource(mImgDrawable);
        mImageView.setFocusable(false);
        mImageView.setClickable(false);



        mTextView.setText(mText);
        mTextView.setClickable(false);
        mTextView.setFocusable(false);
        mTextView.setTextColor(getResources().getColor(R.color.colorBtnPink));
    }

    public void setText(String text)
    {
        mTextView.setText(text);
    }

    public void setText(int resId)
    {
        mTextView.setText(resId);
    }




}
