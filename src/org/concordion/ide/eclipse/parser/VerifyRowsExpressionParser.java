package org.concordion.ide.eclipse.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VerifyRowsExpressionParser implements ExpressionParser {

	@Override
	public void parseExpression(String expression, ProblemReporter problemReporter) {
        Pattern pattern = Pattern.compile("(#.+?) *: *(.+)");
        Matcher matcher = pattern.matcher(expression);
        if (!matcher.matches()) {
            problemReporter.reportError("The expression for a \"verifyRows\" should be of the form: #var : collectionExpr");
        }
        String loopVariableName = matcher.group(1);
        String iterableExpression = matcher.group(2);

        ExpressionValidator.validateEvaluationExpression(iterableExpression, problemReporter);
        ExpressionValidator.validateSetVariableExpression(loopVariableName, problemReporter);
	}
}
