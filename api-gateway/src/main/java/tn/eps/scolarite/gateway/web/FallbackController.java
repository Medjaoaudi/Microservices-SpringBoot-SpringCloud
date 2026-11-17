package tn.eps.scolarite.gateway.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Cible des "fallbackUri: forward:/fallback/{service}" du filtre CircuitBreaker :
 * renvoie un JSON propre (503) au lieu d'une erreur brute quand un service est hors ligne.
 */
@Slf4j
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @RequestMapping("/{service}")
    public Mono<ResponseEntity<Map<String, Object>>> fallback(@PathVariable String service,
                                                              ServerWebExchange exchange) {
        Throwable cause = exchange.getAttribute(ServerWebExchangeUtils.CIRCUITBREAKER_EXECUTION_EXCEPTION_ATTR);
        log.warn("[GATEWAY-FALLBACK] {} indisponible - cause : {}", service,
                cause != null ? cause.toString() : "inconnue");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        body.put("error", HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase());
        body.put("message", "Le service " + service.toUpperCase()
                + " est momentanément indisponible. Merci de réessayer dans quelques instants.");
        body.put("path", exchange.getRequest().getURI().getPath());
        body.put("service", service);
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body));
    }
}
