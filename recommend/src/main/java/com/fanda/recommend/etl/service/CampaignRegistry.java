package com.fanda.recommend.etl.service;

import org.springframework.stereotype.Service;

@Service
public class CampaignRegistry {

    private volatile String activeCampaignArn;

    public void saveActiveCampaignArn(String arn) {
        this.activeCampaignArn = arn;
    }

    public String getActiveCampaignArn(){
        return activeCampaignArn;
    }
}
