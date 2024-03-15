package org.jeecg.modules.license.application.impl;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.api.config.ConfigService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.license.application.LicenseApplication;
import org.jeecg.modules.license.dto.License;
import org.jeecg.modules.license.dto.LicenseActivationRequest;
import org.jeecg.modules.license.dto.LicenseStatusResponse;
import org.jeecg.modules.monitor.application.K8sApplication;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDate;
import java.util.Base64;
import java.util.Optional;

/**
 * @author jiangyan
 */
@Slf4j
@Service
public class LicenseApplicationImpl implements LicenseApplication {

    private final ConfigService configService;

    private static final String LICENSE_DATA_ID = "license.lic";

    private static final String RSA = "RSA";

    private static final String PUBLIC_KEY_FILE = "/publicKeys.pem";

    private final K8sApplication k8sApplication;

    private final NacosConfigProperties nacosConfigProperties;

    public LicenseApplicationImpl(NacosConfigProperties nacosConfigProperties, NacosConfigManager configManager, K8sApplication k8sApplication) {
        this.configService = configManager.getConfigService();
        this.k8sApplication = k8sApplication;
        this.nacosConfigProperties = nacosConfigProperties;
    }

    @SneakyThrows
    @Override
    public LicenseStatusResponse getStatus() {
        LicenseStatusResponse response = new LicenseStatusResponse();
        //查询序列号
        response.setSeriesNo(k8sApplication.getUuidByDefaultNode());
        //查询是否有license文件
        var config = configService.getConfig(LICENSE_DATA_ID,nacosConfigProperties.getGroup(), 6000);
        if(config == null){
            response.setStatus(0);
            return response;
        }
        try{
            byte[] bytes = Base64.getDecoder().decode(config);
            var license = getLicense(response.getSeriesNo(), bytes);
            //如果针对license过期的情况, 则设置状态为-1
            if(license.getExpiryTime().isBefore(LocalDate.now())){
                response.setStatus(-1);
            }
            else{
                response.setStatus(1);
            }
            response.setInfo(license);

        }
        catch(SecurityException err){
            response.setStatus(-2);
        }

        //查询license文件的状态
        return response;
    }

    @SneakyThrows
    @Override
    public void activation(LicenseActivationRequest request) {
        byte[] bytes = Base64.getDecoder().decode(request.getFile());
        var license = getLicense(k8sApplication.getUuidByDefaultNode(), bytes);
        if(license.getExpiryTime().isBefore(LocalDate.now())){
           throw new SecurityException("License过期，无法完成激活操作");
        }
        configService.publishConfig(LICENSE_DATA_ID,nacosConfigProperties.getGroup(), request.getFile());
    }

    @SneakyThrows
    @Override
    public Optional<License> getLicense() {
        var config = configService.getConfig(LICENSE_DATA_ID,null, 6000);
        if(config == null){
            return Optional.empty();
        }
        try {
            var license = getLicense(k8sApplication.getUuidByDefaultNode(), Base64.getDecoder().decode(config));
            return Optional.ofNullable(license);
        }
        catch(Exception err){
            return Optional.empty();
        }
    }

    public License getLicense(String seriesNo, byte[] licenseData) throws Exception {
        var license = new String(licenseData, StandardCharsets.UTF_8);
        String[] parts = license.split("\n");
        if (parts.length != 2) {
            throw new SecurityException("license文件无效");
        }
        String licenseJson = parts[0];
        byte[] signatureBytes = Base64.getDecoder().decode(parts[1]);
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(loadPublicKey());
        signature.update(licenseJson.getBytes(StandardCharsets.UTF_8));
        if(signature.verify(signatureBytes)){
            var data = JSONObject.parseObject(licenseJson, License.class);
            if(!data.getSubject().equals(seriesNo)){
                throw new SecurityException("license无效");
            }
            return data;
        }
        throw new SecurityException("license文件无效");
    }

    private PublicKey loadPublicKey() throws Exception {
        var stream = getClass().getResourceAsStream(PUBLIC_KEY_FILE);
        byte[] keyBytes = stream.readAllBytes();
        stream.close();
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory factory = KeyFactory.getInstance(RSA);
        return factory.generatePublic(spec);
    }

}
