package com.univocity.trader.notification;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface NotForExport {
    // Marker interface to avoid exporting to a google sheets
}
