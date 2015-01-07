package piprint;

public class Utils {
	final static char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
	
	public static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 3];
		int v;
		for ( int j = 0; j < bytes.length; j++ ) {
			v = bytes[j] & 0xFF;
			hexChars[j * 3] = hexArray[v >>> 4];
			hexChars[j * 3 + 1] = hexArray[v & 0x0F];
			hexChars[j * 3 + 2] = ' ';
		}
		return new String(hexChars);
	}
	
	public static String byteToHex(byte aByte) {
		int v = aByte & 0xFF;
		return new String ( new char[] {hexArray[v >>> 4], hexArray[v & 0x0F]} );
	}
	
	public static String byteToBinary(byte aByte) {
		char[] bitChars = new char[8];
		int mask = 1;
		for (int i = 0; i < bitChars.length; i++) {
			if ((aByte & mask) == 0) {
				bitChars[7-i] = '0';
			} else {
				bitChars[7-i] = '1';
			}
			mask = mask << 1;
		}
		
		return new String(bitChars);
	}
}
