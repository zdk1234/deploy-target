package csc.rm.main;


import csc.rm.rmi.RmiServiceConfig;
import csc.rm.rmi.impl.RmiServiceImpl;
import csc.rm.util.LoggerUtil;

/**
 * 功能描述:
 * Created by zdk on 2018/11/7.
 */
public class DeployTargetMain {

    private static final LoggerUtil LOGGER = new LoggerUtil(RmiServiceImpl.class);

    public static void main(String[] args) throws Exception {
        LOGGER.info("<初始化> 开始");
        RmiServiceConfig.init();
        LOGGER.info("<初始化> 启动完成");
    }
}
