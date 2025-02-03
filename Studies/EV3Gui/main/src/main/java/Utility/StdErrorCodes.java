package Utility;

/**
 * List of conventional error codes, recognised across the project
 */
public enum StdErrorCodes {
	ERR_SELECT_NOT_VALID(-1),
	STOP(-2),
	CANCEL_ACTION(-3),
	BLANK_LINE(-4),
	UNKNOWN(-100);
	private int intValue;
	private StdErrorCodes(int intValue){
		this.intValue=intValue;
	}

	public int getIntValue() {
		return intValue;
	}
}
