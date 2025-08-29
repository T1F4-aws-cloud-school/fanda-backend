package com.fanda.recommend.etl.controller;

import com.fanda.recommend.etl.service.CampaignRegistry;
import com.fanda.recommend.etl.service.EtlIngestionService;
import com.fanda.recommend.etl.service.PersonalizeOrchestrationService;
import com.fanda.recommend.s3ingest.dto.PipelineResultDto;
import com.fanda.recommend.s3ingest.dto.UploadResultDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/internal/pipeline")
public class PipelineController {

    private final EtlIngestionService etl;
    private final PersonalizeOrchestrationService p;
    private final CampaignRegistry campaignRegistry;

    @PostMapping("/run")
    public ResponseEntity<PipelineResultDto> run(){
        // 1. ETL -> S3 업로드
        UploadResultDto up = etl.runOnce();

        // 2. datasetgroup / schema / dataset 보장
        String dsgArn = p.ensureDatasetGroup();
        String interSchemaArn = p.ensureSchema("fanda-interactions-schema", p.interactionsSchema());
        String usersSchemaArn = p.ensureSchema("fanda-users-schema", p.usersSchema());
        String itemsSchemaArn = p.ensureSchema("fanda-items-schema", p.itemsSchema());

        String interDsArn = p.ensureDataset(
                dsgArn, "INTERACTIONS",
                p.getProps().getDatasetNames().getInteractions(),
                interSchemaArn);
        String usersDsArn = p.ensureDataset(
                dsgArn, "USERS",
                p.getProps().getDatasetNames().getUsers(),
                usersSchemaArn);
        String itemsDsArn = p.ensureDataset(
                dsgArn, "ITEMS",
                p.getProps().getDatasetNames().getItems(),
                itemsSchemaArn);

        // 3. import 3개 생성 + 완료 대기
        String interjob = p.startImport(interDsArn, up.getInteractionsS3Uri());
        String usersJob = p.startImport(usersDsArn, up.getUsersS3Uri());
        String itemsJob = p.startImport(itemsDsArn, up.getItemsS3Uri());

        p.waitImportJobActive(interjob);
        p.waitImportJobActive(usersJob);
        p.waitImportJobActive(itemsJob);

        // 4. 학습 -> Campaign Update
        String solutionArn = p.ensureSolution(dsgArn);
        String solutionVersionArn = p.createSolutionVersion(solutionArn);
        p.waitSolutionVersionActive(solutionVersionArn);

        String campaignArn = p.upsertCampaign(solutionVersionArn);
        campaignRegistry.saveActiveCampaignArn(campaignArn);

        return ResponseEntity.ok(new PipelineResultDto(up.getRunId(), up.getS3Prefix(), interjob, usersJob, itemsJob, solutionVersionArn, campaignArn));
    }
}
