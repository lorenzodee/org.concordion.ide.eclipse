package org.concordion.ide.eclipse.parser;

import org.w3c.dom.Element;

public interface CommandValidator {
	void parseExpression(String expression, ProblemReporter problemReporter, Element commandElement);
}
