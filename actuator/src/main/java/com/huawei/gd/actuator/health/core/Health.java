package com.huawei.gd.actuator.health.core;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.huawei.gd.actuator.util.Assert;

/**
 * Carries information about the health of a component or subsystem.
 */
public final class Health extends HealthComponent {
    public static final String UNKNOWN = "UNKNOWN";

    public static final String UP = "UP";

    public static final String DOWN = "DOWN";

    public static final String OUT_OF_SERVICE = "OUT_OF_SERVICE";

    private final String status;

    private final Map<String, Object> details;

    /**
     * Create a new {@link Health} instance with the specified status and details.
     * 
     * @param builder the Builder to use
     */
    private Health(Builder builder) {
        Assert.notNull(builder, "Builder must not be null");
        this.status = builder.status;
        this.details = Collections.unmodifiableMap(builder.details);
    }

    Health(String status, Map<String, Object> details) {
        this.status = status;
        this.details = details;
    }

    /**
     * Create a new {@link Builder} instance with an {@link #UNKNOWN} status.
     * 
     * @return a new {@link Builder} instance
     */
    public static Builder unknown() {
        return status(UNKNOWN);
    }

    /**
     * Create a new {@link Builder} instance with an {@link #UP} status.
     * 
     * @return a new {@link Builder} instance
     */
    public static Builder up() {
        return status(UP);
    }

    /**
     * Create a new {@link Builder} instance with an {@link #DOWN} status and the specified exception details.
     * 
     * @param ex the exception
     * @return a new {@link Builder} instance
     */
    public static Builder down(Exception ex) {
        return down().withException(ex);
    }

    /**
     * Create a new {@link Builder} instance with a {@link #DOWN} status.
     * 
     * @return a new {@link Builder} instance
     */
    public static Builder down() {
        return status(DOWN);
    }

    /**
     * Create a new {@link Builder} instance with an {@link #OUT_OF_SERVICE} status.
     * 
     * @return a new {@link Builder} instance
     */
    public static Builder outOfService() {
        return status(OUT_OF_SERVICE);
    }


    /**
     * @param status the status
     * @return a new {@link Builder} instance
     */
    public static Builder status(String status) {
        return new Builder(status);
    }

    /**
     * Return the status of the health.
     * 
     * @return the status (never {@code null})
     */
    @Override
    public String getStatus() {
        return this.status;
    }

    /**
     * Return the details of the health.
     * 
     * @return the details (or an empty map)
     */
    public Map<String, Object> getDetails() {
        return this.details;
    }

    /**
     * Return a new instance of this {@link Health} with all {@link #getDetails() details} removed.
     * 
     * @return a new instance without details
     * @since 2.2.0
     */
    Health withoutDetails() {
        if (this.details.isEmpty()) {
            return this;
        }
        return status(getStatus()).build();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof Health) {
            Health other = (Health) obj;
            return this.status.equals(other.status) && this.details.equals(other.details);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hashCode = this.status.hashCode();
        return 13 * hashCode + this.details.hashCode();
    }

    @Override
    public String toString() {
        return getStatus() + " " + getDetails();
    }

    /**
     * Builder for creating immutable {@link Health} instances.
     */
    public static class Builder {

        private String status;

        private Map<String, Object> details;

        /**
         * Create new Builder instance.
         */
        public Builder() {
            this.status = UNKNOWN;
            this.details = new LinkedHashMap();
        }

        /**
         * Create new Builder instance, setting status to given {@code status}.
         *
         */
        public Builder(String status) {
            Assert.notNull(status, "Status must not be null");
            this.status = status;
            this.details = new LinkedHashMap();
        }

        /**
         * Create new Builder instance, setting status to given {@code status} and details to given {@code details}.
         *
         */
        public Builder(String status, Map<String, ?> details) {
            Assert.notNull(status, "Status must not be null");
            Assert.notNull(details, "Details must not be null");
            this.status = status;
            this.details = new LinkedHashMap(details);
        }

        /**
         * Record detail for given {@link Exception}.
         * 
         * @param ex the exception
         * @return this {@link Builder} instance
         */
        public Builder withException(Throwable ex) {
            Assert.notNull(ex, "Exception must not be null");
            return withDetail("error", ex.getClass().getName() + ": " + ex.getMessage());
        }

        /**
         * Record detail using given {@code key} and {@code value}.
         * 
         * @param key the detail key
         * @param value the detail value
         * @return this {@link Builder} instance
         */
        public Builder withDetail(String key, Object value) {
            Assert.notNull(key, "Key must not be null");
            Assert.notNull(value, "Value must not be null");
            this.details.put(key, value);
            return this;
        }

        /**
         * Record details from the given {@code details} map. Keys from the given map replace any existing keys if there
         * are duplicates.
         * 
         * @param details map of details
         * @return this {@link Builder} instance
         * @since 2.1.0
         */
        public Builder withDetails(Map<String, ?> details) {
            Assert.notNull(details, "Details must not be null");
            this.details.putAll(details);
            return this;
        }

        /**
         * Set status to {@link #UNKNOWN} status.
         * 
         * @return this {@link Builder} instance
         */
        public Builder unknown() {
            return status(UNKNOWN);
        }

        /**
         * Set status to {@link #UP} status.
         * 
         * @return this {@link Builder} instance
         */
        public Builder up() {
            return status(UP);
        }

        /**
         * Set status to {@link #DOWN} and add details for given {@link Throwable}.
         * 
         * @param ex the exception
         * @return this {@link Builder} instance
         */
        public Builder down(Throwable ex) {
            return down().withException(ex);
        }

        /**
         * Set status to {@link #DOWN}.
         * 
         * @return this {@link Builder} instance
         */
        public Builder down() {
            return status(DOWN);
        }

        /**
         * Set status to {@link #OUT_OF_SERVICE}.
         * 
         * @return this {@link Builder} instance
         */
        public Builder outOfService() {
            return status(OUT_OF_SERVICE);
        }

        /**
         * @param status the status
         * @return this {@link Builder} instance
         */
        public Builder status(String status) {
            this.status = status;
            return this;
        }

        /**
         * Create a new {@link Health} instance with the previously specified code and details.
         * 
         * @return a new {@link Health} instance
         */
        public Health build() {
            return new Health(this);
        }
    }
}
