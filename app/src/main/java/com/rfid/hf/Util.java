package com.rfid.hf;

import android.content.Context;
import android.widget.EditText;
import android.widget.Toast;

public class Util {
	
	public static boolean showWarning(Context context, int resRes){
		Toast.makeText(context, resRes, Toast.LENGTH_LONG).show();
		return false;
	}
	
	public static boolean isEtEmpty(EditText editText){
		String str = editText.getText().toString();
		return str==null||str.equals("");
	}
	
	public static boolean isLenLegal(EditText editText){
		if(isEtEmpty(editText))return false;
		String str = editText.getText().toString();
		return str!=null && str.length() % 2 == 0;
	}
	
	public static boolean isEtsLegal(EditText[] ets){
		for(EditText et:ets){
			if (isLenLegal(et)) return true;
		}
		return false;
	}

	public static String bytesToHexString(byte[] src, int offset, int length) {
		StringBuilder stringBuilder = new StringBuilder("");
		try{
			if (src == null || src.length <= 0) {
				return null;
			}
			for (int i = offset; i < length; i++) {
				int v = src[i] & 0xFF;
				String hv = Integer.toHexString(v);
				if (hv.length() == 1) {
					stringBuilder.append(0);
				}
				stringBuilder.append(hv);
			}
			return stringBuilder.toString().toUpperCase();
		}catch(Exception ex)
		{ return null;}
	}

	public static byte[] hexStringToBytes(String hexString) {
		try{
			if (hexString == null || hexString.equals("")) {
				return null;
			}
			hexString = hexString.toUpperCase();
			int length = hexString.length() / 2;
			char[] hexChars = hexString.toCharArray();
			byte[] d = new byte[length];
			for (int i = 0; i < length; i++) {
				int pos = i * 2;
				d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
			}
			return d;
		}catch(Exception ex)
		{return null;}
	}
	private static byte charToByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}

}
