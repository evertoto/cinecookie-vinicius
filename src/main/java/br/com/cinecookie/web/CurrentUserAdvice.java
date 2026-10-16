package br.com.cinecookie.web;

import br.com.cinecookie.user.UserService;
import br.com.cinecookie.user.UserService.CurrentUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(assignableTypes = {PageController.class, MovieController.class})
public class CurrentUserAdvice {
    private final UserService users;

    public CurrentUserAdvice(UserService users) {
        this.users = users;
    }

    @ModelAttribute("currentUser")
    public CurrentUser currentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || !(session.getAttribute("userId") instanceof Long userId)) {
            return null;
        }
        CurrentUser user = users.findCurrentUser(userId).orElse(null);
        if (user == null) {
            session.invalidate();
        }
        return user;
    }
}
