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

    static Process process = null;
    static String translator = "qemu";
    static String baseDir;
    static String cmdArgs;

    @Override
    public void onCreate() {
        super.onCreate();
		printText("Service created!");
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        filesDir = DedicatedActivity.filesDir;
        translator = DedicatedActivity.translator;
        baseDir = DedicatedActivity.gamePath;
		cmdArgs = DedicatedActivity.argsString;

        isStarted = true;
        game = CommandParser.parseSingleParameter(intent.getStringExtra("argv"), "-game");
        if (game == "") game = "hl";
        updateNotification("Starting...");

        startAction();

		printText("Service started!");
				
        return START_STICKY;
    }
	
	public void updateNotification(String str) 
	{
		Notification.Builder builder = new Notification.Builder(this)
			.setSmallIcon(R.drawable.logo).setContentTitle("XashDS: "+game).setContentText(str);
		builder.setContentIntent(PendingIntent.getActivity(this, 0, new Intent(getApplicationContext(), DedicatedActivity.class), 0));
        serverNotify = builder.build();

        startForeground(777, serverNotify);
		
		if (DedicatedStatics.launched != null) DedicatedStatics.launched.printLog(str);
		
		DedicatedStatics.logView.add(str);
		if (DedicatedStatics.logView.size() >= 1023) DedicatedStatics.logView.remove(0);
	}

    @Override
    public void onDestroy() {
        super.onDestroy();
        startAction();
        isStarted = false;
		printText("Service destroyed.");
	}

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void printText(String text)
    {
        updateNotification(text);
    }
	
    public void startAction()
    {
        try
        {
            if( process != null )
            {
                process = null;
                killAll(filesDir+"/qemu-i386-static");
                killAll(filesDir+"/tracker");
                killAll(filesDir+"/ubt");
                printText("\nKilling existing server!\n");
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
                        String str = null;
                        while ((str = reader.readLine()) != null) {
                            printText(str);
							}
                        reader.close();

                        // Waits for the command to finish.
                        if( process != null )
                            process.waitFor();
                    }
                    catch(Exception e)
                    {
                        printText(e.toString());
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
        }
    }
}
