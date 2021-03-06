package csc.rm.util;

import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileOutputStream;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * 功能描述: 上传文件至远程共享文件夹
 * Created by zdk on 2018/11/8.
 */
public class RemoteUploadUtil {

    /**
     * @param bytes
     */
    public static void smbPut(String remoteFilePath, byte[] bytes) throws IOException {
        OutputStream out = null;
        try {
            SmbFile remoteFile = new SmbFile(remoteFilePath);
            out = new BufferedOutputStream(new SmbFileOutputStream(remoteFile));
            out.write(bytes);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * @param smbFile
     */
    public static void smbRemove(SmbFile smbFile) throws IOException {
        if (smbFile != null && smbFile.exists()) {
            if (smbFile.isFile()) {
                smbFile.delete();
            } else {
                for (SmbFile file : smbFile.listFiles()) {
                    smbRemove(file);
                }
                smbFile.delete();
            }
        }
    }
}
