package in.celest.xash3d.dedicated;

import android.app.Activity;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Scroller;
import android.widget.TextView;

import java.io.File;

/**
 * Created by Greg on 13.03.2017.
 */

public class ListActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final String folder = getIntent().getStringExtra("folder");

        ScrollView content = new ScrollView(this);

        TextView header = new TextView(this);
        header.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));
        header.setText("List of folder: "+folder);
        //header.setTextAppearance(this, android.R.);

        File dir = new File(folder);
        File[] files = dir.listFiles();

        for (File f : files)
        {
            TextView v = new TextView(this);
            v.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));
            v.setText(f.getName());
            content.addView(v);
        }

        content.addView(header);

        setContentView(content);
    }
}
