package org.concordion.ide.eclipse.assist;

import org.concordion.ide.eclipse.validator.CommandName;

public enum AssistType {
	ASSERT_EQUALS(CommandName.ASSERT_EQUALS), 
	ASSERT_TRUE(CommandName.ASSERT_TRUE),
	ASSERT_FALSE(CommandName.ASSERT_FALSE),
	RUN(CommandName.RUN),
	EXECUTE(CommandName.EXECUTE),
	SET(CommandName.SET),
	VERIFY_ROWS(CommandName.VERIFY_ROWS),
	ECHO(CommandName.ECHO),
	PARTIAL_COMMAND(),
	UNKNOWN(),
	PARTIAL_NS_PREFIX();
	
	private final CommandName commandName;

	private AssistType() { 
		commandName = null;
	}
	
	private AssistType(CommandName commandName) {
		this.commandName = commandName;
	}
	
	public static AssistType forCommandName(CommandName cmdName) {
		if (cmdName == null) {
			return null;
		}
		
		for (AssistType val : values()) {
			if (val.commandName == cmdName) {
				return val;
			}
		}
		return UNKNOWN;
	}
}