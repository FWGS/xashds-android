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
import java.io.*;

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
	
	static boolean newXash = true;

    private int iconRes;
	
	public static boolean canConnect = false;

    @Override
    public void onCreate() {
        super.onCreate();
		printText(DedicatedStatics.MESS_SERVICE_STARTING);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        filesDir = intent.getStringExtra("files");
        translator = intent.getStringExtra("translator");
        baseDir = intent.getStringExtra("path");
		cmdArgs = intent.getStringExtra("argv");

        iconRes = R.drawable.logo_wait;
		
        isStarted = true;
        game = CommandParser.parseSingleParameter(cmdArgs, "-game");
        if (game == "") game = "hl";
        updateNotification(DedicatedStatics.MESS_BINARIES_STARTING);

        startAction();

		printText(DedicatedStatics.MESS_SERVICE_STARTED);
				
        return START_STICKY;
    }
	
	public void updateNotification(String str) 
	{
		if (process != null)
			if (isRunning()) {
				if ((str.lastIndexOf("player server started") != -1)||(str.lastIndexOf("Game started") != -1)) {
					iconRes = R.drawable.logo_ok;
					canConnect = true;
					}
				if (str.lastIndexOf("SV_Shutdown") != -1) {
					iconRes = R.drawable.logo_wait;
					canConnect = false;
					}
			} else {
				iconRes = R.drawable.logo_error;
				canConnect = false;
			}

			canConnect = canConnect && isRunning() && isStarted;

		if (DedicatedStatics.launched != null) { 
			DedicatedStatics.launched.printLog(str);
			DedicatedStatics.launched.setCanConnect(canConnect);
		}

		DedicatedStatics.logView.add(str);
		if (DedicatedStatics.logView.size() >= 1023) DedicatedStatics.logView.remove(0);
		
		str = ConsoleView.removeColorcodes(str);
			
		Notification.Builder builder = new Notification.Builder(this).setSmallIcon(iconRes).setContentTitle("XashDS: "+game).setContentText(str);
		builder.setContentIntent(PendingIntent.getActivity(this, 0, new Intent(getApplicationContext(), DedicatedActivity.class), 0));
        serverNotify = builder.build();
		
        startForeground(777, serverNotify);
	}

    @Override
    public void onDestroy() {
		printText(DedicatedStatics.MESS_SERVICE_KILLING);
        super.onDestroy();
        startAction();
        isStarted = false;
		canConnect = false;
		if (DedicatedStatics.launched != null) DedicatedStatics.launched.setCanConnect(canConnect);
		printText(DedicatedStatics.MESS_SERVICE_KILLED);
		if (DedicatedStatics.launched != null) DedicatedStatics.launched.printInfo();
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
				killAll(filesDir+"/is3g1g");
                killAll(filesDir+"/ubt");
                printText("\nKilling existing server!\n");
                return;
            }
            killAll(filesDir+"/qemu-i386-static");
            killAll(filesDir+"/ubt");
            if(translator.equals("none"))
            {
                process = Runtime.getRuntime().exec("/system/bin/sh " + filesDir + "/start-x86.sh " + filesDir + " " + baseDir + " " + DedicatedStatics.XASH_BINARY_SSE + " " + cmdArgs);
            }
            else
            if(translator.equals("qemu"))
                process = Runtime.getRuntime().exec(filesDir+"/qemu-i386-static -E XASH3D_BASEDIR="+ baseDir +" "+ filesDir +"/" + DedicatedStatics.XASH_BINARY + " " + cmdArgs);
            else
                process = Runtime.getRuntime().exec("/system/bin/sh " + filesDir + "/start-translator.sh " + filesDir + " " + translator + " " + baseDir + " " + DedicatedStatics.XASH_BINARY + " " + cmdArgs);
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
						BufferedReader errorReader = new BufferedReader(
								new InputStreamReader(process.getErrorStream()));
                        String str = null;
						String errstr = null;
                        while (((str = reader.readLine()) != null) || 
								((errstr = errorReader.readLine()) != null)) {
                            if (str != null) printText(str);
							if (errstr != null) printText(errstr);
							}
                        reader.close();
						errorReader.close();
						
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
	
	public static boolean isRunning() {
		try {
			process.exitValue();
			return false;
		} catch (Exception e) {
			return true;
		}
	}
	
	static void sendCmd(String com)
	{
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
		try {
			writer.write(com);
			writer.newLine();
			writer.flush();
		} catch (Exception e) {}
	}
}
