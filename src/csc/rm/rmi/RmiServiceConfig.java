package csc.rm.rmi;

import csc.rm.util.LoggerUtil;
import csc.rm.util.PropertiesUtil;

import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Objects;

/**
 * 功能描述:
 * Created by zdk on 2018/11/7.
 */
public class RmiServiceConfig {

    private static final LoggerUtil LOGGER = new LoggerUtil(RmiServiceConfig.class);

    public static void init() throws Exception {
        int port;
        try {
            port = Integer.valueOf(Objects.requireNonNull(PropertiesUtil.getValue("rmi.port")));
        } catch (Exception e) {
            port = 1099;
        }
        Registry registry = LocateRegistry.createRegistry(port);
        String context = PropertiesUtil.getValue("rmi.context");
        if (context == null || "".equals(context)) {
            throw new IllegalStateException("conf/config.properties -> rmi.context 设置为空");
        }
        Class<?> clz = Class.forName(PropertiesUtil.getValue("rmi.excuteClass"));
        Remote remote = (Remote) clz.getDeclaredConstructor().newInstance();
        registry.rebind(context, remote);

        LOGGER.info("<初始化> 启动RMI服务 context:" + context + " | port:" + port + " 执行类:" + clz.getName());
    }
}
