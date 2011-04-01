package org.concordion.ide.eclipse.assist;

import java.util.ArrayList;
import java.util.Collection;

public enum AssistType {
	// TODO: Use CommandName enum for command names instead of strings
	ASSERT_EQUALS("assertEquals"), 
	ASSERT_TRUE("assertTrue"),
	ASSERT_FALSE("assertFalse"),
	RUN("run"),
	EXECUTE("execute"),
	SET("set"),
	VERIFY_ROWS("verifyRows"),
	ECHO("echo"),
	NS_PREFIX(),
	UNKNOWN(),
	PARTIAL_NS_PREFIX();
	
	private static final String[] ALL_COMMANDS;
	
	static {
		Collection<String> allCmds = new ArrayList<String>();
		for (AssistType context : values()) {
			String cmdName = context.commandName;
			if (cmdName != null) {
				allCmds.add(cmdName);
			}
		}
		ALL_COMMANDS = allCmds.toArray(new String[allCmds.size()]);
	}
	
	private final String commandName;

	private AssistType() { 
		commandName = null;
	}
	
	private AssistType(String commandName) {
		this.commandName = commandName;
	}
	
	public static AssistType forCommandName(String cmdName) {
		for (AssistType val : values()) {
			String commandName = val.commandName;
			if (commandName != null && commandName.equals(cmdName)) {
				return val;
			}
		}
		return UNKNOWN;
	}
	
	public static String[] allCommands() {
		return (String[]) ALL_COMMANDS.clone();
	}
}