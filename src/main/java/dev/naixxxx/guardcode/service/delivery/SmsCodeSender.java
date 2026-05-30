package dev.naixxxx.guardcode.service.delivery;

import dev.naixxxx.guardcode.config.AppSettings;
import dev.naixxxx.guardcode.domain.DeliveryChannel;
import org.jsmpp.bean.*;
import org.jsmpp.session.BindParameter;
import org.jsmpp.session.SMPPSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class SmsCodeSender implements CodeSender {
    private static final Logger log = LoggerFactory.getLogger(SmsCodeSender.class);
    private final String host, systemId, password, systemType, sourceAddress;
    private final int port;

    public SmsCodeSender(AppSettings settings) {
        this.host = settings.get("smpp.host", "localhost");
        this.port = settings.getInt("smpp.port", 2775);
        this.systemId = settings.get("smpp.system_id");
        this.password = settings.get("smpp.password");
        this.systemType = settings.get("smpp.system_type", "OTP");
        this.sourceAddress = settings.get("smpp.source_addr", "Guardian");
    }

    @Override public DeliveryChannel channel() { return DeliveryChannel.SMS; }

    @Override public void send(String destination, String code) {
        SMPPSession session = new SMPPSession();
        try {
            session.connectAndBind(host, port, new BindParameter(
                    BindType.BIND_TX, systemId, password, systemType,
                    TypeOfNumber.UNKNOWN, NumberingPlanIndicator.UNKNOWN, sourceAddress));
            session.submitShortMessage(
                    systemType,
                    TypeOfNumber.UNKNOWN, NumberingPlanIndicator.UNKNOWN, sourceAddress,
                    TypeOfNumber.UNKNOWN, NumberingPlanIndicator.UNKNOWN, destination,
                    new ESMClass(), (byte) 0, (byte) 1, null, null,
                    new RegisteredDelivery(SMSCDeliveryReceipt.DEFAULT), (byte) 0,
                    new GeneralDataCoding(Alphabet.ALPHA_DEFAULT), (byte) 0,
                    ("Your OTP code: " + code).getBytes(StandardCharsets.UTF_8));
            log.info("OTP SMS sent to {}", destination);
        } catch (Exception e) { throw new DeliveryException("SMPP delivery failed", e); }
        finally { try { session.unbindAndClose(); } catch (Exception ignored) {} }
    }
}
