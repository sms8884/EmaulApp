package com.jaha.server.emaul.model;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by doring on 15. 5. 20..
 */
@Entity
@Table(name = "parcel_notification", indexes = {
        @Index(name = "idx_parcel_apt_id", columnList = "aptId"),
        @Index(name = "idx_parcel_apt_dong", columnList = "dong"),
        @Index(name = "idx_parcel_apt_ho", columnList = "ho"),
        @Index(name = "idx_parcel_sent_date", columnList = "sentDate")
})
public class ParcelNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long id;

    @Column(length = 200)
    public String message;

    public Long aptId;
    @Column(length = 10)
    public String dong;
    @Column(length = 10)
    public String ho;

    public Date sentDate;

    @Column(length = 100)
    public String imageUrl;

    public Boolean notifySuccess;
    public Boolean visible;

}
