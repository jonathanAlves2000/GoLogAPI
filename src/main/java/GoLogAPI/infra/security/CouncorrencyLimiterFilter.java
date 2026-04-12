package GoLogAPI.infra.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.Semaphore;

@Component
public class CouncorrencyLimiterFilter implements Filter{

    private final Semaphore semaphore = new Semaphore(1000); // Máximo de 10 requisições simultâneas

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        boolean acquired = semaphore.tryAcquire();
        if (!acquired) {
            ((HttpServletResponse) response).setStatus(HttpServletResponse.SC_REQUEST_TIMEOUT);
            response.getWriter().write("Muitas requisições simultâneas. Tente novamente.");
            return;
        }
        try {
            chain.doFilter(request, response);
        } finally {
            semaphore.release();
        }
    }
}
