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
	private boolean isGameSelector, isMapSelector, isDllSelector, isBaseSelector;
	
	private String filePath;
	private String relativePath;
	
	private LinearLayout layout;
	private TextView header;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);

        folder = getIntent().getStringExtra("folder");
		isGameSelector = folder.equals("");
		isMapSelector = folder.equals("maps");
		isDllSelector = folder.equals("dlls");
		isBaseSelector = folder.equals("basedir");

        ScrollView content = new ScrollView(this);
		content.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		
		LinearLayout mainlayout = new LinearLayout(this);
		mainlayout.setLayoutParams((new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT)));
		mainlayout.setOrientation(LinearLayout.VERTICAL);

		layout = new LinearLayout(this);
		layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		layout.setOrientation(LinearLayout.VERTICAL);

        header = new TextView(this);
        header.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        if (!isBaseSelector) header.setText("./"+folder);
			else header.setText(getIntent().getStringExtra("dir"));
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
		
		if (isBaseSelector)
		{
			Button ok = new Button(this);
			ok.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));
			ok.setText(R.string.b_select);
			ok.setOnClickListener(new OkClickListener());
			mainlayout.addView(ok);
		}

        mainlayout.addView(header);
		
        File dir;
		
		String game = CommandParser.parseSingleParameter(DedicatedActivity.argsString, "-game");
		if (game.equals("")) game = "valve";
		
		if (isGameSelector) {
			filePath = DedicatedActivity.gamePath;
			relativePath = "";
		} else if (isBaseSelector)
			{
				filePath = getIntent().getStringExtra("dir");
			} else {
					filePath = makeDir(DedicatedActivity.gamePath, game, folder);
					relativePath = folder + "/";
				}
		dir = new File(filePath);
        File[] files = dir.listFiles();
		
		if (isBaseSelector && (!filePath.equals("/")))
		{
			Button toParent = new Button(this);
			toParent.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));
			toParent.setText("..");
			toParent.setOnClickListener(new BaseDirPickerListener(".."));
			layout.addView(toParent);
		} 
		
        if (files != null) for (File f : files)
        {
            Button v = new Button(this);
            v.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));
            v.setText(f.getName());
			if (isBaseSelector) v.setOnClickListener(new BaseDirPickerListener(f.getName()));
				else v.setOnClickListener(new FilePickerListener(f.getName()));
            if (!(isGameSelector&&(!f.isDirectory())))
				if (isMapSelector) {if (f.getName().lastIndexOf(".bsp") != -1) layout.addView(v); }
					else if (!isDllSelector) layout.addView(v);
						else if ((f.getName().lastIndexOf(".dll") != -1)||
									(f.getName().lastIndexOf(".so") != -1)) layout.addView(v);
							else if (isBaseSelector && f.isDirectory()) layout.addView(v);
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
			makeResult(result);
		}
	}
	
	public class OkClickListener implements View.OnClickListener
	{
		@Override
		public void onClick(View p1)
		{
			makeResult(filePath);
		}
	}
	
	public class BaseDirPickerListener implements View.OnClickListener
	{
		String directory;
		
		BaseDirPickerListener(String d)
		{
			directory = d;
		}
		
		@Override
		public void onClick(View p1)
		{
			layout.removeAllViews();
			
			File dir;

			if (directory.equals(".."))
			{
				if (filePath.charAt(filePath.length()-1) == '/') filePath = filePath.substring(0, filePath.length()-2);
				filePath = filePath.substring(0, filePath.lastIndexOf("/"));
				filePath += "/";
			} else 
			{
				if (filePath.charAt(filePath.length()-1) != '/') filePath += "/";
				filePath += directory;
			}
			
			header.setText(filePath);
			
			dir = new File(filePath);
			File[] files = dir.listFiles();

			if (!filePath.equals("/"))
			{
				Button toParent = new Button(ListActivity.this);
				toParent.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));
				toParent.setText("..");
				toParent.setOnClickListener(new BaseDirPickerListener(".."));
				layout.addView(toParent);
			}
				
			if (files != null) for (File f : files)
				{
					Button v = new Button(ListActivity.this);
					v.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));
					v.setText(f.getName());
					if (isBaseSelector) v.setOnClickListener(new BaseDirPickerListener(f.getName()));
					else v.setOnClickListener(new FilePickerListener(f.getName()));
					if (f.isDirectory()) layout.addView(v);
				} else {
				TextView v = new TextView(ListActivity.this);
				v.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));
				v.setText(R.string.l_notfound);
				layout.addView(v);
			}
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
