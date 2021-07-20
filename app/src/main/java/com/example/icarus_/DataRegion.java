package com.example.icarus_;

import android.widget.TextView;

public class DataRegion extends VBR{
    /** In decimal **/
    private long startDataRegionSect;
    private long endDataRegionSect;
    private long startDataRegionDec;
    private long endDataRegionDec;

    public DataRegion(){

    }

    public DataRegion(long startDataRegionSect, long endDataRegionSect, long bytesPerSector){
        this.setStartDataRegionSect(startDataRegionSect);
        this.setEndDataRegionSect(endDataRegionSect);
        this.setStartDataRegionDec(bytesPerSector);
        this.setEndDataRegionDec(bytesPerSector);
    }

    public void setStartDataRegionSect(long startDataRegionSect) { this.startDataRegionSect = startDataRegionSect; }
    public void setEndDataRegionSect(long endDataRegionSect) { this.endDataRegionSect = endDataRegionSect; }
    public void setStartDataRegionDec(long bytesPerSector) { this.startDataRegionDec = getStartDataRegionSect() * bytesPerSector; }
    public void setEndDataRegionDec(long bytesPerSector) { this.endDataRegionDec = (getEndDataRegionSect() * bytesPerSector) + bytesPerSector - 1; }

    public long getStartDataRegionSect() { return startDataRegionSect; }
    public long getEndDataRegionSect() { return endDataRegionSect; }
    public long getStartDataRegionDec() { return startDataRegionDec; }
    public long getEndDataRegionDec() { return endDataRegionDec; }

    public String toString(String resultString) {
        resultString += ("----------| DATA REGION INFORMATION\n\n");
        resultString += ("Start of Data Region (sectors): " + this.getStartDataRegionSect() + "\n");
        resultString += ("End of Data Region (sectors): " + this.getEndDataRegionSect() + "\n");
        resultString += ("Start of Data Region (bytes): " + this.getStartDataRegionDec() + "\n");
        resultString += ("End of Data Region (bytes): " + this.getEndDataRegionDec() + "\n");
        resultString += ("\n");
        return resultString;
    }
}
