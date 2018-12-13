package xin.aitech.eckg.thrift.searchKS.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * @Description:
 * @author: Hugo Ng
 * @Date: 17-7-4
 */
public class LoadConfig {
    private static Logger _logger = LoggerFactory.getLogger(LoadConfig.class);
    private static final String configPath = System.getProperty("user.dir") + "/config/searchKS.properties";
    private static BufferedInputStream bufferedInputStream;
    private static ResourceBundle resourceBundle;

    static {
        try {
            bufferedInputStream = new BufferedInputStream(new FileInputStream(configPath));
            resourceBundle = new PropertyResourceBundle(bufferedInputStream);
            bufferedInputStream.close();
        } catch (FileNotFoundException e) {
            _logger.error("Couldn't find the property file");
            System.exit(0);
        } catch (IOException e) {
            _logger.error("Couldn't find the property file");
            System.exit(0);
        } catch (Exception e) {
            _logger.error(e.getMessage(), e);
            System.exit(0);
        }
    }

    public static ResourceBundle getResourceBundle() {
        return resourceBundle;
    }
}
