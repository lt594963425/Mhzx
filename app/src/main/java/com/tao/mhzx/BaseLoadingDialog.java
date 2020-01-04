package com.tao.mhzx;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;


public class BaseLoadingDialog extends Dialog {
    public Context context;

    public BaseLoadingDialog(Context context) {
        super(context, R.style.base_picture_alert_dialog);
        this.context = context;
        setCancelable(true);
        setCanceledOnTouchOutside(false);
        Window window = getWindow();
    }


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.common_loading);
    }
}