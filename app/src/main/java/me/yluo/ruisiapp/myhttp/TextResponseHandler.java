package me.yluo.ruisiapp.myhttp;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public abstract class TextResponseHandler extends ResponseHandler {
    public static final String UTF8_BOM = "\uFEFF";

    public static String getString(byte[] stringBytes) {
        String toReturn = (stringBytes == null) ? null : new String(stringBytes, StandardCharsets.UTF_8);
        if (toReturn != null && toReturn.startsWith(UTF8_BOM)) {
            return toReturn.substring(1);
        }
        return toReturn;
    }

    @Override
    public void onSuccess(byte[] response) {
        onSuccess(getString(response));
    }

    public abstract void onSuccess(String response);

}
