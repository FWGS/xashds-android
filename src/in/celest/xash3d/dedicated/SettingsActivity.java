package in.celest.xash3d.dedicated;
import android.preference.*;
import android.os.*;
import android.content.*;
import android.provider.Browser;
import android.view.*;

public class SettingsActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener
{
	private String argv;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		PreferenceManager m = getPreferenceManager();
		m.setSharedPreferencesName("dedicated");
		
		addPreferencesFromResource(R.xml.stng_prefs);
		updateAllPrefs(true);
		
		findPreference("basedir").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference p1)
			{
				Intent newi = new Intent(SettingsActivity.this, ListActivity.class);
				newi.putExtra("folder", ListActivity.REQUEST_BASEDIR_SELECT);
				newi.putExtra("dir", getSharedPreferences("dedicated", 0).getString("basedir", "/sdcard/xash").toString());
				startActivityForResult(newi, 1998);
				return true;
			}
		});

		findPreference("translator").setOnPreferenceChangeListener(this);

        findPreference("s_game").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent newi = new Intent(SettingsActivity.this, ListActivity.class);
                newi.putExtra("folder", ListActivity.REQUEST_GAME_SELECT);
                startActivityForResult(newi, 1998);
                return true;
            }
        });
		findPreference("s_dll").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference p1) {
				Intent newi = new Intent(SettingsActivity.this, ListActivity.class);
				newi.putExtra("folder", ListActivity.REQUEST_DLL_SELECT);
				startActivityForResult(newi, 1998);
				return true;
			}
		});

		findPreference("s_remdlls").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				argv = CommandParser.removeAll(argv, "-dll");
				getSharedPreferences("dedicated", 0).edit().putString("s_dll", "").putString("argv", argv).commit();
				updateAllPrefs(false);
				return true;
			}
		});

		findPreference("s_map").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent newi = new Intent(SettingsActivity.this, ListActivity.class);
				newi.putExtra("folder", ListActivity.REQUEST_MAP_SELECT);
				startActivityForResult(newi, 1998);
				return true;
			}
		});

		findPreference("s_rcon").setOnPreferenceChangeListener(this);
		
		ListPreference translators = (ListPreference) findPreference("translator");
		if(System.getProperty("ro.product.cpu.abi") == "x86")
		{
			translators.setEntries(new String[]{"none"});
			translators.setDefaultValue("0");
			translators.setEntryValues(new String[]{"0"});
			if (DedicatedStatics.getTranslatorIndex(this) != 0)
				getSharedPreferences("dedicated", 0).edit().putString("translator", "0").commit();
		} else {
			translators.setEntries(DedicatedActivity.listTranslators());
			translators.setDefaultValue("0");
			translators.setEntryValues(makeValues(DedicatedActivity.listTranslators()));
			if (DedicatedStatics.getTranslatorIndex(this) >= DedicatedActivity.listTranslators().length)
				getSharedPreferences("dedicated", 0).edit().putString("translator", "0").commit();
			
			}

		findPreference("argv").setOnPreferenceChangeListener(this);
		findPreference("s_console").setOnPreferenceChangeListener(this);
		findPreference("s_dev").setOnPreferenceChangeListener(this);
		findPreference("s_log").setOnPreferenceChangeListener(this);
		findPreference("s_coop").setOnPreferenceChangeListener(this);
		findPreference("s_public").setOnPreferenceChangeListener(this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == 1998) if (resultCode == RESULT_OK) {
				String result = data.getStringExtra("result");
				final String folder = data.getStringExtra("folder");
				
				switch (folder) {
					case ListActivity.REQUEST_DLL_SELECT:
						String dlls = getSharedPreferences("dedicated", 0).getString("s_dll", null);
						if (dlls.lastIndexOf(result) == -1)
						{
							if ((dlls != null)&&(dlls != "")) dlls += ", "+result;
								else dlls = result;
						}
						argv = CommandParser.addParam(CommandParser.removeAll(argv, "-dll"), CommandParser.makeParamArgString(dlls, "-dll"));

						getSharedPreferences("dedicated", 0).edit().putString("s_dll", dlls).commit();
						break;
					case ListActivity.REQUEST_MAP_SELECT:
						argv = CommandParser.addParam(CommandParser.removeAll(argv, "+map"), "+map "+result);
						getSharedPreferences("dedicated", 0).edit().putString("s_map", result).commit();
						break;
					case ListActivity.REQUEST_BASEDIR_SELECT:
						getSharedPreferences("dedicated", 0).edit().putString("basedir", result).commit();
						break;
					case ListActivity.REQUEST_GAME_SELECT:
						if (!((result.equals(CommandParser.parseSingleParameter(argv, "-game"))) ||
								(result.equals("valve") && (CommandParser.parseSingleParameter(argv, "-game").equals("")))))	//if not current set game
						{
							//clean other game parameters
							argv = CommandParser.removeAll(CommandParser.removeAll(argv, "+map"), "-dll");
							if (!result.equals("valve")) {
								argv = CommandParser.addParam(CommandParser.removeAll(argv, "-game"), "-game " + result);
							} else {
								argv = CommandParser.removeAll(argv, "-game");
							}
							argv = CommandParser.sort(argv);
							getSharedPreferences("dedicated", 0).edit().putString("s_game", result.equals("valve") ? "" : result).putString("s_map", "").putString("s_dll", "").commit();
						}
						break;
				}


				argv = CommandParser.sort(argv);
				getSharedPreferences("dedicated", 0).edit().putString("argv", argv).commit();
				updateAllPrefs(false);
		}
				
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		String key = preference.getKey();

		switch (key)
		{
			case "argv":
				argv = (String) newValue;
				break;
			case "s_console":
				boolean isConsole = (boolean) newValue;
				if (isConsole) argv = CommandParser.addParam(argv, "-console");
					else argv = argv.replace("-console", "");
				break;
			case "s_dev":
				boolean isDev = (boolean) newValue;
				if (isDev) argv = CommandParser.addParam(argv, "-dev 3");
					else argv = argv.replace("-dev "+CommandParser.parseSingleParameter(argv, "-dev"), "");
				break;
			case "s_log":
				boolean isLog = (boolean) newValue;
				if (isLog) argv = CommandParser.addParam(argv, "-log");
					else argv = argv.replace("-log", "");
				break;
			case "s_coop":
				boolean isCoop = (boolean) newValue;
				if (isCoop) argv = CommandParser.addParam(argv.replace("+deathmatch 1", ""), "+coop 1");
					else argv = CommandParser.addParam(argv.replace("+coop 1", ""), "+deathmatch 1");
				break;
			case "s_public":
				argv = ((boolean) newValue)?CommandParser.addParam(argv, "+public 1"):argv.replace("+public 1", "");
				break;
			case "translator":
				getSharedPreferences("dedicated", 0).edit().putString("translator", (String) newValue).commit();
				break;
			case "s_rcon":
				String pass = (String) newValue;
				argv = CommandParser.removeAll(argv, "+rcon_password");
				if (!pass.equals("")) argv = CommandParser.addParam(argv, "+rcon_password "+pass);
				break;
		}

		argv = CommandParser.sort(argv);
		getSharedPreferences("dedicated", 0).edit().putString("argv", argv).commit();
		updateAllPrefs(false);

		return true;
	}

	private String[] makeValues(String[] in)
	{
		String[] r = new String[in.length];
		for (int i = 0; i < r.length; i++)
		{
			r[i] = new Integer(i).toString();
		}
		return r;
	}

	private void updateAllPrefs(boolean reloadArgv)
	{
		//update argv
		if (reloadArgv) argv = getSharedPreferences("dedicated", 0).getString("argv", "-dev 5 -dll dlls/hl.dll");
		//re-save preferences
		getSharedPreferences("dedicated", 0).edit().
				putString("s_game", CommandParser.parseSingleParameter(argv, "-game")).
				putString("s_dll", CommandParser.parseMultipleParameter(argv, "-dll")).
				putString("s_map", CommandParser.parseSingleParameter(argv, "+map")).
				putBoolean("s_dev", CommandParser.parseLogicParameter(argv, "-dev")).
				putBoolean("s_console", CommandParser.parseLogicParameter(argv, "-console")).
				putBoolean("s_log", CommandParser.parseLogicParameter(argv, "-log")).
				putBoolean("s_coop", CommandParser.parseLogicParameter(argv, "+coop")).
				putString("s_dll", CommandParser.parseMultipleParameter(argv, "-dll")).
				putString("s_game", CommandParser.parseSingleParameter(argv, "-game")).
				putString("s_map", CommandParser.parseSingleParameter(argv, "+map")).
				putString("s_rcon", CommandParser.parseSingleParameter(argv, "+rcon_password")).
				putBoolean("s_public", CommandParser.parseLogicParameter(argv, "+public")).
				commit();

		//set preferences values
		((CheckBoxPreference) findPreference("s_console")).setChecked(CommandParser.parseLogicParameter(argv, "-console"));
		((CheckBoxPreference) findPreference("s_dev")).setChecked(CommandParser.parseLogicParameter(argv, "-dev"));
		((CheckBoxPreference) findPreference("s_log")).setChecked(CommandParser.parseLogicParameter(argv, "-log"));
		((CheckBoxPreference) findPreference("s_coop")).setChecked(CommandParser.parseLogicParameter(argv, "+coop"));
		((CheckBoxPreference) findPreference("s_public")).setChecked(CommandParser.parseLogicParameter(argv, "+public"));

		((EditTextPreference) findPreference("argv")).setText(argv);
		((EditTextPreference) findPreference("s_rcon")).setText(CommandParser.parseSingleParameter(argv, "+rcon_password"));

		//set preferences show values
		findPreference("basedir").setSummary(DedicatedStatics.getBaseDir(this));
		findPreference("translator").setSummary(DedicatedStatics.getTranslator(this));
		findPreference("argv").setSummary(argv);
		findPreference("s_game").setSummary((CommandParser.parseSingleParameter(argv, "-game")=="")?"valve":CommandParser.parseSingleParameter(argv, "-game"));
		findPreference("s_dll").setSummary(CommandParser.parseMultipleParameter(argv, "-dll"));
		findPreference("s_map").setSummary(CommandParser.parseSingleParameter(argv, "+map"));
		findPreference("s_rcon").setSummary(CommandParser.parseSingleParameter(argv, "+rcon_password"));
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		finish();
		return super.onOptionsItemSelected(item);
	}
}
