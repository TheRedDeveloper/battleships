import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/** Provides a custom, visually appealing log format for the Battleships game.
 * 
 * @author Claude */
public class GameLogFormatter extends Formatter {
    @Override
    public String format(LogRecord logRecord) {
        StringBuilder builder = new StringBuilder();
        
        String levelName = logRecord.getLevel().getName();
        switch (levelName) {
            case "INFO" -> builder.append(ANSI.GREEN).append("★ ").append(ANSI.RESET);
            case "WARNING" -> builder.append(ANSI.YELLOW).append("⚠ ").append(ANSI.RESET);
            case "SEVERE" -> builder.append(ANSI.RED).append("✖ ").append(ANSI.RESET);
            default -> builder.append(ANSI.CYAN).append("• ").append(ANSI.RESET);
        }
        
        String className = logRecord.getSourceClassName();
        String methodName = logRecord.getSourceMethodName();
        
        if (className != null && className.contains(".")) {
            className = className.substring(className.lastIndexOf('.') + 1);
        }
        
        if (className != null && className.contains("$")) {
            className = className.substring(0, className.indexOf('$'));
        }
        
        if (methodName != null && methodName.startsWith("lambda$")) {
            int dollarIndex = methodName.indexOf('$', 7);
            if (dollarIndex > 0) {
                String actualMethod = methodName.substring(7, dollarIndex);
                methodName = actualMethod + " (lambda)";
            }
        }
        
        builder.append(ANSI.CYAN)
               .append(className)
               .append(".")
               .append(methodName)
               .append(ANSI.RESET)
               .append(" | ");
        
        switch (levelName) {
            case "INFO" -> builder.append(ANSI.GREEN);
            case "WARNING" -> builder.append(ANSI.YELLOW);
            case "SEVERE" -> builder.append(ANSI.RED);
            default -> builder.append(ANSI.RESET);
        }
        
        builder.append(logRecord.getMessage())
               .append(ANSI.RESET)
               .append("\n");
        
        return builder.toString();
    }
}