package sample.ringthemup;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int PICK_CONTACT = 1;
    private static final int MY_PERMISSIONS_REQUEST = 11;
    private static final int MY_PERMISSIONS_REQUEST_READ_SMS = 522;
    public final static float SCALE_FACTOR = 13f;
    public final static int ANIMATION_DURATION = 300;
    public final static int MINIMUN_X_DISTANCE = 200;
    EditText phone;
    TextInputLayout phoneLayout;
    Toolbar toolbar;
    private Tracker mTracker;
    private ImageView button, imageArrow;
    private static final String TAG = "RingMeUpHome";
    private RelativeLayout rlAnimation;
    private float mFabSize;
    private boolean mRevealFlag = false;
    Animation slideOutLeft,slideOutLeftLayout;
    float xx,yy,x,y;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Window window = this.getWindow();
        window.setStatusBarColor((ContextCompat.getColor(this, R.color.accentDarker)));

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent));

        ImageView contactsImage = (ImageView) findViewById(R.id.contactsImage);

        ImageView whatsappImage = (ImageView) findViewById(R.id.whatsappImage);

        button = (ImageView) findViewById(R.id.button);
        imageArrow = (ImageView) findViewById(R.id.imageArrow);
        xx =  button.getTranslationX();
        yy = button.getTranslationY();
        x = button.getX();
        y = button.getY();
        phoneLayout = (TextInputLayout) findViewById(R.id.contactLabel);
        ImageView button = (ImageView) findViewById(R.id.button);
        ImageView shareImage = (ImageView) findViewById(R.id.shareImage);

        phone = (EditText) findViewById(R.id.contact);
        rlAnimation = (RelativeLayout) findViewById(R.id.rlAnimation);
        mFabSize = getResources().getDimensionPixelSize(R.dimen.fab_size);
        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();

        slideOutLeft = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_out_left);
        slideOutLeftLayout = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.layout_anim);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_SMS)
                    != PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.RECEIVE_SMS)
                    != PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.READ_CONTACTS)
                    != PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS,
                                Manifest.permission.SEND_SMS, Manifest.permission.READ_CONTACTS},
                        MY_PERMISSIONS_REQUEST);
            } else {
                checkNotificationAccess();
            }
        } else {
            checkNotificationAccess();
        }
        contactsImage.setOnClickListener(this);
        button.setOnClickListener(this);
        shareImage.setOnClickListener(this);
        whatsappImage.setOnClickListener(this);

        LocalBroadcastManager.getInstance(this).registerReceiver(onNotice, new IntentFilter("WhatsappMessage"));
        //displayADD();
    }

   /* private void displayADD() {
        AdView adView = (AdView) this.findViewById(R.id.adView);
        // Request for Ads
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }*/

    @Override
    protected void onResume() {
        super.onResume();
        mTracker.setScreenName(TAG);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    private BroadcastReceiver onNotice = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String pack = intent.getStringExtra("package");
            String notificationMessages = intent.getStringExtra("notificationMessages");
            if (pack.equalsIgnoreCase("com.whatsapp") && notificationMessages.contains("you were not picking up the phone and we were worried sick. So just turned your ringtone volume up a bit")) {
                changeSilentMode(context);
            }
        }
    };

    private void changeSilentMode(Context context) {
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.button:
               /* if (!TextUtils.isEmpty(phone.getText())) {
                    String num = phone.getText().toString();
                    if (num.length() <= 10) {
                        displayAlert();
                    } else {
                        if (num.length() == 11 && num.startsWith("0")) {
                            displayAlert();
                        } else {
                            Toast.makeText(getApplicationContext(), "Please enter a valid phone number!",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Please enter a valid phone number!",
                            Toast.LENGTH_LONG).show();
                }
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                break;*/
                onFabPressed(view);
            case R.id.contactsImage:
                //openContacts();

                break;

            case R.id.shareImage:
                shareApp();
                break;

            case R.id.whatsappImage:
                //whatsappNotification();
                restore();
                break;
        }
    }

    private void restore() {
        button.setVisibility(View.VISIBLE);
        button.setScaleX(1);
        button.setScaleY(1);
        RelativeLayout.LayoutParams rel_btn = new RelativeLayout.LayoutParams(
                50, 50);

        rel_btn.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        rel_btn.rightMargin = 30;
        button.refreshDrawableState();
        button.invalidate();
        button.setLayoutParams(rel_btn);
        //rlAnimation.setBackgroundColor(ContextCompat.getColor(this,android.R.color.transparent));
    }

    private void onFabPressed(View view) {
        final float startX = button.getX();

        AnimatorPath path = new AnimatorPath();
        path.moveTo(0, 0);
        path.curveTo(-200, 200, -400, 100, -600, 50);
        final ObjectAnimator anim = ObjectAnimator.ofObject(this, "fabLoc",
                new PathEvaluator(), path.getPoints().toArray());

        anim.setInterpolator(new AccelerateInterpolator());
        anim.setDuration(ANIMATION_DURATION);
        anim.start();

        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float xx =Math.abs(startX - button.getX());
                if (xx > MINIMUN_X_DISTANCE) {
                    if (!mRevealFlag) {
                        // rl123.setY(rl123.getY() + mFabSize / 2);

                        button.animate()
                                .scaleXBy(SCALE_FACTOR)
                                .scaleYBy(SCALE_FACTOR)
                                .setListener(mEndRevealListener)
                                .setDuration(ANIMATION_DURATION);

                        mRevealFlag = true;
                    }
                }
            }
        });

    }
    public void setFabLoc(PathPoint newLoc) {
        button.setTranslationX(newLoc.mX);

        if (mRevealFlag)
            button.setTranslationY(newLoc.mY - (mFabSize / 2));
        else
            button.setTranslationY(newLoc.mY);
    }

    private AnimatorListenerAdapter mEndRevealListener = new AnimatorListenerAdapter() {

        @Override
        public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);

            button.setVisibility(View.GONE);
            rlAnimation.setBackgroundColor(getResources()
                    .getColor(R.color.colorAccent));

            ViewPropertyAnimator animator = imageArrow.animate()
                    .scaleX(1).scaleY(1)
                    .setDuration(ANIMATION_DURATION);

            animator.setStartDelay(1 * 50);
            animator.start();

            imageArrow.startAnimation(slideOutLeft);
            //rlAnimation.startAnimation(slideOutLeftLayout);

            int colorFrom = getResources().getColor(R.color.colorAccent);
            int colorTo = getResources().getColor(android.R.color.transparent);
            ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
            colorAnimation.setDuration(250);// milliseconds
            colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                @Override
                public void onAnimationUpdate(ValueAnimator animator) {
                    rlAnimation.setBackgroundColor((int) animator.getAnimatedValue());
                }
            });
            colorAnimation.start();

            slideOutLeft.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                restore();

                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }
    };
    private void whatsappNotification() {

        if (Settings.Secure.getString(this.getContentResolver(), "enabled_notification_listeners").contains(getApplicationContext().getPackageName())) {
            /*try {
                Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.whatsapp");
                launchIntent.setType("text/plain");
                String text = "Puneet Kapoor is trying to check";
                launchIntent.putExtra(Intent.EXTRA_TEXT, text);
                startActivity(launchIntent);
            } catch (Exception e) {
                Toast.makeText(this, "Oops!! Something went wrong. Please try again after sometime.", Toast.LENGTH_LONG).show();
            }*/

            //Check if package exists or not. If not then code
            //in catch block will be called
            //service is enabled do something
            try {
                PackageManager pm = getPackageManager();
                Intent waIntent = new Intent(Intent.ACTION_SEND);
                waIntent.setType("text/plain");
                String text = getString(R.string.sendWhatsappSMS);
                PackageInfo info = pm.getPackageInfo("com.whatsapp", PackageManager.GET_META_DATA);
                waIntent.setPackage("com.whatsapp");

                waIntent.putExtra(Intent.EXTRA_TEXT, text);
                startActivity(Intent.createChooser(waIntent, "Send via"));
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this, "WhatsApp not Installed", Toast.LENGTH_SHORT)
                        .show();
            }
        } else {
            //service is not enabled try to enabled by calling...
            new AlertDialog.Builder(MainActivity.this)
                    .setMessage("Please enable notification access for this app!!")
                    .setCancelable(false)
                    .setIcon(R.drawable.appicon)
                    .setPositiveButton("ok",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {

                                    Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    getApplicationContext().startActivity(intent);
                                    dialog.cancel();
                                }
                            }).show();
        }
    }

    private void shareApp() {

        String shareBody = getString(R.string.share);

        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "App download");
        //sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);

        String link = "http://play.google.com/store/apps/details?id=sample.myapplication";
        sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody + " " + link);

        startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }

    private void openContacts() {
        phone.setText("");
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, PICK_CONTACT);

    }

    private void sendSMS() {
        if (!TextUtils.isEmpty(phone.getText())) {
            String num = phone.getText().toString();
            if (num.length() <= 10) {
                sendSMSnow(num);
            } else {
                if (num.length() == 11 && num.startsWith("0")) {
                    sendSMSnow(num);
                } else {
                    phoneLayout.setErrorEnabled(true);
                    Toast.makeText(getApplicationContext(), "Please enter a valid phone number!",
                            Toast.LENGTH_LONG).show();
                }
            }
        } else {
            Toast.makeText(getApplicationContext(), "Please enter a valid phone number!",
                    Toast.LENGTH_LONG).show();
        }
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }

    private void sendSMSnow(final String num) {
        String msg = getString(R.string.sendSMS);

        //Getting intent and PendingIntent instance
       /* Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent sentPI = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);*/

        String SENT = "sent";
        String DELIVERED = "delivered";

        Intent sentIntent = new Intent(SENT);
     /*Create Pending Intents*/
        PendingIntent sentPI = PendingIntent.getBroadcast(
                getApplicationContext(), 0, sentIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Intent deliveryIntent = new Intent(DELIVERED);

        PendingIntent deliverPI = PendingIntent.getBroadcast(
                getApplicationContext(), 0, deliveryIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        /* Register for SMS send action */
        registerReceiver(new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                String result = "";

                switch (getResultCode()) {

                    case Activity.RESULT_OK:
                        result = "Transmission successful";
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        result = "Transmission failed. Please check the number.";
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        result = "Radio off";
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        result = "No PDU defined. Please check the number.";
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        result = "No service";
                        break;
                }

                Toast.makeText(getApplicationContext(), result,
                        Toast.LENGTH_SHORT).show();
            }

        }, new IntentFilter(SENT));
     /* Register for Delivery event */
        registerReceiver(new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                Toast.makeText(getApplicationContext(), "Delivered to - " + num,
                        Toast.LENGTH_SHORT).show();
            }

        }, new IntentFilter(DELIVERED));


        try {
            //Get the SmsManager instance and call the sendTextMessage method to send message
            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(num, null, msg, sentPI, deliverPI);
           /* Toast.makeText(getApplicationContext(), "Message Sent!",
                    Toast.LENGTH_LONG).show();*/
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Message not Sent!",
                    Toast.LENGTH_LONG).show();
        }
        /*Toast.makeText(getApplicationContext(), "Message Sent successfully!",
                Toast.LENGTH_LONG).show();*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_CONTACT) {
            if (resultCode == RESULT_OK) {

                Cursor cursor = null;
                String phoneNumber = "";
                List<String> allNumbers = new ArrayList<String>();
                int phoneIdx = 0;
                try {
                    Uri result = data.getData();
                    String id = result.getLastPathSegment();
                    cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?", new String[]{id}, null);
                    phoneIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA);
                    if (cursor.moveToFirst()) {
                        while (cursor.isAfterLast() == false) {
                            phoneNumber = cursor.getString(phoneIdx);
                            allNumbers.add(phoneNumber);
                            cursor.moveToNext();
                        }
                    } else {
                        //no results actions
                    }
                } catch (Exception e) {
                    //error actions
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                    final CharSequence[] items = allNumbers.toArray(new String[allNumbers.size()]);
                    if (items.length > 0) {
                        String selectedNumber = items[0].toString();
                        selectedNumber = selectedNumber.replaceAll("[-() ]+", "");
                        selectedNumber = selectedNumber.replace("+91", "");
                        phone.setText(selectedNumber);
                    }
                }
            }
        } else {
            Toast.makeText(getApplicationContext(), "Error fetching contacts. Please try again!",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void displayAlert() {
        // TODO Auto-generated method stub

        new AlertDialog.Builder(MainActivity.this)
                .setMessage("If the user is having this app installed, this SMS will increase the ringing volume for their device." +
                        "\nAre you sure you want to go ahead?")
                .setCancelable(false)
                .setIcon(R.drawable.appicon)
                .setPositiveButton("ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                sendSMS();
                                dialog.cancel();
                            }
                        })
                .setNegativeButton("cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                dialog.cancel();
                            }
                        })

                .show();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted!!
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.RECEIVE_SMS)
                            != PackageManager.PERMISSION_GRANTED) {

                        // Should we show an explanation?
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                                Manifest.permission.READ_SMS)) {

                            // Show an expanation to the user *asynchronously* -- don't block
                            // this thread waiting for the user's response! After the user
                            // sees the explanation, try again to request the permission.

                        } else {

                            // No explanation needed, we can request the permission.

                            ActivityCompat.requestPermissions(this,
                                    new String[]{Manifest.permission.READ_SMS},
                                    MY_PERMISSIONS_REQUEST_READ_SMS);

                            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                            // app-defined int constant. The callback method gets the
                            // result of the request.
                        }
                    }
                } else {

                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.RECEIVE_SMS)
                            != PackageManager.PERMISSION_GRANTED) {

                        // Should we show an explanation?
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                                Manifest.permission.RECEIVE_SMS)) {

                            // Show an expanation to the user *asynchronously* -- don't block
                            // this thread waiting for the user's response! After the user
                            // sees the explanation, try again to request the permission.

                        } else {

                            // No explanation needed, we can request the permission.

                            ActivityCompat.requestPermissions(this,
                                    new String[]{Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS,
                                            Manifest.permission.SEND_SMS, Manifest.permission.READ_CONTACTS},
                                    MY_PERMISSIONS_REQUEST);

                            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                            // app-defined int constant. The callback method gets the
                            // result of the request.
                        }
                        // permission denied, boo! Disable the
                        // functionality that depends on this permission.
                    }
                }
                checkNotificationAccess();
                return;
            }
        }
    }

    private void checkNotificationAccess() {
        if (!Settings.Secure.getString(this.getContentResolver(), "enabled_notification_listeners").contains(getApplicationContext().getPackageName())) {
            //service is not enabled try to enabled by calling...
            new AlertDialog.Builder(MainActivity.this)
                    .setMessage("Please enable notification access for this app!!")
                    .setCancelable(false)
                    .setIcon(R.drawable.appicon)
                    .setPositiveButton("ok",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {

                                    Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    getApplicationContext().startActivity(intent);
                                    dialog.cancel();
                                }
                            }).show();

        }
    }


}