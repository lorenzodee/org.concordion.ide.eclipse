package org.concordion.ide.eclipse.validator;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMAttr;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@SuppressWarnings("restriction") // IDOMAttr, IStructuredDocumentRegion
public class CommandAttributeVisitor {

	private IDocument document;
	private String nsPrefix;
	private Map<CommandName, CommandValidator> commandParsers = new HashMap<CommandName, CommandValidator>();
	private InvalidCommandHandler invalidCommandHandler;
	private ProblemReporterFactory problemReporterFactory;
	
	public CommandAttributeVisitor(IDocument document, String nsPrefix, ProblemReporterFactory problemReporterFactory, InvalidCommandHandler invalidCommandHandler) {
		this.document = document;
		this.nsPrefix = nsPrefix;
		this.problemReporterFactory = problemReporterFactory;
		this.invalidCommandHandler = invalidCommandHandler;
	}

	public void visitNodeRecursive(Element root) {
		visitNode(root);
		visitChilds(root);
	}

	private void visitChilds(Element root) {
		NodeList nodes = root.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node instanceof Element) {
				Element element = (Element) node;
				visitNode(element);
				visitChilds(element);
			}
		}
	}
	
	public void visitNode(Element root) {
		NamedNodeMap attributes = root.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++) {
			Attr attribute = (Attr) attributes.item(i);
			if (attribute instanceof IDOMAttr && nsPrefix.equals(attribute.getPrefix())) {
				IDOMAttr domAttr = (IDOMAttr) attribute;
				visitAttribute(domAttr);
			}
		}
	}

	private void visitAttribute(IDOMAttr attribute) {
		int line = 0;
		int offset = attribute.getNameRegionStartOffset();
		int nameLen = attribute.getNameRegionTextEndOffset() - offset;
		
		int valueLen = adjustLengthForValueIfExists(attribute, offset, nameLen);
		
		line = getLine(offset);
		visitConcordionAttribute(attribute.getLocalName(), attribute.getValue(), line, offset, nameLen, valueLen, attribute.getOwnerElement());
	}

	private int adjustLengthForValueIfExists(IDOMAttr attribute, int offset, int len) {
		int valueStart = attribute.getValueRegionStartOffset();
		String valueText = attribute.getValueRegionText();
		if (valueStart > 0 && valueText != null) {
			int valueEnd = valueStart + valueText.length();
			len = valueEnd - offset;
		}
		return len;
	}

	private int getLine(int offset) {
		int line;
		try {
			line = document.getLineOfOffset(offset);
		} catch (BadLocationException e) {
			line = 0;
		}
		return line;
	}

	private void visitConcordionAttribute(String localName, String value, int line, int offset, int nameLen, int valueLen, Element element) {
		CommandName cmd = CommandName.fromCommandName(localName);
		if (cmd != null) {
			ProblemReporter problemReporter = problemReporterFactory.createProblemReporter(line, offset, valueLen);
			fireParseCommand(cmd, value, problemReporter, element);
		} else {
			ProblemReporter problemReporter = problemReporterFactory.createProblemReporter(line, offset, nameLen);
			invalidCommandHandler.handleInvalidCommand(localName, value, problemReporter);
		}
	}

	private void fireParseCommand(CommandName cmd, String value, ProblemReporter problemReporter, Element element) {
		CommandValidator parser = commandParsers.get(cmd);
		if (parser != null) {
			parser.parseExpression(value, problemReporter, element);
		}
	}

	public void addCommandParser(CommandName commandName, CommandValidator commandParser) {
		commandParsers.put(commandName, commandParser);
	}
}
