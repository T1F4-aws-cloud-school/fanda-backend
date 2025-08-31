package com.fanda.recommend.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "personalize")
@Getter
@Setter
public class PersonalizeProperties {

    private String region;
    private String importRoleArn;

    private String datasetGroupName;
    private DatasetNames datasetNames;

    private String solutionName;
    private String recipeArn;
    private String campaignName;
    private Integer minProvisionedTPS = 1;

    @Getter @Setter
    public static class DatasetNames {
        private String interactions;
        private String users;
        private String items;
    }
}
