package org.concordion.ide.eclipse.parser;

import org.w3c.dom.Element;

public class SetCommandValidator implements CommandValidator {

	@Override
	public void parseExpression(String expression, ProblemReporter reporter, Element commandElement) {
		ExpressionSupport.validateSetVariableExpression(expression, reporter);
	}
}
