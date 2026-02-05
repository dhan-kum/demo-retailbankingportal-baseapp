package com.eviden.app.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class FileService {

    public void addFilesToZip(File file, String fileName, ZipOutputStream zos) throws IOException {
        if (file.isDirectory()) {
            if (fileName.endsWith("/")) {
                zos.putNextEntry(new ZipEntry(fileName));
                zos.closeEntry();
            } else {
                zos.putNextEntry(new ZipEntry(fileName + "/"));
                zos.closeEntry();
            }
            File[] children = file.listFiles();
            for (File child : children) {
                addFilesToZip(child, fileName + "/" + child.getName(), zos);
            }
        } else {
            try(FileInputStream fis = new FileInputStream(file);){
                ZipEntry zipEntry = new ZipEntry(fileName);
                zos.putNextEntry(zipEntry);
                byte[] bytes = new byte[1024];
                int length;
                while ((length = fis.read(bytes)) >= 0) {
                    zos.write(bytes, 0, length);
                }
                zos.closeEntry();
            }catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
}
