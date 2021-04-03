package com.huawei.gd.actuator.health.indicaor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.huawei.gd.actuator.health.core.Health;
import com.huawei.gd.actuator.health.core.HealthIndicator;

public class DiskSpaceHealthIndicatorTest {
    private static final long THRESHOLD = 1L;

    private static final long TOTAL_SPACE = 10L;
    @Mock
    private File fileMock;

    private HealthIndicator healthIndicator;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        given(this.fileMock.exists()).willReturn(true);
        given(this.fileMock.canRead()).willReturn(true);
        this.healthIndicator = new DiskSpaceHealthIndicator(this.fileMock, THRESHOLD);
    }

    @Test
    public void diskSpaceIsUp() {
        long freeSpace = THRESHOLD + 1;
        given(this.fileMock.getUsableSpace()).willReturn(freeSpace);
        given(this.fileMock.getTotalSpace()).willReturn(TOTAL_SPACE);
        Health health = this.healthIndicator.health();
        assertThat(health.getStatus()).isEqualTo(Health.UP);
        assertThat(health.getDetails().get("threshold")).isEqualTo(THRESHOLD);
        assertThat(health.getDetails().get("free")).isEqualTo(freeSpace);
        assertThat(health.getDetails().get("total")).isEqualTo(TOTAL_SPACE);
    }

    @Test
    public void diskSpaceIsDown() {
        long freeSpace = THRESHOLD - 10;
        given(this.fileMock.getUsableSpace()).willReturn(freeSpace);
        given(this.fileMock.getTotalSpace()).willReturn(TOTAL_SPACE);
        Health health = this.healthIndicator.health();
        assertThat(health.getStatus()).isEqualTo(Health.DOWN);
        assertThat(health.getDetails().get("threshold")).isEqualTo(THRESHOLD);
        assertThat(health.getDetails().get("free")).isEqualTo(freeSpace);
        assertThat(health.getDetails().get("total")).isEqualTo(TOTAL_SPACE);
    }

    @Test
    public void getName() {
        assertThat(this.healthIndicator.getName()).isEqualTo(DiskSpaceHealthIndicator.DISK_SPACE);
    }
}
