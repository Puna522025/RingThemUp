package sample.ringthemup;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;

public class BroadcastManager extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (null != intent && null != intent.getAction() && intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            AudioManager mobilemode = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
//   int streamMaxVolume = mobilemode.getStreamMaxVolume(AudioManager.STREAM_RING);
            switch (mobilemode.getRingerMode()) {
                case AudioManager.RINGER_MODE_SILENT:
                    Log.i("MyApp", "Silent mode");

                    mobilemode.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                    mobilemode.setStreamVolume(AudioManager.STREAM_RING, 4, 0);

                    break;
                case AudioManager.RINGER_MODE_VIBRATE:
                    Log.i("MyApp", "Vibrate mode");
                    mobilemode.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                    mobilemode.setStreamVolume(AudioManager.STREAM_RING, 4, 0);

                    break;
                case AudioManager.RINGER_MODE_NORMAL:
                    Log.i("MyApp", "Normal mode");
                    mobilemode.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                    mobilemode.setStreamVolume(AudioManager.STREAM_RING, 4, 0);

                    break;
            }

        }

    }
}
