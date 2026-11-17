package tn.eps.scolarite.gateway.filters;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Filtre global : trace chaque requête traversant la Gateway
 * (méthode, chemin, route choisie, statut HTTP et temps de réponse).
 */
@Slf4j
@Component
public class RequestLoggingGlobalFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long debut = System.currentTimeMillis();
        var requete = exchange.getRequest();
        return chain.filter(exchange).doFinally(signal -> {
            Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
            log.info("[GATEWAY] {} {} -> route={} statut={} ({} ms)",
                    requete.getMethod(),
                    requete.getURI().getRawPath(),
                    route != null ? route.getId() : "-",
                    exchange.getResponse().getStatusCode(),
                    System.currentTimeMillis() - debut);
        });
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
