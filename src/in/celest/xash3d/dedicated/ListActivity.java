package in.celest.xash3d.dedicated;

import android.app.Activity;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.widget.ScrollView;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.*;
import android.widget.LinearLayout.LayoutParams;

import java.io.File;
import android.view.*;
import android.view.View.*;
import android.content.*;

/**
 * Created by Greg on 13.03.2017.
 */

public class ListActivity extends Activity {
	private String folder;
	private boolean isGameSelector, isMapSelector, isDllSelector;
	
	private String filePath;
	private String relativePath;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        folder = getIntent().getStringExtra("folder");
		isGameSelector = folder.equals("");
		isMapSelector = folder.equals("maps");
		isDllSelector = folder.equals("dlls");

        ScrollView content = new ScrollView(this);
		content.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		
		LinearLayout mainlayout = new LinearLayout(this);
		mainlayout.setLayoutParams((new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT)));
		mainlayout.setOrientation(LinearLayout.VERTICAL);

		LinearLayout layout = new LinearLayout(this);
		layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		layout.setOrientation(LinearLayout.VERTICAL);

        TextView header = new TextView(this);
        header.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        header.setText("./"+folder);
        //header.setTextAppearance(this, android.R.);

		Button exit = new Button(this);
		exit.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		exit.setText(R.string.b_close);
		exit.setOnClickListener(new View.OnClickListener () {
				@Override
				public void onClick(View v) {
					finish();
				}
			});

		mainlayout.addView(exit);

        layout.addView(header);
		
        File dir;
		if (isGameSelector) {
			filePath = DedicatedActivity.gamePath;
			relativePath = "";
		} else {
			filePath = makeDir(DedicatedActivity.gamePath, CommandParser.parseSingleParameter(DedicatedActivity.argsString, "-game"), folder);
			relativePath = folder + "/";
			}
		dir = new File(filePath);
        File[] files = dir.listFiles();

        if (files != null) for (File f : files)
        {
            Button v = new Button(this);
            v.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));
            v.setText(f.getName());
			v.setOnClickListener(new FilePickerListener(f.getName()));
            if (!(isGameSelector&&(!f.isDirectory())))
				if (isMapSelector) {if (f.getName().lastIndexOf(".bsp") != -1) layout.addView(v); }
					else if (!isDllSelector) layout.addView(v);
						else if (f.getName().lastIndexOf(".dll") != -1) layout.addView(v);
        } else {
			TextView v = new TextView(this);
            v.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));
            v.setText(R.string.l_notfound);
            layout.addView(v);
		}
		
		content.addView(layout);
		
		mainlayout.addView(content);

        setContentView(mainlayout);
    }
	
	private String makeDir(String dir1, String game, String dir2)
	{
		String ret = "";
		ret += dir1;
		
		if (dir1.charAt(dir1.length()-1) != '/') ret += "/";
		ret += game;
		ret += "/" + dir2 + "/";
		
		return ret;
	}
	
	public class FilePickerListener implements View.OnClickListener
	{
		private String filename;
		
		FilePickerListener(String f)
		{
			filename = f;
		}

		@Override
		public void onClick(View p1)
		{
			String result;
			if (isMapSelector) {
				result = filename.subSequence(0, filename.lastIndexOf(".")).toString();
			}
			else result = relativePath+filename;
			//Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
			//return filename
			makeResult(result);
		}
	}
	
	private void makeResult(String result)
	{
		Intent data = new Intent();
		
		data.putExtra("folder", folder);
		data.putExtra("result", result);
		
		if (getParent() == null) setResult(RESULT_OK, data);
			else getParent().setResult(RESULT_OK, data);
		
		finish();
	}
}
