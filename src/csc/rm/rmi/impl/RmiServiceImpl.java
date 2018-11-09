package csc.rm.rmi.impl;

import csc.rm.bean.FileBase;
import csc.rm.bean.FileModel;
import csc.rm.rmi.RmiFileTransfer;
import csc.rm.rmi.RmiService;
import csc.rm.util.LoggerUtil;
import csc.rm.util.PropertiesUtil;
import csc.rm.util.RemoteUploadUtil;
import jcifs.smb.SmbFile;

import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Map;

/**
 * 功能描述:
 * Created by zdk on 2018/11/7.
 */
public class RmiServiceImpl extends UnicastRemoteObject implements RmiService, Serializable {

    private static final LoggerUtil LOGGER = new LoggerUtil(RmiServiceImpl.class);

    private final static String BAST_TARGET_PATH = PropertiesUtil.getValue("monitor.targetPath");

    public RmiServiceImpl() throws RemoteException {
        super();
    }

    @Override
    public int getRmiFileTransfer(RmiFileTransfer rmiFileTransfer) throws RemoteException {
        String sourcePath = rmiFileTransfer.getSourcePath();
        sourcePath = sourcePath.replace("//", "\\");
        FileModel fileModel = rmiFileTransfer.getFileModel();
        Map<String, byte[]> dataMap = rmiFileTransfer.getDataMap();

        List<FileBase> addedFileList = fileModel.getAddedFileList();
        List<FileBase> diffFileList = fileModel.getDiffFileList();
        List<FileBase> deletedFileList = fileModel.getDeletedFileList();

        final StringBuilder builder = new StringBuilder();
        if (!addedFileList.isEmpty()) {
            builder.append("\n新增:\n");
            addedFileList.forEach(fileBase -> builder.append("\t").append(fileBase.toString()).append("\n"));
        }
        if (diffFileList.size() != 0) {
            builder.append("\n修改:\n");
            diffFileList.forEach(fileBase -> builder.append("\t").append(fileBase.toString()).append("\n"));
        }
        if (deletedFileList.size() != 0) {
            builder.append("\n删除:\n");
            deletedFileList.forEach(fileBase -> builder.append("\t").append(fileBase.toString()).append("\n"));
        }
        LOGGER.info(builder.toString());

        // 删除文件
        for (FileBase base : deletedFileList) {
            try {
                String sourceFilePath = base.getFilePath();
                String targetFilePath = BAST_TARGET_PATH + sourceFilePath.replace(sourcePath, "");
                targetFilePath = targetFilePath.replace("\\", "/");
                SmbFile remoteFile = new SmbFile(targetFilePath);
                if (remoteFile.isDirectory()) {
                    remoteFile = new SmbFile(targetFilePath + "/");
                }
                RemoteUploadUtil.smbRemove(remoteFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 新建文件夹
        for (FileBase base : addedFileList) {
            if (base.isDirectory()) {
                try {
                    String sourceFilePath = base.getFilePath();
                    String targetFilePath = BAST_TARGET_PATH + sourceFilePath.replace(sourcePath, "");
                    targetFilePath = targetFilePath.replace("\\", "/");
                    SmbFile remoteFile = new SmbFile(targetFilePath);

                    if (!remoteFile.exists()) {
                        remoteFile.mkdir();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        addedFileList.addAll(diffFileList);
        // 同步新增文件并覆盖修改文件
        for (FileBase fileBase : addedFileList) {
            if (!fileBase.isDirectory()) {
                //FileOutputStream fos = null;
                try {
                    String sourceFilePath = fileBase.getFilePath();
                    String targetFilePath = BAST_TARGET_PATH + sourceFilePath.replace(sourcePath, "");

                    targetFilePath = targetFilePath.replace("\\", "/");

                    RemoteUploadUtil.smbPut(targetFilePath, dataMap.get(sourceFilePath));

                    /*File targetFile = new File(targetFilePath);
                    byte[] bytes = dataMap.get(sourceFilePath);
                    fos = new FileOutputStream(targetFile);
                    fos.write(bytes);*/
                } catch (Exception e) {
                    e.printStackTrace();
                }/* finally {
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }*/
            }
        }
        return 0;
    }
}
