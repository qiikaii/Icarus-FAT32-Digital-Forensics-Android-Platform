package com.example.icarus_;

import android.widget.TextView;

public class Partition {
    private String partitionName;
    private String bootableStatus;
    private String partitionType;
    private long startOfPartition;
    private long endOfPartition;
    private long lenOfPartition;
    private VBR vbr;
    private FATable fat;
    private DataRegion dataRegion;
    private FileEntry rootDirectory;


    public Partition() {
    }

    public void setPartitionName(String partitionName) {
        this.partitionName = partitionName;
    }

    public void setBootableStatus(String hexData) {
        switch(hexData) {
            case "80":
                this.bootableStatus = "Bootable";
                break;
            case "00":
                this.bootableStatus = "Non-Bootable";
                break;
            default:
                this.bootableStatus = "Invalid Bootable State";
                break;
        }
    }

    public void setPartitionType(String hexData) {
        switch(hexData) {
            case "00":
                this.partitionType = "Empty";
                break;
            case "01":
                this.partitionType = "FAT12";
                break;
            case "02":
                this.partitionType = "XENIX root";
                break;
            case "03":
                this.partitionType = "XENIX usr";
                break;
            case "04":
                this.partitionType = "FAT16 <32M";
                break;
            case "05":
                this.partitionType = "Extended";
                break;
            case "06":
                this.partitionType = "FAT16";
                break;
            case "07":
                this.partitionType = "HPFS/NTFS/exFAT";
                break;
            case "08":
                this.partitionType = "AIX";
                break;
            case "09":
                this.partitionType = "AIX bootable";
                break;
            case "0A":
                this.partitionType = "OS/2 Boot Manager";
                break;
            case "0B":
                this.partitionType = "W95 FAT32";
                break;
            case "0C":
                this.partitionType = "W95 FAT32 (LBA)";
                break;
            case "0E":
                this.partitionType = "W95 FAT16 (LBA)";
                break;
            case "0F":
                this.partitionType = "W95 Ext'd (LBA)";
                break;
            case "10":
                this.partitionType = "OPUS";
                break;
            case "11":
                this.partitionType = "Hidden FAT12";
                break;
            case "12":
                this.partitionType = "Compaq diagnost";
                break;
            case "14":
                this.partitionType = "Hidden FAT16 <3";
                break;
            case "16":
                this.partitionType = "Hidden FAT16";
                break;
            case "17":
                this.partitionType = "Hidden HPFS/NTFS/exFAT";
                break;
            case "18":
                this.partitionType = "AST SmartSleep";
                break;
            case "1B":
                this.partitionType = "Hidden W95 FAT32 (0Bh)";
                break;
            case "1C":
                this.partitionType = "Hidden W95 FAT32 (OCh)";
                break;
            case "1E":
                this.partitionType = "Hidden W95 FAT16 (0Eh)";
                break;

            case "24":
                this.partitionType = "NEC DOS";
                break;
            case "27":
                this.partitionType = "Hidden NTFS Win";
                break;
            case "39":
                this.partitionType = "Plan 9";
                break;
            case "3C":
                this.partitionType = "PartitionMagic";
                break;
            case "40":
                this.partitionType = "Venix 80286";
                break;
            case "41":
                this.partitionType = "PPC PReP Boot";
                break;
            case "42":
                this.partitionType = "SFS";
                break;
            case "4D":
                this.partitionType = "QNX4.x";
                break;
            case "4E":
                this.partitionType = "QNX4.x 2nd part";
                break;
            case "4F":
                this.partitionType = "QNX4.x 3rd part";
                break;
            case "50":
                this.partitionType = "OnTrack DM";
                break;
            case "51":
                this.partitionType = "OnTrack DM6 Aux 1";
                break;
            case "52":
                this.partitionType = "CP/M";
                break;
            case "53":
                this.partitionType = "OnTrack DM6 Aux 3";
                break;
            case "54":
                this.partitionType = "OnTrackDM6";
                break;
            case "55":
                this.partitionType = "EZ-Drive";
                break;
            case "56":
                this.partitionType = "Golden Bow";
                break;
            case "5C":
                this.partitionType = "Priam Edisk";
                break;
            case "61":
                this.partitionType = "SpeedStor";
                break;
            case "63":
                this.partitionType = "GNU HURD or Sys";
                break;
            case "64":
                this.partitionType = "Novell Netware 286";
                break;
            case "65":
                this.partitionType = "Novell Netware 386";
                break;
            case "70":
                this.partitionType = "DiskSecure Multiboot";
                break;
            case "75":
                this.partitionType = "PC/IX";
                break;
            case "80":
                this.partitionType = "Old Minix";
                break;
            case "81":
                this.partitionType = "Minix / old Lin";
                break;
            case "82":
                this.partitionType = "Linux swap / Solaris";
                break;
            case "83":
                this.partitionType = "Linux";
                break;
            case "84":
                this.partitionType = "OS/2 hidden or";
                break;
            case "85":
                this.partitionType = "Linux extended";
                break;
            case "86":
                this.partitionType = "NTFS volume set 06h";
                break;
            case "87":
                this.partitionType = "NTFS volume set 07h";
                break;
            case "88":
                this.partitionType = "Linux plaintext";
                break;
            case "8E":
                this.partitionType = "Linux LVM";
                break;
            case "93":
                this.partitionType = "Amoeba";
                break;
            case "94":
                this.partitionType = "Amoeba BBT";
                break;
            case "9F":
                this.partitionType = "BSD/OS";
                break;
            case "A0":
                this.partitionType = "IBM Thinkpad hibernate";
                break;
            case "A5":
                this.partitionType = "FreeBSD";
                break;
            case "A6":
                this.partitionType = "OpenBSD";
                break;
            case "A7":
                this.partitionType = "NeXTSTEP";
                break;
            case "A8":
                this.partitionType = "Darwin UFS";
                break;
            case "A9":
                this.partitionType = "NetBSD";
                break;
            case "AB":
                this.partitionType = "Darwin boot";
                break;
            case "AF":
                this.partitionType = "HFS / HFS+";
                break;
            case "B7":
                this.partitionType = "BSDI fs";
                break;
            case "B8":
                this.partitionType = "BSDI swap";
                break;
            case "BB":
                this.partitionType = "Boot Wizard hidden";
                break;
            case "BC":
                this.partitionType = "Acronis FAT32 LBA";
                break;
            case "BE":
                this.partitionType = "Solaris boot";
                break;
            case "BF":
                this.partitionType = "Solaris";
                break;
            case "C1":
                this.partitionType = "DRDOS/sec (FAT12)";
                break;
            case "C4":
                this.partitionType = "DRDOS/sec (FAT16)";
                break;
            case "C6":
                this.partitionType = "DRDOS/sec (FAT16B)";
                break;
            case "C7":
                this.partitionType = "Syrinx";
                break;
            case "DA":
                this.partitionType = "Non-FS data";
                break;
            case "DB":
                this.partitionType = "CP/M / CTOS / D800 / DRMK";
                break;
            case "DE":
                this.partitionType = "Dell Utility";
                break;
            case "DF":
                this.partitionType = "BootIt";
                break;
            case "E1":
                this.partitionType = "DOS access";
                break;
            case "E3":
                this.partitionType = "DOS R/O";
                break;
            case "E4":
                this.partitionType = "SpeedStor";
                break;
            case "EA":
                this.partitionType = "Rufus alignment";
                break;
            case "EB":
                this.partitionType = "BeOS fs";
                break;
            case "EE":
                this.partitionType = "GPT";
                break;
            case "EF":
                this.partitionType = "EFI (FAT-12/16/32)";
                break;
            case "F0":
                this.partitionType = "Linux/PA-RISC boot";
                break;
            case "F1":
                this.partitionType = "SpeedStor";
                break;
            case "F2":
                this.partitionType = "DOS secondary";
                break;
            case "F4":
                this.partitionType = "SpeedStor FAT16B";
                break;
            case "FB":
                this.partitionType = "VMware VMFS";
                break;
            case "FC":
                this.partitionType = "VMware VMKCORE";
                break;
            case "FD":
                this.partitionType = "Linux raid auto";
                break;
            case "FE":
                this.partitionType = "LANstep";
                break;
            case "FF":
                this.partitionType = "BBT";
                break;
            default:
                this.partitionType = "Invalid Partition Type";
        }
    }
    public void setStartOfPartition(long startOfPartition) { this.startOfPartition = startOfPartition; }
    public void setEndOfPartition() { this.endOfPartition = this.startOfPartition + this.lenOfPartition - 1; }
    public void setLenOfPartition(long lenOfPartition){
        this.lenOfPartition = lenOfPartition;
    }

