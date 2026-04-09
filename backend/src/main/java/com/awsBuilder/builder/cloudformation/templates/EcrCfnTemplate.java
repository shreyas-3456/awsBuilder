package com.awsBuilder.builder.cloudformation.templates;

import com.awsBuilder.builder.cloudformation.model.CloudFormationResource;
import com.awsBuilder.builder.diagram.model.NodeDTO;
import com.awsBuilder.builder.diagram.model.ResourceGraph;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class EcrCfnTemplate implements CloudFormationResource {
    @Override
    public String generate(NodeDTO node, ResourceGraph graph) {
        String repoName = node.getProperties().getOrDefault("repository_name", node.getId());
        String imageTagMutability = node.getProperties().getOrDefault("image_tag_mutability", "MUTABLE");
        String scanOnPush = node.getProperties().getOrDefault("scan_on_push", "true");
        String encryptionType = node.getProperties().getOrDefault("encryption_type", "AES256");

        Map<String, Object> resource = new LinkedHashMap<>();
        resource.put("Type", "AWS::ECR::Repository");

        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("RepositoryName", repoName);
        properties.put("ImageTagMutability", imageTagMutability);

        Map<String, Object> scanConfig = new LinkedHashMap<>();
        scanConfig.put("ScanOnPush", Boolean.parseBoolean(scanOnPush));
        properties.put("ImageScanningConfiguration", scanConfig);

        Map<String, Object> encryptionConfig = new LinkedHashMap<>();
        encryptionConfig.put("EncryptionType", encryptionType);
        properties.put("EncryptionConfiguration", encryptionConfig);

        resource.put("Properties", properties);

        Map<String, Object> fragment = new LinkedHashMap<>();
        fragment.put(node.getId(), resource);

        DumperOptions opts = new DumperOptions();
        opts.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        opts.setPrettyFlow(true);
        return new Yaml(opts).dump(fragment);
    }

    @Override
    public String getResourceType() {
        return "ECR";
    }
}
