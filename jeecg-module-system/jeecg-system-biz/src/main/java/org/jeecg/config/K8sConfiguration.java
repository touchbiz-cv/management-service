package org.jeecg.config;

import cn.hutool.core.io.FileUtil;
import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author jiangyan
 */
@Slf4j
@Configuration
public class K8sConfiguration {

    private final static String USER_DIR_SYSTEM_PROPERTY = "user.dir";

    @Bean
    @SneakyThrows
    public KubernetesClient initClient(K8sConfig k8sConfig, NacosConfigManager configManager) {
        var configInfo = configManager.getConfigService().getConfig(k8sConfig.getConfigFile(), configManager.getNacosConfigProperties().getGroup(), 3000L);
        String kubeConfigFile = getKubeConfigFile(configInfo, "/kubeconfig");
         //修改环境变量，重新指定kubeconfig读取位置
        System.setProperty(Config.KUBERNETES_KUBECONFIG_FILE, kubeConfigFile);
        var client = new DefaultKubernetesClient();
         //打印集群信息
        log.info("ApiVersion:{}", client.getApiVersion());
        log.info("MasterUrl:{}", client.getMasterUrl());
        try
        {
            log.info("VersionInfo:{}", JSONObject.toJSON(client.getKubernetesVersion()));
        }
        catch(Exception err){

        }
        return client;
    }

    private String getKubeConfigFile(String config, String kubeConfig) {
        String path = System.getProperty(USER_DIR_SYSTEM_PROPERTY) + kubeConfig;
        path =path.replace("//","/");
        log.info("kubeconfig path:{}", path);
        FileUtil.writeBytes(config.getBytes(), path);
        return path;
    }

}
