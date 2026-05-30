package dev.naixxxx.guardcode.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class OtpExpiryWorker implements AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(OtpExpiryWorker.class);
    private final OtpFacade otpFacade;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "otp-expiry-worker");
        t.setDaemon(true);
        return t;
    });

    public OtpExpiryWorker(OtpFacade otpFacade) { this.otpFacade = otpFacade; }

    public void start(long intervalSeconds) {
        executor.scheduleAtFixedRate(() -> {
            try {
                int changed = otpFacade.expireOverdue();
                if (changed > 0) log.info("Expired {} OTP codes", changed);
            } catch (Exception e) { log.error("OTP expiration job failed", e); }
        }, intervalSeconds, intervalSeconds, TimeUnit.SECONDS);
    }

    @Override public void close() { executor.shutdownNow(); }
}
