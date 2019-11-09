package trudy;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

final class Util {

    public static HttpResponse getResponse(int status, List<Header> headers, byte[] body) {
        ByteBuf buffer = Unpooled.wrappedBuffer(body);
        HttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.valueOf(status), buffer);
        headers.forEach(header -> HttpHeaders.setHeader(response, header.getName(), header.getValue()));
        HttpHeaders.setContentLength(response, buffer.readableBytes());
        HttpHeaders.setDate(response, new Date());
        return response;
    }

    public static HttpResponse getResponse(Response response) {
        return getResponse(response.getStatus(), response.getHeaders(), response.getBody());
    }

    public static List<Header> toHeaders(List<Map.Entry<String, String>> headers) {
        return headers
                .stream()
                .map(header -> new Header(header.getKey(), header.getValue()))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    public static List<Map.Entry<String, String>> toMap(List<Header> headers) {
        return headers
                .stream()
                .map(header -> new AbstractMap.SimpleEntry<>(header.getName(), header.getValue()))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    public static String byteToUtf(byte[] value) {
        if (null == value) {
            return "";
        }
        return new String(value, StandardCharsets.UTF_8);
    }

    public static String byteToHex(byte[] value) {
        StringBuilder sb = new StringBuilder();
        for (byte b : value) {
            sb.append(getHex(b)).append(" ");
        }
        return sb.toString();
    }

    public static String utfToHex(String utfString) {
        return byteToHex(utfString.getBytes(StandardCharsets.UTF_8));
    }

    public static String hexToUtf(String hexString) {
        return byteToUtf(hexToByte(hexString.replaceAll(" ", "")));
    }

    public static byte[] hexToByte(String hexString) {
        hexString = hexString.replaceAll(" ", "");
        if (hexString.length() % 2 == 1) {
            throw new IllegalArgumentException("Invalid hexadecimal string");
        }
        byte[] bytes = new byte[hexString.length() / 2];
        for (int i = 0; i < hexString.length(); i += 2) {
            bytes[i / 2] = getByte(hexString.substring(i, i + 2));
        }
        return bytes;
    }

    private static byte getByte(String hex) {
        int firstDigit = getByte(hex.charAt(0));
        int secondDigit = getByte(hex.charAt(1));
        return (byte) ((firstDigit << 4) + secondDigit);
    }

    private static int getByte(char hex) {
        int digit = Character.digit(hex, 16);
        if (digit == -1) {
            throw new IllegalArgumentException("Invalid hexadecimal char '" + hex + "'");
        }
        return digit;
    }

    private static String getHex(byte num) {
        char[] hex = new char[2];
        hex[0] = Character.forDigit((num >> 4) & 0xF, 16);
        hex[1] = Character.forDigit((num & 0xF), 16);
        return new String(hex);
    }
}
