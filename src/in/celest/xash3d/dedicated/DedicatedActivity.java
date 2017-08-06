package in.celest.xash3d.dedicated;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.content.res.*;
import android.graphics.*;
import android.os.*;
import android.view.*;
import android.view.inputmethod.*;
import android.widget.*;
import android.widget.LinearLayout.*;
import java.io.*;
import java.net.*;
import java.util.*;

import android.os.Process;

public class DedicatedActivity extends Activity {
	//views for launcher screen
	static AutoCompleteTextView cmdLine;
	static LinearLayout output;
	static ScrollView scroll;
	private Button startButton;
	private Button launchXash;

	static String filesDir;
	static boolean isScrolling;

	static String argsString;

	static boolean isNewBinary = false;

	static LayoutParams buttonParams;

	static boolean isRunned = false;
	
	static boolean 	autostarted;
	static boolean 	autolaunch;
	static boolean 	automode;
	static String 	autoFiles;
	static String 	autoArgv;
	static String 	autoGame;
	static String 	autoTranslator;
	
	static Bitmap 	gameIcon = null;

	private static String[] commands =
			{
					"say",
					"echo",
					"sv_cheats",
					"maxplayers",
					"hostname",
					"restart",
					"ip",
					"rcon_password",
					"dethmatch",
					"coop",
					"public",
					"kick",
					"exit",
					"sv_gravity",
					"map"
			};

