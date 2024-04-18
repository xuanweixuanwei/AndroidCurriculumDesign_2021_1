package com.example.meteor.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.meteor.adapter.UserInfoAdapter;
import com.example.meteor.base.BaseFragment;
import com.example.meteor.bean.AgeGroupCount;
import com.example.meteor.bean.DailyUserUsage;
import com.example.meteor.bean.GenderCount;
import com.example.meteor.roomDatabase.dao.AccountDao;
import com.example.meteor.roomDatabase.dao.UsageLogDao;
import com.example.meteor.roomDatabase.database.AppDatabase;
import com.example.meteor.util.enumType.Role;
import com.example.myapplication.R;
import com.example.myapplication.databinding.FragmentDataAnalysisBinding;

import org.w3c.dom.Text;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import timber.log.Timber;


public class DataAnalysisFragment extends BaseFragment<FragmentDataAnalysisBinding> {
    private AppDatabase db;
    private TextView textView;
    int users, ocr, asr, tts, mi, fe, male,allUsers,todayActiveUserCount;
    List<AgeGroupCount> ageGroupCounts;
    List<GenderCount> genderDistribution;
    DailyUserUsage dailyUsageSum;
    String date;
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = AppDatabase.getInstance(requireActivity());
        textView = binding.anaUserCount;
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                AccountDao accountDao = db.AccountDao();
                UsageLogDao usageLogDao = db.UsageLogDao();
                users = accountDao.countRoleUsers(Role.REGULAR_USER.getRole());
                List<AgeGroupCount> ageGroupCounts = accountDao.getAgeGroupCounts();
                List<GenderCount> genderDistribution = accountDao.getGenderDistribution();
                ocr = accountDao.getTotalOcrUsage();
                asr = accountDao.getTotalAsrUsage();
                tts = accountDao.getTotalTtsUsage();
                allUsers = accountDao.countUsers();
                todayActiveUserCount = usageLogDao.getDailyActiveUser(date);
                init();
                Iterator<GenderCount> iterator = genderDistribution.iterator();
                while (iterator.hasNext()) {
                    GenderCount g = iterator.next();
                    switch (g.sex) {
                        case 1:
                            male = g.count;
                            break;
                        case 2:
                            fe = g.count;
                            break;
                        default:
                            mi = g.count;
                            break;
                    }
                }
                 dailyUsageSum =
                        usageLogDao.getDailyUsageSum(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));

                Timber.e("run: %s", users);
            }
        });
        executorService.shutdown();

    }

    private void init() {
        requireActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setText(String.valueOf(users));
                binding.anaOcrCount.setText(String.valueOf(ocr));
                binding.anaTtsCount.setText(String.valueOf(tts));
                binding.anaAsrCount.setText(String.valueOf(asr));
                binding.anaFemaleCount.setText(String.valueOf(fe));
                binding.anaMaleCount.setText(String.valueOf(male));
                binding.anaOtherUserCount.setText(String.valueOf(mi));
                binding.anaCount.setText(String.valueOf(allUsers));
                binding.tvTodayTtsCount.setText(String.valueOf(dailyUsageSum.getTtsCount()));
                binding.tvTodayAsrCount.setText(String.valueOf(dailyUsageSum.getAsrCount()));
                binding.tvTodayOcrCount.setText(String.valueOf(dailyUsageSum.getOcrCount()));
                binding.tvTodayActiveUser.setText(String.valueOf(todayActiveUserCount));
            }
        });
    }
}