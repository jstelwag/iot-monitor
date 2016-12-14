import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Jaap on 13-12-2016.
 */
public class Gooimijweg {

    public static void main(String[] args) {
        System.out.println("hello");

        Pattern pattern = Pattern.compile(Pattern.quote("/") + "(.*?)" + Pattern.quote("/") + "(\\d+)"
                + Pattern.quote("/") + "(\\d+)" + Pattern.quote("/") + "(\\d+)" + Pattern.quote("/"));
        Matcher matcher = pattern.matcher("/float/2/5/1/2/");
        if (matcher.find()) {
            System.out.println(matcher.group(1));
            System.out.println(matcher.group(2));
            System.out.println(matcher.group(3));
            System.out.println(matcher.group(4));
        }
    }
}
