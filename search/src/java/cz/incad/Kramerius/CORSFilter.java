package cz.incad.Kramerius;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CORSFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) res;
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "OPTIONS, POST, GET, PUT, DELETE");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers", "x-requested-with" +
                ",content-type" + //for API HTTP POSTs
                ",authorization" + //for basic access authentication (deprecated)
                ",access-token, client, uid" //for authorization by account service
        );
        chain.doFilter(req, res);
    }

    @Override
    public void destroy() {

    }
}
