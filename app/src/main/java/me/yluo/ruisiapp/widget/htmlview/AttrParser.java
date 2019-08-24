package me.yluo.ruisiapp.widget.htmlview;

import android.text.TextUtils;

import java.util.Hashtable;
import java.util.Locale;

/**
 * html属性解析器
 * 对症下药
 * a - > href
 * img -> src
 * font -> color...
 */
public class AttrParser {

    private static final Hashtable<String, Integer> S_COLOR_MAP;
    // TODO: 2017/5/10  有点不严谨
    public static final int COLOR_NONE = 0x00000001;

    static {
        S_COLOR_MAP = new Hashtable<>();
        S_COLOR_MAP.put("aqua", 0xFF00FFFF);
        S_COLOR_MAP.put("black", COLOR_NONE); // 0xFF000000 -> COLOR_NONE 去掉黑色不然在夜间模式有bug
        S_COLOR_MAP.put("blue", 0xFF0000FF);
        S_COLOR_MAP.put("darkgrey", 0xFFA9A9A9);
        S_COLOR_MAP.put("fuchsia", 0xFFFF00FF);
        S_COLOR_MAP.put("gray", 0xFF808080);
        S_COLOR_MAP.put("grey", 0xFF808080);
        S_COLOR_MAP.put("green", 0xFF008000);
        S_COLOR_MAP.put("lightblue", 0xFFADD8E6);
        S_COLOR_MAP.put("lightgrey", 0xFFD3D3D3);
        S_COLOR_MAP.put("lime", 0xFF00FF00);
        S_COLOR_MAP.put("maroon", 0xFF800000);
        S_COLOR_MAP.put("navy", 0xFF000080);
        S_COLOR_MAP.put("olive", 0xFF808000);
        S_COLOR_MAP.put("orange", 0xFFFFA500);
        S_COLOR_MAP.put("purple", 0xFF800080);
        S_COLOR_MAP.put("red", 0xFFFF0000);
        S_COLOR_MAP.put("silver", 0xFFC0C0C0);
        S_COLOR_MAP.put("teal", 0xFF008080);
        S_COLOR_MAP.put("white", 0xFFFFFFFF);
        S_COLOR_MAP.put("yellow", 0xFFFFFF00);

        //discuz
        S_COLOR_MAP.put("sienna", 0xFFA0522D);
        S_COLOR_MAP.put("darkolivegreen", 0xFF556B2F);
        S_COLOR_MAP.put("darkgreen", 0xFF006400);
        S_COLOR_MAP.put("darkslateblue", 0xFF483D8B);
        S_COLOR_MAP.put("indigo", 0xFF4B0082);
        S_COLOR_MAP.put("darkslategray", 0xFF2F4F4F);
        S_COLOR_MAP.put("darkred", 0xFF8B0000);
        S_COLOR_MAP.put("darkorange", 0xFFFF8C00);
        S_COLOR_MAP.put("slategray", 0xFF708090);
        S_COLOR_MAP.put("dimgray", 0xFF696969);
        S_COLOR_MAP.put("sandybrown", 0xFFF4A460);
        S_COLOR_MAP.put("yellowgreen", 0xFFADFF2F);
        S_COLOR_MAP.put("seagreen", 0xFF2E8B57);
        S_COLOR_MAP.put("mediumturquoise", 0xFF48D1CC);
        S_COLOR_MAP.put("royalblue", 0xFF4169E1);
        S_COLOR_MAP.put("magenta", 0xFFFF00FF);
        S_COLOR_MAP.put("cyan", 0xFF00FFFF);
        S_COLOR_MAP.put("deepskyblue", 0xFF00BFFF);
        S_COLOR_MAP.put("darkorchid", 0xFF9932CC);
        S_COLOR_MAP.put("pink", 0xFFFFC0CB);
        S_COLOR_MAP.put("wheat", 0xFFF5DEB3);
        S_COLOR_MAP.put("lemonchiffon", 0xFFFFFACD);
        S_COLOR_MAP.put("palegreen", 0xFF98FB98);
        S_COLOR_MAP.put("paleturquoise", 0xFFAFEEEE);
        S_COLOR_MAP.put("plum", 0xFFDDA0DD);
    }

    public static HtmlNode.HtmlAttr parserAttr(int type, char[] buf, int len) {
        HtmlNode.HtmlAttr attr = new HtmlNode.HtmlAttr();
        String attrStr = new String(buf, 0, len);
        switch (type) {
            case HtmlTag.A:
                attr.href = getAttrs(attrStr, 0, "href");
                if (attr.href != null && attr.href.contains("&")) {
                    attr.href = attr.href.replace("&amp;", "&");
                }
                break;
            case HtmlTag.IMG:
                attr.src = getAttrs(attrStr, 0, "src");
                if (attr.src != null && attr.src.contains("&")) {
                    attr.src = attr.src.replace("&amp;", "&");
                }
                break;
            case HtmlTag.FONT:
                attr.color = getTextColor(attrStr, 0);
                attr.fontSize = getFontSize(attrStr, 0);
                break;
            case HtmlTag.P://p 标签比较特殊 text-align 也可以是align
                attr.color = getTextColor(attrStr, 0);
                attr.textAlign = getAlign(true, attrStr, 0);
                break;
            case HtmlTag.DIV:
            case HtmlTag.UL:
                attr.align = getAlign(false, attrStr, 0);
                break;
            default:
                break;
        }
        return attr;
    }


