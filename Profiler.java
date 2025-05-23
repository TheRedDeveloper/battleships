import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

/** 
 * Simple profiling utility to measure execution times of different parts of the code.
 * 
 * @author GitHub Copilot
 */
public class Profiler {
    private static final Map<String, Long> startTimes = new HashMap<>();
    private static final Map<String, Long> totalTimes = new HashMap<>();
    private static final Map<String, Integer> callCounts = new HashMap<>();
    
    /**
     * Start measuring time for a specific section.
     * 
     * @param section The name of the section to measure
     */
    public static void start(String section) {
        startTimes.put(section, System.nanoTime());
    }
    
    /**
     * Stop measuring time for a specific section and record the elapsed time.
     * 
     * @param section The name of the section to measure
     */
    public static void stop(String section) {
        if (!startTimes.containsKey(section)) {
            Game.LOGGER.warning("Trying to stop profiling for section that wasn't started: " + section);
            return;
        }
        
        long elapsedNanos = System.nanoTime() - startTimes.get(section);
        totalTimes.put(section, totalTimes.getOrDefault(section, 0L) + elapsedNanos);
        callCounts.put(section, callCounts.getOrDefault(section, 0) + 1);
    }
    
    /**
     * Log profiling results to the game logger.
     */
    public static void logResults() {
        if (totalTimes.isEmpty()) {
            Game.LOGGER.info("No profiling data available.");
            return;
        }
        
        // Sort by total time (descending)
        TreeMap<String, Long> sortedResults = new TreeMap<>((s1, s2) -> {
            int compare = Long.compare(totalTimes.getOrDefault(s2, 0L), 
                                       totalTimes.getOrDefault(s1, 0L));
            return compare != 0 ? compare : s1.compareTo(s2);
        });
        sortedResults.putAll(totalTimes);
        
        StringBuilder sb = new StringBuilder("\n===== PROFILING RESULTS =====\n");
        sb.append(String.format("%-40s %15s %15s %15s\n", 
                  "Section", "Total (ms)", "Calls", "Avg (ms)"));
        sb.append("---------------------------------------------------------------------------------\n");
        
        for (Map.Entry<String, Long> entry : sortedResults.entrySet()) {
            String section = entry.getKey();
            long totalNanos = entry.getValue();
            int count = callCounts.getOrDefault(section, 0);
            long totalMs = TimeUnit.NANOSECONDS.toMillis(totalNanos);
            double avgMs = count > 0 ? (double)totalMs / count : 0;
            
            sb.append(String.format("%-40s %15d %15d %15.2f\n", 
                      section, totalMs, count, avgMs));
        }
        
        sb.append("===== END PROFILING RESULTS =====");
        Game.LOGGER.info(sb.toString());
    }
    
    /**
     * Reset all profiling data.
     */
    public static void reset() {
        startTimes.clear();
        totalTimes.clear();
        callCounts.clear();
    }
}
