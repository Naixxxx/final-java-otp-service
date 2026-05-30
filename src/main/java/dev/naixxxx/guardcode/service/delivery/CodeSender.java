package dev.naixxxx.guardcode.service.delivery;

import dev.naixxxx.guardcode.domain.DeliveryChannel;

public interface CodeSender {
    DeliveryChannel channel();
    void send(String destination, String code);
}