    //只有块状标签才有意义
    //left right center
    //内部布局  align="center"
    //或者文字布局 text-align="center"
    private static int getAlign(boolean isTextAlign, String s, int start) {
        if (isTextAlign) {
            start = getValidStrPos(s, start, "text-align", 15);
        } else {
            start = getValidStrPos(s, start, "align", 10);
        }

        if (start > 0) {
            while (start < s.length() && (s.charAt(start) < 'a' || s.charAt(start) > 'z')) {
                start++;
            }
            if (s.startsWith("right", start)) {
                return HtmlNode.ALIGN_RIGHT;
            } else if (s.startsWith("center", start)) {
                return HtmlNode.ALIGN_CENTER;
            } else if (s.startsWith("left", start)) {
                return HtmlNode.ALIGN_LEFT;
            }
        }
        return HtmlNode.ALIGN_UNDEFINE;
    }

    //color="red" " color:red "
    //attr css
    private static int getTextColor(String s, int start) {
        int j = getValidStrPos(s, start, "color", 10);
        if (j < 0) {
            return COLOR_NONE;
        }
        //color 排除background-color bgcolor
        if (j > start + 5 && ((s.charAt(j - 6) == '-') || (s.charAt(j - 6) == 'g'))) {
            return COLOR_NONE;
        }

        while (j < s.length() - 3) {
            if (s.charAt(j) == '=') {
                while (j < (s.length() - 3) && s.charAt(j) != '\"') {
                    j++;
                }

                if (s.charAt(j) == '\"') {
                    start = j + 1;
                    while (start < s.length()
                            && s.charAt(start) != '\"'
                            && s.charAt(start) != ' '
                            && s.charAt(start) != '\n') {
                        start++;
                    }

                    return getHtmlColor(j + 1, start, s);
                }

                return -1;
            } else if (s.charAt(j) == ':') {
                j++;
                while (j < s.length() - 3 && (s.charAt(j) == ' ' || s.charAt(j) == '\n')) {
                    j++;
                }

                start = j + 1;
                while (start < s.length()
                        && s.charAt(start) != ';'
                        && s.charAt(start) != ' '
                        && s.charAt(start) != '\n'
                        && s.charAt(start) != '\"') {
                    start++;
                }

                return getHtmlColor(j, start, s);
            } else {
                if (s.charAt(j) == '\"') {
                    return COLOR_NONE;
                }
                j++;
            }
        }

        return COLOR_NONE;
    }

    //text-decoration:none underline overline line-through
    //css
    //TextPaint tp = new TextPaint();
    //tp.setUnderlineText(true);  //1
    //tp.setStrikeThruText(true); //2
    //none //0
    public static int getTextDecoration(int start, String s) {
        int j = getValidStrPos(s, start, "text-decoration", 20);
        if (j < 0) {
            return -1;
        }

        while (j < s.length() && (s.charAt(j) < 'a' || s.charAt(j) > 'z')) {
            j++;
        }

        if (s.startsWith("underline", j)) {
            return HtmlNode.DEC_UNDERLINE;
        } else if (s.startsWith("line-through", j)) {
            return HtmlNode.DEC_LINE_THROUGH;
        } else if (s.startsWith("none", j)) {
            return HtmlNode.DEC_NONE;
        }
        return HtmlNode.DEC_UNDEFINE;
    }

    //size="5"
    private static int getFontSize(String source, int start) {
        String s = getAttrs(source, start, "size");
        if (s == null) {
            return -1;
        }
        if (TextUtils.isDigitsOnly(s)) {
            return Integer.parseInt(s);
        }

        return -1;
    }


    //a="b" src="" href=""
    private static String getAttrs(String source, int start, String to) {
        if (source.length() - start - 4 < to.length()) {
            return null;
        }
        int j = getValidStrPos(source, start, to, to.length() + 4);
        if (j < 0) {
            return null;
        }
        //="aaaa"
        j = source.indexOf("=", j);
        if (j < 0) {
            return null;
        }

        j = source.indexOf("\"", j);
        if (j < 0) {
            return null;
        }
        j++;
        if (j > source.length() - 2) {
            return null;
        }

        while (j < (source.length() - 2)
                && (source.charAt(j) == ' '
                || source.charAt(j) == '\n')) {
            j++;
        }

        int pos2 = j + 1;
        while (pos2 < source.length() - 1
                && source.charAt(pos2) != '\"'
                && source.charAt(pos2) != ' '
                && source.charAt(pos2) != '\n') {
            pos2++;
        }

        if (pos2 <= source.length() - 1) {
            return source.substring(j, pos2);
        }
        return null;
    }


    //html color-> android color
    private static int getHtmlColor(int start, int end, String color) {
        if (end - start < 3) {
            return COLOR_NONE;
        }
        if (color.charAt(start) == '#') {
            if (end - start == 9) {
                start += 2;
            }
            if (end - start == 7) {
                String colorText = color.substring(start + 1, end);
                if ("000000".equals(colorText) || "FF000000".equalsIgnoreCase(colorText)) {
                    //修复夜间模式黑色颜色值看不清
                    return COLOR_NONE;
                }
                int colorInt = Integer.parseInt(color.substring(start + 1, end), 16);
                return (colorInt | 0xff000000);
            }
            return COLOR_NONE;
        } else {
            Integer i = S_COLOR_MAP.get(color.substring(start, end).toLowerCase(Locale.US));
            if (i != null) {
                return i;
            }
            return COLOR_NONE;
        }
    }

    //从source中找到to并且指定最小有效长度,如寻找style那么最小有效长度为17
    //style="color:red" 17
    //text-align:left 15
    private static int getValidStrPos(String source, int start, String to, int minlen) {
        int len = source.length() - start;
        if (len < minlen) {
            return -1;
        }

        int pos1 = 0;
        int pos2 = 0;
        while (pos1 <= len - minlen) {
            pos2 = 0;
            while (source.charAt(pos1) == to.charAt(pos2)) {
                pos2++;
                pos1++;
                if (pos2 == to.length()) {
                    return pos1;
                }
            }
            pos1++;
        }
        return -1;
    }
}
