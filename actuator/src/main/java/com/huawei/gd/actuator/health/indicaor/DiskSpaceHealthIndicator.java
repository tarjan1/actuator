package com.huawei.gd.actuator.health.indicaor;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.huawei.gd.actuator.health.core.AbstractHealthIndicator;
import com.huawei.gd.actuator.health.core.Health;
import com.huawei.gd.actuator.health.core.HealthIndicator;


/**
 * A {@link HealthIndicator} that checks available disk space and reports a status of {@link Health#DOWN} when it drops
 * below a configurable threshold.
 */
public class DiskSpaceHealthIndicator extends AbstractHealthIndicator {

    public static final String DISK_SPACE = "diskSpace";
    private static final Log LOGGER = LogFactory.getLog(DiskSpaceHealthIndicator.class);

    private final File path;

    private long threshold = 1024 * 1024 * 1024;// default 1G

    /**
     * Create a new {@code DiskSpaceHealthIndicator} instance.
     * 
     * @param path the Path used to compute the available disk space
     * @param threshold the minimum disk space that should be available
     */
    public DiskSpaceHealthIndicator(File path, long threshold) {
        super();
        this.path = path;
        this.threshold = threshold;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        long diskFreeInBytes = this.path.getUsableSpace();
        if (diskFreeInBytes >= this.threshold) {
            builder.up();
        } else {
            LOGGER.warn("Free disk space below threshold. Available: " + diskFreeInBytes);
            LOGGER.warn("threshold(default_1G):" + this.threshold);
            builder.down();
        }
        builder.withDetail("total", this.path.getTotalSpace()).withDetail("free", diskFreeInBytes)
                .withDetail("threshold", this.threshold);
    }

    @Override
    public String getName() {
        return DISK_SPACE;
    }
}
