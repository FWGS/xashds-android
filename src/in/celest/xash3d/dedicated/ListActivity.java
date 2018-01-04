package in.celest.xash3d.dedicated;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import android.widget.LinearLayout.*;
import java.io.*;

import android.view.View.OnClickListener;

/**
 * Created by Greg on 13.03.2017.
 * ListActivity - used for selecting files and/or folders
 */

public class ListActivity extends Activity {
	private String folder;	//directory to list
	private String filePath;
	private String relativePath;

	private LinearLayout layout;
	private TextView header;

	private int okTextColor = Color.argb(255, 0, 150, 0);	//color to use to highlight matching directories/files

	public static final String REQUEST_GAME_SELECT = "";
	public static final String REQUEST_BASEDIR_SELECT = "basedir";
	public static final String REQUEST_MAP_SELECT = "maps";
	public static final String REQUEST_DLL_SELECT = "dlls";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

        folder = getIntent().getStringExtra("folder");

		//make views
        ScrollView content = new ScrollView(this);
		content.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

		LinearLayout mainlayout = new LinearLayout(this);
		mainlayout.setLayoutParams((new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT)));
		mainlayout.setOrientation(LinearLayout.VERTICAL);

		layout = new LinearLayout(this);
		layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		layout.setOrientation(LinearLayout.VERTICAL);

        header = new TextView(this);
        header.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        if (!folder.equals(REQUEST_BASEDIR_SELECT)) header.setText("./"+folder);
			else header.setText(getIntent().getStringExtra("dir"));

		Button exit = new Button(this);
		exit.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		exit.setText(R.string.b_close);
		exit.setOnClickListener(new OnClickListener () {
				@Override
				public void onClick(View v) {
					finish();
				}
			});

		mainlayout.addView(exit);

		if (folder.equals(REQUEST_BASEDIR_SELECT)) 	//when selecting directory, we need "select" button
		{
			Button ok = new Button(this);
			ok.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			ok.setText(R.string.b_select);
			ok.setOnClickListener(new OkClickListener());
			mainlayout.addView(ok);
		}

        mainlayout.addView(header);

		String game = DedicatedStatics.getGame(this);
		if (game.equals("")) game = "valve";

		//setting up paths
		switch (folder)
		{
			case REQUEST_GAME_SELECT:
				filePath = DedicatedStatics.getBaseDir(this);
				relativePath = "";
				break;
			case REQUEST_BASEDIR_SELECT:
				filePath = getIntent().getStringExtra("dir");
				break;
			default:
				filePath = makeDir(DedicatedStatics.getBaseDir(this), game, folder);
				relativePath = folder + "/";
				break;
		}

		File dir;	//directory we are listing
		dir = new File(filePath);
        File[] files = dir.listFiles();

		if (folder.equals(REQUEST_BASEDIR_SELECT) && (!filePath.equals("/")))	//we need to be able to go to parent directory in folder selector only if current directory is not root
		{
			Button toParent = new Button(this);
			toParent.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			toParent.setText("..");
			toParent.setOnClickListener(new BaseDirPickerListener(".."));
			layout.addView(toParent);
		}

		boolean notfound = true;
        if (files != null) for (File f : files) //directory not empty
        {
            Button v = new Button(this);
            v.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            v.setText(f.getName());
			if (folder.equals("basedir")) v.setOnClickListener(new BaseDirPickerListener(f.getName()));
				else v.setOnClickListener(new FilePickerListener(f.getName()));

			switch (folder)
			{
				case REQUEST_BASEDIR_SELECT:
					if (f.isDirectory())
					{
						if (checkSubdirs(f.getAbsolutePath())) v.setTextColor(okTextColor);
						layout.addView(v);
						if (notfound) notfound = false;
					}
					break;
				case REQUEST_GAME_SELECT:
					if (f.isDirectory())
					{
						layout.addView(v);
						if (notfound) notfound = false;
					}
					break;
				case REQUEST_MAP_SELECT:
					if (f.getName().lastIndexOf(".bsp") != -1)
					{
						layout.addView(v);
						if (notfound) notfound = false;
					}
					break;
				case REQUEST_DLL_SELECT:
					if ((f.getName().lastIndexOf(".dll") != -1)||(f.getName().lastIndexOf(".so") != -1))
					{
						layout.addView(v);
						if (notfound) notfound = false;
					}
					break;
			}
        }

        if (notfound) //empty directory or not found matching files
        {
			TextView v = new TextView(this);
            v.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            v.setText(R.string.l_notfound);
            layout.addView(v);
		}

		content.addView(layout);

		mainlayout.addView(content);

        setContentView(mainlayout);

		getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
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
			if (folder.equals(REQUEST_MAP_SELECT)) //return map without extension
			{
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

			if (directory.equals("..")) //parent directory
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

			boolean notfound = true;
			if (files != null) for (File f : files)
			{
				Button v = new Button(ListActivity.this);
				v.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));
				v.setText(f.getName());
				v.setOnClickListener(new BaseDirPickerListener(f.getName()));
				if (f.isDirectory()) {
					if (checkSubdirs(f.getAbsolutePath())) v.setTextColor(okTextColor);
					layout.addView(v);
					if (notfound) notfound=false;
					}
			}
			if (notfound)
			{
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

	private boolean checkSubdirs(String inDir)
	{
		boolean result = false;

		File[] dirs = new File(inDir).listFiles();

		if (dirs != null) for (File f : dirs)
		{
			if (f.isDirectory() && (f.getName().equals("valve"))) result = true;
		}

		return result;
	}
}
