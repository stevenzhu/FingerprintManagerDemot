package lcb.ab.com.fingerprintmanagerdemot;

import android.Manifest;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v4.os.CancellationSignal;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    FingerprintManagerCompat manager;
    KeyguardManager mKeyguardManager;
    private FingerPrintUtils fingerPrintUiHelper;
    private final static int REQUEST_CODE_FINGER = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        manager = FingerprintManagerCompat.from(this);
        mKeyguardManager = (KeyguardManager) this.getSystemService(Context.KEYGUARD_SERVICE);
        Button btn= (Button) findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (isSatisfactionFingerprint()) {
                        Toast.makeText(MainActivity.this, "请进行指纹识别", Toast.LENGTH_SHORT).show();
                        initFingerPrint();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "系统版本过低不支持指纹识别...", Toast.LENGTH_SHORT).show();
                }


            }
        });
    }
    /**
     * 判断是否满足设置指纹的条件
     *
     * @return true 满足 false 不满足
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public boolean isSatisfactionFingerprint() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "请开启指纹识别权限", Toast.LENGTH_LONG).show();
            return false;
        }
        //硬件是否支持指纹识别
        if (!manager.isHardwareDetected()) {
            Toast.makeText(this, "您手机不支持指纹识别功能", Toast.LENGTH_LONG).show();
            return false;
        }

        //手机是否开启锁屏密码
        if (!mKeyguardManager.isKeyguardSecure()) {
            Toast.makeText(this, "请开启开启锁屏密码，并录入指纹后再尝试", Toast.LENGTH_LONG).show();
            return false;
        }
        //是否有指纹录入
        if (!manager.hasEnrolledFingerprints()) {
            Toast.makeText(this, "您还未录入指纹", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private void initFingerPrint() {
        fingerPrintUiHelper = new FingerPrintUtils(this);
        fingerPrintUiHelper.setFingerPrintListener(new FingerprintManagerCompat.AuthenticationCallback() {
            /**
             * 指纹识别成功
             *
             * @param result
             */
            @Override
            public void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result) {
                Toast.makeText(MainActivity.this, "指纹识别成功", Toast.LENGTH_SHORT).show();
                //Intent intent = new Intent();
                //intent.setClass(MainActivity.this, FingerActivity.class);
            }
            /**
             * 指纹识别失败调用
             */
            @Override
            public void onAuthenticationFailed() {
                Toast.makeText(MainActivity.this, "指纹识别失败", Toast.LENGTH_SHORT).show();
            }
            /**
             *
             * @param helpMsgId
             * @param helpString
             *
             */
            @Override
            public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
                Toast.makeText(MainActivity.this, helpString, Toast.LENGTH_SHORT).show();
            }

            /**
             * 多次指纹密码验证错误后，进入此方法；并且，不能短时间内调用指纹验证
             *
             * @param errMsgId  最多的错误次数
             * @param errString 错误的信息反馈
             */
            @Override
            public void onAuthenticationError(int errMsgId, CharSequence errString) {
                //具体等多长时间为测试
                Log.i("--------", "errMsgId=" + errMsgId + "-----errString" + errString);
                Toast.makeText(MainActivity.this, "指纹识别出错次数过多，稍后重试", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_FINGER) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "识别成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "识别失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    class FingerPrintUtils {

        private CancellationSignal mCancellationSignal;
        private FingerprintManagerCompat mFingerprintManager;

        public FingerPrintUtils(Activity activity) {
            mCancellationSignal = new CancellationSignal();
            mFingerprintManager = FingerprintManagerCompat.from(activity);
        }

        public void setFingerPrintListener(FingerprintManagerCompat.AuthenticationCallback callback) {
            mFingerprintManager.authenticate(null, 0, mCancellationSignal, callback, null);
        }

        public void stopsFingerPrintListener() {
            mCancellationSignal.cancel();
            mCancellationSignal = null;
        }
    }
}
