package tech.comfortheart;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import tech.comfortheart.app.JobRunner;
import tech.comfortheart.util.EncryptUtil;

@Controller
public class PasswordEncryptionController {
    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/encrypt")
    @ResponseBody public String encrypt(@RequestBody String plainText) {
        try {
            EncryptUtil.KeystoreAndCert keyInfo = EncryptUtil.getKeystoreAndCert(JobRunner.APP_ID_FILE, JobRunner.KEYSTORE_FILE_NAME, JobRunner.CERT_FILENAME);
            return EncryptUtil.encrypt(keyInfo.getCertPath(), plainText);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
