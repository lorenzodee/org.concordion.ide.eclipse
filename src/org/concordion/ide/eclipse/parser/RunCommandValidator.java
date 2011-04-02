package org.concordion.ide.eclipse.parser;

import org.w3c.dom.Element;

public class RunCommandValidator implements CommandValidator {

	@Override
	public void parseExpression(String expression, ProblemReporter problemReporter, Element commandElement) {
        String href = commandElement.getAttribute("href");
        if (href == null || href.length() == 0) {
        	problemReporter.reportError("A href attribute value must be specified with the run command run");
        }
	}
}
