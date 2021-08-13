package wse.utils.ini;

import wse.utils.Option;

public class IniOptions {
	
	public static enum DuplicatePropertyRule {
		/** Simply discard duplicate properties within the same group */
		IGNORE,
		/** Override the already existing property with the same name within the same group */
		OVERRIDE,
		/** Append the new value to the existing one */
		APPEND,
		/** Combine the values into a LinkedList&ltString&gt */
		ARRAY
	}
	
	public static enum WhitespaceRule {
		/** Leave all whitespaces as they are. <br><br>
		 * <b>Example:</b> <pre><code>hostname = wse.app\r\n</code></pre><br>
		 * Will have key <code>"hostname "</code> (whitespace at the end)
		 * and value <code>" wse.app"</code> (whitespace at the beginning)<br>
		 * Newline characters are always ignored*/
		IGNORE,
		/** Keys and values will have their string values trimmed of any preceeding or exceeding whitespace characters. */
		TRIM,
		/** All whitespace characters are removed from keys and values. (for example: making "host name" and "hostname" equivalent) */
		REMOVE
		
	}
	
	public static Option<DuplicatePropertyRule> DUPLICATE_PROPERTY_RULE = new Option<>(IniOptions.class, "DUPLICATE_PROPERTY_RULE", DuplicatePropertyRule.OVERRIDE);
	
	public static Option<WhitespaceRule> WHITESPACE_RULE = new Option<>(IniOptions.class, "WHITESPACE_RULE", WhitespaceRule.TRIM);
	
	public static Option<Character> KEY_VALUE_SEPARATOR = new Option<>(IniOptions.class, "KEY_VALUE_SEPARATOR", '=');
	
}
