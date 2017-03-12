package in.celest.xash3d.dedicated;

import android.app.Service;
import android.app.Notification;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
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

    private String filesDir;
	
	Timer updateTimer = new Timer();

    static Process process = null;
    static String translator = "qemu";
    static String baseDir;
    static String cmdArgs;

    @Override
    public void onCreate() {
        super.onCreate();
        //Toast.makeText(this, "Service created",
        //        Toast.LENGTH_SHORT).show();
    }
    
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

        filesDir = DedicatedActivity.filesDir;
        translator = DedicatedActivity.translator;
        baseDir = DedicatedActivity.gamePath;

        isStarted = true;
        game = CommandParser.parseSingleParameter(intent.getStringExtra("argv"), "-game");
        if (game == "") game = "hl";
		updateNotification("Starting...");

		updateNotification("Extracting...");
        extractFiles();

        startAction();
		
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
        startAction();
        isStarted = false;
        //Toast.makeText(this, "Service destroyed",
        //        Toast.LENGTH_SHORT).show();
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
    {
        updateNotification(text);
    }

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

    public void startAction()
    {
        try
        {
            if( process != null )
            {
                //process.destroy();
                process = null;
                killAll(filesDir+"/qemu-i386-static");
                killAll(filesDir+"/tracker");
                killAll(filesDir+"/ubt");
                printText("\nKilling existing server!\n");
                //scroll.fullScroll(ScrollView.FOCUS_DOWN);
                return;
            }
            killAll(filesDir+"/qemu-i386-static");
            killAll(filesDir+"/ubt");
            if(translator == "none")
            {
                process = Runtime.getRuntime().exec("/system/bin/sh " + filesDir + "/start-x86.sh " + filesDir + " " + baseDir + " " + cmdArgs);
            }
            else
            if(translator == "qemu")
                process = Runtime.getRuntime().exec(filesDir+"/qemu-i386-static -E XASH3D_BASEDIR="+ baseDir +" "+ filesDir +"/xash " + cmdArgs);
            else
                process = Runtime.getRuntime().exec("/system/bin/sh " + filesDir + "/start-translator.sh " + filesDir + " " + translator + " " + baseDir + " " + cmdArgs);
            Thread t = new Thread(new Runnable() {
                public void run() {
                    class OutputCallback implements Runnable {
                        String str;
                        OutputCallback(String s) { str = s; }
                        public void run() {
                            printText(str);
                        }
                    }
                    try{

                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(process.getInputStream()));
                        int read;
                        String str = null;
                        while ((str = reader.readLine()) != null) {
                            printText(str);
                            //Handler h = new Handler();
                            //h.post(new OutputCallback(str));
                            //runOnUiThread(new OutputCallback(str));
                        }
                        reader.close();

                        // Waits for the command to finish.
                        if( process != null )
                            process.waitFor();
                    }
                    catch(Exception e)
                    {
                        printText(e.toString());
                        //Handler h = new Handler();
                        //h.post(new OutputCallback(e.toString()));
                        //runOnUiThread(new OutputCallback(e.toString()));
                    }
                    finally
                    {
                    }
                }
            });

            t.start();
        }
        catch(Exception e)
        {
            printText(e.toString()+"\n");
        }
    }

    private void killAll(String pattern)
    {
        try {
            Process p = Runtime.getRuntime().exec("ps");
            InputStream is = p.getInputStream();
            BufferedReader r = new BufferedReader(new InputStreamReader(is));
            String s;
            while ((s=r.readLine())!= null) {
                if (s.contains(pattern)) {
                    String pid = null;
                    for(int i=1; ;i++)
                    {
                        pid = s.split(" ")[i];
                        if(pid.length() > 2)
                            break;
                    }
                    printText("Found existing process:\n" + s + "\n" + "Pid: " + pid + "\n" );
                    Runtime.getRuntime().exec("kill -9 " + pid).waitFor();
                }
            }
            r.close();
            p.waitFor();
        }
        catch (Exception e) {
            printText(e.toString()+"\n");
            //scroll.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }
}
