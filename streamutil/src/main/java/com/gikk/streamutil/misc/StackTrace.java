package com.gikk.streamutil.misc;

/**I mainly use this class since it allows for easy access to the line that printed info to the console
 * 
 * @author Simon
 *
 */
public class StackTrace {
	/**Returns a link to a code position.
	 * Intended to be inserted in an error message.
	 * <br>This will produce an error message in the output console with a clickable link that opens the file that caused the error.
	 * 
	 * <br><br><b>Example</b>
	 * <br><code>System.err.println("Error. Unknown args. " + StackTrace.getStackPos() );</code>
	 * 
	 * @return A string which contains stack trace links
	 */
	public static String getStackPos(){
		String out = "   ";
		StackTraceElement[] e = new Exception().getStackTrace();
		
		for( int i = 1; i < e.length && i < 5; i++){
			String s = e[i].toString();
			int f = s.indexOf("(");
			int l = s.lastIndexOf(")")+1;
			out += s.substring( f , l)+" ";
		}
		return out;
	}
}