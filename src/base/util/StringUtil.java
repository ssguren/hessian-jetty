package base.util;

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {

	private static final String ESCAPE_CHARS = ",'+-&|!^\\:(){}[]\"~：（）｛｝【】";

	private static final String REGEX = "@\\d@";

	private static final String regEx_script = "<script[^>]*?>[\\s\\S]*?<\\/script>"; // 定义script的正则表达式
	private static final String regEx_style = "<style[^>]*?>[\\s\\S]*?<\\/style>"; // 定义style的正则表达式
	private static final String regEx_html = "<[^>]+>"; // 定义HTML标签的正则表达式
	private static final String regEx_space = "\\s*|\t|\r|\n";// 定义空格回车换行符

	public static String addLeadingZero(int num) {
		DecimalFormat df = new DecimalFormat("00000");
		return df.format(num);
	}

	public static String[] split(String str, char c) {
		List<String> list = split2List(str, c);
		return list.toArray(new String[list.size()]);
	}

	public static String[] split(String s, String splitter) {
		if (s == null || splitter == null || s.length() == 0
				|| splitter.length() == 0)
			return null;
		char[] src = s.toCharArray();
		char[] split = splitter.toCharArray();
		List<String> list = new ArrayList<String>();
		int i, j, splitLen = split.length, begin = 0;
		for (i = 0; i < src.length;) {
			for (j = 0; j < splitLen; j++)
				if ((i + j) >= src.length || src[i + j] != split[j])
					break;
			if (j == splitLen) {
				list.add((begin == i) ? null
						: new String(src, begin, i - begin));
				i += splitLen;
				begin = i;
			} else
				i++;
		}
		list.add((begin == src.length) ? null : new String(src, begin,
				src.length - begin));
		String[] ret = new String[list.size()];

		return list.toArray(ret);
	}

	public static String[] split(String s, String splitter, int num) {
		if (s == null || splitter == null || s.length() == 0
				|| splitter.length() == 0)
			return null;
		char[] src = s.toCharArray();
		char[] split = splitter.toCharArray();
		List<String> list = new ArrayList<String>();
		int i, j, splitLen = split.length, begin = 0;
		for (i = 0; i < src.length;) {
			for (j = 0; j < splitLen; j++)
				if ((i + j) >= src.length || src[i + j] != split[j])
					break;
			if (j == splitLen) {
				list.add((begin == i) ? null
						: new String(src, begin, i - begin));
				i += splitLen;
				begin = i;
				if (list.size() == num)
					return list.toArray(new String[num]);
			} else
				i++;
		}
		list.add((begin == src.length) ? null : new String(src, begin,
				src.length - begin));
		String[] ret = new String[list.size()];

		return list.toArray(ret);
	}

	public static List<String> split2List(String str, char c) {
		int len = str.length();
		List<String> list = new ArrayList<String>();
		StringBuffer sb = new StringBuffer("");
		for (int i = 0; i < len; i++) {
			char ch = str.charAt(i);
			if (ch == c) {
				list.add(sb.toString());
				sb.delete(0, sb.length());
			} else {
				sb.append(ch);
			}
		}

		if (sb.length() > 0)
			list.add(sb.toString());

		return list;
	}

	public static boolean isDigitStr(String str) {
		if (isEmptyStr(str))
			return false;

		for (char c : str.toCharArray()) {
			if (c < '0' || c > '9')
				return false;
		}

		return true;
	}

	public static boolean isPhoneNumberStr(String str) {
		if (isDigitStr(str)) {
			if (str.length() == 11)
				return true;
		}

		return false;
	}

	public static boolean isABCStr(String str) {
		if (isEmptyStr(str))
			return false;

		for (char c : str.toCharArray()) {
			if (!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')))
				return false;
		}

		return true;
	}

	public static boolean isLetterStr(String str) {
		if (isEmptyStr(str))
			return false;

		for (char c : str.toCharArray()) {
			if (!isLetter(c))
				return false;
		}

		return true;
	}

	public static boolean isLetter(char c) {
		int k = 0x80;
		return c / k == 0 ? true : false;
	}

	public static boolean isNotNumCode(String str) {
		Pattern pattern = Pattern.compile("[a-zA-Z0-9*?\\s]*");
		Matcher isNumCode = pattern.matcher(str);
		if (!isNumCode.matches()) {
			return true;
		}

		return false;
	}

	public static boolean isEmptyStr(String str) {
		if (str == null || str.trim().equals("")
				|| str.equalsIgnoreCase("null"))
			return true;
		else
			return false;
	}

	public static String array2Str(int[] array, String delimit) {
		String str = "";

		if (array != null && array.length > 0) {
			for (int i = 0; i < array.length; i++) {
				if (i > 0)
					str += delimit;
				str += array[i];
			}
		}

		return str;
	}

	public static String array2Str(String[] array, String delimit) {
		String str = "";

		if (array != null && array.length > 0) {
			for (int i = 0; i < array.length; i++) {
				if (i > 0)
					str += delimit;
				str += array[i];
			}
		}

		return str;
	}

	public static byte[] str2Bytes(String str, String charset) {
		if (null != str) {
			try {
				return str.getBytes(charset);
			} catch (UnsupportedEncodingException e) {
			}
		}

		return null;
	}

	public static String bytes2Str(byte[] bs, String charset) {
		try {
			return new String(bs, charset);
		} catch (Exception e) {
		}

		return null;
	}

	public static String preHandleUseTrim(String qry) {
		return trimInValidChar(preHandleSimple(qry));
	}

	public static String preHandleUseEscape(String qry) {
		return transEscapeChars(preHandleSimple(qry), false);
	}

	public static String preHandleSimple(String qry) {
		StringBuilder sb = new StringBuilder();
		qry = (qry == null ? "" : qry.trim());
		qry = qry.replace('　', ' ');
		qry = trimDelimiter(qry, "/");
		qry = trimDelimiter(qry, "=");

		for (int i = 0; i < qry.length(); i++) {
			char c = qry.charAt(i);
			switch (c) {
			case '*':
				if (i > 0 && qry.charAt(i - 1) != '/'
						&& qry.charAt(i - 1) != '*' && qry.charAt(i - 1) != '?'
						&& qry.charAt(i - 1) != '(') {
					if (qry.charAt(i - 1) != ' ')
						sb.append(c);
				}
				break;
			case '?':
				if (i > 0 && qry.charAt(i - 1) != '/'
						&& qry.charAt(i - 1) != '*' && qry.charAt(i - 1) != '(') {
					if (qry.charAt(i - 1) != ' ')
						sb.append(c);
				}
				break;
			case ' ':
				if (i > 0 && qry.charAt(i - 1) != ' '
						&& qry.charAt(i - 1) != '(' && qry.charAt(i + 1) != ')') {
					sb.append(c);
				}
				break;
			default:
				sb.append(c);
			}
		}

		return sb.toString();
	}

	public static String trimDelimiter(String str, String delimiter) {
		String[] s = StringUtil.split(str, delimiter);
		if (s != null) {
			String temp = "";
			for (int i = 0; i < s.length; i++) {
				if (i > 0)
					temp += delimiter;
				temp += (s[i] == null ? "" : s[i].trim());
			}

			return temp;
		}

		return str;
	}

	public static String trimInValidChar(String s) {
		if (s == null || s.length() == 0)
			return "";

		StringBuffer sb = new StringBuffer(s);
		for (int i = 0; i < sb.length();) {
			char c = sb.charAt(i);
			if (ESCAPE_CHARS.indexOf(c) != -1) {
				sb.deleteCharAt(i);
			} else {
				i++;
			}
		}

		return sb.toString();
	}

	public static String transEscapeChars(String s, boolean wildcard) {
		if (s == null || s.length() == 0)
			return "";

		StringBuilder sb = new StringBuilder();
		char[] chars = s.toCharArray();
		for (char c : chars) {
			if (wildcard && (c == '*' || c == '?')) {
			} else {
				for (int i = 0; i < ESCAPE_CHARS.length(); i++) {
					if (c == ESCAPE_CHARS.charAt(i)) {
						sb.append('\\');
					}
				}
			}
			sb.append(c);
		}

		return sb.toString();
	}

	public static String getSaveName(String className) {
		if (!isEmptyStr(className)) {
			int idx = Math.max(className.lastIndexOf('.') + 1, 0);
			return className.substring(idx);
		}

		return null;
	}

	public static String defaultString(String str) {
		if (str == null)
			return "";
		else
			return str;
	}

	public static String defaultString(String str, String defaultStr) {
		if (str == null)
			if (defaultStr != null)
				return defaultStr;
			else
				return "";
		else
			return str;
	}

	public static String replace(String ori, String replacement) {
		return replace(REGEX, ori, replacement);
	}

	public static String replace(String regex, String ori, String replacement) {
		if (regex != null && ori != null && replacement != null)
			return ori.replaceAll(regex, replacement);

		return ori;
	}

	public static String replace(String ori, List<String> itemList) {
		return replace(REGEX, ori, itemList);
	}

	public static String replace(String regex, String ori, List<String> itemList) {
		String tmp = ori;
		if (tmp != null && itemList != null) {
			for (String item : itemList) {
				tmp = tmp.replaceFirst(regex, item);
			}
		}

		return tmp;
	}

	public static String Str2Hex(String msg) {
		if (isEmptyStr(msg))
			return null;

		StringBuffer res = new StringBuffer();
		char[] chars = msg.toCharArray();
		for (char ch : chars) {
			res.append(Integer.toHexString((int) ch));
		}

		return res.toString();
	}

	public static String Hex2Str(String hex) {
		if (isEmptyStr(hex) || hex.length() % 2 != 0)
			return null;

		StringBuffer res = new StringBuffer();
		for (int i = 0; i < hex.length();) {
			res.append((char) Integer.parseInt(hex.substring(i, i + 2), 16));
			i += 2;
		}

		return res.toString();
	}

	public static String getDefaultStr(String str, String defaultStr) {
		if (isEmptyStr(str))
			return defaultStr;
		else
			return str;
	}

	public static String getDefaultStr(String str) {
		return getDefaultStr(str, "");
	}

	public static String replaceBlank(String str) {
		String dest = "";

		if (str != null) {
			Pattern p = Pattern.compile("\\s*|\t|\r|\n");
			Matcher m = p.matcher(str);
			dest = m.replaceAll("");
		}

		return dest;
	}

	public static String stringValueOf(Object obj) {
		if (null == obj)
			return "";
		return String.valueOf(obj);
	}

	public static Integer stringToInteger(String str) {
		if (isDigitStr(str)) {
			return Integer.valueOf(str);
		}
		return null;
	}

	public static String removeHtmlTag(String content) {

		Pattern p_script = Pattern.compile(regEx_script,
				Pattern.CASE_INSENSITIVE);
		if (!StringUtil.isEmptyStr(content)) {
			Matcher m_script = p_script.matcher(content);
			content = m_script.replaceAll(""); // 过滤script标签

			Pattern p_style = Pattern.compile(regEx_style,
					Pattern.CASE_INSENSITIVE);
			Matcher m_style = p_style.matcher(content);
			content = m_style.replaceAll(""); // 过滤style标签

			Pattern p_html = Pattern.compile(regEx_html,
					Pattern.CASE_INSENSITIVE);
			Matcher m_html = p_html.matcher(content);
			content = m_html.replaceAll(""); // 过滤html标签

			Pattern p_space = Pattern.compile(regEx_space,
					Pattern.CASE_INSENSITIVE);
			Matcher m_space = p_space.matcher(content);
			content = m_space.replaceAll(""); // 过滤空格回车标签
			content = content.replaceAll("&nbsp;", "");
			content = content.trim();
		}
		return content; // 返回文本字符串
	}

	// 从字符中提取汉字
	public static String getChineseChar(String content) {
		String newContent = "";
		Pattern p = Pattern.compile("[\u4E00-\u9FA5]+");
		Matcher m = p.matcher(content);
		while (m.find()) {
			newContent = m.group(0);
		}
		return newContent;
	}

	public static void main(String[] args) {

	}
}