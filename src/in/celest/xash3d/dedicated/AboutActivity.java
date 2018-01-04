package in.celest.xash3d.dedicated;
import android.app.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import android.widget.LinearLayout.LayoutParams;

public class AboutActivity extends Activity
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		LinearLayout mainlayout = new LinearLayout(this);
		mainlayout.setLayoutParams((new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT)));
		mainlayout.setOrientation(LinearLayout.VERTICAL);
		
		LayoutParams lp = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		
		Button close = new Button(this);
		close.setLayoutParams(lp);
		close.setText(R.string.b_close);
		close.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		TextView text1 = new TextView(this);
		text1.setLayoutParams(lp);
		text1.setText(R.string.t_about1);
		
		mainlayout.addView(text1);
		mainlayout.addView(close);
		
		setContentView(mainlayout);
	}
}
