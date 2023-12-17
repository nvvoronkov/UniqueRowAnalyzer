package org.example;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.util.*;

public class UniqueRowAnalyzerApp {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java -jar UniqueRowAnalyzerApp.jar тестовый-файл.txt");
            return;
        }

        String fileName = args[0];

        try {
            String fileContent = downloadFile("https://github.com/PeacockTeam/new-job/releases/download/v1.0/lng-4.txt.gz");
            processFile(fileContent, fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String downloadFile(String fileUrl) throws IOException {
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpGet request = new HttpGet(fileUrl);
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                HttpEntity entity = response.getEntity();

                if (entity != null) {
                    return EntityUtils.toString(entity);
                } else {
                    throw new IOException("Failed to download the file");
                }
            }
        }
    }

    private static void processFile(String fileContent, String fileName) {
        try (BufferedReader reader = new BufferedReader(new StringReader(fileContent))) {
            Map<String, Set<String>> groups = new HashMap<>();
            String line;

            while ((line = reader.readLine()) != null) {
                if (isValidRow(line)) {
                    addToGroup(groups, line);
                }
            }

            List<Map.Entry<String, Set<String>>> sortedGroups = new ArrayList<>(groups.entrySet());
            sortedGroups.sort((a, b) -> Integer.compare(b.getValue().size(), a.getValue().size()));

            int countGroups = 0;

            try (BufferedWriter writer = new BufferedWriter(new FileWriter("result.txt"))) {
                for (Map.Entry<String, Set<String>> entry : sortedGroups) {
                    Set<String> group = entry.getValue();
                    if (group.size() > 1) {
                        countGroups++;
                        writer.write("Группа " + countGroups + "\n");
                        for (String row : group) {
                            writer.write(row + "\n");
                        }
                        writer.write("\n");
                    }
                }
            }

            System.out.println("Количество групп с более чем одним элементом: " + countGroups);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean isValidRow(String line) {
        String regex = "^[^;]+(;[^;]+)*$";
        return line.matches(regex) && !line.contains("\"");
    }

    private static void addToGroup(Map<String, Set<String>> groups, String line) {
        for (Map.Entry<String, Set<String>> entry : groups.entrySet()) {
            String groupKey = entry.getKey();
            Set<String> group = entry.getValue();

            String[] newRow = line.split(";");
            String[] groupElements = groupKey.split(";");

            boolean belongsToGroup = false;

            for (int i = 0; i < newRow.length; i++) {
                if (!newRow[i].isEmpty() && !groupElements[i].isEmpty() && newRow[i].equals(groupElements[i])) {
                    belongsToGroup = true;
                    break;
                }
            }

            if (belongsToGroup) {
                group.add(line);
                return;
            }
        }

        Set<String> newGroup = new HashSet<>();
        newGroup.add(line);
        groups.put(line, newGroup);
    }
}
