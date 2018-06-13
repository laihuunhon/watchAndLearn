package com.lhn.watchandlearn.ui.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lhn.watchandlearn.R;
import com.lhn.watchandlearn.ui.utils.Validator;

public class FormEditText extends LinearLayout{

    private EditText mValueText;
    private ImageButton mBtnClose;
    private ImageButton mBtnCheckmark;
    private int mBackgroundResId;
    private int mBackgroundErrorResId;
    private FormValidator mValidator;

    public FormEditText(final Context context, final AttributeSet attrs){
        super(context, attrs);
        init(context, attrs);
    }

    public FormEditText(final Context context, final AttributeSet attrs, final int defStyle){
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs){
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.view_form_edit_text, this, true);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FormEditText);

        String text = a.getString(R.styleable.FormEditText_text);
        String type = a.getString(R.styleable.FormEditText_hint);
        mValueText = (EditText) view.findViewById(R.id.tvFormValue);

        int inputType = a.getInt(R.styleable.FormEditText_android_inputType, EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        if(inputType != EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS){
            mValueText.setInputType(inputType);
        }

        String imeOptions = a.getString(R.styleable.FormEditText_android_imeOptions);
        if(!TextUtils.isEmpty(imeOptions)){
            mValueText.setPrivateImeOptions(imeOptions);
        }

        if(!TextUtils.isEmpty(text)){
            mValueText.setText(text);
        }

        if(!TextUtils.isEmpty(type)){
            mValueText.setHint(type);
        }

        int textSize = a.getDimensionPixelSize(R.styleable.FormEditText_textSize, 15);
        mValueText.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);

        ColorStateList color = a.getColorStateList(R.styleable.FormEditText_textColor);
        if(color != null){
            mValueText.setTextColor(color);
        }

        mBtnClose = (ImageButton) view.findViewById(R.id.btnClose);
        mBtnCheckmark = (ImageButton) view.findViewById(R.id.btnCheckmark);

        boolean required = a.getBoolean(R.styleable.FormEditText_required, false);
        TextView tvRequired = (TextView) view.findViewById(R.id.tvRequired);
        if(required){
            tvRequired.setVisibility(View.VISIBLE);
        } else{
            boolean needSpace = a.getBoolean(R.styleable.FormEditText_spaceForRequiredIcon, true);
            if(needSpace){
                tvRequired.setVisibility(View.INVISIBLE);
            } else{
                tvRequired.setVisibility(View.GONE);
            }
        }

        mBackgroundResId = a.getResourceId(R.styleable.FormEditText_android_background, R.drawable.bg_text_normal);
        mBackgroundErrorResId = a.getResourceId(R.styleable.FormEditText_backgroundError, R.drawable.bg_text_error);
        setBackgroundResource(mBackgroundResId);
        setOrientation(LinearLayout.HORIZONTAL);

        a.recycle();

        registerListener();
    }

    private void registerListener(){
        setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v){
                mValueText.requestFocusFromTouch();
                InputMethodManager imm = (InputMethodManager) getContext()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(mValueText, InputMethodManager.SHOW_IMPLICIT);
            }
        });

    }

    public EditText getEditText(){
        return mValueText;
    }

    public String getValue(){
        return mValueText.getText().toString();
    }

    public void setValue(String aValue){
        mValueText.setText(aValue);
    }

    public void errorOn(){
        setBackgroundResource(mBackgroundErrorResId);
        mBtnClose.setVisibility(View.VISIBLE);
        invalidate();
    }

    public void errorOff(){
        setBackgroundResource(mBackgroundResId);
        mBtnClose.setVisibility(View.GONE);
        invalidate();
    }

    public void checkmarkOn(){
        setBackgroundResource(R.drawable.bg_text_normal);
        mBtnCheckmark.setVisibility(View.VISIBLE);
        invalidate();
    }

    public void checkmarkOff(){
        setBackgroundResource(R.drawable.bg_text_normal);
        mBtnCheckmark.setVisibility(View.GONE);
        invalidate();
    }

    public void setOnEditorActionListener(TextView.OnEditorActionListener aListener){
        mValueText.setOnEditorActionListener(aListener);
    }

    public boolean validate(){
        if(mValidator != null){
            boolean result = mValidator.validate(getValue());
            if(result){
                errorOff();
            } else{
                errorOn();
            }
            return result;
        } else{
            throw new RuntimeException("There is no FormValidator for this form.");
        }
    }

    public void setValidator(final FormValidator aValidator){
        mValidator = aValidator;
    }
    
    public static interface FormValidator{

        boolean validate(String aValue);
    }

    public static class EmailValidator implements FormValidator{

        public static EmailValidator getInstance(){
            return new EmailValidator();
        }

        @Override
        public boolean validate(final String aValue){
            return !TextUtils.isEmpty(aValue) && Validator.isValidEmail(aValue);
        }
    }

    public static class NotEmptyValidator implements FormValidator{

        public static NotEmptyValidator getInstance(){
            return new NotEmptyValidator();
        }

        @Override
        public boolean validate(final String aValue){
            return !TextUtils.isEmpty(aValue);
        }
    }
}
