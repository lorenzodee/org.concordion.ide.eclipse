package org.concordion.ide.eclipse.validator;

import org.w3c.dom.Element;

public class EvaluationCommandValidator implements CommandValidator {

	@Override
	public void parseExpression(String expression, ProblemReporter problemReporter, Element commandElement) {
		// Run Concordion's syntax validation
		ExpressionSupport.validateEvaluationExpression(expression, problemReporter);
		validateMethodInvocations(expression, problemReporter);
	}

	private void validateMethodInvocations(String expression, ProblemReporter problemReporter) {
		// TODO
    }

}
