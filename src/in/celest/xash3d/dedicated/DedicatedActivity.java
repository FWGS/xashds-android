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
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Button;
import android.widget.Switch;
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
import java.util.*;
import android.view.View.*;
import android.graphics.*;

public class DedicatedActivity extends Activity {
	//views for launcher screen
	static EditText cmdArgs;
	static EditText baseDir;
	static LinearLayout output;
	static ScrollView scroll;
	static Spinner translatorSelector;
	//views for server master screen
	static EditText modDir;
	static EditText serverDlls;
	static EditText serverMap;
	static EditText rconPass;
	static Button 	launchXash;
	static Switch devBox;
	static Switch conoleBox;
	static Switch logBox;
	static Switch deathmatchSwitch;
	static Switch lanSwitch;
	static SharedPreferences mPref;
	static Process process = null;
	static String filesDir;
	static String translator = "qemu";
	static boolean isScrolling;

	static String argsString;
	static String gamePath;

	static boolean isDev = false;

	static LayoutParams buttonParams;

	static boolean isRunned = false;
	static boolean tab = true;

	private MenuItem launcherItem;

    @Override
    protected void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);

		buttonParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		buttonParams.gravity = 5;

		isRunned = DedicatedService.isStarted;

		if (DedicatedStatics.launched != null) DedicatedStatics.launched.finish();
		DedicatedStatics.launched = this;

		if (tab) initLauncher();
		else initMaster();
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

	public void printLog(String strin)
	{
		class OutputCallback implements Runnable {
			String str;
			OutputCallback(String s) { str = s; }
			public void run() {
				printText(str);
			}
		}

		runOnUiThread(new OutputCallback(strin));
	}

	private void loadSettings()
	{
		mPref = getSharedPreferences("dedicated", 0);
		argsString = mPref.getString("argv","-dev 5 -dll dlls/hl.dll");
		cmdArgs.setText(argsString);
		gamePath = mPref.getString("basedir","/sdcard/xash");
		baseDir.setText(gamePath);
		if (translatorSelector != null)
			if (mPref.getInt("translator", 0) < translatorSelector.getCount()) translatorSelector.setSelection(mPref.getInt("translator", 1));
	}

	private void pushLauncherSettings() 
	{
		argsString = cmdArgs.getText().toString();
		gamePath = baseDir.getText().toString();
	}

	private void pushMasterSettings()
	{

		argsString = makeMasterArgs();
	}

	private void saveSettings() 
	{
		SharedPreferences.Editor editor = mPref.edit();
		editor.putString("argv", argsString);
		editor.putString("basedir", gamePath);
		if (translatorSelector != null) editor.putInt("translator", translatorSelector.getSelectedItemPosition());
		try {
			editor.putInt("lastversion", getPackageManager().getPackageInfo(getPackageName(), 0).versionCode);
		} catch (Exception e) {}
		editor.commit();
		editor.apply();
		printText("Settings saved!");
	}

	private void initLauncher()
	{
		tab = true;
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

		for (int i = 0; i < DedicatedStatics.logView.size(); i++)
		{
			printText(DedicatedStatics.logView.get(i));
		}

		cmdArgs = new EditText(this);
		cmdArgs.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		cmdArgs.setSingleLine();
		baseDir = new EditText(this);
		baseDir.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		baseDir.setSingleLine();


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
		externalPicker.setEnabled(false);

		launchXash = new Button(this);
		launchXash.setText(R.string.b_start_xash);
		launchXash.setLayoutParams(buttonParams);
		launchXash.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					startXash();
				}
			});
		launchXash.setEnabled(DedicatedService.canConnect);

		// Set launch button title here
		startButton.setText(isRunned?R.string.b_start_stop:R.string.b_start_launch);
		startButton.setLayoutParams(buttonParams);
		startButton.setOnClickListener( new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					isRunned = !isRunned;
					startButton.setText(isRunned?R.string.b_start_stop:R.string.b_start_launch);

					pushLauncherSettings();
					saveSettings();

					if (isRunned) {
						startServer();
					} else {
						stopServer();
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
				translatorSelector = new Spinner(this);
				translatorSelector.setAdapter(adapter);
				translatorSelector.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
				translatorSelector.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
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
				launcher.addView(translatorSelector);
			}
		}
		button_bar.addView(startButton);
		//if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)  //SD card pick API enabled on 5.0(v21) and higher
		//button_bar.addView(externalPicker);
		if (isXashInstalled()) button_bar.addView(launchXash);
		launcher.addView(button_bar);
		scroll.addView(output);
		launcher.addView(scroll);

		loadSettings();

		setContentView(launcher);

		getActionBar().setDisplayHomeAsUpEnabled(false);
	}

	void initMaster() {
		tab = false;
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
		modDir.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v)
			{
				modDir.setText("");
				return true;
			}
		});

		serverDlls = new EditText(this);
		serverDlls.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		serverDlls.setHint(R.string.h_dlls);
		serverDlls.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v)
				{
					serverDlls.setText("");
					return true;
				}
			});

		serverMap = new EditText(this);
		serverMap.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		serverMap.setHint(R.string.h_map);
		serverMap.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v)
				{
					serverMap.setText("");
					return true;
				}
			});
		
		rconPass = new EditText(this);
		rconPass.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

		devBox = new Switch(this);
		devBox.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		devBox.setText(R.string.v_isdev);

		conoleBox = new Switch(this);
		conoleBox.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		conoleBox.setText(R.string.v_isconsole);

		logBox = new Switch(this);
		logBox.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		logBox.setText(R.string.v_islog);
		logBox.setEnabled(false);

		deathmatchSwitch = new Switch(this);
		deathmatchSwitch.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		deathmatchSwitch.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					deathmatchSwitch.setText(deathmatchSwitch.isChecked()?R.string.v_isdm:R.string.v_iscoop);
				}
			});
			
		lanSwitch = new Switch(this);
		lanSwitch.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		lanSwitch.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					lanSwitch.setText(lanSwitch.isChecked()?R.string.v_ispublic:R.string.v_islan);
				}
			});

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

		//master.addView(saveButton);
		master.addView(gameNameView);
		master.addView(modDir);
		master.addView(makeListButton("", R.string.b_select));
		master.addView(gameDllsView);
		master.addView(serverDlls);
		master.addView(makeListButton("dlls", R.string.b_select));
		master.addView(gameMapView);
		master.addView(serverMap);
		master.addView(makeListButton("maps", R.string.b_select));
		master.addView(serverPassView);
		master.addView(rconPass);
		master.addView(devBox);
		master.addView(conoleBox);
		master.addView(logBox);
		master.addView(deathmatchSwitch);
		master.addView(lanSwitch);

		loadSettings();
		parseArgsToMaster(argsString);

		setContentView(masterScroll); //let us see that Master-Scroll!

		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	private Button makeListButton(String dir, int txtResId)
	{
		Button b = new Button(this);
		b.setLayoutParams(buttonParams);
		b.setText(txtResId);
		b.setOnClickListener(new ListViewOpener(dir));
		return b;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		menu.add(Menu.NONE, 1, Menu.NONE, R.string.b_master);
		menu.add(Menu.NONE, 2, Menu.NONE, R.string.b_refresh_cache);
		menu.add(Menu.NONE, 3, Menu.NONE, R.string.b_about);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId()) {
			case android.R.id.home:
				if (tab) pushLauncherSettings();
					else pushMasterSettings();
				saveSettings();
				initLauncher();
				launcherItem.setTitle(R.string.b_master);
				return true;
			case 1:
				if (tab) pushLauncherSettings();
					else pushMasterSettings();
				saveSettings();
				launcherItem = item;
				if (tab) initMaster();
				else initLauncher();
				if (tab) item.setTitle(R.string.b_master);
				else item.setTitle(R.string.b_master_close);
				return true;
			case 2:
				saveSettings();
				refreshCache();
				return true;
			case 3:
				startActivity(new Intent(DedicatedActivity.this, AboutActivity.class));
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	public String makeMasterArgs() 
	{
		String ret = "";
		if (devBox.isChecked()) ret += "-dev 3 ";
		if (conoleBox.isChecked()) ret += "-console ";
		if (logBox.isChecked()) ret += "-log ";
		if (!modDir.getText().toString().equals("")) ret += CommandParser.makeParamArgString(modDir.getText().toString(), "-game");
		if (!serverDlls.getText().toString().equals("")) ret += CommandParser.makeParamArgString(serverDlls.getText().toString(), "-dll");
		if (!serverMap.getText().toString().equals("")) ret += CommandParser.makeParamArgString(serverMap.getText().toString(), "+map");
		if (!rconPass.getText().toString().equals("")) ret += CommandParser.makeParamArgString(rconPass.getText().toString(), "+rcon_password");
		if (deathmatchSwitch.isChecked()) ret += "+deathmatch 1 ";
			else ret += "+coop 1 ";
		if (lanSwitch.isChecked()) ret += "+public 1";
		return ret;
	}

	public void parseArgsToMaster(String args) {
		modDir.setText(CommandParser.parseSingleParameter(args, "-game"));
		serverDlls.setText(CommandParser.parseMultipleParameter(args, "-dll"));
		serverMap.setText(CommandParser.parseSingleParameter(args, "+map"));
		rconPass.setText(CommandParser.parseSingleParameter(args, "+rcon_password"));
		devBox.setChecked(CommandParser.parseLogicParameter(args, "-dev"));
		conoleBox.setChecked(CommandParser.parseLogicParameter(args, "-console"));
		logBox.setChecked(CommandParser.parseLogicParameter(args, "-log"));
		if (CommandParser.parseSingleParameter(args, "+deathmatch").equals("1")) deathmatchSwitch.setChecked(true);
		if (CommandParser.parseSingleParameter(args, "+coop").equals("1")) deathmatchSwitch.setChecked(false);
		if (CommandParser.parseSingleParameter(args, "+public").equals("1")) lanSwitch.setChecked(true);
		deathmatchSwitch.setText(deathmatchSwitch.isChecked()?R.string.v_isdm:R.string.v_iscoop);
		lanSwitch.setText(lanSwitch.isChecked()?R.string.v_ispublic:R.string.v_islan);
	}

	public void startServer()
	{
		unpackAssets();
		Intent dedicatedServer = new Intent(DedicatedActivity.this, DedicatedService.class);
		dedicatedServer.putExtra("argv", argsString);
		dedicatedServer.putExtra("path", gamePath);
		this.startService(dedicatedServer);
	}

	public void stopServer()
	{
		stopService(new Intent(DedicatedActivity.this, DedicatedService.class));
	}

	@Override
	protected void onDestroy()
	{
		DedicatedStatics.launched = null;
		saveSettings();
		super.onDestroy();
	}

	@Override
	protected void onPause()
	{
		saveSettings();
		super.onPause();
	}

	public class ListViewOpener implements View.OnClickListener
	{
		String folder;

		ListViewOpener(String f)
		{
			folder = f;
		}

		@Override
		public void onClick(View p1)
		{
			pushMasterSettings();
			saveSettings();
			Intent newi = new Intent(DedicatedActivity.this, ListActivity.class);
			newi.putExtra("folder", folder);
			startActivityForResult(newi, 1998);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == 1998) if (resultCode == RESULT_OK) {
				String result = data.getStringExtra("result");
				final String folder = data.getStringExtra("folder");
				printText("Returned result: "+result);

				switch (folder) {
					case "dlls":
						if (serverDlls.getText().toString().lastIndexOf(result) == -1) 
							if (serverDlls.getText().toString().equals("")) serverDlls.append(result);
							else serverDlls.append(", "+result);
						break;
					case "maps":
						serverMap.setText(result);
						break;
					case "":
						if (!modDir.getText().toString().equals(result)) {
							serverDlls.setText("");
							serverMap.setText("");
						}
						modDir.setText((result.equals("valve"))?"":result);
						break;
				}

				pushMasterSettings();
				saveSettings();
			}
	}

	public void startXash()
    {
		Intent intent = new Intent(this, ConnectActivity.class);
		startActivity(intent);
    }

    public boolean isXashInstalled()
	{
		try
		{
			getPackageManager().getPackageInfo("in.celest.xash3d.hl", PackageManager.GET_ACTIVITIES);
			return true;
		} catch (PackageManager.NameNotFoundException e) {}

		return false;
	}

	public void unpackAsset(String name) throws Exception {
		printText("Unpacking "+name+"...");
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
		printText("[OK]\n");
	}

	public void unpackAssets()
	{
		File f = new File(filesDir+"/xash");
		try 
		{

			if(!f.exists() || (getPackageManager().getPackageInfo(getPackageName(), 0).versionCode != mPref.getInt("lastversion", 1)) )
			{
				//Unpack files now
				//scroll.fullScroll(ScrollView.FOCUS_DOWN);
				unpackAsset("xash");
				//scroll.fullScroll(ScrollView.FOCUS_DOWN);
				unpackAsset("xash_sse2");
				//scroll.fullScroll(ScrollView.FOCUS_DOWN);
				unpackAsset("start-translator.sh");
				//scroll.fullScroll(ScrollView.FOCUS_DOWN);
				unpackAsset("tracker");
				//scroll.fullScroll(ScrollView.FOCUS_DOWN);
				unpackAsset("qemu-i386-static");

				printText("[OK]\nSetting permissions.\n");
				//scroll.fullScroll(ScrollView.FOCUS_DOWN);
				Runtime.getRuntime().exec("chmod 777 " + filesDir + "/xash " + filesDir + "/xash_sse2 " + filesDir + "/tracker " + filesDir + "/qemu-i386-static ").waitFor();
			}
		} catch (Exception e) {}
	}

	public void setCanConnect(final boolean can)
	{
		runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					launchXash.setEnabled(can);
				}
			});
	}

	public void refreshCache()
	{
		File dir = new File(filesDir);
		File[] oldAssets = dir.listFiles();

		printText("\nRefreshing...");
		if (oldAssets != null) for (File f : oldAssets) 
				if (f.delete()) printText("Successfuly removed "+f.getName());
		printText("");
		unpackAssets();
	}
}
