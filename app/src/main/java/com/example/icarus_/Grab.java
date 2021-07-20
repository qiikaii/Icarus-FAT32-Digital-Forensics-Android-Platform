package com.example.icarus_;

import android.net.Uri;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class Grab extends MainActivity {
    /*** Get Hex Data String in Big Endian Mode ***/
    public StringBuilder getBEHexData(Uri uri, long startCount, long endCount) throws IOException {
        int decimalValue;
        StringBuilder hexString = new StringBuilder();

        try {
            InputStream file1 = getContentResolver().openInputStream(uri);
            file1.skip(startCount);
            for (long i = startCount; i <= endCount; i++) {
                decimalValue = file1.read();
                hexString.append(String.format("%02X", decimalValue));
            }

            file1.close();
            return hexString;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /*** Get Hex Data String in Little Endian Mode ***/
    public StringBuilder getLEHexData(Uri uri, long startCount, long endCount) throws IOException {
        int decimalValue;
        StringBuilder hexString = new StringBuilder();

        try {
            InputStream file1 = getContentResolver().openInputStream(uri);
            file1.skip(startCount);
            for (long i = startCount; i <= endCount; i++) {
                decimalValue = file1.read();
                hexString.append(String.format("%02X", decimalValue));
            }

            StringBuilder hexLE = new StringBuilder();
            for (int j = hexString.length(); j != 0; j -= 2) {
                hexLE.append(hexString.substring(j - 2, j));
            }

            file1.close();
            return hexLE;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public StringBuilder getLEHexData(String hexString) throws IOException {
        StringBuilder hexLE = new StringBuilder();
        for (int j = hexString.length(); j != 0; j -= 2) {
            hexLE.append(hexString.substring(j - 2, j));
        }

        return hexLE;
    }

    /*** Convert Hex to ASCII String  ***/
    public String getHexToASCII(StringBuilder hexString) {
        StringBuilder temp = new StringBuilder();
        for (int i = 0; i < hexString.length(); i += 2) {
            String str = hexString.substring(i, i + 2);
            temp.append((char) Integer.parseInt(str, 16));
        }
        return temp.toString();
    }

    /*** Concat two Strings of Hex ***/
    public StringBuilder concatHex(StringBuilder firstHex, StringBuilder secondHex) {
        StringBuilder concatHex = new StringBuilder();
        concatHex.append(firstHex).append(secondHex);
        return concatHex;
    }

    /*** Change Hex to LE to Decimal ***/
    public long getHexLEDec(Uri uri, long startCount, long endCount) throws IOException {
        return getHexToDecimal(getLEHexData(uri, startCount, endCount));
    }

    /*** Change Hex to LE to Decimal ***/
    public long getHexLEDec(String hexString) throws IOException {
        return getHexToDecimal(getLEHexData(hexString));
    }

    /*** Change Decimal to Binary ***/
    public String getDecToBin(long decValue) {

        String binValue = Long.toBinaryString(decValue);

        if (binValue.length() != 16) {
            int pad = 16 - binValue.length();   // Padding binary to allow conversion for date/time
            StringBuilder sb = new StringBuilder();

            while (sb.length() < pad) {
                sb.append('0');
            }

            sb.append(binValue);

            return sb.toString();

        } else {

            return binValue;

        }
    }

    /*** Change Binary to Date ***/

    public String getBinToDate(String binDate){

        String Date,Year,Month,Day,yearBin,monthBin,dayBin;

        yearBin = binDate.substring(0,7);
        monthBin = binDate.substring(7,11);
        dayBin = binDate.substring(11,16);

        Year = String.valueOf(Integer.parseInt(yearBin,2) + 1980);
        Month = String.valueOf(Integer.parseInt(monthBin,2));
        Day = String.valueOf(Integer.parseInt(dayBin,2));

        Date = String.join("-", Day, Month, Year);

        return Date;
    }

    /*** Change Binary to Date ***/

    public String getBinToTime(String binTime){

        String Time,Hour,Min,Sec,hourBin,minBin,secBin;
        if(binTime.equals("0000000000000000")){
            return "Nil";
        }

        hourBin = binTime.substring(0,5);
        minBin = binTime.substring(5,11);
        secBin = binTime.substring(11,16);

        Hour = String.valueOf(Integer.parseInt(hourBin,2));
        Min = String.valueOf(Integer.parseInt(minBin,2));
        Sec = String.valueOf(Integer.parseInt(secBin,2) * 2);

        Time = String.join(":", Hour, Min, Sec);

        return Time;
    }

    public String getDecBinTime(long decValue) {
        return getBinToTime(getDecToBin(decValue));
    }

    public String getDecBinDate(long decValue) {
        return getBinToDate(getDecToBin(decValue));
    }

}

