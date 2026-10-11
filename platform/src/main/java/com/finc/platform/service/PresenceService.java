package com.finc.platform.service;

import com.finc.platform.entity.User;
import com.finc.platform.entity.UserStatus;
import com.finc.platform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PresenceService {

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public void heartbeat(Long userId) {
        userRepository.findById(userId).ifPresent(u -> {
            u.setLastSeen(LocalDateTime.now());
            if (u.getStatus() != UserStatus.ONLINE) {
                u.setStatus(UserStatus.ONLINE);
            }
            userRepository.save(u);
        });
    }

    @Transactional
    public int sweepStaleUsers(int awayAfterMinutes, int offlineAfterMinutes) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime awayCutoff = now.minusMinutes(awayAfterMinutes);
        LocalDateTime offlineCutoff = now.minusMinutes(offlineAfterMinutes);

        List<User> active = userRepository.findByStatusIn(
                List.of(UserStatus.ONLINE, UserStatus.AWAY, UserStatus.BUSY));

        int changed = 0;
        for (User u : active) {
            LocalDateTime seen = u.getLastSeen() != null ? u.getLastSeen() : u.getCreatedAt();
            if (seen == null) continue;

            if (seen.isBefore(offlineCutoff)) {
                if (u.getStatus() != UserStatus.OFFLINE) {
                    u.setStatus(UserStatus.OFFLINE);
                    userRepository.save(u);
                    changed++;
                }
            } else if (seen.isBefore(awayCutoff)) {
                if (u.getStatus() != UserStatus.AWAY) {
                    u.setStatus(UserStatus.AWAY);
                    userRepository.save(u);
                    changed++;
                }
            }
        }
        return changed;
    }
}