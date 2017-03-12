package in.celest.xash3d.dedicated;

import android.app.Service;
import android.app.Notification;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import android.app.*;
import java.util.*;

/**
 * Created by Greg on 11.03.2017.
 */

public class DedicatedService extends Service {
    public static final String BROADCAST_ACTION =
            "com.example.android.threadsample.BROADCAST";
    // Defines the key for the status "extra" in an Intent
    public static final String EXTENDED_DATA_STATUS =
            "com.example.android.threadsample.STATUS";

    public static boolean isStarted;
    public static Notification serverNotify;
	
	public String game;
	
	Timer updateTimer = new Timer();

    @Override
    public void onCreate() {
        super.onCreate();
        Toast.makeText(this, "Service created",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        isStarted = true;
        game = CommandParser.parseSingleParameter(intent.getStringExtra("argv"), "-game");
        if (game == "") game = "hl";
		updateNotification("Starting...");

		updateNotification("Extracting...");
        extractFiles();
		
        Toast.makeText(this, "Server started",
                Toast.LENGTH_SHORT).show();
				
		updateTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					updateNotification(DedicatedStatics.lastMessage);
				}
			}, 1000, 100);
    }
	
	public void updateNotification(String str) 
	{
		Notification.Builder builder = new Notification.Builder(this)
			.setSmallIcon(R.drawable.logo).setContentTitle("XashDS: "+game).setContentText(str);
		builder.setContentIntent(PendingIntent.getActivity(this, 0, new Intent(getApplicationContext(), DedicatedActivity.class), 0));
        serverNotify = builder.build();

        startForeground(777, serverNotify);
	}

    @Override
    public void onDestroy() {
        super.onDestroy();
        isStarted = false;
        Toast.makeText(this, "Service destroyed",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void extractFiles()
    {
        try {
            File f = new File(DedicatedActivity.filesDir + "/xash");
            if (!f.exists() || (getPackageManager().getPackageInfo(getPackageName(), 0).versionCode != DedicatedActivity.mPref.getInt("lastversion", 1))) {
                //Unpack files now
                printText("Unpacking xash... ");
                //scroll.fullScroll(ScrollView.FOCUS_DOWN);
                unpackAsset("xash");
                printText("[OK]\nUnpacking xash_sse2 ...");
                //scroll.fullScroll(ScrollView.FOCUS_DOWN);
                unpackAsset("xash_sse2");
                printText("[OK]\nUnpacking start-translator.sh ...");
                //scroll.fullScroll(ScrollView.FOCUS_DOWN);
                unpackAsset("start-translator.sh");
                printText("[OK]\nUnpacking tracker ...");
                //scroll.fullScroll(ScrollView.FOCUS_DOWN);
                unpackAsset("tracker");
                printText("[OK]\nUnpacking qemu-i386-static ...");
                //scroll.fullScroll(ScrollView.FOCUS_DOWN);
                printText("[OK]\nSetting permissions.\n");
                //scroll.fullScroll(ScrollView.FOCUS_DOWN);
                Runtime.getRuntime().exec("chmod 777 " + DedicatedActivity.filesDir + "/xash " + DedicatedActivity.filesDir + "/xash_sse2 " + DedicatedActivity.filesDir + "/tracker " + DedicatedActivity.filesDir + "/qemu-i386-static").waitFor();
            }
        } catch (Exception e) {}
    }

    public void printText(String text)
    {   }

    public void unpackAsset(String name) throws Exception
    {
        AssetManager assetManager = getApplicationContext().getAssets();
        byte[] buffer = new byte[1024];
        int read;
        InputStream in = assetManager.open(name);
        OutputStream out = new FileOutputStream(DedicatedActivity.filesDir + "/" + name );
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
        out.close();
        in.close();
    }
}
