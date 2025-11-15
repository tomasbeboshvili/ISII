package es.upm.cervezas.api.controller;

import es.upm.cervezas.api.dto.MenuResponse;
import es.upm.cervezas.service.MenuService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/menu")
public class MenuController {

    private static final Logger log = LoggerFactory.getLogger(MenuController.class);

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping
    public MenuResponse menu(@RequestHeader(value = "X-Auth-Token", required = false) String token) {
        log.debug("Generando menú para token {}", token);
        return menuService.buildMenu(token);
    }
}
