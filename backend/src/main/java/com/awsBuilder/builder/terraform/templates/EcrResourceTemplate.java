package com.awsBuilder.builder.terraform.templates;

import com.awsBuilder.builder.diagram.model.NodeDTO;
import com.awsBuilder.builder.diagram.model.ResourceGraph;
import com.awsBuilder.builder.terraform.model.TerraformResource;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class EcrResourceTemplate implements TerraformResource {
    @Override
    public String getResourceType() {
        return "ECR";
    }

    @Override
    public String generate(NodeDTO node, ResourceGraph graph, String regionAlias) {
        String id = node.getId();
        Map<String, String> props = node.getProperties();
        String repoName = props.getOrDefault("repository_name", id);
        String imageTagMutability = props.getOrDefault("image_tag_mutability", "MUTABLE");
        String scanOnPush = props.getOrDefault("scan_on_push", "true");
        String encryptionType = props.getOrDefault("encryption_type", "AES256");
        String forceDelete = props.getOrDefault("force_delete", "false");

        String provider = (regionAlias != null && !regionAlias.isEmpty()) ? "  provider = aws." + regionAlias + "\n" : "";

        return String.format("""
            resource "aws_ecr_repository" "%s" {
            %s  name                 = "%s"
              image_tag_mutability = "%s"
              force_delete         = %s

              image_scanning_configuration {
                scan_on_push = %s
              }

              encryption_configuration {
                encryption_type = "%s"
              }

              tags = {
                Name = "%s"
              }
            }
            """, id, provider, repoName, imageTagMutability, forceDelete,
                scanOnPush, encryptionType, id);
    }
}
