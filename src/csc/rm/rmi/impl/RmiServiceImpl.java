package csc.rm.rmi.impl;

import csc.rm.bean.FileModel;
import csc.rm.rmi.RmiFileTransfer;
import csc.rm.rmi.RmiService;
import csc.rm.util.FileUtil;

import java.io.*;
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
        List<String> deletedFileList = fileModel.getDeletedFileList();
        deletedFileList.stream().map(File::new).forEach(FileUtil::deleteFile);

        // 新增文件
        List<String> addedFileList = fileModel.getAddedFileList();
        // 新建文件夹
        addedFileList.stream().map(File::new).filter(File::isDirectory).forEach(File::mkdir);
        // 新建文件
        for (String filePath : addedFileList) {
            File file = new File(filePath);
            if (!file.isDirectory()) {
                FileOutputStream fos = null;
                BufferedOutputStream bos = null;
                try {
                    String absolutePath = file.getAbsolutePath();
                    byte[] bytes = dataMap.get(absolutePath);
                    fos = new FileOutputStream(file);
                    bos = new BufferedOutputStream(fos);
                    bos.write(bytes);
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
                    if (bos != null) {
                        try {
                            bos.close();
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
