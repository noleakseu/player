package trudy;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpObject;

public class App {

    static ResponseService response = null;
    static ProxyService proxy = null;
    static String APP_NAME;
    static String APP_VERSION;
    static String APP_VENDOR;

    private static void showIntro() {
        System.out.println(APP_NAME + " v." + APP_VERSION + " " + APP_VENDOR);
    }

    private static void showError() {
        System.out.println("Arguments required: <host> <port>, i.e. java -jar player-" + APP_VERSION + ".jar 0.0.0.0 8080");
    }

    public static void main(String[] args) {
        try {
            APP_NAME = null != App.class.getPackage().getImplementationTitle() ? App.class.getPackage().getImplementationTitle() : "title";
            APP_VERSION = null != App.class.getPackage().getImplementationVersion() ? App.class.getPackage().getImplementationVersion() : "version";
            APP_VENDOR = null != App.class.getPackage().getImplementationVendor() ? App.class.getPackage().getImplementationVendor() : "vendor";
            showIntro();

            if (args.length != 2) {
                showError();
                System.exit(0);
            }
            int proxyPort = Integer.parseInt(args[1]);
            String proxyHost = args[0];

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (null != response) {
                    response.stop();
                }
                if (null != proxy) {
                    proxy.stop();
                }
            }));

            response = new ResponseService();
            proxy = new ProxyService() {
                @Override
                public HttpObject onExchange(FullHttpRequest request, FullHttpResponse response) {
                    Exchange exchange = new Exchange(request, response);
                    HttpObject intercepted = exchange.replace(App.response.getMatchingResponse(request.getUri()));
                    System.out.println(exchange);
                    return intercepted;
                }
            };

            response.start();
            System.out.println("Found response " + response.load(System.getProperty("user.dir")));
            proxy.start(proxyHost, proxyPort);
            System.out.println("Intercepting requests on " + proxyHost + ":" + proxyPort + ", hit Ctrl+C to stop");
        } catch (Exception e) {
            System.err.println("Error occurred: " + e.getMessage());
        }
    }
}
