package dev.naixxxx.guardcode.service.delivery;

import dev.naixxxx.guardcode.domain.DeliveryChannel;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class SenderRegistry {
    private final Map<DeliveryChannel, CodeSender> senders = new EnumMap<>(DeliveryChannel.class);
    public SenderRegistry(List<CodeSender> senderList) {
        senderList.forEach(s -> senders.put(s.channel(), s));
    }
    public CodeSender get(DeliveryChannel channel) {
        CodeSender sender = senders.get(channel);
        if (sender == null) throw new IllegalArgumentException("Unsupported channel: " + channel);
        return sender;
    }
}
