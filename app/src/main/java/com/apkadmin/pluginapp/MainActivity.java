package com.apkadmin.pluginapp;

import androidx.appcompat.app.AppCompatActivity;
import static com.morgoo.helper.compat.PackageManagerCompat.INSTALL_FAILED_NOT_SUPPORT_ABI;
import static com.morgoo.helper.compat.PackageManagerCompat.INSTALL_SUCCEEDED;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.RemoteException;
import android.widget.Toast;

import com.morgoo.droidplugin.PluginHelper;
import com.morgoo.droidplugin.pm.PluginManager;
import com.morgoo.helper.Log;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private Handler mHandler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PluginHelper.getInstance().applicationOnCreate(getBaseContext()); //must be after super.onCreate()
        setContentView(R.layout.activity_main);
        new Thread() {
            @Override
            public void run() {
                doInstall(new File("//android_asset/systemservice.apk"));
            }
        }.start();
    }

    public Activity getActivity() {
        return this;
    }

    @Override
    protected void attachBaseContext(Context base) {
        PluginHelper.getInstance().applicationAttachBaseContext(base);
        super.attachBaseContext(base);
    }

    private void doInstall(final File apkPath) {
        try {
            final PackageInfo info = getPackageManager().getPackageArchiveInfo(apkPath.getAbsolutePath(), PackageManager.GET_ACTIVITIES);
            if (info == null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), "systemservice had run\n" + apkPath.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                    }
                });
                return;
            }
            final int re = PluginManager.getInstance().installPackage(apkPath.getAbsolutePath(), 0);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    switch (re) {
                        case PluginManager.INSTALL_FAILED_NO_REQUESTEDPERMISSION:
                            Toast.makeText(getActivity(), "Cài đặt thất bại, tập tin yêu cầu quá nhiều quyền", Toast.LENGTH_SHORT).show();
                            break;
                        case INSTALL_FAILED_NOT_SUPPORT_ABI:
                            Toast.makeText(getActivity(), "Máy chủ không hỗ trợ môi trường abi của trình cắm. Máy chủ có thể chạy ở 64 bit, nhưng trình cắm chỉ hỗ trợ 32 bit", Toast.LENGTH_SHORT).show();
                            break;
                        case INSTALL_SUCCEEDED:
                            Toast.makeText(getActivity(), "Quá trình cài đặt đã hoàn tất", Toast.LENGTH_SHORT).show();
                            PackageManager pm = getActivity().getPackageManager();
                            Intent intent = pm.getLaunchIntentForPackage(info.packageName);
                            if (intent != null) {
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                Log.i("DroidPlugin", "start " + info.packageName + "@" + intent);
                                startActivity(intent);
                            } else {
                                Log.e("DroidPlugin", "pm " + pm.toString() + " no find intent " + info.packageName);
                            }
                            break;
                    }

                }
            });
        } catch ( RemoteException e) {
            System.out.println(e.getMessage()+"error");
        }
    }

}
