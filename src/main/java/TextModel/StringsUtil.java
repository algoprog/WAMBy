package TextModel;

/**
 *
 * @author Chris Samarinas
 */
public class StringsUtil {
    public static String removeSpaces(String str) {
       return str.replaceAll("\\r\\n|\\r|\\n", " ");
    }
    
    public static boolean containsDate(String str) {
        String month_pattern = "(jan(uary)?)|(feb(ruary)?)|(mar(ch)?)|(apr(il)?)|(may)|(jun(e)?)|(jul(y)?)|(aug(ust)?)|(sep(tember)?)|(oct(ober)?)|(nov(ember)?)|(dec(ember)?)";
        
        String pattern = "(.*)(?i)("
                + "(([0-9]+.*)(((s(ec(ond)?)?)|(m(in(ute)?)?)|(h(our)?)|(d(ay)?)|(w(eek)?)|(m(onth)?)|(y(ear)?))(s)?)\\s+ago)"
                + "|(([0-9]{1,2})/([0-9]{1,2})/(19([0-9]){2}))"
                + "|(([0-9]{1,2})/([0-9]{1,2})/(19([0-9]){2}))"
                + "|(("+month_pattern+")\\s+[0-9]{1,2})"
                + "|([0-9]{1,2}(th|st|nd|rd)?\\s+("+month_pattern+"))"
                + "|([0-9]{1,2}:[0-9]{2}\\s+(pm|am))"
                + ")(.*)";
        return str.matches(pattern);
    }
    
    public static boolean containsUserLink(String str) {
        String pattern = "((.*)/)?(?i)((u(ser(s)?)?)|(profile)|(activity)|(posts)|(member(s)?))((\\.php)|(\\.aspx)|(\\.html)|(/))(.*)";
        
        return str.matches(pattern);
    }
}
