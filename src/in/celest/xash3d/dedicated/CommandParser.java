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

    public static String wordFrom(String in, int index) {
        String ret = "";
        for (int i = index; (i < in.length())&&(in.charAt(i) != ' '); i++) {
            ret += in.charAt(i);
        }
        return ret;
    }
}
