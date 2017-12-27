package in.celest.xash3d.dedicated;

import java.util.*;
import android.content.*;

public class DedicatedStatics
{
	public static DedicatedActivity launched = null;
	public static List<String> 	logView = new ArrayList();
	
	public static String MESS_SERVICE_STARTING	= "[XashDSAndroid]\033[33mStarting service...\033[0m";
	public static String MESS_BINARIES_STARTING = "[XashDSAndroid]\033[33mExecuting binaries...\033[0m";
	public static String MESS_SERVICE_STARTED 	= "[XashDSAndroid]\033[32mService successfully started!\033[0m";
	public static String MESS_SERVICE_KILLING 	= "[XashDSAndroid]\033[33mKilling service...\033[0m";
	public static String MESS_SERVICE_KILLED 	= "[XashDSAndroid]\033[31mService killed.\033[0m";
	
	public static String XASH_BINARY 	= "xash-old";
	public static String XASH_BINARY_SSE= "xash_sse2";
	
	private static String XASH_BINARY_NEW 	= "xash";
	private static String XASH_BINARY_SSE_NEW= "xash";
	private static String XASH_BINARY_OLD 	= "xash-old";
	private static String XASH_BINARY_SSE_OLD= "xash_sse2";
	
	public static void chstr(boolean isnew)
	{
		if (isnew)
		{
			XASH_BINARY = XASH_BINARY_NEW;
			XASH_BINARY_SSE = XASH_BINARY_SSE_NEW;
		} else {
			XASH_BINARY = XASH_BINARY_OLD;
			XASH_BINARY_SSE = XASH_BINARY_SSE_OLD;
		}
	}
	
	public static boolean isNewBin()
	{
		return XASH_BINARY == XASH_BINARY_NEW;
	}
	
	public static String getBaseDir(Context context)
	{
		return context.getSharedPreferences("dedicated", 0).getString("basedir", "/sdcard/xash");
	}
	
	public static String getArgv(Context context)
	{
		return context.getSharedPreferences("dedicated", 0).getString("argv", "-dev 5 -dll dlls/hl.dll");
	}
	
	public static String getGame(Context context)
	{
		return CommandParser.parseSingleParameter(context.getSharedPreferences("dedicated", 0).getString("argv", "-dev 5 -dll dlls/hl.dll"), "-game");
	}
	
	public static String getTranslator(Context context)
	{
		int i = Integer.valueOf(context.getSharedPreferences("dedicated", 0).getString("translator", "0"));
		String[] t = DedicatedActivity.listTranslators();
		if (i < t.length) return t[i];
			else {
				context.getSharedPreferences("dedicated", 0).edit().putString("translator", "0").commit();
				return t[0];
			}
	}
	
	public static int getTranslatorIndex(Context context)
	{
		return Integer.valueOf(context.getSharedPreferences("dedicated", 0).getString("translator", "0"));
	}

	public static int getMaxLogLength(Context context)
	{
		return Integer.valueOf(context.getSharedPreferences("dedicated", 0).getString("a_maxloglines", "512"));
	}
}
