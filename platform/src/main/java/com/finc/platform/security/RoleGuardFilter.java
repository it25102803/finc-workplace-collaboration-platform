package com.finc.platform.security;

import com.finc.platform.entity.RoleType;
import com.finc.platform.entity.User;
import com.finc.platform.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@Component
public class RoleGuardFilter extends OncePerRequestFilter {

    @Autowired
    private UserRepository userRepository;

    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/forgot-password",
            "/api/auth/reset-password",
            "/api/auth/validate-reset-token",
            "/api/auth/logout"
    );

    private static final Set<String> PUBLIC_READ_PREFIXES = Set.of(
            "/api/departments",
            "/api/presence",
            "/api/public"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws ServletException, IOException {

        String path = req.getRequestURI();
        String method = req.getMethod();

        if (!path.startsWith("/api/")) {
            chain.doFilter(req, res);
            return;
        }

        if (PUBLIC_PATHS.contains(path)) {
            chain.doFilter(req, res);
            return;
        }

        boolean mutating = "POST".equals(method) || "PUT".equals(method)
                || "PATCH".equals(method) || "DELETE".equals(method);

        if ("GET".equals(method)) {
            for (String p : PUBLIC_READ_PREFIXES) {
                if (path.startsWith(p)) {
                    chain.doFilter(req, res);
                    return;
                }
            }
        }

        Long actorId = resolveActorId(req);
        if (actorId == null) {
            deny(res, "actorId is required (login required)");
            return;
        }
        User actor = userRepository.findById(actorId).orElse(null);
        if (actor == null) {
            deny(res, "Unknown user: " + actorId);
            return;
        }

        if (Boolean.FALSE.equals(actor.getEnabled())) {
            deny(res, "Your account has been disabled. Contact the administrator.");
            return;
        }

        RoleType role = actor.getRole() != null ? actor.getRole().getRoleType() : null;
        if (role == null) {
            deny(res, "Your account has no role assigned");
            return;
        }

        if (path.matches("^/api/users/\\d+/(profile|change-password|profile-picture)$")) {
            Long pathId = extractIdFromPath(path);
            if (pathId != null && !pathId.equals(actorId)) {
                deny(res, "You can only modify your own profile");
                return;
            }
            chain.doFilter(req, res);
            return;
        }

        if ("GET".equals(method) && "/api/users".equals(path)) {
            if (role != RoleType.ROLE_SYSTEM_ADMIN) {
                deny(res, "Access denied: SYSTEM_ADMIN role required to view the user directory");
                return;
            }
        }

        if (path.startsWith("/api/admin/")) {
            if (role != RoleType.ROLE_SYSTEM_ADMIN) {
                deny(res, "Access denied: SYSTEM_ADMIN role required");
                return;
            }
        }
        else if (path.startsWith("/api/departments") && mutating) {
            if (role != RoleType.ROLE_SYSTEM_ADMIN) {
                deny(res, "Access denied: SYSTEM_ADMIN role required to modify departments");
                return;
            }
        }
        else if (path.startsWith("/api/kb") && mutating) {
            if (role != RoleType.ROLE_SYSTEM_ADMIN && role != RoleType.ROLE_DEPT_ADMIN) {
                deny(res, "Access denied: DEPARTMENT_ADMIN or SYSTEM_ADMIN required");
                return;
            }
        }
        else if (path.startsWith("/api/tasks") && mutating) {
            boolean progressOrStatus = path.matches(".*/(status|progress|comments|attachments)(/\\d+)?$");
            if (!progressOrStatus) {
                if (role != RoleType.ROLE_SYSTEM_ADMIN
                        && role != RoleType.ROLE_DEPT_ADMIN
                        && role != RoleType.ROLE_TEAM_LEADER) {
                    deny(res, "Access denied: TEAM_LEADER or above required to manage tasks");
                    return;
                }
            }
        }

        chain.doFilter(req, res);
    }

    private Long resolveActorId(HttpServletRequest req) {
        String param = req.getParameter("actorId");
        if (param != null && !param.isBlank()) {
            try { return Long.parseLong(param); } catch (NumberFormatException ignored) {}
        }
        param = req.getParameter("userId");
        if (param != null && !param.isBlank()) {
            try { return Long.parseLong(param); } catch (NumberFormatException ignored) {}
        }
        String header = req.getHeader("X-User-Id");
        if (header != null && !header.isBlank()) {
            try { return Long.parseLong(header); } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    private Long extractIdFromPath(String path) {
        String[] parts = path.split("/");
        for (String p : parts) {
            try { return Long.parseLong(p); } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    private void deny(HttpServletResponse res, String message) throws IOException {
        res.setStatus(HttpServletResponse.SC_FORBIDDEN);
        res.setContentType("application/json");
        res.getWriter().write("{\"error\":\"" + message + "\"}");
    }
}