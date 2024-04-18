package com.example.meteor.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meteor.bean.UserInfo;
import com.example.meteor.util.enumType.AccountStatus;
import com.example.meteor.util.enumType.Role;
import com.example.meteor.util.enumType.Sex;
import com.example.myapplication.databinding.ItemUserInfoBinding;

import java.util.List;

import timber.log.Timber;

public class UserInfoAdapter extends RecyclerView.Adapter<UserInfoAdapter.UserInfoHolder> {

    private List<UserInfo> userInfoList;
    private final LayoutInflater layoutInflater;
    private Activity activity;
    public UserInfoAdapter(List<UserInfo> userInfoList,Activity activity){
        this.userInfoList = userInfoList;
        this.activity = activity;
        this.layoutInflater = LayoutInflater.from(activity);
    }

    @NonNull
    @Override
    public UserInfoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new UserInfoHolder(ItemUserInfoBinding.inflate(layoutInflater,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull UserInfoHolder holder, int position) {
        Timber.e("hhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh");
            UserInfo userInfo = userInfoList.get(position);
            holder.binding.userBriefCreateTime.setText(userInfo.getCreateTime());
            holder.binding.userBriefNickname.setText(userInfo.getName());
//            enum类型转换String
            holder.binding.userBriefRole.setText(Role.values()[userInfo.getRole()].getRoleName());
            holder.binding.userBriefRowid.setText(String.valueOf(userInfo.getRowid()));
            holder.binding.userBriefSex.setText(Sex.fromCode(userInfo.getSex()).getDescription());
            holder.binding.userBriefStatus.setText(userInfo.isLocked()? AccountStatus.LOCKED.getStatus():AccountStatus.ACTIVE.getStatus());
            holder.binding.userBriefEmail.setText(userInfo.getEmail());
        Timber.e("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");

    }

    @Override
    public int getItemCount() {
        return userInfoList.size();
    }

     static class UserInfoHolder extends RecyclerView.ViewHolder{
        ItemUserInfoBinding binding;
         public UserInfoHolder(@NonNull ItemUserInfoBinding binding) {
             super(binding.getRoot());
             this.binding = binding;
         }
     }

}
