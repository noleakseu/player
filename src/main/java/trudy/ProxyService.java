package trudy;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersAdapter;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;

import java.net.InetSocketAddress;

abstract class ProxyService {

    private static HttpProxyServer instance;

    ProxyService() {
    }

    void start(String host, int port) {
        if (!isStarted()) {
            instance = DefaultHttpProxyServer
                    .bootstrap()
                    .withAddress(new InetSocketAddress(host, port))
                    //.withTransparent(true)
                    .withFiltersSource(new HttpFiltersSourceAdapter() {
                                           @Override
                                           public int getMaximumResponseBufferSizeInBytes() {
                                               return 10 * 1024 * 1024;
                                           }

                                           @Override
                                           public int getMaximumRequestBufferSizeInBytes() {
                                               return 10 * 1024 * 1024;
                                           }

                                           @Override
                                           public HttpFilters filterRequest(HttpRequest request, ChannelHandlerContext ctx) {
                                               return new HttpFiltersAdapter(request) {
                                                   @Override
                                                   public HttpResponse proxyToServerRequest(HttpObject httpObject) {
                                                       return super.proxyToServerRequest(httpObject);
                                                   }

                                                   @Override
                                                   public HttpObject serverToProxyResponse(HttpObject response) {
                                                       if (response instanceof FullHttpResponse && request instanceof FullHttpRequest) {
                                                           return onExchange((FullHttpRequest) request, (FullHttpResponse) response);
                                                       }
                                                       return response;
                                                   }
                                               };
                                           }
                                       }
                    )
                    .start();
        }
    }

    abstract HttpObject onExchange(FullHttpRequest request, FullHttpResponse response);

    boolean isStarted() {
        return null != instance;
    }

    void stop() {
        if (isStarted()) {
            instance.stop();
            instance = null;
        }
    }
}
