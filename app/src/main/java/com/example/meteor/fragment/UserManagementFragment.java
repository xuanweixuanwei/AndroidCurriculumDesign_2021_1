package com.example.meteor.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import com.example.meteor.activity.AddUserActivity;
import com.example.meteor.adapter.UserInfoAdapter;
import com.example.meteor.base.BaseFragment;
import com.example.meteor.bean.UserInfo;
import com.example.meteor.roomDatabase.database.AppDatabase;
import com.example.myapplication.databinding.FragmentUserManagementBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import timber.log.Timber;

public class UserManagementFragment extends BaseFragment<FragmentUserManagementBinding> {
    private RecyclerView userInfoRV;
    private AppDatabase db;
    private UserInfoAdapter userInfoAdapter;
    private List<UserInfo> allUserInfo = new ArrayList<>(10);

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = AppDatabase.getInstance(requireActivity());
        userInfoAdapter = new UserInfoAdapter(allUserInfo, requireActivity());
        userInfoRV = binding.itemRecyclerView;
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        userInfoRV.setLayoutManager(layoutManager);
        userInfoRV.setAdapter(userInfoAdapter);

        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                final List<UserInfo> loadedUserInfo = db.AccountDao().getAllUserInfo();
                // 将查询结果传递回主线程进行UI更新
                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        allUserInfo.clear();
                        allUserInfo.addAll(loadedUserInfo);
                        userInfoAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
        initListener();
    }

    private void initListener() {
        binding.btAddUserByAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(requireActivity(), AddUserActivity.class));
            }
        });

    }
}
