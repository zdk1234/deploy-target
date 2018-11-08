package csc.rm.rmi.impl;

import csc.rm.bean.FileBase;
import csc.rm.bean.FileModel;
import csc.rm.rmi.RmiFileTransfer;
import csc.rm.rmi.RmiService;
import csc.rm.util.FileUtil;
import csc.rm.util.PropertiesUtil;

import java.io.File;
import java.io.FileOutputStream;
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

    public RmiServiceImpl() throws RemoteException {
        super();
    }

    @Override
    public int getRmiFileTransfer(RmiFileTransfer rmiFileTransfer) throws RemoteException {
        String sourcePath = rmiFileTransfer.getSourcePath();
        sourcePath = sourcePath.replace("//", "\\");
        FileModel fileModel = rmiFileTransfer.getFileModel();
        Map<String, byte[]> dataMap = rmiFileTransfer.getDataMap();

        // 删除文件
        List<FileBase> deletedFileList = fileModel.getDeletedFileList();
        for (FileBase base : deletedFileList) {
            String sourceFilePath = base.getFilePath();
            String targetFilePath = PropertiesUtil.getValue("monitor.targetPath") + sourceFilePath.replace(sourcePath, "");
            File targetFile = new File(targetFilePath);
            FileUtil.deleteFile(targetFile);
        }

        // 新增文件
        List<FileBase> addedFileList = fileModel.getAddedFileList();
        // 新建文件夹
        addedFileList.stream().filter(FileBase::isDirectory).map(fileBase -> new File(fileBase.getFilePath())).forEach(File::mkdir);

        List<FileBase> diffFileList = fileModel.getDiffFileList();
        addedFileList.addAll(diffFileList);
        // 同步新增文件并覆盖修改文件
        for (FileBase fileBase : addedFileList) {
            if (!fileBase.isDirectory()) {
                FileOutputStream fos = null;
                try {
                    String sourceFilePath = fileBase.getFilePath();
                    String targetFilePath = PropertiesUtil.getValue("monitor.targetPath") + sourceFilePath.replace(sourcePath, "");
                    File targetFile = new File(targetFilePath);
                    byte[] bytes = dataMap.get(sourceFilePath);
                    fos = new FileOutputStream(targetFile);
                    fos.write(bytes);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return 0;
    }
}
