CREATE TABLE IF NOT EXISTS aws_vpcs (
    vpc_id              TEXT PRIMARY KEY,
    region              TEXT,
    cidr_block          TEXT,
    state               TEXT,
    is_default          BOOLEAN,
    dhcp_options_id     TEXT,
    instance_tenancy    TEXT,
    owner_id            TEXT,
    ipv6_cidr_blocks    JSONB,
    tags                JSONB,
    collected_at        TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS aws_subnets (
    subnet_id                           TEXT PRIMARY KEY,
    vpc_id                              TEXT,
    region                              TEXT,
    availability_zone                   TEXT,
    availability_zone_id                TEXT,
    cidr_block                          TEXT,
    ipv6_cidr_block                     TEXT,
    available_ip_address_count          INT,
    state                               TEXT,
    is_default                          BOOLEAN,
    map_public_ip_on_launch             BOOLEAN,
    assign_ipv6_address_on_creation     BOOLEAN,
    owner_id                            TEXT,
    subnet_arn                          TEXT,
    outpost_arn                         TEXT,
    tags                                JSONB,
    collected_at                        TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS aws_internet_gateways (
    igw_id          TEXT PRIMARY KEY,
    region          TEXT,
    owner_id        TEXT,
    state           TEXT,
    vpc_id          TEXT,
    tags            JSONB,
    collected_at    TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS aws_ec2_instances (
    instance_id             TEXT PRIMARY KEY,
    region                  TEXT,
    name                    TEXT,
    state                   TEXT,
    state_reason            TEXT,
    instance_type           TEXT,
    launch_time             TIMESTAMPTZ,
    private_ip              TEXT,
    public_ip               TEXT,
    private_dns             TEXT,
    public_dns              TEXT,
    availability_zone       TEXT,
    vpc_id                  TEXT,
    subnet_id               TEXT,
    security_groups         JSONB,
    iam_instance_profile    TEXT,
    key_name                TEXT,
    platform                TEXT,
    architecture            TEXT,
    hypervisor              TEXT,
    virtualization_type     TEXT,
    root_device_name        TEXT,
    root_device_type        TEXT,
    ebs_optimized           BOOLEAN,
    ena_support             BOOLEAN,
    source_dest_check       BOOLEAN,
    monitoring_state        TEXT,
    tenancy                 TEXT,
    host_id                 TEXT,
    capacity_reservation_id TEXT,
    ami_id                  TEXT,
    ami_launch_index        INT,
    kernel_id               TEXT,
    ramdisk_id              TEXT,
    network_interfaces      JSONB,
    block_device_mappings   JSONB,
    metadata_options        JSONB,
    enclave_options         JSONB,
    hibernation_options     BOOLEAN,
    cpu_options             JSONB,
    spot_instance_req_id    TEXT,
    instance_lifecycle      TEXT,
    vcpu_count              INT,
    memory_gib              DOUBLE PRECISION,
    tags                    JSONB,
    collected_at            TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS aws_ebs_volumes (
    volume_id           TEXT PRIMARY KEY,
    region              TEXT,
    instance_id         TEXT,
    device_name         TEXT,
    size_gb             INT,
    state               TEXT,
    volume_type         TEXT,
    encrypted           BOOLEAN,
    kms_key_id          TEXT,
    iops                INT,
    throughput          INT,
    multi_attach        BOOLEAN,
    snapshot_id         TEXT,
    availability_zone   TEXT,
    create_time         TIMESTAMPTZ,
    attachments         JSONB,
    tags                JSONB,
    collected_at        TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS aws_s3_buckets (
    bucket_name             TEXT PRIMARY KEY,
    region                  TEXT,
    creation_date           TIMESTAMPTZ,
    versioning              TEXT,
    mfa_delete              TEXT,
    encryption_rules        JSONB,
    public_access_block     JSONB,
    bucket_policy           JSONB,
    acl_owner               TEXT,
    acl_grants              JSONB,
    tags                    JSONB,
    lifecycle_rules         JSONB,
    logging_target_bucket   TEXT,
    logging_target_prefix   TEXT,
    website_config          JSONB,
    replication_config      JSONB,
    collected_at            TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS aws_albs (
    alb_arn                  TEXT PRIMARY KEY,
    alb_name                 TEXT,
    region                   TEXT,
    dns_name                 TEXT,
    canonical_hosted_zone    TEXT,
    scheme                   TEXT,
    state                    TEXT,
    state_reason             TEXT,
    vpc_id                   TEXT,
    type                     TEXT,
    ip_address_type          TEXT,
    security_groups          JSONB,
    availability_zones       JSONB,
    created_time             TIMESTAMPTZ,
    customer_owned_ipv4_pool TEXT,
    attributes               JSONB,
    tags                     JSONB,
    collected_at             TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS aws_alb_listeners (
    listener_arn        TEXT PRIMARY KEY,
    alb_arn             TEXT,
    region              TEXT,
    port                INT,
    protocol            TEXT,
    ssl_policy          TEXT,
    certificates        JSONB,
    default_actions     JSONB,
    alpn_policy         JSONB,
    tags                JSONB,
    collected_at        TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS aws_alb_listener_rules (
    rule_arn        TEXT PRIMARY KEY,
    listener_arn    TEXT,
    alb_arn         TEXT,
    region          TEXT,
    priority        TEXT,
    is_default      BOOLEAN,
    conditions      JSONB,
    actions         JSONB,
    tags            JSONB,
    collected_at    TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS aws_alb_target_groups (
    tg_arn                  TEXT PRIMARY KEY,
    tg_name                 TEXT,
    region                  TEXT,
    protocol                TEXT,
    protocol_version        TEXT,
    port                    INT,
    vpc_id                  TEXT,
    target_type             TEXT,
    ip_address_type         TEXT,
    health_check_enabled    BOOLEAN,
    health_check_protocol   TEXT,
    health_check_port       TEXT,
    health_check_path       TEXT,
    health_check_interval   INT,
    health_check_timeout    INT,
    healthy_threshold       INT,
    unhealthy_threshold     INT,
    matcher_http_codes      TEXT,
    load_balancer_arns      JSONB,
    attributes              JSONB,
    tags                    JSONB,
    collected_at            TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS aws_alb_target_health (
    id                  BIGSERIAL PRIMARY KEY,
    tg_arn              TEXT,
    region              TEXT,
    target_id           TEXT,
    target_port         INT,
    target_az           TEXT,
    health_state        TEXT,
    health_reason_code  TEXT,
    health_description  TEXT,
    collected_at        TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS aws_lambda_functions (
    function_arn                TEXT PRIMARY KEY,
    function_name               TEXT,
    region                      TEXT,
    runtime                     TEXT,
    handler                     TEXT,
    role                        TEXT,
    code_size                   BIGINT,
    description                 TEXT,
    timeout_seconds             INT,
    memory_mb                   INT,
    memory_min_mb               INT,
    memory_max_mb               INT,
    timeout_max_seconds         INT,
    ephemeral_storage_max_mb    INT,
    concurrency_limit           INT,
    language                    TEXT,
    status                      TEXT,
    supported_architectures     JSONB,
    environment_variables       JSONB,
    vpc_config                  JSONB,
    layers                      JSONB,
    tags                        JSONB,
    raw_config                  JSONB,
    source                      TEXT,
    collected_at                TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS aws_dynamodb_tables (
    table_arn               TEXT PRIMARY KEY,
    table_name              TEXT,
    region                  TEXT,
    table_status            TEXT,
    billing_mode            TEXT,
    read_capacity_units     INT,
    write_capacity_units    INT,
    table_class             TEXT,
    item_count              BIGINT,
    table_size_bytes        BIGINT,
    stream_enabled          BOOLEAN,
    stream_view_type        TEXT,
    encryption_type         TEXT,
    point_in_time_recovery  BOOLEAN,
    ttl_enabled             BOOLEAN,
    global_secondary_indexes JSONB,
    local_secondary_indexes  JSONB,
    description             TEXT,
    use_case                TEXT,
    tags                    JSONB,
    raw_config              JSONB,
    source                  TEXT,
    collected_at            TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS aws_api_gateways (
    api_id                  TEXT PRIMARY KEY,
    api_name                TEXT,
    region                  TEXT,
    protocol_type           TEXT,
    endpoint_type           TEXT,
    description             TEXT,
    api_key_source          TEXT,
    auth_types              JSONB,
    cors_configuration      JSONB,
    route_selection_expression TEXT,
    stages                  JSONB,
    use_case                TEXT,
    status                  TEXT,
    tags                    JSONB,
    raw_config              JSONB,
    source                  TEXT,
    collected_at            TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS aws_sqs_queues (
    queue_url               TEXT PRIMARY KEY,
    queue_name              TEXT,
    region                  TEXT,
    fifo_queue              BOOLEAN,
    delay_seconds           INT,
    max_message_size        INT,
    message_retention_seconds INT,
    visibility_timeout      INT,
    receive_wait_time_seconds INT,
    content_based_dedup     BOOLEAN,
    redrive_policy          JSONB,
    kms_master_key_id       TEXT,
    policy                  JSONB,
    tags                    JSONB,
    raw_config              JSONB,
    source                  TEXT,
    collected_at            TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS aws_sns_topics (
    topic_arn               TEXT PRIMARY KEY,
    topic_name              TEXT,
    region                  TEXT,
    display_name            TEXT,
    fifo_topic              BOOLEAN,
    content_based_dedup     BOOLEAN,
    kms_master_key_id       TEXT,
    policy                  JSONB,
    subscriptions_confirmed INT,
    subscriptions_pending   INT,
    subscriptions_deleted   INT,
    tags                    JSONB,
    raw_config              JSONB,
    source                  TEXT,
    collected_at            TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS aws_kinesis_streams (
    stream_arn              TEXT PRIMARY KEY,
    stream_name             TEXT,
    region                  TEXT,
    stream_status           TEXT,
    stream_mode             TEXT,
    shard_count             INT,
    retention_period_hours  INT,
    encryption_type         TEXT,
    kms_key_id              TEXT,
    has_enhanced_monitoring BOOLEAN,
    tags                    JSONB,
    raw_config              JSONB,
    source                  TEXT,
    collected_at            TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS aws_cloudwatch_alarms (
    alarm_arn               TEXT PRIMARY KEY,
    alarm_name              TEXT,
    region                  TEXT,
    metric_name             TEXT,
    namespace               TEXT,
    statistic               TEXT,
    period                  INT,
    evaluation_periods      INT,
    threshold               DOUBLE PRECISION,
    comparison_operator     TEXT,
    alarm_description       TEXT,
    alarm_actions           JSONB,
    ok_actions              JSONB,
    dimensions              JSONB,
    tags                    JSONB,
    raw_config              JSONB,
    source                  TEXT,
    collected_at            TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS aws_xray_sampling_rules (
    rule_arn                TEXT PRIMARY KEY,
    rule_name               TEXT,
    region                  TEXT,
    priority                INT,
    fixed_rate              DOUBLE PRECISION,
    reservoir_size          INT,
    service_name            TEXT,
    service_type            TEXT,
    host                    TEXT,
    http_method             TEXT,
    url_path                TEXT,
    resource_arn            TEXT,
    version                 INT,
    tags                    JSONB,
    raw_config              JSONB,
    source                  TEXT,
    collected_at            TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS aws_cloudtrail_trails (
    trail_arn               TEXT PRIMARY KEY,
    trail_name              TEXT,
    region                  TEXT,
    s3_bucket_name          TEXT,
    s3_key_prefix           TEXT,
    is_multi_region         BOOLEAN,
    log_file_validation     BOOLEAN,
    include_global_events   BOOLEAN,
    enable_logging          BOOLEAN,
    kms_key_id              TEXT,
    sns_topic_arn           TEXT,
    cloud_watch_logs_group  TEXT,
    tags                    JSONB,
    raw_config              JSONB,
    source                  TEXT,
    collected_at            TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS aws_ecr_repositories (
    repository_arn          TEXT PRIMARY KEY,
    repository_name         TEXT,
    region                  TEXT,
    registry_id             TEXT,
    repository_uri          TEXT,
    image_tag_mutability    TEXT,
    scan_on_push            BOOLEAN,
    encryption_type         TEXT,
    kms_key_id              TEXT,
    created_at              TIMESTAMPTZ,
    tags                    JSONB,
    raw_config              JSONB,
    source                  TEXT,
    collected_at            TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS aws_ecs_clusters (
    cluster_arn                     TEXT PRIMARY KEY,
    cluster_name                    TEXT,
    region                          TEXT,
    status                          TEXT,
    capacity_providers              JSONB,
    settings                        JSONB,
    container_insights_enabled      BOOLEAN,
    registered_container_instances  INT,
    active_services_count           INT,
    running_tasks_count             INT,
    tags                            JSONB,
    raw_config                      JSONB,
    source                          TEXT,
    collected_at                    TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS aws_ecs_task_definitions (
    task_definition_arn         TEXT PRIMARY KEY,
    family                      TEXT,
    region                      TEXT,
    revision                    INT,
    cpu                         TEXT,
    memory                      TEXT,
    network_mode                TEXT,
    requires_compatibilities    JSONB,
    execution_role_arn          TEXT,
    task_role_arn               TEXT,
    container_definitions       JSONB,
    volumes                     JSONB,
    tags                        JSONB,
    raw_config                  JSONB,
    source                      TEXT,
    collected_at                TIMESTAMPTZ DEFAULT NOW()
);

-- Ensure missing columns are added if table existed before schema update
ALTER TABLE aws_ec2_instances ADD COLUMN IF NOT EXISTS vcpu_count INT;
ALTER TABLE aws_ec2_instances ADD COLUMN IF NOT EXISTS memory_gib DOUBLE PRECISION;

CREATE TABLE IF NOT EXISTS aws_elasticache_clusters (
    cluster_id              TEXT PRIMARY KEY,
    cluster_name            TEXT,
    region                  TEXT,
    engine                  TEXT,
    engine_version          TEXT,
    node_type               TEXT,
    num_cache_nodes         INT,
    status                  TEXT,
    port                    INT,
    subnet_group_name       TEXT,
    preferred_availability_zone TEXT,
    preferred_maintenance_window TEXT,
    snapshot_retention_limit INT,
    snapshot_window         TEXT,
    tags                    JSONB,
    raw_config              JSONB,
    source                  TEXT,
    collected_at            TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS aws_efs_file_systems (
    file_system_id          TEXT PRIMARY KEY,
    file_system_name        TEXT,
    region                  TEXT,
    lifecycle_state         TEXT,
    performance_mode        TEXT,
    throughput_mode         TEXT,
    provisioned_throughput  DOUBLE PRECISION,
    encrypted               BOOLEAN,
    kms_key_id              TEXT,
    size_in_bytes           BIGINT,
    number_of_mount_targets INT,
    creation_token          TEXT,
    tags                    JSONB,
    raw_config              JSONB,
    source                  TEXT,
    collected_at            TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS aws_eventbridge_rules (
    rule_arn                TEXT PRIMARY KEY,
    rule_name               TEXT,
    region                  TEXT,
    event_bus_name          TEXT,
    description             TEXT,
    state                   TEXT,
    schedule_expression     TEXT,
    event_pattern           JSONB,
    role_arn                TEXT,
    managed_by              TEXT,
    targets                 JSONB,
    tags                    JSONB,
    raw_config              JSONB,
    source                  TEXT,
    collected_at            TIMESTAMPTZ DEFAULT NOW()
);
