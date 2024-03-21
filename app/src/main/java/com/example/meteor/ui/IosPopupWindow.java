package com.example.meteor.ui;

import android.app.Activity;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.myapplication.R;

public class IosPopupWindow  extends PopupWindow {


    private Activity mActivity;

    private OnClickListener mOnClickListener;

    public void setPopupOnClickListener(OnClickListener mPopupOnClickListener){
        this.mOnClickListener  = mPopupOnClickListener;
    }

    public interface OnClickListener{
        void cameraOnClick();//拍照
        void albumOnClick();//相册
        void cancel();//取消
    }


    public IosPopupWindow(Activity context, OnClickListener itemsOnClick) {
        this.mActivity = context;
        mOnClickListener = (OnClickListener) itemsOnClick;
        // 设置布局文件
        View view = LayoutInflater.from(context).inflate(R.layout.ios_popup_window_layout, null);
        setContentView(view);

//        Animation animation = new ScaleAnimation(R.style.popupWindow_show);
//        animation.setDuration(500);
//        animation.setRepeatCount(Animation.INFINITE);

        // 为了避免部分机型不显示，我们需要重新设置一下宽高
        setWidth( RelativeLayout.LayoutParams.MATCH_PARENT);
        setHeight( RelativeLayout.LayoutParams.WRAP_CONTENT);

        //设置背景,这个没什么效果，不添加会报错.设置pop透明效果
        setBackgroundDrawable(new ColorDrawable(0x0000));

        // 设置pop获取焦点，如果为false点击返回按钮会退出当前Activity，如果pop中有Editor的话，focusable必须要为true
        //设置点击弹窗外隐藏自身
        setFocusable(true);

        // 设置pop可点击，为false点击事件无效，默认为true
        setTouchable(true);

        // 设置点击pop外侧消失，默认为false；在focusable为true时点击外侧始终消失
        setOutsideTouchable(true);

        //设置动画
        setAnimationStyle(R.style.popupWindow_show);

//        setAnimationStyle(R.style.ios_PopupWindow);

        //设置消失监听
        setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                dismissPopupWindow();
            }
        });

        //设置PopupWindow的View点击事件
        setOnPopupViewClick(view);


    }

    public void show(View parentView){
        //设置背景色
        setBackgroundAlpha(0.5f);
        //设置位置
        showAtLocation(parentView, Gravity.BOTTOM, 0, 66);
    }

    public void setBackgroundAlpha(float alpha) {
        WindowManager.LayoutParams lp = mActivity.getWindow().getAttributes();
        lp.alpha = alpha;
        mActivity.getWindow().setAttributes(lp);
    }

    /**
     * 弹窗点击事件
     *
     * @param view
     */
    private void setOnPopupViewClick(View view) {


        TextView tv_cancel,tv_choose_form_album,tv_take_photo;

        tv_take_photo = (TextView) view.findViewById(R.id.tv_take_photos);
        tv_take_photo.setOnClickListener(v -> {
            mOnClickListener.cameraOnClick();
            dismissPopupWindow();

        });

        tv_choose_form_album = (TextView) view.findViewById(R.id.tv_choose_from_album);
        tv_choose_form_album.setOnClickListener(v -> {
            mOnClickListener.albumOnClick();
            dismissPopupWindow();
        });

        tv_cancel = (TextView) view.findViewById(R.id.tv_cancel);
        tv_cancel.setOnClickListener(v -> {
            //取消
            dismissPopupWindow();
            mOnClickListener.cancel();
        });
    }


    private void dismissPopupWindow() {
        dismiss();
        setBackgroundAlpha(1f);
    }

}