    public void setVBR(VBR vbr) {
        this.vbr = vbr;
    }
    public void setFAT(FATable fat) {this.fat = fat;}
    public void setDataRegion(DataRegion dataRegion) { this.dataRegion = dataRegion; }
    public void setRootDirectory(FileEntry rootDirectory) {this.rootDirectory = rootDirectory; }

    public String getPartitionName() {return partitionName; }
    public String getBootableStatus() {
        return bootableStatus;
    }
    public String getPartitionType() {
        return partitionType;
    }

    public long getStartOfPartition() {
        return startOfPartition;
    }
    public long getEndOfPartition() {
        return endOfPartition;
    }
    public long getLenOfPartition(){ return lenOfPartition; }

    public VBR getVBR() {
        return vbr;
    }
    public FATable getFAT() { return this.fat; }
    public DataRegion getDataRegion() { return this.dataRegion; }
    public FileEntry getRootDirectory() { return this.rootDirectory; }

    public String toString(String resultString) {
        resultString += ("----------| " + this.getPartitionName() + " |----------\n\n");
        resultString += ("Bootable Status: " + this.getBootableStatus() + "\n");
        resultString += ("Partition Type: " + this.getPartitionType() + "\n");
        resultString += ("Start of Partition (Sectors): " + this.getStartOfPartition() + "\n");
        resultString += ("End of Partition (Sectors): " + this.getEndOfPartition() + "\n");
        resultString += ("Len of Partition (Sectors): " + this.getLenOfPartition() + "\n");
        resultString += ("\n");
        return resultString;
    }

}


