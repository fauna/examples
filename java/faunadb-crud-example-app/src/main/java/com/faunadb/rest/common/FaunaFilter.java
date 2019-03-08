package com.faunadb.rest.common;

import com.faunadb.client.FaunaClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@Component
public class FaunaFilter implements Filter {

    private static final String LAST_TXN_TIME_HEADER_NAME = "X-Last-Txn-Time";

    private static final Logger logger = LoggerFactory.getLogger(FaunaFilter.class);

    @Autowired
    protected FaunaClient faunaClient;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        // Extract lastTxnTime from Request and sync Client
        Optional<String> lastTxnTimeHeader = Optional.ofNullable(request.getHeader(LAST_TXN_TIME_HEADER_NAME));
        lastTxnTimeHeader.ifPresent(lastTxnTime -> {
            try {
                Long.parseLong(lastTxnTime);
                // TODO: call client for synchronizing lastTxnTime when API is ready (ENG-812)
                // faunaClient.syncLastTxnTime(lastTxnTime);
            }
            catch(NumberFormatException e) {
                logger.warn("Invalid lastTxnTime value provided: [{}]", lastTxnTime);
            }
        });

        filterChain.doFilter(request, response);

        // TODO: call client for getting lastTxtTime when API is ready (ENG-812)
        // Get updated lastTxnTime from Client and add it into the Response
        long lastTnxTime = 1551987105592472L; // faunaClient.getSyncLastTxnTime();
        response.setHeader(LAST_TXN_TIME_HEADER_NAME, Long.toString(lastTnxTime));

    }
}
