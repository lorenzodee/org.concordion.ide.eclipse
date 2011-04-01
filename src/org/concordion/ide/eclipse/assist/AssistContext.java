package org.concordion.ide.eclipse.assist;

public class AssistContext {
	private AssistType type;
	private String prefix;
	
	private AssistContext(AssistType type, String chars) {
		this.type = type;
		this.prefix = chars;
	}

	public AssistType getType() {
		return type;
	}

	public String getPrefix() {
		return prefix;
	}
	
	public boolean hasPrefix() {
		return prefix != null && prefix.length() > 0;
	}

	@Override
	public String toString() {
		return "AssistContext [type=" + type + ", prefix=" + prefix + "]";
	}
	
	public static AssistContext forType(AssistType type) {
		return new AssistContext(type, null);
	}

	public static AssistContext withPrefix(String prefix) {
		return new AssistContext(AssistType.NS_PREFIX, prefix);
	}
	
	public static AssistContext partialNsPrefix(String prefix) {
		return new AssistContext(AssistType.PARTIAL_NS_PREFIX, prefix);
	}

	public static AssistContext unknown() {
		return forType(AssistType.UNKNOWN);
	}
}
