package in.celest.xash3d.dedicated;

import android.app.Activity;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.ArrayList;
import android.os.Environment;

public class DedicatedActivity extends Activity {
	//views for launcher screen
	static EditText cmdArgs;
	static EditText baseDir;
	static LinearLayout output;
	static ScrollView scroll;
	//views for servrr master screen
	static EditText modDir;
	static EditText serverDlls;
	static EditText serverMap;
	static EditText rconPass;
	
	static SharedPreferences mPref;
	static Process process = null;
	static String filesDir;
	static String translator = "qemu";
	static boolean isScrolling;

	static String argsString;
	static String gamePath;
	
	static LayoutParams buttonParams;

	static boolean isRunned = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);

		buttonParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		buttonParams.gravity = 5;

		isRunned = DedicatedService.isStarted;

		initLauncher();
	}

	public void unpackAsset(String name) throws Exception
	{
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

	void loadSettings()
	{
		mPref = getSharedPreferences("dedicated", 0);
		argsString = mPref.getString("argv","-dev 5 -dll dlls/hl.dll");
		cmdArgs.setText(argsString);
		gamePath = mPref.getString("basedir","/sdcard/xash");
		baseDir.setText(gamePath);
	}
	
	void pushLauncherSettings() 
	{
		argsString = cmdArgs.getText().toString();
		gamePath = baseDir.getText().toString();
	}
	
	void pushMasterSettings()
	{
		argsString = makeMasterArgs();
	}
	
	void saveSettings() 
	{
		SharedPreferences.Editor editor = mPref.edit();
		editor.putString("argv", argsString);
		editor.putString("basedir", gamePath);
		try {
			editor.putInt("lastversion", getPackageManager().getPackageInfo(getPackageName(), 0).versionCode);
		} catch (Exception e) {}
		editor.commit();
		editor.apply();
		printText("Settings saved!");
	}

	void initLauncher()
	{
		setTitle(R.string.launcher_head);

		filesDir = getApplicationContext().getFilesDir().getPath();
		// Build layout
		LinearLayout launcher = new LinearLayout(this);
		launcher.setOrientation(LinearLayout.VERTICAL);
		launcher.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		TextView titleView = new TextView(this);
		titleView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		titleView.setText(R.string.l_args);
		titleView.setTextAppearance(this, android.R.attr.textAppearanceLarge);
		TextView titleView2 = new TextView(this);
		titleView2.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		titleView2.setText(R.string.l_path);
		titleView2.setTextAppearance(this, android.R.attr.textAppearanceLarge);
		output = new LinearLayout(this);
		output.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		output.setOrientation(LinearLayout.VERTICAL);
		cmdArgs = new EditText(this);
		cmdArgs.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		baseDir = new EditText(this);
		baseDir.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

		LinearLayout button_bar = new LinearLayout(this);
		button_bar.setOrientation(LinearLayout.HORIZONTAL);
		button_bar.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		final Button startButton = new Button(this);
		scroll = new ScrollView(this);
		Button externalPicker = new Button(this);

		externalPicker.setText(R.string.b_sd);
		externalPicker.setLayoutParams(buttonParams);
		externalPicker.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				printText("Trying to access...");

				Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
				startActivityForResult(intent, 42);
				
				initLauncher();
			}
		});

		Button launch_master = new Button(this);
		launch_master.setText(R.string.b_master);
		launch_master.setLayoutParams(buttonParams);
		launch_master.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				pushLauncherSettings();
				saveSettings();
				initMaster();
			}
		});

		// Set launch button title here
		startButton.setText(isRunned?R.string.b_start_stop:R.string.b_start_launch);
		startButton.setLayoutParams(buttonParams);
		startButton.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try
				{
					isRunned = !isRunned;
					startButton.setText(isRunned?R.string.b_start_stop:R.string.b_start_launch);

					if (isRunned) {
						startServer();
					} else {
						stopServer();
					}


					pushLauncherSettings();
					saveSettings();
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
		launcher.addView(launch_master);
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
		button_bar.addView(startButton);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)  //SD card pick API enabled on 5.0(v21) and higher
			button_bar.addView(externalPicker);
		launcher.addView(button_bar);
		scroll.addView(output);
		launcher.addView(scroll);

		loadSettings();

		setContentView(launcher);
		
		getActionBar().setDisplayHomeAsUpEnabled(false);
	}

	void initMaster() {
		setTitle(R.string.master_head);

		ScrollView masterScroll = new ScrollView(this);
		
		LinearLayout master = new LinearLayout(this);
		master.setOrientation(LinearLayout.VERTICAL);
		master.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		
		TextView gameNameView = new TextView(this);
		gameNameView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		gameNameView.setText(R.string.l_game);
		gameNameView.setTextAppearance(this, android.R.attr.textAppearanceLarge);

		TextView gameDllsView = new TextView(this);
		gameDllsView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		gameDllsView.setText(R.string.l_dlls);
		gameDllsView.setTextAppearance(this, android.R.attr.textAppearanceLarge);
		
		TextView gameMapView = new TextView(this);
		gameMapView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		gameMapView.setText(R.string.l_map);
		gameMapView.setTextAppearance(this, android.R.attr.textAppearanceLarge);

		TextView serverPassView = new TextView(this);
		serverPassView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		serverPassView.setText(R.string.l_rcon);
		serverPassView.setTextAppearance(this, android.R.attr.textAppearanceLarge);
		
		modDir = new EditText(this);
		modDir.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		modDir.setHint(R.string.h_game);

		serverDlls = new EditText(this);
		serverDlls.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		serverDlls.setHint(R.string.h_dlls);
		
		serverMap = new EditText(this);
		serverMap.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		serverMap.setHint(R.string.h_map);
		
		rconPass = new EditText(this);
		rconPass.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		
		Button saveButton = new Button(this);
		saveButton.setLayoutParams(buttonParams);
		saveButton.setText(R.string.l_save);
		saveButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				pushMasterSettings();
				saveSettings();
			}
		});
		
		masterScroll.addView(master);
		
		master.addView(saveButton);
		master.addView(gameNameView);
		master.addView(modDir);
		master.addView(gameDllsView);
		master.addView(serverDlls);
		master.addView(gameMapView);
		master.addView(serverMap);
		master.addView(serverPassView);
		master.addView(rconPass);

		loadSettings();
		parseArgsToMaster(argsString);

		setContentView(masterScroll); //let us see that Master-Scroll!
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId()) {
			case android.R.id.home:
				initLauncher();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	public String makeMasterArgs() 
	{
		String ret = "";
		if (!modDir.getText().toString().equals("")) ret += CommandParser.makeParamArgString(modDir.getText().toString(), "-game");
		if (!serverDlls.getText().toString().equals("")) ret += CommandParser.makeParamArgString(serverDlls.getText().toString(), "-dll");
		if (!serverMap.getText().toString().equals("")) ret += CommandParser.makeParamArgString(serverMap.getText().toString(), "+map");
		if (!rconPass.getText().toString().equals("")) ret += CommandParser.makeParamArgString(rconPass.getText().toString(), "+rcon_password");
		return ret;
	}
	
	public void parseArgsToMaster(String args) {
		modDir.setText(CommandParser.parseSingleParameter(args, "-game"));
		serverDlls.setText(CommandParser.parseMultipleParameter(args, "-dll"));
		serverMap.setText(CommandParser.parseSingleParameter(args, "+map"));
		rconPass.setText(CommandParser.parseSingleParameter(args, "+rcon_password"));
	}

	public void startServer()
	{
		Intent dedicatedServer = new Intent(DedicatedActivity.this, DedicatedService.class);
		dedicatedServer.putExtra("argv", argsString);
		dedicatedServer.putExtra("path", gamePath);
		this.startService(dedicatedServer);
	}

	public void stopServer()
	{
		stopService(new Intent(DedicatedActivity.this, DedicatedService.class));
	}
}
