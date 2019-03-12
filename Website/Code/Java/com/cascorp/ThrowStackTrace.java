package com.cascorp;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

public class ThrowStackTrace {
	public static String getStackTrace(Throwable aThrowable) {
		final Writer result = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(result);
		aThrowable.printStackTrace(printWriter);
		return result.toString();
	}
}
