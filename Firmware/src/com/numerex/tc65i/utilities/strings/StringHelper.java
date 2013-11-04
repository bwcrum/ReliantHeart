package com.numerex.tc65i.utilities.strings;

public class StringHelper {
	public static String stringReplace(String text, String searchString, String replacementString) throws Exception {
		StringBuffer stringBuffer = new StringBuffer();
		
		int searchStringPosition = text.indexOf(searchString);
		int startPosition = 0;
		int searchStringLength = searchString.length();
		
		while (searchStringPosition != -1) {
			stringBuffer.append(text.substring(startPosition, searchStringPosition)).append(replacementString);
			startPosition = searchStringPosition + searchStringLength;
			searchStringPosition = text.indexOf(searchString, startPosition);
		}
		
		stringBuffer.append(text.substring(startPosition, text.length()));
		return stringBuffer.toString();
	}
}
