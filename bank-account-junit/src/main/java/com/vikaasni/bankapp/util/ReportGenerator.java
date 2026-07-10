package com.vikaasni.bankapp.util;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ReportGenerator {

    public static void main(String[] args) {
        System.out.println("Generating test analysis report...");
        
        Path surefireDir = Paths.get("target", "surefire-reports");
        Path jacocoXmlPath = Paths.get("target", "site", "jacoco", "jacoco.xml");
        Path outputDir = Paths.get("dashboard");
        Path outputFile = outputDir.resolve("data.json");

        if (!Files.exists(surefireDir)) {
            System.err.println("Error: target/surefire-reports directory not found. Please run 'mvn test' first.");
            System.exit(1);
        }

        try {
            // Create output directory if it doesn't exist
            Files.createDirectories(outputDir);

            StringBuilder json = new StringBuilder();
            json.append("{\n");

            // 1. Build Metadata
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            json.append("  \"metadata\": {\n");
            json.append("    \"timestamp\": \"").append(timestamp).append("\"\n");
            json.append("  },\n");

            // 2. Parse Test Suites
            parseTestSuites(surefireDir.toFile(), json);
            json.append(",\n");

            // 3. Parse Code Coverage (JaCoCo)
            parseCoverage(jacocoXmlPath.toFile(), json);

            json.append("\n}");

            // Write output file
            try (FileWriter writer = new FileWriter(outputFile.toFile())) {
                writer.write(json.toString());
            }

            System.out.println("Report successfully written to: " + outputFile.toAbsolutePath());

        } catch (Exception e) {
            System.err.println("Failed to generate report: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void parseTestSuites(File dir, StringBuilder json) throws Exception {
        File[] files = dir.listFiles((d, name) -> name.startsWith("TEST-") && name.endsWith(".xml"));
        if (files == null || files.length == 0) {
            json.append("  \"testRun\": {\n");
            json.append("    \"totalTests\": 0,\n");
            json.append("    \"successes\": 0,\n");
            json.append("    \"failures\": 0,\n");
            json.append("    \"errors\": 0,\n");
            json.append("    \"skipped\": 0,\n");
            json.append("    \"timeSeconds\": 0.0,\n");
            json.append("    \"testSuites\": []\n");
            json.append("  }");
            return;
        }

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        // Disable DTD loading to avoid parser exceptions on jacoco/surefire xmls
        dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

        int totalTests = 0;
        int totalFailures = 0;
        int totalErrors = 0;
        int totalSkipped = 0;
        double totalTime = 0.0;

        List<String> suitesJson = new ArrayList<>();

        for (File file : files) {
            Document doc = dBuilder.parse(file);
            doc.getDocumentElement().normalize();

            Element root = doc.getDocumentElement();
            String name = root.getAttribute("name");
            int tests = Integer.parseInt(root.getAttribute("tests"));
            int failures = Integer.parseInt(root.getAttribute("failures"));
            int errors = root.hasAttribute("errors") ? Integer.parseInt(root.getAttribute("errors")) : 0;
            int skipped = root.hasAttribute("skipped") ? Integer.parseInt(root.getAttribute("skipped")) : 0;
            double time = Double.parseDouble(root.getAttribute("time"));

            totalTests += tests;
            totalFailures += failures;
            totalErrors += errors;
            totalSkipped += skipped;
            totalTime += time;

            StringBuilder suite = new StringBuilder();
            suite.append("    {\n");
            suite.append("      \"name\": \"").append(escapeJson(name)).append("\",\n");
            suite.append("      \"tests\": ").append(tests).append(",\n");
            suite.append("      \"failures\": ").append(failures).append(",\n");
            suite.append("      \"errors\": ").append(errors).append(",\n");
            suite.append("      \"skipped\": ").append(skipped).append(",\n");
            suite.append("      \"time\": ").append(time).append(",\n");
            suite.append("      \"cases\": [\n");

            NodeList caseNodes = doc.getElementsByTagName("testcase");
            List<String> casesJson = new ArrayList<>();
            for (int i = 0; i < caseNodes.getLength(); i++) {
                Element cElem = (Element) caseNodes.item(i);
                String caseName = cElem.getAttribute("name");
                double caseTime = cElem.hasAttribute("time") && !cElem.getAttribute("time").isEmpty()
                        ? Double.parseDouble(cElem.getAttribute("time")) : 0.0;

                String status = "SUCCESS";
                String errorMessage = "";

                if (cElem.getElementsByTagName("failure").getLength() > 0) {
                    status = "FAILURE";
                    Element fail = (Element) cElem.getElementsByTagName("failure").item(0);
                    errorMessage = fail.getAttribute("message");
                    if (errorMessage.isEmpty()) {
                        errorMessage = fail.getTextContent();
                    }
                } else if (cElem.getElementsByTagName("error").getLength() > 0) {
                    status = "ERROR";
                    Element err = (Element) cElem.getElementsByTagName("error").item(0);
                    errorMessage = err.getAttribute("message");
                    if (errorMessage.isEmpty()) {
                        errorMessage = err.getTextContent();
                    }
                } else if (cElem.getElementsByTagName("skipped").getLength() > 0) {
                    status = "SKIPPED";
                }

                StringBuilder kase = new StringBuilder();
                kase.append("        {\n");
                kase.append("          \"name\": \"").append(escapeJson(caseName)).append("\",\n");
                kase.append("          \"time\": ").append(caseTime).append(",\n");
                kase.append("          \"status\": \"").append(status).append("\"");
                if (!errorMessage.isEmpty()) {
                    kase.append(",\n          \"message\": \"").append(escapeJson(errorMessage)).append("\"");
                }
                kase.append("\n        }");
                casesJson.add(kase.toString());
            }
            suite.append(String.join(",\n", casesJson));
            suite.append("\n      ]\n");
            suite.append("    }");
            suitesJson.add(suite.toString());
        }

        int totalSuccesses = totalTests - totalFailures - totalErrors - totalSkipped;

        json.append("  \"testRun\": {\n");
        json.append("    \"totalTests\": ").append(totalTests).append(",\n");
        json.append("    \"successes\": ").append(totalSuccesses).append(",\n");
        json.append("    \"failures\": ").append(totalFailures).append(",\n");
        json.append("    \"errors\": ").append(totalErrors).append(",\n");
        json.append("    \"skipped\": ").append(totalSkipped).append(",\n");
        json.append("    \"timeSeconds\": ").append(totalTime).append(",\n");
        json.append("    \"testSuites\": [\n");
        json.append(String.join(",\n", suitesJson));
        json.append("\n    ]\n");
        json.append("  }");
    }

    private static void parseCoverage(File file, StringBuilder json) {
        json.append("  \"coverage\": ");
        if (!file.exists()) {
            json.append("null");
            return;
        }

        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);
            doc.getDocumentElement().normalize();

            Element root = doc.getDocumentElement();
            NodeList topCounters = root.getChildNodes();
            
            StringBuilder globalStats = new StringBuilder();
            globalStats.append("{\n");

            List<String> countersJson = new ArrayList<>();
            for (int i = 0; i < topCounters.getLength(); i++) {
                if (topCounters.item(i) instanceof Element) {
                    Element item = (Element) topCounters.item(i);
                    if ("counter".equals(item.getTagName())) {
                        String type = item.getAttribute("type").toLowerCase();
                        int missed = Integer.parseInt(item.getAttribute("missed"));
                        int covered = Integer.parseInt(item.getAttribute("covered"));
                        int total = missed + covered;
                        double percentage = total == 0 ? 0.0 : ((double) covered / total) * 100.0;

                        StringBuilder counter = new StringBuilder();
                        counter.append("    \"").append(type).append("\": {\n");
                        counter.append("      \"covered\": ").append(covered).append(",\n");
                        counter.append("      \"missed\": ").append(missed).append(",\n");
                        counter.append("      \"percentage\": ").append(Math.round(percentage * 100.0) / 100.0).append("\n");
                        counter.append("    }");
                        countersJson.add(counter.toString());
                    }
                }
            }
            globalStats.append(String.join(",\n", countersJson));

            // Parse Packages Coverage
            NodeList pkgNodes = root.getElementsByTagName("package");
            List<String> pkgsJson = new ArrayList<>();
            for (int i = 0; i < pkgNodes.getLength(); i++) {
                Element pkg = (Element) pkgNodes.item(i);
                String pkgName = pkg.getAttribute("name").replace("/", ".");
                
                NodeList pkgChildren = pkg.getChildNodes();
                int lineCovered = 0, lineMissed = 0;
                for (int j = 0; j < pkgChildren.getLength(); j++) {
                    if (pkgChildren.item(j) instanceof Element) {
                        Element item = (Element) pkgChildren.item(j);
                        if ("counter".equals(item.getTagName()) && "LINE".equals(item.getAttribute("type"))) {
                            lineMissed = Integer.parseInt(item.getAttribute("missed"));
                            lineCovered = Integer.parseInt(item.getAttribute("covered"));
                            break;
                        }
                    }
                }
                int lineTotal = lineCovered + lineMissed;
                double linePct = lineTotal == 0 ? 0.0 : ((double) lineCovered / lineTotal) * 100.0;

                StringBuilder pkgJson = new StringBuilder();
                pkgJson.append("      {\n");
                pkgJson.append("        \"name\": \"").append(escapeJson(pkgName)).append("\",\n");
                pkgJson.append("        \"covered\": ").append(lineCovered).append(",\n");
                pkgJson.append("        \"missed\": ").append(lineMissed).append(",\n");
                pkgJson.append("        \"percentage\": ").append(Math.round(linePct * 100.0) / 100.0).append("\n");
                pkgJson.append("      }");
                pkgsJson.add(pkgJson.toString());
            }

            globalStats.append(",\n    \"packages\": [\n");
            globalStats.append(String.join(",\n", pkgsJson));
            globalStats.append("\n    ]\n");

            globalStats.append("  }");
            json.append(globalStats);

        } catch (Exception e) {
            System.err.println("Warning: Could not parse coverage file: " + e.getMessage());
            json.append("null");
        }
    }

    private static String escapeJson(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
