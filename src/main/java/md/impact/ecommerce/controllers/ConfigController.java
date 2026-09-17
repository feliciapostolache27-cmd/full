package md.impact.ecommerce.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import md.impact.ecommerce.dtos.ConfigInfoResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Lecția 6 — expune profilul Spring activ, ca să se vadă din frontend că
 * aceeași aplicație rulează diferit pe dev vs. prod, fără nicio schimbare de
 * cod (doar de config).
 */
@RestController
@RequestMapping("/api/config")
@Tag(name = "Config", description = "Informații despre mediul de rulare curent (Lecția 6 — 12-Factor App)")
public class ConfigController {

    private final Environment environment;
    private final String appName;

    public ConfigController(Environment environment, @Value("${spring.application.name}") String appName) {
        this.environment = environment;
        this.appName = appName;
    }

    @Operation(summary = "Profilul Spring activ (dev/prod) și numele aplicației")
    @GetMapping("/info")
    public ConfigInfoResponse info() {
        String[] activeProfiles = environment.getActiveProfiles();
        String profile = activeProfiles.length > 0 ? activeProfiles[0] : "default";
        return new ConfigInfoResponse(appName, profile);
    }
}
