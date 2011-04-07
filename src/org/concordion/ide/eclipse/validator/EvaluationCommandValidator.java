package org.concordion.ide.eclipse.validator;

import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.concordion.ide.eclipse.EclipseUtils;
import org.concordion.ide.eclipse.JdtUtils;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.w3c.dom.Element;

public class EvaluationCommandValidator implements CommandValidator {

	// See SimpleEvaluator from Concordion
	private static final String METHOD_NAME_PATTERN = "([a-z][a-zA-Z0-9_]*)";
    private static final String LHS_VARIABLE_PATTERN = "#" + METHOD_NAME_PATTERN;
    private static final String RHS_VARIABLE_PATTERN = "(" + LHS_VARIABLE_PATTERN + "|#TEXT|#HREF)";
    private static final String METHOD_CALL_PARAMS = METHOD_NAME_PATTERN + " *\\( *" + RHS_VARIABLE_PATTERN + "(, *" + RHS_VARIABLE_PATTERN + " *)*\\)";
    private static final String METHOD_CALL_NO_PARAMS = METHOD_NAME_PATTERN + " *\\( *\\)";
    
    private static final Pattern METHOD_NO_PARAMS_PATTERN = Pattern.compile(METHOD_CALL_NO_PARAMS);
    private static final Pattern METHOD_WITH_PARAMS_PATTERN = Pattern.compile(METHOD_CALL_PARAMS);

	private IType fixture;
	private Map<String, IMethod> methodMap;

	public EvaluationCommandValidator(IType fixture) {
		this.fixture = fixture;
	}

	@Override
	public void parseExpression(String expression, ProblemReporter problemReporter, Element commandElement) {
		// Run Concordion's syntax validation
		ExpressionSupport.validateEvaluationExpression(expression, problemReporter);
		
		// Check for valid method invocations on fixture
		if (fixture != null) {
			validateMethodInvocations(expression, problemReporter);
			
			// TODO: Check for valid property references
		}
	}

	private void validateMethodInvocations(String expression, ProblemReporter problemReporter) {
		Matcher noParamsMatcher = METHOD_NO_PARAMS_PATTERN.matcher(expression);
		Matcher withParamsMatcher = METHOD_WITH_PARAMS_PATTERN.matcher(expression);
		
		if (noParamsMatcher.matches()) {
			String method = noParamsMatcher.group(1);
			validateMethodExists(method, problemReporter);
			validateMethodParamCountMatches(method, 0, problemReporter);
		}
		
		if (withParamsMatcher.matches()) {
			String methodName = withParamsMatcher.group(1);
			int paramCount = countCommas(expression) + 1;
			validateMethodExists(methodName, problemReporter);
			validateMethodParamCountMatches(methodName, paramCount, problemReporter);
		}
    }
	
	private void validateMethodExists(String methodName, ProblemReporter problemReporter) {
		Map<String, IMethod> methods = loadMethods();
		
		IMethod method = methods.get(methodName);
		if (method == null || !method.exists()) {
			problemReporter.reportError("Method " + methodName + " does not exist in fixture " + fixture.getFullyQualifiedName());
		}
	}
	
	private Map<String, IMethod> loadMethods() {
		if (methodMap == null) {
			try {
				methodMap = JdtUtils.getAccessibleNonTestMethods(fixture);
			} catch (JavaModelException e) {
				EclipseUtils.logError("Unable to load methods for fixture", e);
				return Collections.emptyMap();
			}
		}
		return methodMap;
	}

	private void validateMethodParamCountMatches(String methodName, int paramCount, ProblemReporter problemReporter) {
		Map<String, IMethod> methods = loadMethods();
		
		IMethod method = methods.get(methodName);
		if (method != null) {
			int actualParamCount;
			try {
				actualParamCount = method.getParameterNames().length;
				if (actualParamCount != paramCount) {
					problemReporter.reportError("Wrong parameter count for method " + methodName + ", expected " + actualParamCount + " parameters");
				}
			} catch (JavaModelException e) {
				// Ignore, try next method
			}
		}
	}
	
	private static int countCommas(String expression) {
		return expression.replaceAll("[^,]","").length();
	}
}
