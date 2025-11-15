package es.upm.cervezas.service;

import es.upm.cervezas.api.dto.MenuResponse;
import es.upm.cervezas.domain.User;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MenuService {

    private final TokenAuthenticationService tokenAuthenticationService;

    public MenuService(TokenAuthenticationService tokenAuthenticationService) {
        this.tokenAuthenticationService = tokenAuthenticationService;
    }

    public MenuResponse buildMenu(String token) {
        User user = tokenAuthenticationService.findUserByToken(token).orElse(null);
        if (user == null) {
            return new MenuResponse(false, false, false, false,
                    List.of("Registrarse", "Verificar Edad", "Activar Cuenta", "Iniciar Sesión"));
        }

        boolean profileCompleted = user.getCity() != null && user.getCountry() != null;
        boolean canShareBeer = user.isActivated();
        boolean canLogTasting = user.isActivated();

        List<String> actions = new ArrayList<>();
        if (!user.isActivated()) {
            actions.add("Reenviar activación");
        } else {
            actions.add("Crear degustación");
            actions.add("Valorar cerveza");
            actions.add("Consultar cervezas");
        }

        if (!profileCompleted) {
            actions.add("Completar perfil");
        }

        return new MenuResponse(
                user.isActivated(),
                profileCompleted,
                canShareBeer,
                canLogTasting,
                actions
        );
    }
}
