package org.concordion.ide.eclipse.parser;

import org.w3c.dom.Element;

public class EvaluationCommandValidator implements CommandValidator {

	@Override
	public void parseExpression(String expression, ProblemReporter problemReporter, Element commandElement) {
		ExpressionSupport.validateEvaluationExpression(expression, problemReporter);
	}

}
