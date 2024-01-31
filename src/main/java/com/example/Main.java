package com.example;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClients;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Main {

    private final static String ACCESS_TOKEN = "test-key-1";
    private final static String PROJECT_ID = "41ec7395-eeda-4c8e-bc73-5585d48e5161";
    private final static String PRIMARY_KEY = "tax_code";
    private final static int MAX_CHANGES = 150;
    private final static boolean EMULATION = true;

    public static void main(String[] args) {
        String command = args[0]; //create or read
        
        if (args.length < 2) {
            System.out.println("Usage:");
            System.out.println("java -jar your-jar-file.jar create file.csv");
            System.out.println("java -jar your-jar-file.jar read task-id");
            return;
        }

        try {
            if ("create".equals(args[0])) {
                String taskId = createTask(args[1]);
                System.out.println("Created Task ID: " + taskId);
            }
            else if ("read".equals(args[0])) {
                readTask(args[1]);
            }
            else {
                System.out.println("Action must be 'create' or 'read'");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String createTask(String filePath) throws IOException {
        HttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("https://service.4hse.com/people-sync/create");

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();

        // Add file parameter
        builder.addBinaryBody("file", new File(filePath), ContentType.APPLICATION_OCTET_STREAM, "file.csv");

        // Add other parameters
        builder.addTextBody("access-token", ACCESS_TOKEN);
        builder.addTextBody("project_id", PROJECT_ID);
        builder.addTextBody("pk", PRIMARY_KEY);
        builder.addTextBody("emulation", String.valueOf(EMULATION));
        builder.addTextBody("max_changes", String.valueOf(MAX_CHANGES));

        HttpEntity multipart = builder.build();
        httpPost.setEntity(multipart);

        // Execute the request
        HttpResponse response = httpClient.execute(httpPost);

        // Parse the response JSON
        JSONParser parser = new JSONParser();
        try (InputStream content = response.getEntity().getContent()) {
            JSONObject jsonResponse = (JSONObject) parser.parse(new InputStreamReader(content));
            return (String) jsonResponse.get("task_id");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void readTask(String taskId) throws IOException {
        HttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet("https://service.4hse.com/people-sync/view?id=" + taskId + "&access-token=" + ACCESS_TOKEN);

        // Execute the request
        HttpResponse response = httpClient.execute(httpGet);

        // Parse and print the response JSON
        JSONParser parser = new JSONParser();
        try (InputStream content = response.getEntity().getContent()) {
            JSONObject jsonResponse = (JSONObject) parser.parse(new InputStreamReader(content));
            System.out.println("Task: " + jsonResponse.toJSONString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
