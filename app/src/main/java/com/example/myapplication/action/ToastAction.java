package com.example.myapplication.action;

import androidx.annotation.StringRes;

import com.hjq.toast.ToastUtils;


public interface ToastAction {

    public static final String TAG = "ToastAction";

    default void toast(CharSequence text) {
        ToastUtils.show(text);
    }

    default void toast(@StringRes int id) {
        ToastUtils.show(id);
    }

   default   void toast(Object object) {
        ToastUtils.show(object);
    }
}