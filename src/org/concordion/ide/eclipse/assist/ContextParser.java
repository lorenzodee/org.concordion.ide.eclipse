package org.concordion.ide.eclipse.assist;


public class ContextParser {
	private static final int NOT_FOUND = -1;
	
	public AssistContext findContext(String document, int offset, String concordionNamespacePrefix) {
		// Supported contexts:
		// concordion:assertTrue="|"  -> ASSERT_TRUE
		// concordion:                -> NS_PREFIX
		// concordion:asse|           -> NS_PREFIX, 
		
		// Is cursor after double quote? -> check if in value for a concordion attribute
		int beforeCursor = offset - 1;
		if (isDoublequote(document, beforeCursor)) {
			int commandEnd = beforeEqualSign(document, offset - 2);
			int commandStart = afterColon(document, commandEnd - 1);
			AssistType assistType = command(document, commandStart, commandEnd);
			return AssistContext.forType(assistType); // TODO: Implement prefix chars for method proposals
		} else if (isColon(document, beforeCursor)) {
			// Cursor is after colon -> check if after concordion namespace prefix
			return nsPrefix(document, concordionNamespacePrefix, beforeCursor);
		} else if (isLetter(document, beforeCursor)) {
			// Cursor is after letter - check if partial concordion command with namespace prefix
			int beforeColon = consumeLetters(document, beforeCursor);
			AssistContext command = inCommand(document, beforeColon, beforeCursor, concordionNamespacePrefix);
			if (command.getType() == AssistType.UNKNOWN) {
				return inPrefix(document, beforeColon, beforeCursor, concordionNamespacePrefix);
			} else {
				return command;
			}
		}
		
		return AssistContext.unknown();
	}

	private AssistContext inPrefix(String document, int beforeWhitespace, int beforeCursor, String nsPrefix) {
		if (isWhitespace(beforeWhitespace, document)) {
			String letters = document.substring(beforeWhitespace + 1, beforeCursor + 1);
			if (letters.length() <= nsPrefix.length()) {
				String nsPrefLetters = nsPrefix.substring(0, letters.length());
				if (nsPrefLetters.equals(letters)) {
					return AssistContext.partialNsPrefix(letters);
				}
			}
		}
		return AssistContext.unknown();
	}
	
	private boolean isWhitespace(int beforeColon, String document) {
		return beforeColon >= 0 && Character.isWhitespace(document.charAt(beforeColon));
	}

	private AssistContext nsPrefix(String document, String concordionNamespacePrefix, int beforeCursor) {
		int beforeWhitespace = consumeLetters(document, beforeCursor - 1);
		return AssistContext.forType(nsPrefixType(document, beforeWhitespace + 1, beforeCursor, concordionNamespacePrefix));
	}
	
	private AssistContext inCommand(String document, int beforeColon, int beforeCursor, String concordionNamespacePrefix) {
		if (isColon(document, beforeColon)) {
			AssistContext prefix = nsPrefix(document, concordionNamespacePrefix, beforeColon);
			if (prefix.getType() == AssistType.NS_PREFIX) {
				String commandPrefix = document.substring(beforeColon + 1, beforeCursor + 1);
				return AssistContext.withPrefix(commandPrefix);
			}
		}
		return AssistContext.unknown();
	}

	private boolean isLetter(String document, int offset) {
		if (offset >= 0) {
			return Character.isLetter(document.charAt(offset));
		}
		return false;
	}

	private AssistType nsPrefixType(String document, int afterWhitespace, int beforeOffset, String concordionNamespacePrefix) {
		if (afterWhitespace != NOT_FOUND && beforeOffset != NOT_FOUND) {
			String nsPfx = document.substring(afterWhitespace, beforeOffset);
			if (nsPfx.equals(concordionNamespacePrefix)) {
				return AssistType.NS_PREFIX;
			}
		}
		
		return AssistType.UNKNOWN;
	}
	
	private AssistType command(String document, int commandStart, int commandEnd) {
		if (commandStart != NOT_FOUND && commandEnd != NOT_FOUND) {
			String cmdStr = document.substring(commandStart, commandEnd);
			return AssistType.forCommandName(cmdStr);
		} else {
			return AssistType.UNKNOWN;
		}
	}
	
	private int afterColon(String document, int pos) {
		pos = consumeLetters(document, pos);
		
		if (pos >= 0 && isColon(document, pos)) {
			return pos + 1;
		} else {
			return NOT_FOUND;
		}
	}
	
	private int consumeLetters(String document, int pos) {
		while (pos >= 0 && Character.isLetter(document.charAt(pos))) {
			pos--;
		}
		return pos;
	}
	
	private boolean isColon(String document, int pos) {
		return pos >= 0 && document.charAt(pos) == ':';
	}
	
	private int beforeEqualSign(String document, int offset) {
		int pos = skipWhitespace(document, offset);
		if (isEqualSign(document, pos)) {
			return skipWhitespace(document, pos);
		}
		
		return NOT_FOUND;
	}
	
	private boolean isEqualSign(String document, int pos) {
		return pos >= 0 && document.charAt(pos) == '=';
	}
	
	private int skipWhitespace(String document, int offset) {
		// >= 0 --> -1 in case whitespace runs all the way to the beginning
		while (offset >= 0 && Character.isWhitespace(document.charAt(offset))) {
			offset--;
		}
		return offset;
	}
	
	private boolean isDoublequote(String document, int offset) {
		return offset >= 0 && document.charAt(offset) == '"';
	}
}
