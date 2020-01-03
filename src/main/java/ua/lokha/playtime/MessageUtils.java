package ua.lokha.playtime;

import org.apache.commons.lang3.StringUtils;

public class MessageUtils {
    public static String colored(String text) {
        if(text == null || text.isEmpty()) {
            return text;
        }
        text = replaceColorCodeOnly(text);
        text = StringUtils.replace(text, "\\n", "\n");
        text = StringUtils.remove(text, '\r');
        return text;

    }

    private static String replaceColorCodeOnly(String text) {
        char[] chars = text.toCharArray();
        boolean change = false;
        for (int i = 0; i < chars.length - 1; i++) {
            if (chars[i] == '&') {
                switch (chars[i + 1]) {
                    case '0':
                    case '1':
                    case '2':
                    case '3':
                    case '4':
                    case '5':
                    case '6':
                    case '7':
                    case '8':
                    case '9':
                    case 'a':
                    case 'b':
                    case 'c':
                    case 'd':
                    case 'e':
                    case 'f':
                    case 'l':
                    case 'm':
                    case 'n':
                    case 'o':
                    case 'r':
                    case 'k':
                        change = true;
                        chars[i] = '§';
                        i++;
                }
            }
        }

        if (change) {
            text = new String(chars);
        }
        return text;
    }

    public static String colored(String text, String... replaced) {
        return colored(replaced(text, replaced));
    }

    public static String replaced(String text, String... replaced) {
        for(int i = 1; i < replaced.length; i += 2) {
            text = StringUtils.replace(text, replaced[i - 1], replaced[i]);
        }
        return text;
    }

    private static final char[] colors = "0123456789abcdef".toCharArray();
}
