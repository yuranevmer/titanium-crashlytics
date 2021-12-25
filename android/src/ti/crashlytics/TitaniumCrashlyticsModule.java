/**
 * This file was auto-generated by the Titanium Module SDK helper for Android
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-present by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 *
 */
package ti.crashlytics;

import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.KrollRuntime;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.TiExceptionHandler;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

@Kroll.module(name="TitaniumCrashlytics", id="ti.crashlytics")
public class TitaniumCrashlyticsModule extends KrollModule
{
	// Methods
	@Kroll.method
	public void log(String message)
	{
		FirebaseCrashlytics.getInstance().log(message);
	}

	@Kroll.method
	public void crash() {
		throw new RuntimeException("This is a crash");
	}

	@Kroll.setProperty
	public void setUserId(String userId)
	{
		FirebaseCrashlytics.getInstance().setUserId(userId);
	}

	@Kroll.setProperty
	public void setCrashlyticsCollectionEnabled(boolean crashlyticsCollectionEnabled)
	{
		FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(crashlyticsCollectionEnabled);
	}

	@Kroll.method
	public void trackCustomValue(String key, String userProperty) {
		FirebaseCrashlytics.getInstance().setCustomKey(key, userProperty);
	}
	
	public static StackTraceElement[] generateStackTrace(String javaStack, String jsStack, String errorSourceName, String errorMessage, String errorLineSource, int errorLine) {
		String bigStack;
		
		if (javaStack != null && jsStack != null) {
			bigStack = javaStack.substring(javaStack.indexOf(System.lineSeparator()) + 1) + jsStack.substring(jsStack.indexOf(System.lineSeparator()) + 1);
		} else if (javaStack != null) {
			bigStack = javaStack.substring(javaStack.indexOf(System.lineSeparator()) + 1);
		} else if (jsStack != null) {
			bigStack = jsStack.substring(jsStack.indexOf(System.lineSeparator()) + 1);
		} else {
			StackTraceElement[] trace = new StackTraceElement[] {
				new StackTraceElement(errorSourceName, errorMessage, errorLineSource, errorLine)
			};
			return trace;
		}
		
		String[] stackElements = bigStack.split(System.lineSeparator());
		StackTraceElement[] trace = new StackTraceElement[stackElements.length + 1];
		trace[0] = new StackTraceElement(errorSourceName, errorMessage, errorLineSource, errorLine);
		for (int i = 0; i < stackElements.length; i++) {
			String[] splitByParen = stackElements[i].substring(0, stackElements[i].length() - 1).split("\\(");
				
			String fileName = "";
			int lineNumber = 0;
			if (splitByParen[1].indexOf(":") > -1) {
				String[] insideParen = splitByParen[1].split(":");
				for (int j = 0; j < insideParen.length; j++) {
					if ((insideParen[j].matches("[0-9]+"))) {
				        // within the parenthesis, the first number is the line number, for both javaStack and jsStack
				        lineNumber = Integer.parseInt(insideParen[j]);
				        break;
					} else {
				        // within the parenthesis, everything before first number is the fileName
				        fileName += insideParen[j] + ":";
				    }
				}
				// remove extra ":"
				fileName = fileName.substring(0, fileName.length() - 1);
			} else {
				fileName = splitByParen[1];
				lineNumber = 0;
			}

			int lastIndex = splitByParen[0].lastIndexOf('.');

			String declaringClass = "";
			String methodName = "";

			if (lastIndex == -1) {
				declaringClass = "unknown";
				methodName = "unknown";
			} else {
				declaringClass = splitByParen[0].substring(0, lastIndex).trim();
				methodName = splitByParen[0].substring(lastIndex + 1);
				
				if (declaringClass == null) {
					declaringClass = "unknown";
				}
				if (methodName == null) {
					methodName = "unknown";
				}
			}

			trace[i+1] = new StackTraceElement(declaringClass, methodName, fileName, lineNumber);
	    }
			
		return trace;
	}

	@Kroll.onAppCreate
	public static void onAppCreate(TiApplication app) {
		KrollRuntime.setPrimaryExceptionHandler(new TiExceptionHandler(){
			@Override
			public void handleException(ExceptionMessage error)
			{
				Throwable th = new Throwable();
				th.setStackTrace(generateStackTrace(error.javaStack, error.jsStack, error.sourceName, error.message, error.lineSource, error.line));
				FirebaseCrashlytics.getInstance().recordException(th);
				super.handleException(error);
			}
		});
	}
}
