package dev.naixxxx.guardcode.service.delivery;

import dev.naixxxx.guardcode.domain.DeliveryChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;

public class FileCodeSender implements CodeSender {
    private static final Logger log = LoggerFactory.getLogger(FileCodeSender.class);
    private final Path output;

    public FileCodeSender(String path) { this.output = Path.of(path == null ? "otp-output.txt" : path); }
    @Override public DeliveryChannel channel() { return DeliveryChannel.FILE; }

    @Override public void send(String destination, String code) {
        String row = "%s destination=%s code=%s%n".formatted(LocalDateTime.now(), destination, code);
        try {
            Files.writeString(output, row, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            log.info("OTP saved to file {}", output.toAbsolutePath());
        } catch (Exception e) { throw new DeliveryException("File delivery failed", e); }
    }
}
