package in.celest.xash3d.dedicated;
import android.view.*;
import android.content.*;
import android.graphics.*;
import java.util.*;
import android.widget.*;

public class ConsoleView extends View
{
	private List<String> strings = new ArrayList();

	private Paint bg = new Paint();
	private Paint basictext = new Paint();
	
	private int scrollindex = 1;
	
	private boolean isSelected;
	private int defTextCol = Color.WHITE;
	
	private static ConsoleView highlighted = null;

	private float h, w;
	
	ConsoleView(Context context)
	{
		super(context);

		bg.setColor(Color.TRANSPARENT);
		basictext.setColor(defTextCol);
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		
		basictext.setColor(defTextCol);
		
		h = canvas.getHeight();
		w = canvas.getWidth();

		//paint bg
		canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), bg);
		
		//paint strings from last
		int y = 20;
		int deltay = 20;
		String current;
		for (int i = strings.size()-scrollindex; i >= 0; i--)
		{
			current = strings.get(i);
			
			//char-by-char string draw
			int tempx = 10;
			float cw = 0;
			
			boolean listencolor = false;
			int colorcode = 0;
			
			for (int j = 0; j < current.length(); j++)
			{
				final String c = String.valueOf(current.charAt(j));
				cw = basictext.measureText(c);
				
				switch (c.charAt(0))
				{
					case '\033':
						listencolor = true;
						colorcode = 0;
						break;
					case '[':
						if (listencolor) break;
					case 'm':
						if (listencolor)
						{
							listencolor = false;
							
							switch (colorcode)
							{
								case 0:
									basictext.setColor(defTextCol);
									break;
								case 30:
									basictext.setColor(Color.DKGRAY);
									break;
								case 31:
									basictext.setColor(Color.RED);
									break;
								case 32:
									basictext.setColor(Color.GREEN);
									break;
								case 33:
									basictext.setARGB(255, 255, 100, 0);
									break;
								case 34:
									basictext.setColor(Color.BLUE);
									break;
								case 35:
									basictext.setColor(Color.RED);
									break;
								case 36:
									basictext.setColor(Color.CYAN);
									break;
								case 37:
									basictext.setColor(Color.GRAY);
									break;
							}
							
							break;
						} //! don't need 'break' after 'if'
					default:
						if (listencolor)
						{
							colorcode = colorcode*10 + Integer.valueOf(c);
						}
						else 
						{
							if (tempx+cw > w-20) 
							{
								y += deltay;
								tempx = 10;
							}
							canvas.drawText(c, tempx, y, basictext);
							
							tempx += cw;
						}
				}
			}
			
			y += deltay;
		}
		
		h = y;
	}
	
	public void addString(String new_s)
	{
		strings.add(new_s);
		invalidate();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		if (isSelected)
		{
			defTextCol = Color.WHITE;
			bg.setColor(Color.TRANSPARENT);
			isSelected = false;
			
			String text = removeColorcodes(strings.get(strings.size()-1));
			
			ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE); 
			ClipData clip = ClipData.newPlainText(text, text);
			clipboard.setPrimaryClip(clip);
			
			Toast.makeText(getContext(), "Text copied to clipboard", Toast.LENGTH_SHORT).show();
			
			highlighted = null;
		} else {
			defTextCol = Color.BLACK;
			bg.setColor(Color.WHITE);
			if (highlighted != null) highlighted.toggleSelected();
			highlighted = this;
			isSelected = true;
		}
		invalidate();
		
		return super.onTouchEvent(event);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		w = ((WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth();
		
		int y = 20;
		int deltay = 20;
		String current;
		for (int i = strings.size()-scrollindex; i >= 0; i--)
		{
			current = strings.get(i);

			//char-by-char string parse
			int tempx = 10;
			float cw = 0;

			boolean listencolor = false;

			for (int j = 0; j < current.length(); j++)
			{
				final String c = String.valueOf(current.charAt(j));
				cw = basictext.measureText(c);

				switch (c.charAt(0))
				{
					case '\033':
						listencolor = true;
						break;
					case '[':
						if (listencolor) break;
					case 'm':
						if (listencolor) 
						{
							listencolor = false;
							break;
						}
					default:
						if (!listencolor)
						{
							if (tempx+cw > w-20) 
							{
								y += deltay;
								tempx = 10;
							}
							tempx += cw;
						}
				}
			}

			y += deltay;
		}
		
		setMinimumHeight(y - deltay/2);
		
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
	
	public void toggleSelected()
	{
		if (isSelected)
		{
			defTextCol = Color.WHITE;
			bg.setColor(Color.TRANSPARENT);
			isSelected = false;
		} else {
			defTextCol = Color.BLACK;
			bg.setColor(Color.WHITE);
			isSelected = true;
		}
		
		invalidate();
	}
	
	public static String removeColorcodes(String current)
	{
		String ret = "";
		
		boolean listencolor = false;

		for (int j = 0; j < current.length(); j++)
		{
			final String c = String.valueOf(current.charAt(j));

			switch (c.charAt(0))
			{
				case '\033':
					listencolor = true;
					break;
				case '[':
					if (listencolor) break;
				case 'm':
					if (listencolor) 
					{
						listencolor = false;
						break;
					}
				default:
					if (!listencolor)
					{
						ret += c;
					}
			}
		}
		
		return ret;
	}
}
