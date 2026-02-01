package util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 *
 * @author nathee
 */
public class AppLogUtil {

    static int countRunning = 0;
    static SimpleDateFormat logDateFmt = new SimpleDateFormat("yyyy-MM-dd");
    static FileHandler fh = null;
    static FileHandler fhHtml = null;

    public static void log(Class t, String type, Exception e) {
        Logger logger = Logger.getLogger(t.getName());
        if (fh == null) {
            try {
                fh = new FileHandler("application-" + logDateFmt.format(new Date()) + ".log", true);
            } catch (IOException ex) {
            }
        }

        if (fh != null) {
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);

            String msg = e.getMessage();
            int lineNumber = e.getStackTrace()[0].getLineNumber();

            switch (type) {
                case "error":
                    logger.log(Level.SEVERE, "{0}:{1}:{2}", new Object[]{msg, t.getName(), lineNumber});
                    break;
                case "warning":
                    logger.log(Level.WARNING, "{0}:{1}:{2}", new Object[]{msg, t.getName(), lineNumber});
                    logger.warning(msg);
                    break;
                default:
                    logger.log(Level.INFO, "{0}:{1}:{2}", new Object[]{msg, t.getName(), lineNumber});
                    logger.info(msg);
                    break;
            }
        }
    }

    public static void htmlFile(String htmlText) {
        countRunning++;
        try {
            File f = new File("source_" + countRunning + ".html");
            try ( BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {
                bw.write(htmlText);
                bw.flush();
                bw.close();
            }
        } catch (IOException e) {
        }

    }
}
