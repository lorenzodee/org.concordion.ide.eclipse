package org.concordion.ide.eclipse;

import org.concordion.ide.eclipse.parser.CommandAttributeVisitor;
import org.concordion.ide.eclipse.parser.CommandName;
import org.concordion.ide.eclipse.parser.InvalidCommandHandlerImpl;
import org.concordion.ide.eclipse.parser.ProblemReporterFactory;
import org.concordion.ide.eclipse.parser.RootElementParser;
import org.concordion.ide.eclipse.parser.SetExpressionParser;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.ui.internal.reconcile.validator.ISourceValidator;
import org.eclipse.wst.validation.internal.core.ValidationException;
import org.eclipse.wst.validation.internal.provisional.core.IReporter;
import org.eclipse.wst.validation.internal.provisional.core.IValidationContext;
import org.eclipse.wst.validation.internal.provisional.core.IValidator;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
import org.w3c.dom.Element;

@SuppressWarnings("restriction")
public class ConcordionValidator implements IValidator, ISourceValidator {

	private IDocument document;
	private RootElementParser rootElementParser = new RootElementParser();
	
	@Override
	public void connect(IDocument document) {
		this.document = document;
	}
	
	@Override
	public void disconnect(IDocument document) {
		this.document = null;
	}
	
	@Override
	public void cleanup(IReporter reporter) {
		// No cleanup required after validation for this validator
	}
	
	@Override
	public void validate(IValidationContext helper, IReporter reporter) throws ValidationException {
		System.err.println("Validation");
		reporter.removeAllMessages(this);
		// TODO: Use DOM model for isConcordionSpec
		if (rootElementParser.isConcordionSpec(document)) {
			IStructuredModel model = StructuredModelManager.getModelManager().getExistingModelForRead(document);
			if (model instanceof IDOMModel) {
				// TODO: Always XML model, for all html files? Change RootElementParser to parse DOM model instead of string
				IDOMModel domModel = (IDOMModel) model;
				doParseSpec(domModel.getDocument(), reporter, rootElementParser.getNamespacePrefix());
			}
		}
	}

	// TODO: Inherit ISourceValidator at all as we don't support partial validation? See if partial needs to be enabled as scope in plugin XML
	@Override
	public void validate(IRegion dirtyRegion, IValidationContext helper, IReporter reporter) {
		try {
			System.err.print("Partial ");
			validate(helper, reporter);
		} catch (ValidationException e) {
			// Ignore - document is currently being edited and might be in an invalid state
			// validate() will be called again when saving, and will throw any remaining
			// ValidationExceptions
		}
	}

	private void doParseSpec(IDOMDocument domDoc, IReporter reporter, String nsPrefix) {
		Element root = domDoc.getDocumentElement();
		CommandAttributeVisitor specParser = new CommandAttributeVisitor(
			document,
			nsPrefix, 
			new ProblemReporterFactory(this, reporter),
			new InvalidCommandHandlerImpl());
		
		specParser.addCommandParser(CommandName.SET, new SetExpressionParser());
		
		specParser.visitNodeRecursive(root);
	}
}
