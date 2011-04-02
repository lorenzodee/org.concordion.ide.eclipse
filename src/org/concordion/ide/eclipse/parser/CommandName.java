package org.concordion.ide.eclipse.parser;

import java.util.HashMap;
import java.util.Map;

public enum CommandName {
	ASSERT_EQUALS("assertEquals"), 
	ASSERT_TRUE("assertTrue"),
	ASSERT_FALSE("assertFalse"),
	RUN("run"),
	EXECUTE("execute"),
	SET("set"),
	VERIFY_ROWS("verifyRows"),
	PARAMS("params"),
	ECHO("echo");
	
	private String commandName;
	private static final Map<String, CommandName> name2cmd = new HashMap<String, CommandName>();
	
	static {
		for (CommandName cmd : values()) {
			name2cmd.put(cmd.getCommandName(), cmd);
		}
	}

	private CommandName(String commandName) {
		this.commandName = commandName;
	}

	public String getCommandName() {
		return commandName;
	}

	/** CommandName for this name, or <code>null</code> if not found */
	public static CommandName fromCommandName(String cmdName) {
		return name2cmd.get(cmdName);
	}
}
