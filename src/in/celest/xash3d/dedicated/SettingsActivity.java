package in.celest.xash3d.dedicated;
import android.preference.*;
import android.os.*;
import android.content.*;

public class SettingsActivity extends PreferenceActivity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		PreferenceManager m = getPreferenceManager();
		m.setSharedPreferencesName("dedicated");
		
		addPreferencesFromResource(R.xml.stng_prefs);
		
		Preference p = (Preference) findPreference("basedir");
		p.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
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
		
		ListPreference translators = (ListPreference) findPreference("translator");
		translators.setEntries(DedicatedActivity.listTranslators());
		translators.setDefaultValue("0");
		translators.setEntryValues(makeValues(DedicatedActivity.listTranslators()));
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == 1998) if (resultCode == RESULT_OK) {
				String result = data.getStringExtra("result");
				final String folder = data.getStringExtra("folder");
				
				switch (folder) {
					/*case "dlls":
						if (serverDlls.getText().toString().lastIndexOf(result) == -1) 
							if (serverDlls.getText().toString().equals("")) serverDlls.append(result);
							else serverDlls.append(", "+result);
						pushMasterSettings();
						break;
					case "maps":
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
