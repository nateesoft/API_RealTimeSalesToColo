package util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Thread-safe application logger.
 * Writes INFO-level to logs/info-YYYY-MM-DD.log
 * Writes WARNING/SEVERE to logs/error-YYYY-MM-DD.log
 * Rotates files daily automatically.
 */
public class AppLogUtil {

    private static final String LOG_DIR = "logs";
    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("yyyy-MM-dd");
    private static final Logger LOGGER = Logger.getLogger("AppLog");

    private static FileHandler infoHandler;
    private static FileHandler errorHandler;
    private static String currentLogDate = "";

    static {
        LOGGER.setUseParentHandlers(false);
        rotate();
    }

    private static synchronized void rotate() {
        String today = DATE_FMT.format(new Date());
        if (today.equals(currentLogDate)) {
            return;
        }
        currentLogDate = today;
        try {
            Files.createDirectories(Paths.get(LOG_DIR));

            if (infoHandler != null) {
                LOGGER.removeHandler(infoHandler);
                infoHandler.close();
            }
            if (errorHandler != null) {
                LOGGER.removeHandler(errorHandler);
                errorHandler.close();
            }

            // info-YYYY-MM-DD.log — INFO only (filtered out WARNING/SEVERE)
            infoHandler = new FileHandler(LOG_DIR + "/info-" + today + ".log", true);
            infoHandler.setFormatter(new LogLineFormatter());
            infoHandler.setLevel(Level.INFO);
            infoHandler.setFilter(r -> r.getLevel().intValue() < Level.WARNING.intValue());

            // error-YYYY-MM-DD.log — WARNING and SEVERE
            errorHandler = new FileHandler(LOG_DIR + "/error-" + today + ".log", true);
            errorHandler.setFormatter(new LogLineFormatter());
            errorHandler.setLevel(Level.WARNING);

            LOGGER.addHandler(infoHandler);
            LOGGER.addHandler(errorHandler);
            LOGGER.setLevel(Level.ALL);
        } catch (IOException e) {
            System.err.println("[AppLogUtil] ไม่สามารถสร้างไฟล์ log: " + e.getMessage());
        }
    }

    // ── Public API ────────────────────────────────────────────────────────────

    public static void info(Class<?> caller, String message) {
        rotate();
        System.out.println("[INFO] [" + caller.getSimpleName() + "] " + message);
        LOGGER.log(record(Level.INFO, caller, message, null));
    }

    public static void error(Class<?> caller, String message, Throwable t) {
        rotate();
        System.err.println("[ERROR] [" + caller.getSimpleName() + "] " + message);
        LOGGER.log(record(Level.SEVERE, caller, message, t));
    }

    public static void warning(Class<?> caller, String message) {
        rotate();
        System.out.println("[WARN] [" + caller.getSimpleName() + "] " + message);
        LOGGER.log(record(Level.WARNING, caller, message, null));
    }

    /** compat: เรียกแบบเดิม log(Class, "error"/"warning"/"info", Exception) */
    public static void log(Class<?> caller, String type, Exception e) {
        rotate();
        Level level;
        switch (type) {
            case "error":   level = Level.SEVERE;  break;
            case "warning": level = Level.WARNING; break;
            default:        level = Level.INFO;    break;
        }
        if (level == Level.SEVERE) {
            System.err.println("[ERROR] [" + caller.getSimpleName() + "] " + e.getMessage());
        } else {
            System.out.println("[" + type.toUpperCase() + "] [" + caller.getSimpleName() + "] " + e.getMessage());
        }
        LOGGER.log(record(level, caller, e.getMessage(), e));
    }

    // ── Internals ─────────────────────────────────────────────────────────────

    private static LogRecord record(Level level, Class<?> caller, String msg, Throwable t) {
        LogRecord r = new LogRecord(level, msg != null ? msg : "(null)");
        r.setSourceClassName(caller.getSimpleName());
        r.setSourceMethodName("");
        if (t != null) {
            r.setThrown(t);
        }
        return r;
    }

    static class LogLineFormatter extends Formatter {
        private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        @Override
        public String format(LogRecord r) {
            String ts = LocalDateTime.now().format(TS);
            String cls = r.getSourceClassName() != null ? r.getSourceClassName() : "-";
            String msg = r.getMessage() != null ? r.getMessage() : "";

            StringBuilder sb = new StringBuilder();
            sb.append("[").append(ts).append("]")
              .append(" [").append(r.getLevel().getName()).append("]")
              .append(" [").append(cls).append("] ")
              .append(msg).append("\n");

            if (r.getThrown() != null) {
                StringWriter sw = new StringWriter();
                r.getThrown().printStackTrace(new PrintWriter(sw));
                sb.append(sw);
            }
            return sb.toString();
        }
    }

    // ── Legacy helper ─────────────────────────────────────────────────────────

    public static void htmlFile(String htmlText) {
        try {
            java.io.File f = new java.io.File("source.html");
            try (java.io.BufferedWriter bw = new java.io.BufferedWriter(new java.io.FileWriter(f))) {
                bw.write(htmlText);
            }
        } catch (IOException e) {
            error(AppLogUtil.class, "htmlFile failed", e);
        }
    }
}
