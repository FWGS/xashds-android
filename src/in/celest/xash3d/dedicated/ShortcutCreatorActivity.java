package in.celest.xash3d.dedicated;
import android.app.*;
import android.os.*;
import android.content.*;
import android.widget.*;
import android.view.*;
import android.graphics.*;

public class ShortcutCreatorActivity extends Activity
{
	EditText sname;
	Switch launch;
	Switch launchm;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		LinearLayout layout = new LinearLayout(this);
		layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));
		layout.setOrientation(LinearLayout.VERTICAL);
		
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		
		TextView t1 = new TextView(this);
		t1.setLayoutParams(lp);
		t1.setText(R.string.l_scut_name);
		
		sname = new EditText(this);
		sname.setLayoutParams(lp);
		String name = DedicatedStatics.getGame(this);
		if (name.equals("")) name = "Half-Life";
		sname.setText(name+" server");
		
		launch = new Switch(this);
		launch.setLayoutParams(lp);
		launch.setText(R.string.l_scut_gamel);
		launch.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v)
			{
				launchm.setEnabled(launch.isChecked());
			}
		});
		
		launchm = new Switch(this);
		launchm.setLayoutParams(lp);
		launchm.setEnabled(false);
		launchm.setText(R.string.l_scut_gamem1);
		launchm.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v)
			{
				launchm.setText(launchm.isChecked()?R.string.l_scut_gamem2:R.string.l_scut_gamem1);
			}
		});
		
		Button ok = new Button(this);
		ok.setLayoutParams(lp);
		ok.setText("OK");
		ok.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v)
			{
				createShortcut();
				finish();
			}
		});
		
		Button close = new Button(this);
		close.setLayoutParams(lp);
		close.setText(R.string.b_close);
		close.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v)
			{
				finish();
			}
		});
		
		layout.addView(t1);
		layout.addView(sname);
		layout.addView(launch);
		layout.addView(launchm);
		layout.addView(ok);
		layout.addView(close);
		setContentView(layout);
	}
	
	private void createShortcut()
	{
		Intent shortcutIntent = new Intent(getApplicationContext(),
										   DedicatedActivity.class);
		shortcutIntent.setAction(Intent.ACTION_MAIN);

		shortcutIntent.putExtra("autostart", true);
		shortcutIntent.putExtra("autolaunch", launch.isChecked());
		shortcutIntent.putExtra("automode", launchm.isChecked());
		shortcutIntent.putExtra("translator", DedicatedStatics.getTranslator(this));
		shortcutIntent.putExtra("files", DedicatedActivity.filesDir);
		shortcutIntent.putExtra("game", DedicatedStatics.getBaseDir(this));
		shortcutIntent.putExtra("argv", DedicatedStatics.getArgv(this));

		Intent addIntent = new Intent();
		addIntent
            .putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
		addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, sname.getText().toString());
		//addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
		//				   Intent.ShortcutIconResource.fromContext(getApplicationContext(),
		//														   R.drawable.logo));
		Bitmap scaledBitmap = Bitmap.createScaledBitmap(DedicatedActivity.gameIcon, 128, 128, true);
		addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON,
						   scaledBitmap);

		addIntent
            .setAction("com.android.launcher.action.INSTALL_SHORTCUT");
		addIntent.putExtra("duplicate", true);
		getApplicationContext().sendBroadcast(addIntent);
	}
}
