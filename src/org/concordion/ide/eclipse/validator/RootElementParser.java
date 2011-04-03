package org.concordion.ide.eclipse.validator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.IDocument;

public class RootElementParser {
	private final static Pattern PATTERN = Pattern.compile("<[hH][tT][mM][lL][^>]*xmlns:([^>]*)=\"http://www.concordion.org/");
	private static final int MAX_MATCH_LEN = 512;
	private String nsPrefix;
	
	public boolean isConcordionSpec(IDocument document) {
		if (document != null) {
			return isConcordionSpec(document.get());
		} else {
			return false;
		}
	}
	
	public boolean isConcordionSpec(String document) {
		if (document == null) {
			return false;
		}
		
		Matcher matcher = PATTERN.matcher(document);
		matcher.region(0, Math.min(document.length(), MAX_MATCH_LEN));
		if (matcher.find()) {
			nsPrefix = matcher.group(1).trim();
			return true;
		} else {
			nsPrefix = null;
			return false;
		}
	}

	public String getNamespacePrefix() {
		return nsPrefix;
	}
}
