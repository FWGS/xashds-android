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
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.Spinner;

//import android.content.Context;
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
import java.util.List;
import java.util.ArrayList;

public class DedicatedActivity extends Activity {
	static EditText cmdArgs;
	static EditText baseDir;
	static LinearLayout output;
	static ScrollView scroll;
	static SharedPreferences mPref;
	static Process process = null;
	static String filesDir;
	static String translator = "qemu";
	static boolean isScrolling;
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
        output = new LinearLayout(this);
        output.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        output.setOrientation(LinearLayout.VERTICAL);
        cmdArgs = new EditText(this);
        cmdArgs.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        baseDir = new EditText(this);
        baseDir.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		Button startButton = new Button(this);
		scroll = new ScrollView(this);
		
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
					if(!f.exists() || (getPackageManager().getPackageInfo(getPackageName(), 0).versionCode != mPref.getInt("lastversion", 1)) )
					{
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
						unpackAsset("qemu-i386-static");
						printText("[OK]\nSetting permissions.\n");
						//scroll.fullScroll(ScrollView.FOCUS_DOWN);
						Runtime.getRuntime().exec("chmod 777 " + filesDir + "/xash " + filesDir + "/xash_sse2 " + filesDir + "/tracker " + filesDir + "/qemu-i386-static").waitFor();
					}
					editor.putInt("lastversion", getPackageManager().getPackageInfo(getPackageName(), 0).versionCode);
					editor.commit();
					editor.apply();
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
						process = Runtime.getRuntime().exec("/system/bin/sh " + filesDir + "/start-x86.sh " + filesDir + " " + baseDir.getText().toString() + " " + cmdArgs.getText().toString());
					}
					else
					if(translator == "qemu")
						process = Runtime.getRuntime().exec(filesDir+"/qemu-i386-static -E XASH3D_BASEDIR="+ baseDir.getText().toString() +" "+ filesDir +"/xash " + cmdArgs.getText().toString());
					else
						process = Runtime.getRuntime().exec("/system/bin/sh " + filesDir + "/start-translator.sh " + filesDir + " " + translator + " " + baseDir.getText().toString() + " " + cmdArgs.getText().toString());
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
									runOnUiThread(new OutputCallback(str));
								}
								reader.close();
								
								// Waits for the command to finish.
								if( process != null )
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
					printText(e.toString()+"\n");
				}
            }
        });
		launcher.addView(titleView);
		launcher.addView(cmdArgs);
		launcher.addView(titleView2);
		launcher.addView(baseDir);
		// Add other options here
		if(System.getProperty("ro.product.cpu.abi") == "x86")
			translator = "none";
		else
		{
		final String[] list = listTranslators();
        if(list.length > 1)
        {
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_dropdown_item, list);
				adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				Spinner spinner = new Spinner(this);
				spinner.setAdapter(adapter);
				spinner.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
				spinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
					@Override
					public void onItemSelected(AdapterView<?> parent, View view, 
							int pos, long id) {
						translator = list[pos];
					}
					@Override
					public void onNothingSelected(AdapterView<?> parent) {
						translator = "qemu";
					}

				});
				launcher.addView(spinner);
			}
		}
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
    private String[] listTranslators()
    {
		try
		{
			String[] list = {
				"com.eltechs.es",
				"com.eltechs.erpg",
				"com.eltechs.doombyeltechs",
				"com.eltechs.hereticbyeltechs",
				"ru.buka.petka1"
				};
			List<String> list2 = new ArrayList<String>();
			list2.add("qemu");
			for(String s : list)
			{
				File f = new File("/data/data/" + s + "/lib/libubt.so");
				if(f.exists())
					list2.add(s);
			}
			
			return list2.toArray(new String[list2.size()]);
		}
		catch(Exception e)
		{
			String[] dummy = {"qemu"};
			return dummy;
		}
	}
	private void printText(String str)
	{
		TextView line = new TextView(this);
		line.setText(str);
		line.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		if(output.getChildCount() > 1024)
			output.removeViewAt(0);
		output.addView(line);
		if( !isScrolling )
		scroll.postDelayed(new Runnable() {
			@Override
			public void run() {
				scroll.fullScroll(ScrollView.FOCUS_DOWN);
				isScrolling = false;
			}
		}, 200);
		isScrolling = true;

		//croll.fullScroll(ScrollView.FOCUS_DOWN);
	}
}
