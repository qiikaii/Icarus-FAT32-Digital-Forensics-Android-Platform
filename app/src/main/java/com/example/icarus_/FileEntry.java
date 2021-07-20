package com.example.icarus_;

import android.widget.TextView;

import java.util.ArrayList;

public class FileEntry extends Grab {
    private String lfname;
    private String sfname;
    private String nameExt;
    private long fileAttribute;
    private String createdTime;
    private String createdDate;
    private String accessedDate;
    private long firstClusterLoc;
    private String writtenTime;
    private String writtenDate;
    private long sizeOfFile;

    private ArrayList<Long> listOfClusters;
    private ArrayList<StringBuilder> listOfData;
    private ArrayList<FileEntry> listOfFileAndDir;

    public FileEntry() {
    }

    public FileEntry(ArrayList<Long> listOfClusters, ArrayList<StringBuilder> listOfData) {
        this.setListOfClusters(listOfClusters);
        this.setListOfData(listOfData);
    }

    public void setLFname(String lfname) {this.lfname = lfname;}
    public void setSFname(String sfname) { this.sfname = sfname;}
    public void setNameExt(String nameExt) { this.nameExt = nameExt;}
    public void setFileAttribute(long fileAttribute) { this.fileAttribute = fileAttribute; }
    public void setCreatedTime (long createdTime) { this.createdTime = getDecBinTime(createdTime); }
    public void setCreatedDate (long createdDate) { this.createdDate = getDecBinDate(createdDate); }
    public void setAccessedDate (long accessedDate) { this.accessedDate = getDecBinDate(accessedDate); }
    public void setFirstClusterLoc(long firstClusterLoc) { this.firstClusterLoc = firstClusterLoc; }
    public void setWrittenTime (long writtenTime) { this.writtenTime = getDecBinTime(writtenTime); }
    public void setWrittenDate (long writtenDate) { this.writtenDate = getDecBinDate(writtenDate); }
    public void setSizeOfFile(long sizeOfFile) { this.sizeOfFile = sizeOfFile; }

    public void setListOfClusters(ArrayList<Long> listOfClusters) { this.listOfClusters = listOfClusters; }
    public void setListOfData(ArrayList<StringBuilder> listOfData) {this.listOfData = listOfData;}
    public void setListOfFileAndDir(ArrayList<FileEntry> listOfFileAndDir) {this.listOfFileAndDir = listOfFileAndDir;}

    public String getLFname() {return lfname;}
    public String getSFname() {return sfname;}
    public String getNameExt() {return nameExt;}
    public long getFileAttribute() {return fileAttribute;}
    public String getCreatedTime() {return createdTime;}
    public String getCreatedDate() { return createdDate; }
    public String getAccessedDate () { return accessedDate; }
    public long getFirstClusterLoc() { return firstClusterLoc; }
    public String getWrittenTime() { return writtenTime; }
    public String getWrittenDate() { return writtenDate; }
    public long getSizeOfFile() { return sizeOfFile; }

    public ArrayList<Long> getListOfClusters() {return listOfClusters;}
    public ArrayList<StringBuilder> getListOfData() { return listOfData; }
    public ArrayList<FileEntry> getListOfFileAndDir() { return listOfFileAndDir; }

    public String toString(String resultString) {
        if (this.getFileAttribute() == 32) {
            resultString += ("-----| FILE" + "\n\n");
            resultString += ("LFN: " + this.getLFname() + "\n");
            resultString += ("SFN: " + this.getSFname() + "\n");
            resultString += ("SFN Ext: " + this.getNameExt() + "\n");
            resultString += ("File Attribute (Dec): " + this.getFileAttribute() + "\n");
            resultString += ("Created Time: " + this.getCreatedDate() + " " + this.getCreatedTime() + "\n");
            resultString += ("Accessed Date: " + this.getAccessedDate() + "\n");
            resultString += ("First Cluster Location: " + this.getFirstClusterLoc() + "\n");
            resultString += ("Written Time: " + this.getWrittenDate() + " " + this.getWrittenTime() + "\n");
            resultString += ("Size of File (Bytes): " + this.getSizeOfFile() + "\n\n");
        } else if (this.getFileAttribute() == 16) {
            resultString += ("-----| DIRECTORY " + "\n\n");
            resultString += ("LFN: " + this.getLFname() + "\n");
            resultString += ("SFN: " + this.getSFname() + "\n");
            resultString += ("SFN Ext: " + this.getNameExt() + "\n");
            resultString += ("File Attribute (Dec): " + this.getFileAttribute() + "\n");
            resultString += ("Created Time: " + this.getCreatedDate() + " " + this.getCreatedTime() + "\n");
            resultString += ("Accessed Date: " + this.getAccessedDate() + "\n");
            resultString += ("First Cluster Location: " + this.getFirstClusterLoc() + "\n");
            resultString += ("Written Time: " + this.getWrittenDate() + " " + this.getWrittenTime() + "\n");
            resultString += ("Size of File (Bytes): " + this.getSizeOfFile() + "\n\n");
        } else {
            resultString += ("-----|  Non-File and Non-Directory" + "\n\n");
            resultString += ("LFN: " + this.getLFname() + "\n");
            resultString += ("SFN: " + this.getSFname() + "\n");
            resultString += ("SFN Ext: " + this.getNameExt() + "\n");
            resultString += ("File Attribute (Dec): " + this.getFileAttribute() + "\n");
            resultString += ("Created Time: " + this.getCreatedDate() + " " + this.getCreatedTime() + "\n");
            resultString += ("Accessed Date: " + this.getAccessedDate() + "\n");
            resultString += ("First Cluster Location: " + this.getFirstClusterLoc() + "\n");
            resultString += ("Written Time: " + this.getWrittenDate() + " " + this.getWrittenTime() + "\n");
            resultString += ("Size of File (Bytes): " + this.getSizeOfFile() + "\n\n");
        }
        return resultString;
    }
}

