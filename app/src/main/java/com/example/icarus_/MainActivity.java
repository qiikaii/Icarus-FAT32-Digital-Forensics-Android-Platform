package com.example.icarus_;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

public class MainActivity extends AppCompatActivity {

    Button startAnalyseButton, startCarveButton;
    TextView displayText;
    private static final int ANALYSE_FILE = 01;
    private static final int CARVE_FILE = 02;
    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // For hiding top bar
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);

        // Permission notifier
        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                Toast.makeText(MainActivity.this, "Permission to write to external storage granted!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                Toast.makeText(MainActivity.this, "Permission to write to external storage denied!", Toast.LENGTH_SHORT).show();
            }
        };

        // Check if permission is enabled
        TedPermission.with(this)
                .setPermissionListener(permissionListener)
                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .check();

        // Make Icarus directory
        File file = new File (Environment.getExternalStorageDirectory(), "Icarus");
        if (file.exists()) {

        }
        else {
            file.mkdirs();
        }

        // Detect Start Analyse Button
        startAnalyseButton = (Button) findViewById(R.id.startAnalyseButton);
        displayText = (TextView) findViewById(R.id.displayText);
        displayText.setText("");
        startAnalyseButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            public void onClick(View v) {
                Intent openFileIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                openFileIntent.addCategory(Intent.CATEGORY_OPENABLE);
                openFileIntent.setType("*/*");
                startActivityForResult(openFileIntent, ANALYSE_FILE);
            }
        });

        // Detect Start Carve Button
        startCarveButton = (Button) findViewById(R.id.startCarveButton);
        displayText = (TextView) findViewById(R.id.displayText);
        displayText.setText("");
        startCarveButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            public void onClick(View v) {
                Intent openFileIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                openFileIntent.addCategory(Intent.CATEGORY_OPENABLE);
                openFileIntent.setType("*/*");
                startActivityForResult(openFileIntent, CARVE_FILE);
            }
        });
    }

    private class TaskExecutor extends AsyncTask<setParams, String, String> {
        FrameLayout progressbar = (FrameLayout) findViewById(R.id.progressBarHolder);
        AlphaAnimation inAnimation;
        AlphaAnimation outAnimation;
        String resultString = " \n";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            startAnalyseButton.setEnabled(false);
            inAnimation = new AlphaAnimation(0f, 1f);
            inAnimation.setDuration(200);
            progressbar.setAnimation(inAnimation);
            progressbar.setVisibility(View.VISIBLE);
            displayText.setText("");
        }

        @Override
        protected void onPostExecute(String resultString) {
            outAnimation = new AlphaAnimation(1f, 0f);
            outAnimation.setDuration(200);
            progressbar.setAnimation(outAnimation);
            progressbar.setVisibility(View.GONE);
            startAnalyseButton.setEnabled(true);
            displayText.setText(resultString);
        }

        @Override
        protected void onProgressUpdate(String... Updates) {
            for (String update : Updates){
                displayText.setText(update);
            }
        }

        @Override
        protected String doInBackground(setParams... params) {
            Looper.prepare();
            int requestCode = params[0].requestCode;
            int resultCode = params[0].resultCode;
            Intent data = params[0].data ;

            MBR mbr = new MBR();

            if (requestCode == ANALYSE_FILE && resultCode == Activity.RESULT_OK) {
                Uri uri = data.getData();
                int partitionCounter = 0;
                long startCount = 0L;
                Boolean validMBR = false;

                try {
                    mbr = getMBR(uri, startCount + 0); // Instantiate new MBR object

                    if (mbr.chkMBRValidity()) {
                        mbr.setPartition1(getMBR_PartitionInfo(uri, startCount + 446));
                        mbr.setPartition2(getMBR_PartitionInfo(uri, startCount + 462));
                        mbr.setPartition3(getMBR_PartitionInfo(uri, startCount + 478));
                        mbr.setPartition4(getMBR_PartitionInfo(uri, startCount + 494));

                        mbr.getPartition1().setEndOfPartition();
                        mbr.getPartition2().setEndOfPartition();
                        mbr.getPartition3().setEndOfPartition();
                        mbr.getPartition4().setEndOfPartition();

                        resultString = mbr.appendValidMBRResult(resultString);
                        validMBR = true;
                    } else {
                        resultString = mbr.appendInvalidMBRResult(resultString);
                        validMBR = false;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("Unable to read file");
                }

                if (validMBR == true) {
                    try {
                        Partition[] partitionAvailability = {mbr.getPartition1(), mbr.getPartition2(),
                                mbr.getPartition3(), mbr.getPartition4()};
                        for (Partition partition : partitionAvailability) {
                            if (partition.getPartitionType().equals("Extended") || partition.getPartitionType().equals("W95 Ext'd (LBA)")) {

                                Boolean loopedAllExtPartitions = false;
                                long priExtPartitionStart = partition.getStartOfPartition();

                                do {
                                    Boolean validExtMBR = false;
                                    ExtMBR extmbr = new ExtMBR();
                                    try {
                                        extmbr = getExtMBR(uri, partition.getStartOfPartition() * 512);
                                        if (extmbr.chkExtMBRValidity()) {
                                            extmbr.setExtPartition(getExtMBR_PartitionInfo(uri, (partition.getStartOfPartition() * 512) + 446, partition.getStartOfPartition(), priExtPartitionStart));
                                            extmbr.getExtPartition().setEndOfPartition();

                                            resultString = extmbr.appendValidExtMBR(resultString);
                                            validExtMBR = true;
                                        } else {
                                            resultString += extmbr.appendInvalidExtMBR(resultString);
                                            validExtMBR = false;
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                        System.out.println("Unable to read file");
                                    }

                                    if (validExtMBR == true) {
                                        try {
                                            if (!extmbr.getExtPartition().getPartitionType().equals("Empty")) {
                                                partitionCounter++;

                                                extmbr.getExtPartition().setVBR(getVBRInfo(uri, extmbr.getExtPartition().getStartOfPartition() * 512));
                                                extmbr.getExtPartition().setPartitionName("EXT PARTITION " + partitionCounter + ": " +
                                                        extmbr.getExtPartition().getVBR().getVolumeLabel() + " (" + extmbr.getExtPartition().getVBR().getFileSystemLabel() + ")");

                                                long startFirstFatSect, endFirstFatSect, endLastFatSect, startDataRegionSect, endDataRegionSect;

                                                startFirstFatSect = extmbr.getExtPartition().getStartOfPartition() + extmbr.getExtPartition().getVBR().getReservedAreaSize();
                                                endFirstFatSect = startFirstFatSect + extmbr.getExtPartition().getVBR().getBit32SectorsOfFat() - 1;

                                                startDataRegionSect = startFirstFatSect;

                                                for (int index = 0; index < extmbr.getExtPartition().getVBR().getNumOfFats(); index++) {
                                                    startDataRegionSect = startDataRegionSect + extmbr.getExtPartition().getVBR().getBit32SectorsOfFat();
                                                }
                                                endLastFatSect = startDataRegionSect - 1;
                                                endDataRegionSect = extmbr.getExtPartition().getStartOfPartition() + extmbr.getExtPartition().getVBR().getBit32Sectors() - 1;

                                                FATable fat = new FATable(startFirstFatSect, endFirstFatSect, endLastFatSect,
                                                        extmbr.getExtPartition().getVBR().getBytesPerSector());
                                                extmbr.getExtPartition().setFAT(getFATInfo(uri, fat.getStartFirstFatDec(), fat));

                                                DataRegion dataRegion = new DataRegion(startDataRegionSect, endDataRegionSect,
                                                        extmbr.getExtPartition().getVBR().getBytesPerSector());
                                                extmbr.getExtPartition().setDataRegion(dataRegion);

                                                /*** Generation of Report ***/
                                                resultString = extmbr.getExtPartition().toString(resultString);
                                                resultString = extmbr.getExtPartition().getVBR().toString(resultString);
                                                resultString = extmbr.getExtPartition().getFAT().toString(resultString);
                                                resultString = extmbr.getExtPartition().getDataRegion().toString(resultString);
                                                partition.setStartOfPartition(extmbr.getExtPartition().getCalExt2MBR());
                                            } else {
                                                loopedAllExtPartitions = true;
                                                partition.setStartOfPartition(priExtPartitionStart);
                                            }
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                            System.out.println("Unable to read file");
                                        }
                                    }
                                    if (extmbr.getExtPartition().getExt2Offset() == 0L) {
                                        loopedAllExtPartitions = true;
                                        partition.setStartOfPartition(priExtPartitionStart);
                                    }
                                } while (loopedAllExtPartitions == false);
                            } else {
                                if (!partition.getPartitionType().equals("Empty")) {

                                    partitionCounter++;

                                    partition.setVBR(getVBRInfo(uri, partition.getStartOfPartition() * 512));
                                    partition.setPartitionName("PARTITION " + partitionCounter + ": " +
                                            partition.getVBR().getVolumeLabel() + " (" + partition.getVBR().getFileSystemLabel() + ")");

                                    long startFirstFatSect, endFirstFatSect, endLastFatSect, startDataRegionSect, endDataRegionSect;

                                    startFirstFatSect = partition.getStartOfPartition() + partition.getVBR().getReservedAreaSize();
                                    endFirstFatSect = startFirstFatSect + partition.getVBR().getBit32SectorsOfFat() - 1;

                                    startDataRegionSect = startFirstFatSect;

                                    for (int index = 0; index < partition.getVBR().getNumOfFats(); index++) {
                                        startDataRegionSect = startDataRegionSect + partition.getVBR().getBit32SectorsOfFat();
                                    }
                                    endLastFatSect = startDataRegionSect - 1;
                                    endDataRegionSect = partition.getStartOfPartition() + partition.getVBR().getBit32Sectors() - 1;

                                    FATable fat = new FATable(startFirstFatSect, endFirstFatSect, endLastFatSect, partition.getVBR().getBytesPerSector());
                                    partition.setFAT(getFATInfo(uri, fat.getStartFirstFatDec(), fat));

                                    DataRegion dataRegion = new DataRegion(startDataRegionSect, endDataRegionSect, partition.getVBR().getBytesPerSector());
                                    partition.setDataRegion(dataRegion);

                                    resultString = partition.toString(resultString);
                                    resultString = partition.getVBR().toString(resultString);
                                    resultString = partition.getFAT().toString(resultString);
                                    resultString = partition.getDataRegion().toString(resultString);

                                } else {
                                    //Ignore
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.out.println("Unable to read file");
                    }

                }
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Scroll down to view file system information", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            else if (requestCode == CARVE_FILE && resultCode == Activity.RESULT_OK) {
                Uri uri = data.getData();
                int partitionCounter = 0;
                long startCount = 0L;
                Boolean validMBR = false;
                String pathName = Environment.getExternalStorageDirectory() + "/Icarus";

                try {
                    mbr = getMBR(uri, startCount + 0); // Instantiate new MBR object

                    if (mbr.chkMBRValidity()) {
                        mbr.setPartition1(getMBR_PartitionInfo(uri, startCount + 446));
                        mbr.setPartition2(getMBR_PartitionInfo(uri, startCount + 462));
                        mbr.setPartition3(getMBR_PartitionInfo(uri, startCount + 478));
                        mbr.setPartition4(getMBR_PartitionInfo(uri, startCount + 494));

                        mbr.getPartition1().setEndOfPartition();
                        mbr.getPartition2().setEndOfPartition();
                        mbr.getPartition3().setEndOfPartition();
                        mbr.getPartition4().setEndOfPartition();

                        resultString = mbr.appendValidMBRResult(resultString);
                        validMBR = true;
                    } else {
                        resultString = mbr.appendInvalidMBRResult(resultString);
                        validMBR = false;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("Unable to read file");
                }

                if (validMBR == true) {
                    try {

                        Partition[] partitionAvailability = {mbr.getPartition1(), mbr.getPartition2(),
                                mbr.getPartition3(), mbr.getPartition4()};

                        for (Partition partition : partitionAvailability) {
                            if (partition.getPartitionType().equals("Extended") || partition.getPartitionType().equals("W95 Ext'd (LBA)")) {

                                Boolean loopedAllExtPartitions = false;
                                long priExtPartitionStart = partition.getStartOfPartition();

                                do {
                                    Boolean validExtMBR = false;
                                    ExtMBR extmbr = new ExtMBR();

                                    try {
                                        extmbr = getExtMBR(uri, partition.getStartOfPartition() * 512);
                                        if (extmbr.chkExtMBRValidity()) {
                                            extmbr.setExtPartition(getExtMBR_PartitionInfo(uri, (partition.getStartOfPartition() * 512) + 446, partition.getStartOfPartition(), priExtPartitionStart));
                                            extmbr.getExtPartition().setEndOfPartition();
                                            resultString = extmbr.appendValidExtMBR(resultString);
                                            validExtMBR = true;
                                        } else {
                                            resultString = extmbr.appendInvalidExtMBR(resultString);
                                            validExtMBR = false;
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                        System.out.println("Unable to read file");
                                    }

                                    if (validExtMBR == true) {
                                        try {
                                            if (!extmbr.getExtPartition().getPartitionType().equals("Empty")) {
                                                partitionCounter++;

                                                extmbr.getExtPartition().setVBR(getVBRInfo(uri, extmbr.getExtPartition().getStartOfPartition() * 512));
                                                extmbr.getExtPartition().setPartitionName("EXT PARTITION " + partitionCounter + ": " +
                                                        extmbr.getExtPartition().getVBR().getVolumeLabel() + " (" +
                                                        extmbr.getExtPartition().getVBR().getFileSystemLabel() + ")");

                                                File file = new File (pathName, "/"+extmbr.getExtPartition().getPartitionName());
                                                if (file.exists()) {

                                                }
                                                else {
                                                    file.mkdirs();
                                                }

                                                long startFirstFatSect, endFirstFatSect, endLastFatSect, startDataRegionSect, endDataRegionSect;

                                                startFirstFatSect = extmbr.getExtPartition().getStartOfPartition() + extmbr.getExtPartition().getVBR().getReservedAreaSize();
                                                endFirstFatSect = startFirstFatSect + extmbr.getExtPartition().getVBR().getBit32SectorsOfFat() - 1;

                                                startDataRegionSect = startFirstFatSect;

                                                for (int index = 0; index < extmbr.getExtPartition().getVBR().getNumOfFats(); index++) {
                                                    startDataRegionSect = startDataRegionSect + extmbr.getExtPartition().getVBR().getBit32SectorsOfFat();
                                                }
                                                endLastFatSect = startDataRegionSect - 1;
                                                endDataRegionSect = extmbr.getExtPartition().getStartOfPartition()
                                                        + extmbr.getExtPartition().getVBR().getBit32Sectors() - 1;

                                                FATable fat = new FATable(startFirstFatSect, endFirstFatSect, endLastFatSect,
                                                        extmbr.getExtPartition().getVBR().getBytesPerSector());
                                                extmbr.getExtPartition().setFAT(getFATInfo(uri, fat.getStartFirstFatDec(), fat));

                                                DataRegion dataRegion = new DataRegion(startDataRegionSect, endDataRegionSect,
                                                        extmbr.getExtPartition().getVBR().getBytesPerSector());
                                                extmbr.getExtPartition().setDataRegion(dataRegion);

                                                ArrayList<StringBuilder> listOfRootDirData;
                                                ArrayList<Long> listOfRootDirCluster;
                                                listOfRootDirCluster = getListOfClusterTraverse(uri, fat, extmbr.getExtPartition().getVBR().getRootCluster());
                                                listOfRootDirData = getListOfDirDataTraverse(uri, fat, dataRegion, listOfRootDirCluster,
                                                        extmbr.getExtPartition().getVBR().getBytesPerCluster());
                                                FileEntry rootDirectory = new FileEntry(listOfRootDirCluster, listOfRootDirData);

                                                ArrayList<FileEntry> listOfFileAndDir = new ArrayList<>();
                                                rootDirectory.setListOfFileAndDir(traverseDirectory(uri, fat, dataRegion, extmbr.getExtPartition()
                                                                .getVBR().getBytesPerCluster(), listOfRootDirData, listOfFileAndDir,
                                                        pathName+"/"+extmbr.getExtPartition().getPartitionName()));
                                                extmbr.getExtPartition().setRootDirectory(rootDirectory);

                                                /*** Generation of Report ***/
                                                String temp = "----------| " + extmbr.getExtPartition().getPartitionName() + " |----------\n\n";
                                                resultString += temp;
                                                resultString = printAllFileAndDir(extmbr.getExtPartition().getRootDirectory().getListOfFileAndDir(), resultString);
                                                partition.setStartOfPartition(extmbr.getExtPartition().getCalExt2MBR());
                                                publishProgress(resultString);
                                                System.out.println("Mampos already");
                                            } else {
                                                loopedAllExtPartitions = true;
                                                partition.setStartOfPartition(priExtPartitionStart);
                                            }
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                            System.out.println("Unable to read file");
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    if (extmbr.getExtPartition().getExt2Offset() == 0L) {
                                        loopedAllExtPartitions = true;
                                        partition.setStartOfPartition(priExtPartitionStart);
                                    }
                                } while (loopedAllExtPartitions == false);
                            } else {
                                if (!partition.getPartitionType().equals("Empty")) {

                                    partitionCounter++;

                                    partition.setVBR(getVBRInfo(uri, partition.getStartOfPartition() * 512));
                                    partition.setPartitionName("PARTITION " + partitionCounter + ": " +
                                            partition.getVBR().getVolumeLabel() + " (" + partition.getVBR().getFileSystemLabel() + ")");

                                    File file = new File (pathName, "/"+partition.getPartitionName());
                                    if (file.exists()) {

                                    }
                                    else {
                                        file.mkdirs();
                                    }

                                    long startFirstFatSect, endFirstFatSect, endLastFatSect, startDataRegionSect, endDataRegionSect;

                                    startFirstFatSect = partition.getStartOfPartition() + partition.getVBR().getReservedAreaSize();
                                    endFirstFatSect = startFirstFatSect + partition.getVBR().getBit32SectorsOfFat() - 1;

                                    startDataRegionSect = startFirstFatSect;

                                    for (int index = 0; index < partition.getVBR().getNumOfFats(); index++) {
                                        startDataRegionSect = startDataRegionSect + partition.getVBR().getBit32SectorsOfFat();
                                    }
                                    endLastFatSect = startDataRegionSect - 1;
                                    endDataRegionSect = partition.getStartOfPartition() + partition.getVBR().getBit32Sectors() - 1;

                                    FATable fat = new FATable(startFirstFatSect, endFirstFatSect, endLastFatSect, partition.getVBR().getBytesPerSector());
                                    partition.setFAT(getFATInfo(uri, fat.getStartFirstFatDec(), fat));

                                    DataRegion dataRegion = new DataRegion(startDataRegionSect, endDataRegionSect, partition.getVBR().getBytesPerSector());
                                    partition.setDataRegion(dataRegion);

                                    ArrayList<StringBuilder> listOfRootDirData;
                                    ArrayList<Long> listOfRootDirCluster;
                                    listOfRootDirCluster = getListOfClusterTraverse(uri, fat, partition.getVBR().getRootCluster());
                                    listOfRootDirData = getListOfDirDataTraverse(uri, fat, dataRegion, listOfRootDirCluster, partition.getVBR().getBytesPerCluster());
                                    FileEntry rootDirectory = new FileEntry(listOfRootDirCluster, listOfRootDirData);

                                    ArrayList<FileEntry> listOfFileAndDir = new ArrayList<FileEntry>();
                                    rootDirectory.setListOfFileAndDir(traverseDirectory(uri, fat, dataRegion, partition.getVBR().getBytesPerCluster(),
                                            listOfRootDirData, listOfFileAndDir, pathName+"/"+partition.getPartitionName()));
                                    partition.setRootDirectory(rootDirectory);

                                    /*** Generation of Report ***/
                                    String temp = "----------| " + partition.getPartitionName() + " |----------\n\n";
                                    resultString += temp;
                                    resultString = printAllFileAndDir(partition.getRootDirectory().getListOfFileAndDir(), resultString);
                                    publishProgress(resultString);
                                } else {
                                    //Ignore
                                }
                            }
                        }
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                        System.out.println("Unable to read file");
                    }
                }
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Scroll down to view carve file information", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            else {

            }

            return resultString;
        }
    }

    private static class setParams {
        int requestCode;
        int resultCode;
        Intent data;

        setParams(int requestCode, int resultCode, Intent data){
            this.requestCode = requestCode;
            this.resultCode = resultCode;
            this.data = data;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        setParams newParam = new setParams(requestCode, resultCode, data);
        TaskExecutor task = new TaskExecutor();
        task.execute(newParam);
    }

    public String printAllFileAndDir (ArrayList<FileEntry> listOfFileAndDir, String resultString) throws IOException {
        for (int i = 0; i < listOfFileAndDir.size(); i++) {
            resultString = listOfFileAndDir.get(i).toString(resultString);
            if (listOfFileAndDir.get(i).getFileAttribute() == 16) {
                printAllFileAndDir(listOfFileAndDir.get(i).getListOfFileAndDir(), resultString);
            }
        }
        return resultString;
    }

    public Callable<Void> toCallable (final Runnable runnable) {
        return new Callable<Void>(){
            @Override
            public Void call(){
                runnable.run();
                return null;
            }
        };
    }

    class createCallable implements Callable<OutputStream> {

        int i;
        Uri uri;
        DataRegion dataRegion;
        ArrayList<Long> clusterNumList;
        long bytesPerCluster;
        long totalFileSize;
        OutputStream outputStream;
        String pathName;

        public createCallable(int i, Uri uri, DataRegion dataRegion,
                            ArrayList<Long> clusterNumList, long bytesPerCluster, long totalFileSize, OutputStream outputStream, String pathName) {
            this.i = i;
            this.uri = uri;
            this.dataRegion = dataRegion;
            this.clusterNumList = clusterNumList;
            this.bytesPerCluster = bytesPerCluster;
            this.totalFileSize = totalFileSize;
            this.outputStream = outputStream;
            this.pathName = pathName;
        }

        public OutputStream call() {
            try{
                if (totalFileSize >= bytesPerCluster) {
                    long startCount = (clusterNumList.get(i) - 2) * bytesPerCluster;
                    long endCount = (clusterNumList.get(i) -1) * bytesPerCluster;
                    //System.out.println("Starting: " + startCount + " Ending: " + endCount + " Current File Size: " + totalFileSize);
                    outputStream.write(getHexBEBuf(uri, (dataRegion.getStartDataRegionDec() + startCount), (dataRegion.getStartDataRegionDec() + endCount - 1)));
                } else {
                    long startCount = (clusterNumList.get(i) - 2) * bytesPerCluster;
                    long endCount = (clusterNumList.get(i) -2) * bytesPerCluster + totalFileSize;
                    //System.out.println("Final Starting: " + startCount + " Final Ending: " + endCount + " Current File Size: " + totalFileSize);
                    outputStream.write(getHexBEBuf(uri, (dataRegion.getStartDataRegionDec() + startCount), (dataRegion.getStartDataRegionDec() + endCount - 1)));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return outputStream;
        }
    }

    // Collecting all the data for File
    public void carving(Uri uri, DataRegion dataRegion,
                        ArrayList<Long> clusterNumList, long bytesPerCluster, long totalFileSize, String pathName) throws IOException {
        System.out.println("Carving " +  pathName + " File Size = " + totalFileSize);
        File file = new File(pathName);
        file.createNewFile();
        OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file));
        ExecutorService executor = Executors.newSingleThreadExecutor();
        List<Future<OutputStream>> futures = new ArrayList<>();

        try {
            for (int i = 0; i < clusterNumList.size(); i++) {
                futures.add(executor.submit(new createCallable(i, uri , dataRegion, clusterNumList, bytesPerCluster, totalFileSize, outputStream, pathName)));
                if (totalFileSize >= bytesPerCluster) {
                    totalFileSize -= bytesPerCluster;
                }
            }

            for (Future<OutputStream> fut : futures) {
                OutputStream os = fut.get();
                os.flush();
            }

            executor.shutdown();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

/*    public void carving(Uri uri, DataRegion dataRegion,
                        ArrayList<Long> clusterNumList, long bytesPerCluster, long totalFileSize, String pathName) throws IOException {
        System.out.println("Carving " +  pathName + " File Size = " + totalFileSize);
        File file = new File(pathName);
        file.createNewFile();
        OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file));


        try {
            for (int i = 0; i < clusterNumList.size(); i++) {
                if (totalFileSize >= bytesPerCluster) {
                    long startCount = (clusterNumList.get(i) - 2) * bytesPerCluster;
                    long endCount = (clusterNumList.get(i) -1) * bytesPerCluster;
                    totalFileSize = totalFileSize - bytesPerCluster;
                    System.out.println("Starting: " + startCount + " Ending: " + endCount + " Current File Size: " + totalFileSize);
                    outputStream.write(getHexBEBuf(uri, (dataRegion.getStartDataRegionDec() + startCount), (dataRegion.getStartDataRegionDec() + endCount - 1)));
                } else {
                    long startCount = (clusterNumList.get(i) - 2) * bytesPerCluster;
                    long endCount = (clusterNumList.get(i) -2) * bytesPerCluster + totalFileSize;
                    System.out.println("Final Starting: " + startCount + " Final Ending: " + endCount + " Current File Size: " + totalFileSize);
                    outputStream.write(getHexBEBuf(uri, (dataRegion.getStartDataRegionDec() + startCount), (dataRegion.getStartDataRegionDec() + endCount - 1)));
                }
            }

            outputStream.flush();
            outputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    /*** Change Decimal to Binary ***/
    public String getDecToBin(long decValue) {

        String binValue = Long.toBinaryString(decValue);

        if (binValue.length() != 16) {
            int pad = 16 - binValue.length(); // Padding binary to allow conversion for date/time
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
    public String getBinToDate(String binDate) {

        String Date, Year, Month, Day, yearBin, monthBin, dayBin;

        yearBin = binDate.substring(0, 7);
        monthBin = binDate.substring(7, 11);
        dayBin = binDate.substring(11, 16);

        Year = String.valueOf(Integer.parseInt(yearBin, 2) + 1980);
        Month = String.valueOf(Integer.parseInt(monthBin, 2));
        Day = String.valueOf(Integer.parseInt(dayBin, 2));

        Date = String.join("-", Day, Month, Year);

        return Date;
    }

    /*** Change Binary to Date ***/
    public String getBinToTime(String binTime) {

        String Time, Hour, Min, Sec, hourBin, minBin, secBin;
        if (binTime.equals("0000000000000000")) {
            return "Nil";
        }

        hourBin = binTime.substring(0, 5);
        minBin = binTime.substring(5, 11);
        secBin = binTime.substring(11, 16);

        Hour = String.valueOf(Integer.parseInt(hourBin, 2));
        Min = String.valueOf(Integer.parseInt(minBin, 2));
        Sec = String.valueOf(Integer.parseInt(secBin, 2) * 2);

        Time = String.join(":", Hour, Min, Sec);

        return Time;
    }


    /***** ***** ***** ***** FUNCTIONS TO GRAB HEX ***** ***** ***** *****/
    /***** ***** ***** ***** FUNCTIONS TO GRAB HEX ***** ***** ***** *****/
    /***** ***** ***** ***** FUNCTIONS TO GRAB HEX ***** ***** ***** *****/
    /***** ***** ***** ***** FUNCTIONS TO GRAB HEX ***** ***** ***** *****/

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

    public byte[] getHexBEBuf(Uri uri, long startCount, long endCount) throws IOException {
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            is.skip(startCount);

            byte[] buffer = new byte[(int) endCount - (int)startCount + 1];
            is.read(buffer, 0, (int)endCount - (int) startCount + 1);

            is.close();
            return buffer;
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


    /*** Change Hex to Decimal ***/ //Long is used in scenario when number is too huge.
    public long getHexToDecimal(StringBuilder hexString) {
        long decValue = Long.parseLong(String.valueOf(hexString), 16);
        return decValue;
    }

    /*** Change Hex to LE to Decimal ***/
    public long getHexLEDec(Uri uri, long startCount, long endCount) throws IOException {
        return getHexToDecimal(getLEHexData(uri, startCount, endCount));
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

    /*** Get Hex Data String in Big Endian Mode (STRING VERSION) ***/
    public StringBuilder getBEHexData(ArrayList<StringBuilder> hexData, long startCount, long endCount)  {
        StringBuilder hexString = new StringBuilder();

        for (long i = startCount; i <= endCount; i++) {
            hexString.append(hexData.get((int)i));
        }

        return hexString;
    }

    /*** Get Hex Data String in Little Endian Mode (STRING VERSION) ***/
    public StringBuilder getLEHexData(ArrayList<StringBuilder> hexData, long startCount, long endCount) {
        StringBuilder hexString = new StringBuilder();

        for (long i = startCount; i <= endCount; i++) {
            hexString.append(hexData.get((int)i));
        }
        StringBuilder hexLE = new StringBuilder();
        for (int j = hexString.length(); j != 0; j -= 2) {
            hexLE.append(hexString.substring(j - 2, j));
        }
        return hexLE;
    }

    /*** Change Hex to LE to Decimal (STRING VERSION) ***/
    public long getHexLEDec(ArrayList<StringBuilder> hexData, long startCount, long endCount) throws IOException {
        return getHexToDecimal(getLEHexData(hexData, startCount, endCount));
    }

    /*** Change Hex to BE to Decimal (STRING VERSION) ***/
    public long getHexBEDec(ArrayList<StringBuilder> hexData, long startCount, long endCount) throws IOException {
        long hexToDecimal = getHexToDecimal(getBEHexData(hexData, startCount, endCount));
        return hexToDecimal;
    }

    /***** ***** ***** ***** START OF GRABBING RECORDS ***** ***** ***** *****/
    /***** ***** ***** ***** START OF GRABBING RECORDS ***** ***** ***** *****/
    /***** ***** ***** ***** START OF GRABBING RECORDS ***** ***** ***** *****/
    /***** ***** ***** ***** START OF GRABBING RECORDS ***** ***** ***** *****/

    /*** Create new MBR object with its' information ***/
    public MBR getMBR(Uri uri, long startCount) throws IOException {
        MBR mbr = new MBR(getLEHexData(uri, startCount + 440, startCount + 444).toString());
        mbr.setSignatureType(getLEHexData(uri, startCount + 510, startCount + 511).toString());
        return mbr;
    }

    /*** Create new Partition object with its' information ***/
    public Partition getMBR_PartitionInfo(Uri uri, long startCount) throws IOException {
        Partition partition = new Partition();
        partition.setBootableStatus(getLEHexData(uri, startCount + 0, startCount + 0).toString());
        partition.setPartitionType(getLEHexData(uri, startCount + 4, startCount + 4).toString());
        partition.setStartOfPartition(getHexLEDec(uri, startCount + 8, startCount + 11));
        partition.setLenOfPartition(getHexLEDec(uri, startCount + 12, startCount + 15));
        return partition;
    }

    /*** Create new Volume Boot Record object with its' information ***/
    public VBR getVBRInfo(Uri uri, long startCount) throws IOException {
        VBR vbr = new VBR();
        vbr.setOEM(getHexToASCII(getBEHexData(uri, startCount + 3, startCount + 10)));
        vbr.setBytesPerSector(getHexLEDec(uri, startCount + 11, startCount + 12));
        vbr.setSectorsPerCluster(getHexLEDec(uri, startCount + 13, startCount + 13));
        vbr.setReservedAreaSize(getHexLEDec(uri, startCount + 14, startCount + 15));
        vbr.setNumOfFats(getHexLEDec(uri, startCount + 16, startCount + 16));
        vbr.setMaxRootFiles(getHexLEDec(uri, startCount + 17, startCount + 18));
        vbr.setBit16Sectors(getHexLEDec(uri, startCount + 19, startCount + 20));
        vbr.setMediaType(getLEHexData(uri, startCount + 21, startCount + 21).toString());
        vbr.setOffset(getHexLEDec(uri, startCount + 28, startCount + 31));
        vbr.setBit32Sectors(getHexLEDec(uri, startCount + 32, startCount + 35));
        vbr.setBit32SectorsOfFat(getHexLEDec(uri, startCount + 36, startCount + 39));
        vbr.setRootCluster(getHexLEDec(uri, startCount + 44, startCount + 47));
        vbr.setVolumeLabel(getHexToASCII(getBEHexData(uri, startCount + 71, startCount + 81)));
        vbr.setFileSystemLabel(getHexToASCII(getBEHexData(uri, startCount + 82, startCount + 89)));
        vbr.setBytesPerCluster();
        return vbr;
    }

    /*** Create new FAT object with its' information ***/
    public FATable getFATInfo(Uri uri, long startCount, FATable fat) throws IOException {
        fat.setFatID(getLEHexData(uri, startCount + 0, startCount + 3).toString());
        fat.setEndClusterMarker(getLEHexData(uri, startCount + 4, startCount + 7).toString());
        return fat;
    }

    /*** Create new ExtMBR object with its' information ***/
    public ExtMBR getExtMBR(Uri uri, long startCount) throws IOException {
        ExtMBR extmbr = new ExtMBR();
        extmbr.setSignatureType(getLEHexData(uri, startCount + 510, startCount + 511).toString());
        return extmbr;
    }

    /*** Create new ExtPartition object with its' information ***/
    public ExtPartition getExtMBR_PartitionInfo(Uri uri, long startCount, long startExtMBR, long priExtPartitionStart) throws IOException {
        ExtPartition extPartition = new ExtPartition(startExtMBR);
        extPartition.setExtBootableStatus(getLEHexData(uri, startCount + 0, startCount + 0).toString());
        extPartition.setPartitionType(getLEHexData(uri, startCount + 4, startCount + 4).toString());
        extPartition.setPriExtPartitionStart(priExtPartitionStart);
        extPartition.setStartOfPartition(getHexLEDec(uri, startCount + 8, startCount + 11));
        extPartition.setLenOfPartition(getHexLEDec(uri, startCount + 12, startCount + 15));
        extPartition.setExt2Offset(getHexLEDec(uri, startCount + 24, startCount + 27));
        extPartition.setCalExt2MBR();
        return extPartition;
    }

    //Get List of Clusters link to the whole data
    public ArrayList<Long> getListOfClusterTraverse(Uri uri, FATable fat, Long clusterNum) throws IOException {
        ArrayList<Long> clusterNumlist = new ArrayList<Long>();
        clusterNumlist.add(clusterNum);
        boolean endOfClusterReach = false;
        do {
            long clusterHex = getHexLEDec(uri, (fat.getStartFirstFatDec() + (4 * (clusterNum))), (fat.getStartFirstFatDec() + ((4 * (clusterNum)) + 3)));

            if ((!fat.chkDmgCluster(clusterHex)) && !fat.chkEOFCluster(clusterHex)) {
                clusterNumlist.add(clusterHex);
                clusterNum = clusterNumlist.get(clusterNumlist.size() - 1);
            } else {
                endOfClusterReach = true;
            }
        } while (endOfClusterReach == false);

        return clusterNumlist;
    }


    // Collecting all the data for Directory
    public ArrayList<StringBuilder> getListOfDirDataTraverse(Uri uri, FATable fat, DataRegion dataRegion,
                                                             ArrayList<Long> clusterNumList, long bytesPerCluster) throws IOException {
        ArrayList<StringBuilder> dirContent = new ArrayList<StringBuilder>();
        long totalFileSize = clusterNumList.size() * bytesPerCluster;

        for (int i = 0; i < clusterNumList.size(); i++) {
            if (totalFileSize >= bytesPerCluster) {
                System.out.println("Total File Size: " + totalFileSize);
                totalFileSize = totalFileSize - (bytesPerCluster);
                System.out.println("Dir Cluster No.: "+ clusterNumList.get(i) + " Cluster No for Cal: " + (clusterNumList.get(i) - 2));
                for (long j=((clusterNumList.get(i) - 2) * bytesPerCluster); j<((clusterNumList.get(i) - 1) * bytesPerCluster); j++) {
                    dirContent.add(getBEHexData(uri, (dataRegion.getStartDataRegionDec() + j),
                            (dataRegion.getStartDataRegionDec() + j)));
                }
            } else {
                System.out.println("END Total File Size: " + totalFileSize);
                System.out.println("END Dir Cluster No.: "+ clusterNumList.get(i) + " Cluster No for Cal: " + (clusterNumList.get(i) - 2));
                for (long j=(clusterNumList.get(i) - 2) * bytesPerCluster; j<((clusterNumList.get(i) - 2) * bytesPerCluster + totalFileSize); j++) {
                    dirContent.add(getBEHexData(uri, (dataRegion.getStartDataRegionDec() + j),
                            (dataRegion.getStartDataRegionDec() + j)));
                }
            }
        }

        return dirContent;
    }

    public ArrayList<FileEntry> traverseDirectory(Uri uri, FATable fat, DataRegion dataRegion, long bytesPerCluster,
                                                  ArrayList<StringBuilder> listOfDirData, ArrayList<FileEntry> listOfFileAndDir, String pathName) throws IOException, InterruptedException {

        int numOfLFNentries;
        long startCount = 0;
        long endCount = listOfDirData.size()-1;

        while (startCount < endCount) {
            numOfLFNentries = 0;
            System.out.println("Traversing Directory");
            if (getHexLEDec(listOfDirData, startCount + 11, startCount + 11) == 0) {
                // EMPTY
                startCount = startCount + 32;
            } else if (getHexLEDec(listOfDirData, startCount + 11, startCount + 11) == 15) {
                // LONG FILE NAME DETECTED
                if (getHexLEDec(listOfDirData, startCount, startCount) == 0 || (
                        getHexLEDec(listOfDirData, startCount, startCount) == 46 &&
                                getHexLEDec(listOfDirData, startCount+11, startCount+11) == 16)) {
                    // Skip as entry is 0 OR a '.' Directory;
                    startCount = startCount + 32;
                } else if (getHexLEDec(listOfDirData, startCount, startCount) == 229) {
                    // File OR Directory is deleted.
                    startCount = startCount + 32;
                } else {
                    // LONG FILE
                    // File is present
                    numOfLFNentries = Integer.parseInt(getBEHexData(listOfDirData, startCount, startCount).toString());
                    numOfLFNentries = numOfLFNentries - 40;

                    // Generate long file name
                    String fullLFname = "";
                    for (int i = 0; i < numOfLFNentries; i++) {
                        String tempLFname = "";
                        if (getHexBEDec(listOfDirData, startCount + 1, startCount + 1) != 255 &&
                                getHexBEDec(listOfDirData, startCount + 1, startCount + 1) != 0) {
                            tempLFname = tempLFname + getHexToASCII(getBEHexData(listOfDirData, startCount + 1, startCount + 1));
                        }
                        if (getHexBEDec(listOfDirData, startCount + 3, startCount + 3) != 255 &&
                                getHexBEDec(listOfDirData, startCount + 3, startCount + 3) != 0) {
                            tempLFname = tempLFname + getHexToASCII(getBEHexData(listOfDirData, startCount + 3, startCount + 3));
                        }
                        if (getHexBEDec(listOfDirData, startCount + 5, startCount + 5) != 255 &&
                                getHexBEDec(listOfDirData, startCount + 5, startCount + 5) != 0) {
                            tempLFname = tempLFname + getHexToASCII(getBEHexData(listOfDirData, startCount + 5, startCount + 5));
                        }
                        if (getHexBEDec(listOfDirData, startCount + 7, startCount + 7) != 255 &&
                                getHexBEDec(listOfDirData, startCount + 7, startCount + 7) != 0) {
                            tempLFname = tempLFname + getHexToASCII(getBEHexData(listOfDirData, startCount + 7, startCount + 7));
                        }
                        if (getHexBEDec(listOfDirData, startCount + 9, startCount + 9) != 255 &&
                                getHexBEDec(listOfDirData, startCount + 9, startCount + 9) != 0) {
                            tempLFname = tempLFname + getHexToASCII(getBEHexData(listOfDirData, startCount + 9, startCount + 9));
                        }
                        if (getHexBEDec(listOfDirData, startCount + 14, startCount + 14) != 255 &&
                                getHexBEDec(listOfDirData, startCount + 14, startCount + 14) != 0) {
                            tempLFname = tempLFname + getHexToASCII(getBEHexData(listOfDirData, startCount + 14, startCount + 14));
                        }
                        if (getHexBEDec(listOfDirData, startCount + 16, startCount + 16) != 255 &&
                                getHexBEDec(listOfDirData, startCount + 16, startCount + 16) != 0) {
                            tempLFname = tempLFname + getHexToASCII(getBEHexData(listOfDirData, startCount + 16, startCount + 16));
                        }
                        if (getHexBEDec(listOfDirData, startCount + 18, startCount + 18) != 255 &&
                                getHexBEDec(listOfDirData, startCount + 18, startCount + 18) != 0) {
                            tempLFname = tempLFname + getHexToASCII(getBEHexData(listOfDirData, startCount + 18, startCount + 18));
                        }
                        if (getHexBEDec(listOfDirData, startCount + 20, startCount + 20) != 255 &&
                                getHexBEDec(listOfDirData, startCount + 20, startCount + 20) != 0) {
                            tempLFname = tempLFname + getHexToASCII(getBEHexData(listOfDirData, startCount + 20, startCount + 20));
                        }
                        if (getHexBEDec(listOfDirData, startCount + 22, startCount + 22)  != 255 &&
                                getHexBEDec(listOfDirData, startCount + 22, startCount + 22) != 0) {
                            tempLFname = tempLFname + getHexToASCII(getBEHexData(listOfDirData, startCount + 22, startCount + 22));
                        }
                        if (getHexBEDec(listOfDirData, startCount + 24, startCount + 24) != 255 &&
                                getHexBEDec(listOfDirData, startCount + 24, startCount + 24) != 0) {
                            tempLFname = tempLFname + getHexToASCII(getBEHexData(listOfDirData, startCount + 24, startCount + 24));
                        }
                        if (getHexBEDec(listOfDirData, startCount + 28, startCount + 28) != 255 &&
                                getHexBEDec(listOfDirData, startCount + 28, startCount + 28) != 0) {
                            tempLFname = tempLFname + getHexToASCII(getBEHexData(listOfDirData, startCount + 28, startCount + 28));
                        }
                        if (getHexBEDec(listOfDirData, startCount + 30, startCount + 30) != 255 &&
                                getHexBEDec(listOfDirData, startCount + 30, startCount + 30) != 0) {
                            tempLFname = tempLFname + getHexToASCII(getBEHexData(listOfDirData, startCount + 30, startCount + 30));
                        }
                        fullLFname = tempLFname + fullLFname;
                        startCount = startCount + 32;
                    }

                    if (getHexLEDec(listOfDirData, startCount + 11, startCount + 11) == 32) {
                        // FILE ONLY
                        FileEntry fileEntry = new FileEntry();
                        fileEntry.setLFname(fullLFname);
                        fileEntry.setSFname(getHexToASCII(getBEHexData(listOfDirData, startCount, startCount + 7)));
                        fileEntry.setNameExt(getHexToASCII(getBEHexData(listOfDirData, startCount + 8, startCount + 10))
                                .replace("ÿ", "").replace(" ", "").replace("�", ""));
                        fileEntry.setFileAttribute(getHexLEDec(listOfDirData, startCount + 11, startCount + 11));
                        // 13th is time in tenths of seconds.
                        fileEntry.setCreatedTime(getHexLEDec(listOfDirData, startCount + 14, startCount + 15));
                        fileEntry.setCreatedDate(getHexLEDec(listOfDirData, startCount + 16, startCount + 17));
                        fileEntry.setAccessedDate(getHexLEDec(listOfDirData, startCount + 18, startCount + 19));
                        fileEntry.setFirstClusterLoc(getHexToDecimal(concatHex(getLEHexData(listOfDirData, startCount + 20, startCount + 21),
                                getLEHexData(listOfDirData, startCount + 26, startCount + 27))));
                        fileEntry.setWrittenTime(getHexLEDec(listOfDirData, startCount + 22, startCount + 23));
                        fileEntry.setWrittenDate(getHexLEDec(listOfDirData, startCount + 24, startCount + 25));
                        fileEntry.setSizeOfFile(getHexLEDec(listOfDirData, startCount + 28, startCount + 31));
                        fileEntry.setListOfClusters(getListOfClusterTraverse(uri, fat, fileEntry.getFirstClusterLoc()));

                        carving(uri, dataRegion, fileEntry.getListOfClusters(), bytesPerCluster, fileEntry.getSizeOfFile(), pathName+"/"+fileEntry.getLFname());
                        listOfFileAndDir.add(fileEntry);
                        startCount = startCount + 32;
                    }
                    else if (getHexLEDec(listOfDirData, startCount + 11, startCount + 11) == 16){
                        //DIRECTORY ONLY
                        FileEntry fileEntry = new FileEntry();
                        fileEntry.setLFname(fullLFname);
                        fileEntry.setSFname(getHexToASCII(getBEHexData(listOfDirData, startCount, startCount + 7)));
                        fileEntry.setNameExt(getHexToASCII(getBEHexData(listOfDirData, startCount + 8, startCount + 10))
                                .replace("ÿ", "").replace(" ", "").replace("�", ""));
                        fileEntry.setFileAttribute(getHexLEDec(listOfDirData, startCount + 11, startCount + 11));
                        // 13th is time in tenths of seconds.
                        fileEntry.setCreatedTime(getHexLEDec(listOfDirData, startCount + 14, startCount + 15));
                        fileEntry.setCreatedDate(getHexLEDec(listOfDirData, startCount + 16, startCount + 17));
                        fileEntry.setAccessedDate(getHexLEDec(listOfDirData, startCount + 18, startCount + 19));
                        fileEntry.setFirstClusterLoc(getHexToDecimal(concatHex(getLEHexData(listOfDirData, startCount + 20, startCount + 21),
                                getLEHexData(listOfDirData, startCount + 26, startCount + 27))));
                        fileEntry.setWrittenTime(getHexLEDec(listOfDirData, startCount + 22, startCount + 23));
                        fileEntry.setWrittenDate(getHexLEDec(listOfDirData, startCount + 24, startCount + 25));
                        fileEntry.setSizeOfFile(getHexLEDec(listOfDirData, startCount + 28, startCount + 31));
                        fileEntry.setListOfClusters(getListOfClusterTraverse(uri, fat, fileEntry.getFirstClusterLoc()));
                        fileEntry.setListOfData(getListOfDirDataTraverse(uri, fat, dataRegion, fileEntry.getListOfClusters(), bytesPerCluster));
                        ArrayList<FileEntry> listOfAnotherFileAndDir = new ArrayList<FileEntry>();
                        File file = new File (pathName, "/"+fileEntry.getLFname());
                        if (file.exists()) {

                        }
                        else {
                            file.mkdirs();
                        }

                        fileEntry.setListOfFileAndDir(traverseDirectory(uri, fat, dataRegion, bytesPerCluster,
                                fileEntry.getListOfData(), listOfAnotherFileAndDir, pathName+"/"+fileEntry.getLFname()));
                        listOfFileAndDir.add(fileEntry);
                        startCount = startCount + 32;
                    }
                    else {
                        //INVALID FILE
                        // System.out.println("INNER INVALID FILE DETECTED: " + fileEntry.getSFname() + " ext " + fileEntry.getNameExt() + " " + getHexLEDec(listOfDirData, startCount + 11, startCount + 11));
                        startCount = startCount + 32;
                    }
                }
            } else if (getHexLEDec(uri, startCount + 11, startCount + 11) == 1 ||
                    getHexLEDec(uri, startCount + 11, startCount + 11) == 2 ||
                    getHexLEDec(uri, startCount + 11, startCount + 11) == 4 ||
                    getHexLEDec(uri, startCount + 11, startCount + 11) == 32) {
                // READ ONLY || HIDDEN FILE || SYSTEM FILE || ARCHIVE
                // Currently unsure what will happen for the following files;
                // System.out.println("UNSURE FILE DETECTED");
                startCount = startCount + 32;
            } else if (
                    getHexLEDec(uri, startCount + 11, startCount + 11) == 8) {
                // VOLUME_ID
                // System.out.println("VOLUME LABEL DETECTED");
                startCount = startCount + 32;
            } else {
                // INCORRECT FILE FORMAT
                // System.out.println("INVALID FILE DETECTED: " + fileEntry.getSFname() + " ext " + fileEntry.getNameExt() + " " + getHexLEDec(listOfDirData, startCount + 11, startCount + 11));
                startCount = startCount + 32;
            }
        }
        return listOfFileAndDir;
    }
}


