package com.jaha.server.emaul.model;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by doring on 15. 5. 15..
 */
@Entity
@Table(name = "traffic_cache", indexes = {
        @Index(name = "idx_traffic_cache", columnList = "cacheDate")
})
public class TrafficCache {
    @Id
    public String cacheKey;

    @Column(columnDefinition = "TEXT NOT NULL")
    public String json;

    @Column(nullable = false)
    public Date cacheDate;

    @Column(nullable = false)
    public Integer expireMinutes;
}
