package in.celest.xash3d.dedicated;
import android.app.*;
import android.os.*;
import android.widget.*;
import android.widget.RelativeLayout.*;
import android.text.*;
import android.view.*;
import android.content.*;

public class ConnectActivity extends Activity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		LinearLayout layout = new LinearLayout(this);
		layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		layout.setOrientation(LinearLayout.VERTICAL);
		
		Button xacksButton = new Button(this);
		xacksButton.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		xacksButton.setText(R.string.b_start_xash_hacks);
		xacksButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v)
			{
				startHacks();
				finish();
			}
		});

		Button connectButton = new Button(this);
		connectButton.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		connectButton.setText(R.string.b_start_xash_connect);
		connectButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v)
				{
					startConnect();
					finish();
				}
			});
			
		Button closeButton = new Button(this);
		closeButton.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		closeButton.setText(R.string.b_close);
		closeButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v)
				{
					finish();
				}
			});
			
		layout.addView(xacksButton);
		layout.addView(connectButton);
		layout.addView(closeButton);
		
		setContentView(layout);
	}
	
	public void startHacks() {
		String arguments = DedicatedActivity.autostarted?DedicatedActivity.autoArgv:DedicatedStatics.getArgv(this);
		
		String game = CommandParser.parseSingleParameter(arguments, "-game");
		Intent intent = new Intent();
		intent.setAction("in.celest.xash3d.START");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		intent.putExtra("argv", "-dev 3 +xashds_hacks 1 +rcon_address 127.0.0.1 +rcon_password "+CommandParser.parseSingleParameter(arguments, "+rcon_password"));
		if (!game.equals("")) intent.putExtra("gamedir", game);

		startActivity(intent);
	}
	
	public void startConnect(){
		String arguments = DedicatedActivity.autostarted?DedicatedActivity.autoArgv:DedicatedStatics.getArgv(this);

		String game = CommandParser.parseSingleParameter(arguments, "-game");
		Intent intent = new Intent();
		intent.setAction("in.celest.xash3d.START");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		intent.putExtra("argv", "-dev 3 +connect localhost:27015");
		if (!game.equals("")) intent.putExtra("gamedir", game);
		
		startActivity(intent);
	}
}
