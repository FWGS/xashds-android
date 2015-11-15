package in.celest.xash3d.dedicated;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.content.Intent;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Button;
import android.widget.TextView;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class DedicatedActivity extends Activity {
	static EditText cmdArgs;
	static EditText baseDir;
	static TextView output;
	static SharedPreferences mPref;
	static Process process = null;
	static String filesDir;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        filesDir = getApplicationContext().getFilesDir().getPath();
        // Build layout
        LinearLayout launcher = new LinearLayout(this);
        launcher.setOrientation(LinearLayout.VERTICAL);
        launcher.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        TextView titleView = new TextView(this);
        titleView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        titleView.setText("Command-line arguments");
        titleView.setTextAppearance(this, android.R.attr.textAppearanceLarge);
        TextView titleView2 = new TextView(this);
        titleView2.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        titleView2.setText("Game path");
        titleView2.setTextAppearance(this, android.R.attr.textAppearanceLarge);
        output = new TextView(this);
        output.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        output.setTextAppearance(this, android.R.attr.textAppearanceLarge);
        cmdArgs = new EditText(this);
        cmdArgs.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        baseDir = new EditText(this);
        baseDir.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		Button startButton = new Button(this);
		ScrollView scroll = new ScrollView(this);
		
		// Set launch button title here
		startButton.setText("Launch!");
		LayoutParams buttonParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		buttonParams.gravity = 5;
		startButton.setLayoutParams(buttonParams);
		startButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
				try
				{
					SharedPreferences.Editor editor = mPref.edit();
					editor.putString("argv", cmdArgs.getText().toString());
					editor.putString("basedir", baseDir.getText().toString());
					File f = new File(filesDir+"/xash");
					if(!f.exists() || (getPackageManager().getPackageInfo(getPackageName(), 0).versionCode != mPref.getInt("version", 1)) )
					{
						//Unpack files now
						output.append("Unpacking xash... ");
						unpackAsset("xash");
						output.append("[OK]\nUnpacking qemu-i386-static ...");
						unpackAsset("qemu-i386-static");
						output.append("[OK]\nSetting permissions.\n");
						Runtime.getRuntime().exec("chmod 777 " + filesDir + "/xash "  + filesDir + "/qemu-i386-static").waitFor();
					}
					editor.putInt("lastversion", getPackageManager().getPackageInfo(getPackageName(), 0).versionCode);
					editor.commit();
					editor.apply();
					if( process != null )
					{
						process.destroy();
						process = null;
						output.append("\nKilling existing server!\n");
						return;
					}
					killAll(filesDir+"/qemu-i386-static");
					process = Runtime.getRuntime().exec(filesDir+"/qemu-i386-static -E XASH3D_BASEDIR="+ baseDir.getText().toString() +" "+ filesDir +"/xash " + cmdArgs.getText().toString());
					Thread t = new Thread(new Runnable() {
						public void run() {
							class OutputCallback implements Runnable {
									String str;
									OutputCallback(String s) { str = s; }
									public void run() {
										output.append(str+"\n");
									}
								}
							try{
								
								BufferedReader reader = new BufferedReader(
										new InputStreamReader(process.getInputStream()));
								int read;
								String str = null;
								while ((str = reader.readLine()) != null) {
									runOnUiThread(new OutputCallback(str));
								}
								reader.close();
								
								// Waits for the command to finish.
								process.waitFor();
							}
							catch(Exception e)
							{
								runOnUiThread(new OutputCallback(e.toString()));
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
					output.append(e.toString()+"\n");
				}
            }
        });
		launcher.addView(titleView);
		launcher.addView(cmdArgs);
		launcher.addView(titleView2);
		launcher.addView(baseDir);
		// Add other options here
		launcher.addView(startButton);
		scroll.addView(output);
		launcher.addView(scroll);
        setContentView(launcher);
		mPref = getSharedPreferences("dedicated", 0);
		cmdArgs.setText(mPref.getString("argv","-dev 5 -dll dlls/hl.dll"));
		baseDir.setText(mPref.getString("basedir","/sdcard/xash"));
	}
	private void unpackAsset(String name) throws Exception {
		AssetManager assetManager = getApplicationContext().getAssets();
		byte[] buffer = new byte[1024];
		int read;
		InputStream in = assetManager.open(name);
		OutputStream out = new FileOutputStream(filesDir + "/" + name );
		while((read = in.read(buffer)) != -1){
			out.write(buffer, 0, read);
		}
		out.close(); 
		in.close();
	}
	private void killAll(String pattern) {
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
				output.append("Found existing process:\n" + s + "\n" + "Pid: " + pid + "\n" );
				Runtime.getRuntime().exec("kill -9 " + pid).waitFor();
            }
        }
        r.close();
        p.waitFor();
    } catch (Exception e) {
        output.append(e.toString()+"\n");
    }
}
	
}
