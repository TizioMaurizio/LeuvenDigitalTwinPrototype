package Utility;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * a list of accepted commands for the view
 */
public class GlobalKeywords {
	public static final String STOP_CMD="stop";
	public static final String CANCEL_TURN_CMD=("cancel");
	public static final String EXIT_TURN_CMD=("exit");
	public static final String BLANK_LINE="";

	//commands legend
	public static Map<String, String> legend= new HashMap<>();
	static {
		legend.put(STOP_CMD, "to stop the current selection");
		legend.put(CANCEL_TURN_CMD, "to cancel the current action and restart from scratch (when possible)");
		legend.put(EXIT_TURN_CMD, "to cancel the current action and restart from scratch (when possible)");
	}
}
