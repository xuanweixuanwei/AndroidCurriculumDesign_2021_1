package com.example.meteor.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewbinding.ViewBinding;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;
//写数据分析版本的时候，后面的activity和fragment改用viewBinding
public class BaseActivity<T extends ViewBinding> extends AppCompatActivity {
    protected T binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initBinding();
    }

    private void initBinding() {
        Type superClass = getClass().getGenericSuperclass();

        if (superClass instanceof ParameterizedType) {
            Type type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
            Class<T> classType = (Class<T>) type;
            try {
                Method method = classType.getMethod("inflate", LayoutInflater.class);
                binding = (T) method.invoke(null, getLayoutInflater());
                setContentView(Objects.requireNonNull(binding).getRoot());
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }
}