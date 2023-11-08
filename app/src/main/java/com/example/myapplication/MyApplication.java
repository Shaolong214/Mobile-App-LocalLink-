package com.example.myapplication;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class MyApplication extends Application {
    public static Context mContext;
    public static ArrayList<UserBean> allUserList;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        initUserFireBase();
    }

    public static void initUserFireBase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            allUserList = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                UserBean user = document.toObject(UserBean.class);
                                user.setId(document.getId());
                                allUserList.add(user);
                            }
                        } else {
                        }
                    }
                });

    }
}
