package storageSpecs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Configuration {
    private double maxSizeLimit;
    private List<String> forbiddenExtensions;
    private Map<String, Integer> fileCountLimits;

    public Configuration() {
        maxSizeLimit = 16106127360.0;
        forbiddenExtensions = new ArrayList<>();
        fileCountLimits = new HashMap<>();
    }

    public Configuration(double maxSizeLimit, List<String> ForbiddenExtensions, Map<String, Integer> fileCountLimits) {
        this.maxSizeLimit = maxSizeLimit;
        this.forbiddenExtensions = ForbiddenExtensions;
        this.fileCountLimits = fileCountLimits;
    }

    public Configuration(double maxSizeLimit, List<String> ForbiddenExtensions) {
        this.maxSizeLimit = maxSizeLimit;
        this.forbiddenExtensions = ForbiddenExtensions;
        this.fileCountLimits = new HashMap<>();
    }

    public double getMaxSizeLimit() {
        return maxSizeLimit;
    }

    public void setMaxSizeLimit(double maxSizeLimit) {
        this.maxSizeLimit = maxSizeLimit;
    }

    public List<String> getForbiddenExtensions() {
        return forbiddenExtensions;
    }

    public void setForbiddenExtensions(List<String> forbiddenExtensions) {
        this.forbiddenExtensions = forbiddenExtensions;
    }

    public Map<String, Integer> getFileCountLimits() {
        return fileCountLimits;
    }

    public void setFileCountLimits(Map<String, Integer> fileCountLimits) {
        this.fileCountLimits = fileCountLimits;
    }
}
