package com.example.meteor.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.view.MenuItem;

import com.example.meteor.AppConstant;
import com.example.meteor.adapter.ViewPager2BottomNavAdapter;
import com.example.meteor.base.BaseActivity;
import com.example.meteor.fragment.ApplicationManagementFragment;
import com.example.meteor.fragment.DataAnalysisFragment;
import com.example.meteor.fragment.FeedbackFragment;
import com.example.meteor.fragment.UserManagementFragment;
import com.example.myapplication.R;
import com.example.myapplication.databinding.ActivityAdminApplicationBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ManagementActivity extends BaseActivity<ActivityAdminApplicationBinding> {

    private BottomNavigationView bottomNavigationView;
    private ViewPager2 viewPager2;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                getSharedPreferences(AppConstant.preferenceFileName, MODE_PRIVATE).edit()
                        .putBoolean(AppConstant.loginState, false)
                        .putString(AppConstant.userEmail, "")
                        .putString(AppConstant.userPasswordSHA, "")
                        .apply();
                finish();
            }
        };
        timer.schedule(task, 1000);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initAdapter();
        initListener();
    }

    private void initListener() {
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.analysis_item:
                        viewPager2.setCurrentItem(0, true);
                        break;
                    case R.id.user_manage_item:
                        viewPager2.setCurrentItem(1, true);
                        break;
                    case R.id.feedback_item:
                        viewPager2.setCurrentItem(2, true);
                        break;
                    case R.id.apply_item:
                        viewPager2.setCurrentItem(3, true);
                        break;
                    default:
                        viewPager2.setCurrentItem(0,true);
                        break;
                }
                return true;
            }
        });

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                switch (position) {
                    case 0:
                        bottomNavigationView.setSelectedItemId(R.id.analysis_item);
                        break;
                    case 1:
                        bottomNavigationView.setSelectedItemId(R.id.user_manage_item);
                        break;
                    case 2:
                        bottomNavigationView.setSelectedItemId(R.id.feedback_item);
                        break;
                    case 3:
                        bottomNavigationView.setSelectedItemId(R.id.apply_item);
                        break;
                    default:
                        bottomNavigationView.setSelectedItemId(R.id.analysis_item);
                        viewPager2.setCurrentItem(0,true);
                        break;
                }
            }
        });
    }

    private void initAdapter() {
        ViewPager2BottomNavAdapter navAdapter = new ViewPager2BottomNavAdapter(this,
                initFragmentList());
        viewPager2.setAdapter(navAdapter);
    }

    private List<Fragment> initFragmentList() {
        List<Fragment> fragmentList = new ArrayList<>();
        fragmentList.add(new DataAnalysisFragment());
        fragmentList.add(new UserManagementFragment());
        fragmentList.add(new FeedbackFragment());
        fragmentList.add(new ApplicationManagementFragment());
        return fragmentList;
    }

    private void initView() {
        bottomNavigationView = binding.bootomnav;
        viewPager2 = binding.viewpager2;
    }
}