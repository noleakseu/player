package trudy;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class ResponseService {

    static final String EXT = ".trudy";
    private static final String FILENAME = "responses" + EXT;
    private static final Type TYPE_TOKEN = new TypeToken<ArrayList<Response>>() {}.getType();
    private static final Gson GSON = new GsonBuilder().registerTypeHierarchyAdapter(byte[].class, new ByteArrayToBase64TypeAdapter()).create();

    private static ArrayList<Response> instance;

    private static final class ByteArrayToBase64TypeAdapter implements JsonSerializer<byte[]>, JsonDeserializer<byte[]> {
        public byte[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return Base64.getDecoder().decode(json.getAsString());
        }

        public JsonElement serialize(byte[] src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(Base64.getEncoder().encodeToString(src));
        }
    }

    ResponseService() {
    }

    ResponseService start() throws IOException {
        if (null == instance) {
            final Path path = Paths.get(FILENAME);
            if (Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
                try (InputStreamReader reader = new InputStreamReader(new FileInputStream(FILENAME), StandardCharsets.UTF_8)) {
                    instance = GSON.fromJson(new JsonReader(reader), TYPE_TOKEN);
                }
            } else {
                instance = new ArrayList<>();
            }
        }
        return this;
    }

    Response getMatchingResponse(String uri) {
        if (null != uri) {
            return instance.stream().filter(item -> uri.startsWith(item.getUri())).findFirst().orElse(null);
        }
        return null;
    }

    ResponseService remove(String uri) throws IOException {
        if (null != uri) {
            instance.removeIf(item -> (uri.equals(item.getUri())));
            save();
        }
        return this;
    }

    ResponseService update(Response response) throws IOException {
        if (null != response) {
            instance.removeIf(item -> (response.getUri().equals(item.getUri())));
            instance.add(response);
            save();
        }
        return this;
    }

    List<Response> getAll() {
        return instance;
    }

    ResponseService save(String path, List<Response> items) throws IOException {
        if (null != path && null != items) {
            try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(path), StandardCharsets.UTF_8)) {
                GSON.toJson(items, TYPE_TOKEN, new JsonWriter(writer));
            }
        }
        return this;
    }

    String load(String path) throws IOException {
        String file;
        try (Stream<Path> paths = Files.walk(Paths.get(path))) {
            List<String> files = paths
                    .map(Path::toString)
                    .filter(name -> name.endsWith(EXT))
                    .collect(Collectors.toList());
            if (!files.isEmpty()) {
                file = files.get(0);
                try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
                    instance = GSON.fromJson(new JsonReader(reader), TYPE_TOKEN);
                }
                if (null == instance) {
                    throw new IOException("unknown format of " + file);
                }
            } else {
                throw new NoSuchElementException("No *" + EXT + " file found");
            }
        } catch (NoSuchElementException e) {
            throw new IOException(e.getMessage());
        }
        return file;
    }

    private void save() throws IOException {
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(FILENAME), StandardCharsets.UTF_8)) {
            GSON.toJson(instance, TYPE_TOKEN, new JsonWriter(writer));
        }
    }

    void stop() {
        instance = null;
    }
}
