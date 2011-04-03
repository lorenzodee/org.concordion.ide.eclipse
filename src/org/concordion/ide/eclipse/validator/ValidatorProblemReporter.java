package org.concordion.ide.eclipse.validator;

import org.eclipse.wst.validation.internal.operations.LocalizedMessage;
import org.eclipse.wst.validation.internal.provisional.core.IMessage;
import org.eclipse.wst.validation.internal.provisional.core.IReporter;
import org.eclipse.wst.validation.internal.provisional.core.IValidator;

@SuppressWarnings("restriction") // LocalizedMessage
public class ValidatorProblemReporter implements ProblemReporter {

	private IValidator validator;
	private IReporter reporter;
	private int line;
	private int offset;
	private int length;
	
	public ValidatorProblemReporter(IValidator validator, IReporter reporter, int line, int offset, int length) {
		this.validator = validator;
		this.reporter = reporter;
		this.line = line;
		this.offset = offset;
		this.length = length;
	}

	@Override
	public void reportError(String msgStr) {
		report(IMessage.HIGH_SEVERITY, msgStr);
	}

	@Override
	public void reportWarning(String msgStr) {
		report(IMessage.NORMAL_SEVERITY, msgStr);
	}

	@Override
	public void reportInfo(String msgStr) {
		report(IMessage.LOW_SEVERITY, msgStr);
	}

	private void report(int severity, String msgStr) {
		LocalizedMessage msg = new LocalizedMessage(severity, msgStr);
		msg.setLineNo(line);
		msg.setOffset(offset);
		msg.setLength(length);
		reporter.addMessage(validator, msg);
	}
}
