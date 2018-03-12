package com.reformer.nfclibrary;

import android.os.Bundle;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Random;

/**
 * Created by Administrator on 2018-03-12.
 */

public class RfNfcKey {

    private static final String SAMPLE_LOYALTY_CARD_AID = "52464D4A01";//52464D4A01
    // ISO-DEP command HEADER for selecting an AID.
    // Format: [Class | Instruction | Parameter 1 | Parameter 2]
    private static final String SELECT_APDU_HEADER = "00A40400";
    // "OK" status word sent in response to SELECT AID command (0x9000)
    private static final byte[] SELECT_OK_SW = HexStringToByteArray("9000");
    // "UNKNOWN" status word sent in response to invalid APDU command (0x0000)
    private static final byte[] UNKNOWN_CMD_SW = HexStringToByteArray("0000");
    private static final byte[] SELECT_APDU = BuildSelectApdu(SAMPLE_LOYALTY_CARD_AID);
    private static final byte[] READ_CARD_ID = new byte[]{(byte)0x00,(byte)0xb0,(byte)0x00,(byte)0x00,(byte)0x08,(byte)0x01,(byte)0x56,(byte)0xA0,(byte)0x01};

    private static RfNfcKey instance = null;
    private String cardId = "F0000001";
    private String password = "n46jF1uYE93LPg8V";
    private boolean enable = false;
    private static final byte[] cardLen = new byte[]{0x04};
    private byte[] tempByte = new byte[4];

    private RfNfcKey(){}


    public static RfNfcKey getInstance(){
        if (instance == null) {
            synchronized (RfNfcKey.class) {
                if (instance == null) {
                    instance = new RfNfcKey();
                }
            }
        }
        return instance;
    }

    public void setCardId(String cardId){
        if (cardId == null || cardId.length() != 8)
            return;
        cardId = cardId.toUpperCase();
        for (int i = 0;i<cardId.length();i++){
            if (cardId.charAt(i) < '0' || cardId.charAt(i) > 'F'
                    || (cardId.charAt(i) > '9' && cardId.charAt(i) < 'A')){
                return;
            }
        }
        this.cardId = cardId;
    }

    public String getCardId() {
        return cardId;
    }


    public void setPassword(String password){
        if (password == null || password.getBytes().length != 16)
            return;
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setEnable(boolean enable){
        this.enable = enable;
    }

    public boolean isEnable(){
        return enable;
    }

    byte[] processCommandApdu(byte[] commandApdu, Bundle extras){
        if (!enable){
            return UNKNOWN_CMD_SW;
        }
        if (Arrays.equals(SELECT_APDU, commandApdu)) {
            new Random().nextBytes(tempByte);
            return ConcatArrays(tempByte, SELECT_OK_SW);
        } else if (commandApdu.length >= 9) {
            byte[] actionAPDU = Arrays.copyOf(commandApdu,9);
            if (Arrays.equals(READ_CARD_ID, actionAPDU) && commandApdu.length == 13) {
                byte[] randomNum = new byte[4];
                System.arraycopy(commandApdu,9,randomNum,0,4);
                for (int i = 0; i< 4; i++){
                    randomNum[i] = (byte)(randomNum[i] ^ tempByte[i]);
                }
                byte[] accountBytes = HexStringToByteArray(cardId);//.getBytes();
                byte[] p = ConcatArrays(randomNum,cardLen,accountBytes);
                byte[] key = password.getBytes();
                byte[] c = new AES128Enc().aes128cbc_Pkcs7_Enc(p,key);
                return ConcatArrays(c, SELECT_OK_SW);
            }else{
                return UNKNOWN_CMD_SW;
            }
        } else {
            return UNKNOWN_CMD_SW;
        }
    }

    /**
     * Utility method to convert a byte array to a hexadecimal string.
     *
     * @param bytes Bytes to convert
     * @return String, containing hexadecimal representation.
     */
    private static String ByteArrayToHexString(byte[] bytes) {
        final char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
        char[] hexChars = new char[bytes.length * 2]; // Each byte has two hex characters (nibbles)
        int v;
        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF; // Cast bytes[j] to int, treating as unsigned value
            hexChars[j * 2] = hexArray[v >>> 4]; // Select hex character from upper nibble
            hexChars[j * 2 + 1] = hexArray[v & 0x0F]; // Select hex character from lower nibble
        }
        return new String(hexChars);
    }

    /**
     * Utility method to convert a hexadecimal string to a byte string.
     *
     * <p>Behavior with input strings containing non-hexadecimal characters is undefined.
     *
     * @param s String containing hexadecimal characters to convert
     * @return Byte array generated from input
     * @throws IllegalArgumentException if input length is incorrect
     */
    private static byte[] HexStringToByteArray(String s) throws IllegalArgumentException {
        int len = s.length();
        if (len % 2 == 1) {
            throw new IllegalArgumentException("Hex string must have even number of characters");
        }
        byte[] data = new byte[len / 2]; // Allocate 1 byte per 2 hex characters
        for (int i = 0; i < len; i += 2) {
            // Convert each character into a integer (base-16), then bit-shift into place
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    /**
     * Utility method to concatenate two byte arrays.
     * @param first First array
     * @param rest Any remaining arrays
     * @return Concatenated copy of input arrays
     */
    private static byte[] ConcatArrays(byte[] first, byte[]... rest) {
        int totalLength = first.length;
        for (byte[] array : rest) {
            totalLength += array.length;
        }
        byte[] result = Arrays.copyOf(first, totalLength);
        int offset = first.length;
        for (byte[] array : rest) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }

    /**
     * Build APDU for SELECT AID command. This command indicates which service a reader is
     * interested in communicating with. See ISO 7816-4.
     *
     * @param aid Application ID (AID) to select
     * @return APDU for SELECT AID command
     */
    public static byte[] BuildSelectApdu(String aid) {
        // Format: [CLASS | INSTRUCTION | PARAMETER 1 | PARAMETER 2 | LENGTH | DATA]
        return HexStringToByteArray(SELECT_APDU_HEADER + String.format("%02X",
                aid.length() / 2) + aid);
    }

//    public static String getStringByFormat(long milliseconds,String format) {
//        String thisDateTime = null;
//        try {
//            SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat(format);
//            thisDateTime = mSimpleDateFormat.format(milliseconds);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return thisDateTime;
//    }
}
