package com.example.icarus_;

import android.widget.TextView;

public class ExtMBR {
    private ExtPartition extPartition;
    private String signatureType;

    public ExtMBR() {
    }

    public void setExtPartition(ExtPartition extPartition) {
        this.extPartition = extPartition;
    }
    public void setSignatureType(String signatureType) {
        this.signatureType = signatureType;
    }
    public ExtPartition getExtPartition() {
        return extPartition;
    }
    public String getSignatureType() {
        return signatureType;
    }

    public Boolean chkExtMBRValidity() {
        if (this.getSignatureType().equals("AA55"))
        {
            return true;
        }
        else {
            return false;
        }
    }

    public String appendValidExtMBR(String resultString) {
        resultString += ("================================================\n");
        resultString += ("=====| START OF EXT FILE SYSTEM INFORMATION |=====\n");
        resultString += ("================================================\n\n");
        resultString += ("ExtMBR detected. Signature Type: " + this.getSignatureType() + "\n\n");
        return resultString;
    }

    public String appendInvalidExtMBR(String resultString) {
        resultString += ("================================================\n");
        resultString += ("=====| START OF EXT FILE SYSTEM INFORMATION |=====\n");
        resultString += ("================================================\n\n");
        resultString += ("Invalid ExtMBR. ExtMBR cannot be detected." + "\n\n");
        return resultString;
    }


}
