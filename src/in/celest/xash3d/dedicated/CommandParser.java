package in.celest.xash3d.dedicated;

/**
 * Created by Greg on 11.03.2017.
 */

public class CommandParser {
    public static String makeParamArgString(String in, String param)
    {
        String ret = "";
        String temp = "";

        for (int i = 0; i < in.length(); i++)
        {
            if (in.charAt(i) == ',') {
                ret += param + " " + temp + " ";
                temp = "";
                i++;
            } else {
                temp += in.charAt(i);
            }
        }

        ret += param + " " + temp + " ";

        return ret;
    }

    public static String parseSingleParameter(String args, String param) {
        int i = args.indexOf(param);
        if (i != -1) {
            i += new String(param).length() + 1;
            return wordFrom(args, i);
        } else return "";
    }

    public static boolean parseLogicParameter(String args, String param) {
        if (args.indexOf(param) != -1)
        {
            return true;
        } else return false;
    }

    public static String parseMultipleParameter(String args, String param) {
        String ret = "";
        boolean first = true;
        for (int i = args.indexOf(param); i >= 0; i = args.indexOf(param, i)) {
            i += new String(param).length() + 1;
            if (!first) ret += ", ";
            else first = false;
            ret += wordFrom(args, i);
        }
        return ret;
    }

    public static int paramCount(String args, String param)
    {
        if (args.lastIndexOf(param) == -1) return 0;
            else
            {
                int i = 0;
                for (i = args.indexOf(param); i >= 0; i = args.indexOf(param, i));
                return i;
            }
    }

    public static String removeAll(String args, String param)
    {
        for (int i = args.indexOf(param); i >= 0; i = args.indexOf(param)) {
            int j = i;
            boolean f = true;
            while (j < args.length())
            {
                if (j < args.length()-1)
                    if (args.charAt(j) == ' ')
                        if (!((args.charAt(j+1) != '+') || (args.charAt(j+1) != '-'))) break;
                j++;
            }
            args = args.replace(args.substring(i, j), "");
        }

        return format(args);
    }

    public static String format(String args)
    {
        String ret = "";
        for (int i = 0; i < args.length(); i++)
        {
            char c = args.charAt(i);
            if (i == 0)
            {
                if (c != ' ') ret += c;
            } else {
                if (!((c == ' ') && (args.charAt(i-1) == ' '))) ret += c;
            }
        }
        return ret;
    }

    public static String addParam(String args, String param)
    {
        if (args.charAt(args.length() - 1) != ' ')  args += " ";
        args += param;
        return format(args);
    }

    public static String wordFrom(String in, int index) {
        String ret = "";
        for (int i = index; (i < in.length())&&(in.charAt(i) != ' '); i++) {
            ret += in.charAt(i);
        }
        return ret;
    }
}
