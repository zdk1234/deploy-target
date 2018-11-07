package csc.rm.rmi.impl;

import csc.rm.bean.FileBase;
import csc.rm.bean.FileModel;
import csc.rm.rmi.RmiFileTransfer;
import csc.rm.rmi.RmiService;
import csc.rm.util.FileUtil;

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
        FileModel fileModel = rmiFileTransfer.getFileModel();
        Map<String, byte[]> dataMap = rmiFileTransfer.getDataMap();

        // 删除文件
        List<FileBase> deletedFileList = fileModel.getDeletedFileList();
        deletedFileList.stream().map(fileBase -> new File(fileBase.getFilePath())).forEach(FileUtil::deleteFile);

        // 新增文件
        List<FileBase> addedFileList = fileModel.getAddedFileList();
        // 新建文件夹
        addedFileList.stream().filter(FileBase::isDirectory).map(fileBase -> new File(fileBase.getFilePath())).forEach(File::mkdir);
        // 新建文件
        for (FileBase fileBase : addedFileList) {
            File file = new File(fileBase.getFilePath());
            if (!fileBase.isDirectory()) {
                FileOutputStream fos = null;
                try {
                    String absolutePath = file.getAbsolutePath();
                    byte[] bytes = dataMap.get(absolutePath);
                    fos = new FileOutputStream(file);
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

        List<FileBase> diffFileList = fileModel.getDiffFileList();
        // 新建文件
        for (FileBase fileBase : diffFileList) {
            File file = new File(fileBase.getFilePath());
            if (!fileBase.isDirectory()) {
                FileOutputStream fos = null;
                try {
                    String absolutePath = file.getAbsolutePath();
                    byte[] bytes = dataMap.get(absolutePath);
                    fos = new FileOutputStream(file);
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
