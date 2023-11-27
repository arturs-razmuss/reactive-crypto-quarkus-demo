package com.arpc.crypto.price;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@QuarkusTest
@Disabled
public class PublishProtoSchemaTest {

    @Test
    void shouldPublishProtoSchema() throws Exception {
        String schemaRegistryUrl = "http://127.0.0.1:8081";
        String schemaFilePath = "orders.proto";

        SchemaUpload.registerSchema(schemaRegistryUrl, schemaFilePath);
    }
}
