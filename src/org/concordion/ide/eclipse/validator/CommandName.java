package org.concordion.ide.eclipse.validator;

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
	
	private static final String[] ALL_COMMAND_NAMES;
	private static final Map<String, CommandName> name2cmd = new HashMap<String, CommandName>();
	
	private String commandName;
	
	static {
		for (CommandName cmd : values()) {
			name2cmd.put(cmd.cmdName(), cmd);
		}
		
		ALL_COMMAND_NAMES = name2cmd.keySet().toArray(new String[0]);
	}

	private CommandName(String commandName) {
		this.commandName = commandName;
	}

	public String cmdName() {
		return commandName;
	}

	/** CommandName for this name, or <code>null</code> if not found */
	public static CommandName fromCommandName(String cmdName) {
		return name2cmd.get(cmdName);
	}
	
	public static String[] allCommandNames() {
		return ALL_COMMAND_NAMES.clone();
	}
}
