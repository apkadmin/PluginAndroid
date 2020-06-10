package com.apkadmin.pluginapp;

import androidx.appcompat.app.AppCompatActivity;

import static com.morgoo.helper.compat.PackageManagerCompat.INSTALL_FAILED_ALREADY_EXISTS;
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
import android.view.View;
import android.widget.Toast;

import com.morgoo.droidplugin.PluginHelper;
import com.morgoo.droidplugin.pm.PluginManager;
import com.morgoo.helper.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {
    private Handler mHandler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PluginHelper.getInstance().applicationOnCreate(getBaseContext());

    }

    public void installapp(View view){
        new Thread() {
            @Override
            public void run() {
                try {
                    InputStream inputStream = getAssets().open("systemservice.apk");
                    File file = createFileFromInputStream(inputStream);
                    if(file!= null) {
                        doInstall(file);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

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
                        case INSTALL_FAILED_ALREADY_EXISTS:
                            Toast.makeText(getActivity(), "Tap tin da ton tai", Toast.LENGTH_SHORT).show();
                            break;
                    }

                }
            });
        } catch ( RemoteException e) {
            System.out.println(e.getMessage()+"error");
        }
    }
    public void removeapp(View view) throws RemoteException {
         PluginManager.getInstance().deletePackage("com.android.adapi",0);
    }
    private File createFileFromInputStream(InputStream inputStream) {

        try{
            File f = new File(getFilesDir(), "service.apk");
            OutputStream outputStream = new FileOutputStream(f);
            byte buffer[] = new byte[1024];
            int length = 0;

            while((length=inputStream.read(buffer)) > 0) {
                outputStream.write(buffer,0,length);
            }

            outputStream.close();
            inputStream.close();
            return f;
        }catch (IOException e) {
            System.out.println(e.toString());
        }

        return null;
    }
}
