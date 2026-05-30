package dev.naixxxx.guardcode;

import com.sun.net.httpserver.HttpServer;
import dev.naixxxx.guardcode.api.AdminHandler;
import dev.naixxxx.guardcode.api.AuthHandler;
import dev.naixxxx.guardcode.api.OtpHandler;
import dev.naixxxx.guardcode.config.AppSettings;
import dev.naixxxx.guardcode.config.Database;
import dev.naixxxx.guardcode.dao.OtpPolicyRepository;
import dev.naixxxx.guardcode.dao.OtpRepository;
import dev.naixxxx.guardcode.dao.UserRepository;
import dev.naixxxx.guardcode.domain.UserRole;
import dev.naixxxx.guardcode.security.AuthFilter;
import dev.naixxxx.guardcode.security.JwtService;
import dev.naixxxx.guardcode.security.Passwords;
import dev.naixxxx.guardcode.service.AdminFacade;
import dev.naixxxx.guardcode.service.AuthFacade;
import dev.naixxxx.guardcode.service.OtpExpiryWorker;
import dev.naixxxx.guardcode.service.OtpFacade;
import dev.naixxxx.guardcode.service.delivery.*;
import dev.naixxxx.guardcode.service.delivery.*;
import dev.naixxxx.guardcode.util.CodeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.Executors;

public class GuardCodeApplication {
    private static final Logger log = LoggerFactory.getLogger(GuardCodeApplication.class);

    public static void main(String[] args) throws Exception {
        AppSettings app = new AppSettings("application.properties");
        Database database = new Database(app);
        database.initSchema();

        UserRepository userRepo = new UserRepository(database.dataSource());
        OtpPolicyRepository policyRepo = new OtpPolicyRepository(database.dataSource());
        OtpRepository otpRepo = new OtpRepository(database.dataSource());

        JwtService jwt = new JwtService(app.get("jwt.secret"), app.getLong("jwt.ttl.minutes", 60));
        AuthFacade auth = new AuthFacade(userRepo, new Passwords(), jwt);

        SenderRegistry senders = new SenderRegistry(List.of(
                new MailCodeSender(new AppSettings("email.properties")),
                new SmsCodeSender(new AppSettings("sms.properties")),
                new TelegramCodeSender(new AppSettings("telegram.properties")),
                new FileCodeSender(app.get("otp.file.path", "otp-output.txt"))
        ));
        OtpFacade otp = new OtpFacade(policyRepo, otpRepo, senders, new CodeFactory());
        AdminFacade admin = new AdminFacade(policyRepo, userRepo);

        HttpServer server = HttpServer.create(new InetSocketAddress(app.getInt("server.port", 8080)), 0);
        server.setExecutor(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));
        server.createContext("/auth", new AuthHandler(auth));
        var otpContext = server.createContext("/otp", new OtpHandler(otp));
        otpContext.getFilters().add(new AuthFilter(jwt, UserRole.USER));
        var adminContext = server.createContext("/admin", new AdminHandler(admin));
        adminContext.getFilters().add(new AuthFilter(jwt, UserRole.ADMIN));

        OtpExpiryWorker worker = new OtpExpiryWorker(otp);
        worker.start(app.getLong("otp.expiration.scan.seconds", 30));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            worker.close();
            server.stop(1);
            database.close();
        }));

        server.start();
        log.info("OTP Guardian started on port {}", app.getInt("server.port", 8080));
    }
}