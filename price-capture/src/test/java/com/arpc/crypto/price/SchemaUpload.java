package com.arpc.crypto.price;

import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SchemaUpload {

        public static void registerSchema(String schemaRegistryUrl, String schemaFilePath) throws Exception {
            String schemaPayload = new String(Files.readAllBytes(Paths.get("src", "main","proto",schemaFilePath)));

            JSONObject schemaRequest = new JSONObject();
            schemaRequest.put("schema", schemaPayload);
            schemaRequest.put("schemaType", "PROTOBUF");

            String url = schemaRegistryUrl + "/subjects/orders/versions";
            String contentType = "application/vnd.schemaregistry.v1+json";


            try (var client = HttpClient.newHttpClient()) {
                var request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .method("POST", HttpRequest.BodyPublishers.ofString(schemaRequest.toString()))
                        .header("Content-Type", contentType)
                        .build();
                var response = client.send(request, HttpResponse.BodyHandlers.discarding());
                if (response.statusCode() != 200) {
                    throw new Exception("Failed to register schema: " + response.statusCode());
                }
            }
        }
}
