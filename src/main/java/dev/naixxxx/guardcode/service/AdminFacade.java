package dev.naixxxx.guardcode.service;

import dev.naixxxx.guardcode.dao.OtpPolicyRepository;
import dev.naixxxx.guardcode.dao.UserRepository;
import dev.naixxxx.guardcode.dto.AdminDtos;

import java.util.List;

public class AdminFacade {
    private final OtpPolicyRepository policyRepo;
    private final UserRepository userRepo;

    public AdminFacade(OtpPolicyRepository policyRepo, UserRepository userRepo) {
        this.policyRepo = policyRepo; this.userRepo = userRepo;
    }

    public AdminDtos.PolicyResponse getPolicy() {
        var p = policyRepo.get();
        return new AdminDtos.PolicyResponse(p.codeLength(), p.lifetimeSeconds());
    }

    public AdminDtos.PolicyResponse updatePolicy(AdminDtos.PolicyRequest req) {
        if (req.codeLength() < 4 || req.codeLength() > 10) throw new ServiceException(400, "Code length must be 4..10");
        if (req.lifetimeSeconds() < 30 || req.lifetimeSeconds() > 3600) throw new ServiceException(400, "Lifetime must be 30..3600 seconds");
        var p = policyRepo.update(req.codeLength(), req.lifetimeSeconds());
        return new AdminDtos.PolicyResponse(p.codeLength(), p.lifetimeSeconds());
    }

    public List<AdminDtos.UserView> users() {
        return userRepo.listNonAdmins().stream()
                .map(u -> new AdminDtos.UserView(u.id(), u.login(), u.role(), u.createdAt()))
                .toList();
    }

    public void deleteUser(long id) {
        if (!userRepo.deleteRegularUser(id)) throw new ServiceException(404, "Regular user not found");
    }
}
