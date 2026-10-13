package com.finc.platform.service;

import com.finc.platform.entity.Channel;
import com.finc.platform.entity.ChannelType;
import com.finc.platform.entity.RoleType;
import com.finc.platform.entity.User;
import com.finc.platform.repository.UserRepository;
import org.springframework.stereotype.Service;

// Who is chatting and what they may see or change. The actor id comes from the verified login token.
@Service
public class ChatAccess {

    private final UserRepository userRepository;

    public ChatAccess(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User actor(Long actorId) {
        if (actorId == null) {
            throw new SecurityException("Login required");
        }
        return userRepository.findById(actorId)
                .orElseThrow(() -> new SecurityException("Unknown user"));
    }

    // The name stored as a message's sender; the chat page compares it with the logged-in username.
    public String senderName(User user) {
        return user.getUsername();
    }

    public boolean isAdmin(User user) {
        RoleType role = user.getRole() == null ? null : user.getRole().getRoleType();
        return role == RoleType.ROLE_SYSTEM_ADMIN || role == RoleType.ROLE_DEPT_ADMIN;
    }

    public boolean isMember(Channel channel, User user) {
        String email = user.getEmail();
        return email != null && channel.getMemberEmails() != null && channel.getMemberEmails().stream()
                .anyMatch(member -> member != null && member.trim().equalsIgnoreCase(email.trim()));
    }

    // Public channels are open to every logged-in user; group chats and direct messages only to members.
    public boolean canView(Channel channel, User user) {
        return channel.getType() == ChannelType.PUBLIC || isMember(channel, user);
    }

    // Admins manage public channels; members run their own group chats; direct messages never change.
    public boolean canManage(Channel channel, User user) {
        if (channel.getType() == ChannelType.DIRECT) {
            return false;
        }
        if (channel.getType() == ChannelType.PUBLIC) {
            return isAdmin(user);
        }
        return isAdmin(user) || isMember(channel, user);
    }

    public void requireView(Channel channel, User user) {
        if (!canView(channel, user)) {
            throw new SecurityException("You are not a member of this conversation");
        }
    }

    public void requireManage(Channel channel, User user) {
        if (!canManage(channel, user)) {
            throw new SecurityException("You are not allowed to manage this conversation");
        }
    }
}
