package com.example.demo.auth.infra;

import com.example.demo.user.entity.Role;
import com.example.demo.user.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class SessionManager {
  private final FindByIndexNameSessionRepository<? extends Session> sessionRepository;

  // ----------------------------------------------------------------------------------------------

  public void createSession(HttpServletRequest req, User user) {
    Set<Role> roles = user.roles();
    var authorities =
        roles.stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
            .collect(Collectors.toUnmodifiableSet());

    SecurityContext securityContext = SecurityContextHolder.getContext();
    Authentication authentication =
        new UsernamePasswordAuthenticationToken(user.username(), null, authorities);
    securityContext.setAuthentication(authentication);

    HttpSession session = req.getSession(true);
    session.setAttribute(
        HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);
  }

  public String getUsername() {
    return SecurityContextHolder.getContext().getAuthentication().getName();
  }

  public void invalidateSession(HttpServletRequest req) {
    HttpSession session = req.getSession(false);
    if (session != null) {
      session.invalidate();
    }
    SecurityContextHolder.clearContext();
  }

  // TODO 現在のセッション以外にしたほうがいいかな？
  public void invalidateAllSessionsForCurrentUser() {
    String username = getUsername();
    sessionRepository
        .findByPrincipalName(username)
        .forEach((id, session) -> sessionRepository.deleteById(id));
  }
}
