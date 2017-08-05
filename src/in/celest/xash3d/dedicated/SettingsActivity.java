package in.celest.xash3d.dedicated;
import android.preference.*;
import android.os.*;
import android.content.*;
import android.provider.Browser;

public class SettingsActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener
{
	private String argv;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		PreferenceManager m = getPreferenceManager();
		m.setSharedPreferencesName("dedicated");

		argv = getSharedPreferences("dedicated", 0).getString("argv", "-dev 5 -dll dlls/hl.dll");
		getSharedPreferences("dedicated", 0).edit().
				putString("s_game", CommandParser.parseSingleParameter(argv, "-game")).
				putString("s_dll", CommandParser.parseMultipleParameter(argv, "-dll")).
				putString("s_map", CommandParser.parseSingleParameter(argv, "+map")).
				putBoolean("s_dev", CommandParser.parseLogicParameter(argv, "-dev")).
				putBoolean("s_console", CommandParser.parseLogicParameter(argv, "-console")).
				putBoolean("s_log", CommandParser.parseLogicParameter(argv, "-log")).
				putBoolean("s_coop", CommandParser.parseLogicParameter(argv, "+coop")).
				putString("s_dll", CommandParser.parseMultipleParameter(argv, "-dll")).
				commit();
		
		addPreferencesFromResource(R.xml.stng_prefs);
		
		Preference based = findPreference("basedir");
		based.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
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

		Preference dlls = findPreference("s_dll");
		dlls.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference p1) {
				Intent newi = new Intent(SettingsActivity.this, ListActivity.class);
				newi.putExtra("folder", ListActivity.REQUEST_DLL_SELECT);
				startActivityForResult(newi, 1998);
				return true;
			}
		});
		
		ListPreference translators = (ListPreference) findPreference("translator");
		translators.setEntries(DedicatedActivity.listTranslators());
		translators.setDefaultValue("0");
		translators.setEntryValues(makeValues(DedicatedActivity.listTranslators()));

		findPreference("argv").setOnPreferenceChangeListener(this);
		findPreference("s_console").setOnPreferenceChangeListener(this);
		findPreference("s_dev").setOnPreferenceChangeListener(this);
		findPreference("s_coop").setOnPreferenceChangeListener(this);
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

						getSharedPreferences("dedicated", 0).edit().putString("s_dll", dlls).putString("argv", argv).commit();
						((EditTextPreference) findPreference("argv")).setText(argv);
						break;
					/*case "maps":
						serverMap.setText(result);
						pushMasterSettings();
						break;*/
					case ListActivity.REQUEST_BASEDIR_SELECT:
						getSharedPreferences("dedicated", 0).edit().putString("basedir", result).commit();
						break;
					/*case "":
						if (!modDir.getText().toString().equals(result)) {
							serverDlls.setText("");
							serverMap.setText("");
						}
						modDir.setText((result.equals("valve"))?"":result);
						pushMasterSettings();
						break;*/
				}
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
				((CheckBoxPreference) findPreference("s_console")).setChecked(CommandParser.parseLogicParameter(argv, "-console"));
				((CheckBoxPreference) findPreference("s_dev")).setChecked(CommandParser.parseLogicParameter(argv, "-dev"));
				((CheckBoxPreference) findPreference("s_log")).setChecked(CommandParser.parseLogicParameter(argv, "-log"));
				((CheckBoxPreference) findPreference("s_coop")).setChecked(CommandParser.parseLogicParameter(argv, "+coop"));
				getSharedPreferences("dedicated", 0).edit().
						putString("s_game", CommandParser.parseSingleParameter(argv, "-game")).
						putString("s_dll", CommandParser.parseMultipleParameter(argv, "-dll")).
						putString("s_map", CommandParser.parseSingleParameter(argv, "+map")).
						putBoolean("s_dev", CommandParser.parseLogicParameter(argv, "-dev")).
						putBoolean("s_console", CommandParser.parseLogicParameter(argv, "-console")).
						putBoolean("s_log", CommandParser.parseLogicParameter(argv, "-log")).
						putBoolean("s_coop", CommandParser.parseLogicParameter(argv, "+coop")).
						putString("s_dll", CommandParser.parseMultipleParameter(argv, "-dll")).
						commit();
				break;
			case "s_console":
				boolean isConsole = (boolean) newValue;
				argv = getSharedPreferences("dedicated", 0).getString("argv", "-dev 5 -dll dlls/hl.dll");

				if (isConsole) argv = CommandParser.addParam(argv, "-console");
					else argv = argv.replace("-console", "");
				break;
			case "s_dev":
				boolean isDev = (boolean) newValue;
				if (isDev) argv = CommandParser.addParam(argv, "-dev 3");
					else argv = argv.replace("-dev "+CommandParser.parseSingleParameter(argv, "-dev"), "");
				break;
			case "s_coop":
				boolean isCoop = (boolean) newValue;
				if (isCoop) argv = CommandParser.addParam(argv.replace("+deathmatch 1", ""), "+coop 1");
					else argv = CommandParser.addParam(argv.replace("+coop 1", ""), "+deathmatch 1");
				break;
		}

		((EditTextPreference) findPreference("argv")).setText(argv);
		getSharedPreferences("dedicated", 0).edit().putString("argv", argv).commit();

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
}
