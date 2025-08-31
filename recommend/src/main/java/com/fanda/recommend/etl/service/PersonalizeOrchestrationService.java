package com.fanda.recommend.etl.service;

import com.fanda.recommend.property.PersonalizeProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.personalize.PersonalizeClient;
import software.amazon.awssdk.services.personalize.model.*;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PersonalizeOrchestrationService {

    private final PersonalizeProperties props;

    private PersonalizeClient client(){
        return PersonalizeClient.builder().region(Region.of(props.getRegion())).build();
    }

    // csv header 와 1:1
    private static final String INTERACTIONS_SCHEMA = """
            {"type":"record","name":"Interactions","namespace":"com.amazonaws.personalize.schema",
             "fields":[
                {"name": "USER_ID","type": "string"},
                {"name": "ITEM_ID","type": "string"},
                {"name": "TIMESTAMP","type": "long"},
                {"name": "EVENT_TYPE","type": "string"},
                {"name": "EVENT_VALUE","type": ["null","float"]}
             ],"version":"1.0"}""";

    private static final String USERS_SCHEMA = """
            {
              "type": "record",
              "name": "Users",
              "namespace": "com.amazonaws.personalize.schema",
              "fields": [
                { "name": "USER_ID",            "type": "string" },
                { "name": "CUSTOMER_SEGMENT",   "type": ["null","string"], "categorical": true },
                { "name": "PURCHASE_FREQUENCY", "type": ["null","string"], "categorical": true },
                { "name": "ROLE",               "type": ["null","string"], "categorical": true },
                { "name": "USERNAME",           "type": ["null","string"] }
              ],
              "version": "1.0"
            }
            """;


    private static final String ITEMS_SCHEMA = """
        {
          "type":"record",
          "name":"Items",
          "namespace":"com.amazonaws.personalize.schema",
          "fields":[
            { "name": "ITEM_ID",       "type": "string" },
            { "name": "NAME",          "type": ["null","string"], "textual": true },
            { "name": "PRICE_TIER",    "type": ["null","string"], "categorical": true },
            { "name": "PRICE_SEGMENT", "type": ["null","string"], "categorical": true }
          ],
          "version":"1.0"
        }
        """;

    public String interactionsSchema() { return INTERACTIONS_SCHEMA; }
    public String usersSchema()        { return USERS_SCHEMA; }
    public String itemsSchema()        { return ITEMS_SCHEMA; }

    public String ensureDatasetGroup() {
        try (var c = client()) {
            Optional<DatasetGroupSummary> hit = c.listDatasetGroups(ListDatasetGroupsRequest.builder().build())
                    .datasetGroups().stream()
                    .filter(d -> d.name().equals(props.getDatasetGroupName()))
                    .findFirst();
            if (hit.isPresent()) return hit.get().datasetGroupArn();

            String arn = c.createDatasetGroup(CreateDatasetGroupRequest.builder()
                            .name(props.getDatasetGroupName())
                            .build())
                    .datasetGroupArn();
            waitDatasetGroupActive(c, arn);
            return arn;
        }
    }
    private void waitDatasetGroupActive(PersonalizeClient c, String arn) {
        for (int i = 0; i < 60; i++) { // ~5분
            String s = c.describeDatasetGroup(DescribeDatasetGroupRequest.builder().datasetGroupArn(arn).build())
                    .datasetGroup().status();
            if ("ACTIVE".equalsIgnoreCase(s)) return;
            if (s != null && s.contains("FAILED")) throw new RuntimeException("DatasetGroup failed: " + s);
            sleep(5000);
        }
        throw new RuntimeException("Timeout waiting DatasetGroup ACTIVE: " + arn);
    }

    public String ensureSchema(String name, String avro) {
        try (var c = client()) {
            Optional<DatasetSchemaSummary> hit = c.listSchemas(ListSchemasRequest.builder().build())
                    .schemas().stream().filter(s -> s.name().equals(name)).findFirst();
            if (hit.isPresent()) return hit.get().schemaArn();
            return c.createSchema(CreateSchemaRequest.builder().name(name).schema(avro).build()).schemaArn();
        }
    }

    public String ensureDataset(String dsgArn, String type, String name, String schemaArn) {
        try (var c = client()) {
            Optional<DatasetSummary> hit = c.listDatasets(ListDatasetsRequest.builder().build())
                    .datasets().stream().filter(d -> d.name().equals(name)).findFirst();
            if (hit.isPresent()) return hit.get().datasetArn();

            String arn = c.createDataset(CreateDatasetRequest.builder()
                            .name(name)
                            .datasetType(type) // "INTERACTIONS" | "USERS" | "ITEMS"
                            .datasetGroupArn(dsgArn)
                            .schemaArn(schemaArn)
                            .build())
                    .datasetArn();
            waitDatasetActive(c, arn);
            return arn;
        }
    }
    private void waitDatasetActive(PersonalizeClient c, String arn) {
        for (int i = 0; i < 60; i++) { // ~5분
            String s = c.describeDataset(DescribeDatasetRequest.builder().datasetArn(arn).build())
                    .dataset().status();
            if ("ACTIVE".equalsIgnoreCase(s)) return;
            if (s != null && s.contains("FAILED")) throw new RuntimeException("Dataset failed: " + s);
            sleep(5000);
        }
        throw new RuntimeException("Timeout waiting Dataset ACTIVE: " + arn);
    }

    // ====== Import ======
    public String startImport(String datasetArn, String s3Uri) {
        try (var c = client()) {
            String jobName = "import-" + Instant.now().getEpochSecond();
            return c.createDatasetImportJob(CreateDatasetImportJobRequest.builder()
                            .jobName(jobName)
                            .datasetArn(datasetArn)
                            .dataSource(DataSource.builder().dataLocation(s3Uri).build())
                            .roleArn(props.getImportRoleArn())
                            .build())
                    .datasetImportJobArn();
        }
    }
    public void waitImportJobActive(String jobArn) {
        try (var c = client()) {
            for (int i = 0; i < 240; i++) { // ~20분
                String s = c.describeDatasetImportJob(DescribeDatasetImportJobRequest.builder()
                                .datasetImportJobArn(jobArn).build())
                        .datasetImportJob().status();
                if ("ACTIVE".equalsIgnoreCase(s)) return;
                if (s != null && s.contains("FAILED")) throw new RuntimeException("Import failed: " + s);
                sleep(5000);
            }
            throw new RuntimeException("Timeout waiting Import ACTIVE: " + jobArn);
        }
    }

    // ====== Solution & Campaign ======
    public String ensureSolution(String dsgArn) {
        try (var c = client()) {
            Optional<SolutionSummary> hit = c.listSolutions(ListSolutionsRequest.builder()
                            .datasetGroupArn(dsgArn).build())
                    .solutions().stream().filter(s -> s.name().equals(props.getSolutionName())).findFirst();
            if (hit.isPresent()) return hit.get().solutionArn();

            return c.createSolution(CreateSolutionRequest.builder()
                            .name(props.getSolutionName())
                            .datasetGroupArn(dsgArn)
                            .recipeArn(props.getRecipeArn())
                            .build())
                    .solutionArn();
        }
    }

    public String createSolutionVersion(String solutionArn) {
        try (var c = client()) {
            return c.createSolutionVersion(CreateSolutionVersionRequest.builder()
                            .solutionArn(solutionArn)
                            .build())
                    .solutionVersionArn();
        }
    }
    public void waitSolutionVersionActive(String solutionVersionArn) {
        try (var c = client()) {
            for (int i = 0; i < 720; i++) { // ~2시간
                String s = c.describeSolutionVersion(DescribeSolutionVersionRequest.builder()
                                .solutionVersionArn(solutionVersionArn).build())
                        .solutionVersion().status();
                if ("ACTIVE".equalsIgnoreCase(s)) return;
                if (s != null && s.contains("FAILED")) throw new RuntimeException("Solution training failed: " + s);
                sleep(10000);
            }
            throw new RuntimeException("Timeout waiting SolutionVersion ACTIVE: " + solutionVersionArn);
        }
    }

    public String upsertCampaign(String solutionVersionArn) {
        try (var c = client()) {
            Optional<CampaignSummary> hit = c.listCampaigns(ListCampaignsRequest.builder().build())
                    .campaigns().stream().filter(cp -> cp.name().equals(props.getCampaignName())).findFirst();

            if (hit.isEmpty()) {
                String arn = c.createCampaign(CreateCampaignRequest.builder()
                                .name(props.getCampaignName())
                                .solutionVersionArn(solutionVersionArn)
                                .minProvisionedTPS(props.getMinProvisionedTPS())
                                .build())
                        .campaignArn();
                waitCampaignActive(c, arn);
                return arn;
            } else {
                String arn = hit.get().campaignArn();
                c.updateCampaign(UpdateCampaignRequest.builder()
                        .campaignArn(arn)
                        .solutionVersionArn(solutionVersionArn)
                        .minProvisionedTPS(props.getMinProvisionedTPS())
                        .build());
                waitCampaignActive(c, arn);
                return arn;
            }
        }
    }
    private void waitCampaignActive(PersonalizeClient c, String arn) {
        for (int i = 0; i < 180; i++) { // ~30분
            String s = c.describeCampaign(DescribeCampaignRequest.builder().campaignArn(arn).build())
                    .campaign().status();
            if ("ACTIVE".equalsIgnoreCase(s)) return;
            if (s != null && s.contains("FAILED")) throw new RuntimeException("Campaign failed: " + s);
            sleep(10000);
        }
        throw new RuntimeException("Timeout waiting Campaign ACTIVE: " + arn);
    }

    private static void sleep(long ms) { try { Thread.sleep(ms); } catch (InterruptedException ignored) {} }

    public PersonalizeProperties getProps() { return props; }
}