	@Override
    protected void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);

		buttonParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		buttonParams.gravity = 5;

		isRunned = DedicatedService.isStarted;

		if (DedicatedStatics.launched != null) DedicatedStatics.launched.finish();
		DedicatedStatics.launched = this;

		try { initLauncher(); }
		catch (Exception e)
		{
			Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
		}
		
		if(getIntent().getBooleanExtra("autostart", false))
		{
			autostarted = true;
			autolaunch = getIntent().getBooleanExtra("autolaunch", false);
			automode = getIntent().getBooleanExtra("automode", false);
			
			Toast.makeText(this, autolaunch?"Game will automatically begin after server start":"Autostarting dedicated server...", Toast.LENGTH_LONG).show();
			
			autoGame = getIntent().getStringExtra("game");
			autoFiles = getIntent().getStringExtra("files");
			autoArgv = getIntent().getStringExtra("argv");
			autoTranslator = getIntent().getStringExtra("translator");
			
			startServer(autoGame, autoFiles, autoArgv, autoTranslator);
			isRunned = true;
			startButton.setText(isRunned?R.string.b_start_stop:R.string.b_start_launch);
		}
	}

    public static String[] listTranslators()
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
		ConsoleView line = new ConsoleView(this);
		line.addString(str);
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

	//Launcher initialization
	//Called on activity start and on maser close
	private void initLauncher()
	{
		setTitle(R.string.launcher_head);

		filesDir = getApplicationContext().getFilesDir().getPath();
		

		setContentView(R.layout.launcher_layout);
		
		//Init layout
		output = (LinearLayout) findViewById(R.id.logOutput);
		scroll = (ScrollView) findViewById(R.id.logScroll);
		
		//Fill log in already exists
		for (int i = 0; i < DedicatedStatics.logView.size(); i++)
		{
			printText(DedicatedStatics.logView.get(i));
		}
		
		TextView v = (TextView) findViewById(R.id.lnch_info);
		v.setText("Game directory: " + DedicatedStatics.getBaseDir(this) + "\n" +
					"Commamd-line args: " + DedicatedStatics.getArgv(this) + "\n" +
					"Launch using: " + DedicatedStatics.getTranslator(this));

		cmdLine = (AutoCompleteTextView) findViewById(R.id.cmdLine);
		cmdLine.setOnLongClickListener(new View.OnLongClickListener() {
			@Override 
			public boolean onLongClick(View v)
			{
				sendCommand(cmdLine.getText().toString());
				cmdLine.setText("");
				return true;
			}
		});
		cmdLine.setImeOptions(EditorInfo.IME_ACTION_SEND);
		cmdLine.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if ((actionId == EditorInfo.IME_ACTION_SEND) || (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
				{
					sendCommand(cmdLine.getText().toString());
					cmdLine.setText("");
					return true;
				} else {
					return false;
				}
			}
		});
		cmdLine.setThreshold(1);
		cmdLine.setAdapter(new ArrayAdapter<String>(this, android.R.layout.select_dialog_singlechoice, commands));
		
		launchXash = (Button) findViewById(R.id.startXash);
		launchXash.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					startXash();
				}
			});
		launchXash.setEnabled(DedicatedService.canConnect);

		startButton = (Button) findViewById(R.id.startButton);
		// Set launch button title here
		startButton.setText(isRunned?R.string.b_start_stop:R.string.b_start_launch);
		startButton.setOnClickListener( new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					isRunned = !isRunned;
					startButton.setText(isRunned?R.string.b_start_stop:R.string.b_start_launch);

					if (isRunned) {
						startServer();
					} else {
						stopServer();
					}
				}
			});
			
		if (!isXashInstalled()) launchXash.setVisibility(View.INVISIBLE);
		
		isNewBinary = getSharedPreferences("dedicated", 0).getBoolean("newxash", false);
		DedicatedStatics.chstr(isNewBinary);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		menu.add(Menu.NONE, 1, Menu.NONE, R.string.b_refresh_cache);
		menu.add(Menu.NONE, 2, Menu.NONE, R.string.b_about);
		menu.add(Menu.NONE, 3, Menu.NONE, R.string.b_scut);
		menu.add(Menu.NONE, 4, Menu.NONE, R.string.b_settings);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId()) {
			case 1:
				refreshCache();
				return true;
			case 2:
				startActivity(new Intent(DedicatedActivity.this, AboutActivity.class));
				return true;
			case 3:
				createShortcut();
				return true;
			case 4:
				startActivity(new Intent(DedicatedActivity.this, SettingsActivity.class));
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	public void startServer()
	{
		unpackAssets();
		Intent dedicatedServer = new Intent(DedicatedActivity.this, DedicatedService.class);
		dedicatedServer.putExtra("argv", argsString);
		dedicatedServer.putExtra("path", DedicatedStatics.getBaseDir(this));
		dedicatedServer.putExtra("translator", DedicatedStatics.getTranslator(this));
		dedicatedServer.putExtra("files", filesDir);
		this.startService(dedicatedServer);
	}
	
	public void startServer(String game, String filesPath, String argv, String ctranslator)
	{
		getIcon();
		unpackAssets();
		Intent dedicatedServer = new Intent(DedicatedActivity.this, DedicatedService.class);
		dedicatedServer.putExtra("argv", argv);
		dedicatedServer.putExtra("path", game);
		dedicatedServer.putExtra("translator", ctranslator);
		dedicatedServer.putExtra("files", filesPath);
		this.startService(dedicatedServer);
	}

	public void stopServer()
	{
		autostarted = false;
		stopService(new Intent(DedicatedActivity.this, DedicatedService.class));
	}

	@Override
	protected void onDestroy()
	{
		DedicatedStatics.launched = null;
		super.onDestroy();
	}

	@Override
	protected void onPause()
	{
		super.onPause();
	}

	@Override
	protected void onResume()
	{
		initLauncher();
		super.onResume();
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

			if(!f.exists() || (getPackageManager().getPackageInfo(getPackageName(), 0).versionCode != getSharedPreferences("dedicated", 0).getInt("lastversion", 1)) )
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
				
				unpackAsset("xash-old");
				
				printText("[OK]\nSetting permissions.\n");
				//scroll.fullScroll(ScrollView.FOCUS_DOWN);
				Runtime.getRuntime().exec("chmod 777 " + filesDir + "/xash " + filesDir + "/xash_sse2 " + filesDir + "/tracker " + filesDir + "/qemu-i386-static "+filesDir+"/xash-old ").waitFor();
				
				getSharedPreferences("dedicated", 0).edit().putInt("lastversion", getPackageManager().getPackageInfo(getPackageName(), 0).versionCode).commit();
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
					if (can&&autolaunch&&autostarted)
					{
						autolaunch = false;
						if (automode)
						{
							String arguments = autostarted?autoArgv:argsString;

							String game = CommandParser.parseSingleParameter(arguments, "-game");
							Intent intent = new Intent();
							intent.setAction("in.celest.xash3d.START");
							intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

							intent.putExtra("argv", "-dev 3 +xashds_hacks 1 +rcon_address 127.0.0.1 +rcon_password "+CommandParser.parseSingleParameter(arguments, "+rcon_password"));
							if (!game.equals("")) intent.putExtra("gamedir", game);

							startActivity(intent);
						}
						else
						{
							String arguments = autostarted?autoArgv:argsString;

							String game = CommandParser.parseSingleParameter(arguments, "-game");
							Intent intent = new Intent();
							intent.setAction("in.celest.xash3d.START");
							intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

							intent.putExtra("argv", "-dev 3 +connect localhost:27015");
							if (!game.equals("")) intent.putExtra("gamedir", game);

							startActivity(intent);
						}
					}
				}
			});
	}

	private void createShortcut()
	{
		getIcon();
		Intent intent = new Intent(DedicatedActivity.this, ShortcutCreatorActivity.class);
		startActivity(intent);
	}
	
	public void getIcon()
	{
		Bitmap icon = null;
		
		int size = (int) getResources().getDimension(android.R.dimen.app_icon_size);
		
		String game = DedicatedStatics.getGame(this);
		String gamedirstring = DedicatedStatics.getBaseDir(this)+"/"+(game.length()!=0?game:"valve");
		try
		{
			icon = Bitmap.createScaledBitmap(BitmapFactory.decodeFile(gamedirstring+"/icon.png"), size, size, false);
		}
		catch(Exception e)
		{
		}
		if(icon == null) try
			{
				icon = Bitmap.createScaledBitmap(BitmapFactory.decodeFile(gamedirstring+"/game.ico"), size, size, false);
			}
			catch(Exception e)
			{
			}
		if(icon == null) try
			{
				FilenameFilter icoFilter = new FilenameFilter() {
					public boolean accept(File dir, String name) {
						if(name.endsWith(".ico") || name.endsWith(".ICO")) {
							return true;
						}
						return false;
					}
				};

				File gamedirfile = new File(gamedirstring);
				String files[] = gamedirfile.list(icoFilter);
				icon = Bitmap.createScaledBitmap(BitmapFactory.decodeFile(gamedirstring+"/"+files[0]), size, size, false);
			}
			catch(Exception e)
			{
				// Android may not support ico loading, so fallback if something going wrong
				icon = BitmapFactory.decodeResource(getResources(), R.drawable.logo);
			}

			
		Bitmap background = BitmapFactory.decodeResource(getResources(), R.drawable.logo);
		Bitmap result = Bitmap.createBitmap(icon.getWidth(), icon.getHeight(), icon.getConfig());
		Canvas canvas = new Canvas(result);
		canvas.drawBitmap(icon, 0, 0, null);
		canvas.drawBitmap(Bitmap.createScaledBitmap(background, icon.getWidth()/2, icon.getHeight()/2, true), 0, 0, null);
		
		gameIcon = result;
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
	
	public void sendCommand(String s)
	{
		if ((!s.equals(""))&&DedicatedService.isStarted)
		{
			//DedicatedService.sendCmd(s);
			try { sendRconCommand("localhost", 27015, CommandParser.parseSingleParameter(argsString, "+rcon_password"), s); } catch (Exception e) {}
			printText("/> "+s);
		}
	}
	
	public static void sendRconCommand(String ip, int port, String password, String command) throws Exception
	{
		byte[] header = new byte[]{(byte)255, (byte)255, (byte)255, (byte)255};
		String input = "rcon " + password + " " + command;
		byte[] sendData = new byte[1024];

		//fill first packet bytes
		for (int i = 0; i < header.length; i++)
		{
			sendData[i] = header[i];
		}

		//fill the message
		for (int i = header.length; (i < 1024)&&(i < header.length+input.length()); i++)
		{
			sendData[i] = (byte) input.charAt(i-header.length);
		}

		//send command
		DatagramSocket s = new DatagramSocket();

		InetAddress addr = InetAddress.getByName(ip);

		DatagramPacket pack = new DatagramPacket(sendData, sendData.length, addr, port);
		s.send(pack);
		s.close();
	}
}
