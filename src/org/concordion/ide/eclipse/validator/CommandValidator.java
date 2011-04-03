package org.concordion.ide.eclipse.validator;

import org.w3c.dom.Element;

public interface CommandValidator {
	void parseExpression(String expression, ProblemReporter problemReporter, Element commandElement);
}
