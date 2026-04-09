#!/usr/bin/env python3
"""
AWS Meta Scraper → Your Existing Tables (ALL REGIONS)
=======================================================
Scrapes AWS PUBLIC catalog data across EVERY AWS region.
Stores into your exact existing tables with region column populated.

Skip logic:  each (table, region) combo is checked — already scraped = skipped.
Force re-scrape one table:  FORCE_TABLE=aws_ec2_instances python aws_meta_scraper.py
Force one region:           FORCE_REGION=us-east-1 python aws_meta_scraper.py
Force both:                 FORCE_TABLE=rds_inventory FORCE_REGION=eu-west-1 python aws_meta_scraper.py

Usage:
  export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/terraform_builder
  export SPRING_DATASOURCE_USERNAME=builder_user
  export SPRING_DATASOURCE_PASSWORD=builder_pass
  python aws_meta_scraper.py
"""

import boto3
import json
import os
import re
import sys
import logging
import psycopg2
from datetime import datetime, timezone
from botocore.exceptions import ClientError, EndpointResolutionError, NoRegionError

logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(message)s", datefmt="%H:%M:%S")
log = logging.getLogger("aws-meta")

FORCE_TABLE  = os.environ.get("FORCE_TABLE",  "").strip()
FORCE_REGION = os.environ.get("FORCE_REGION", "").strip()

# ─── CONFIG ──────────────────────────────────────────────────────────────────

def get_db_config():
    DEFAULT_URL  = "jdbc:p6spy:postgresql://localhost:5432/terraform_builder"
    DEFAULT_USER = "builder_user"
    DEFAULT_PASS = "builder_pass"
    if os.environ.get("PG_HOST"):
        return {"host": os.environ["PG_HOST"], "port": int(os.environ.get("PG_PORT","5432")),
                "dbname": os.environ.get("PG_DATABASE","terraform_builder"),
                "user": os.environ.get("PG_USER",DEFAULT_USER),
                "password": os.environ.get("PG_PASSWORD",DEFAULT_PASS)}
    url  = os.environ.get("SPRING_DATASOURCE_URL", DEFAULT_URL)
    user = os.environ.get("SPRING_DATASOURCE_USERNAME", DEFAULT_USER)
    pwd  = os.environ.get("SPRING_DATASOURCE_PASSWORD", DEFAULT_PASS)
    clean = re.sub(r"^jdbc:(p6spy:)?postgresql://","",url)
    m = re.match(r"([^/:]+)(?::(\d+))?/([^?]+)", clean)
    if not m: log.error("Cannot parse JDBC URL: %s", url); sys.exit(1)
    return {"host":m.group(1),"port":int(m.group(2) or "5432"),
            "dbname":m.group(3),"user":user,"password":pwd}

# ─── HELPERS ─────────────────────────────────────────────────────────────────

def _serial(obj):
    if isinstance(obj, datetime): return obj.isoformat()
    if isinstance(obj, (bytes,bytearray)): return obj.decode("utf-8",errors="replace")
    if isinstance(obj, set): return list(obj)
    return str(obj)

def j(obj): return json.dumps(obj, default=_serial)

def safe(fn, fallback=None):
    try: return fn()
    except Exception as e: log.warning("    API call skipped: %s", e); return fallback

def pages(client, method, key, **kwargs):
    try:
        paginator = client.get_paginator(method)
        out = []
        for page in paginator.paginate(**kwargs):
            out.extend(page.get(key,[]))
        return out
    except Exception as e:
        log.warning("    Paginate %s skipped: %s", method, e)
        return []

def get_all_regions(base_session):
    """Get every enabled + disabled AWS region."""
    ec2 = base_session.client("ec2", region_name="us-east-1")
    try:
        regions = ec2.describe_regions(AllRegions=True)["Regions"]
        if FORCE_REGION:
            return [r for r in regions if r["RegionName"] == FORCE_REGION]
        return regions
    except Exception as e:
        log.error("Cannot fetch regions: %s", e)
        sys.exit(1)

# ─── DB ──────────────────────────────────────────────────────────────────────

_col_cache = {}

def get_table_cols(conn, table):
    if table in _col_cache: return _col_cache[table]
    with conn.cursor() as cur:
        cur.execute("""SELECT column_name FROM information_schema.columns
                       WHERE table_name=%s AND table_schema='public'
                       ORDER BY ordinal_position""", (table,))
        cols = [r[0] for r in cur.fetchall()]
    _col_cache[table] = cols
    return cols

def region_already_done(conn, table, region):
    """True if this (table, region) already has rows — skips re-scrape."""
    if FORCE_TABLE == table: return False
    try:
        cols = get_table_cols(conn, table)
        if "region" not in cols: return False          # table has no region col — do once
        with conn.cursor() as cur:
            cur.execute(f"SELECT 1 FROM {table} WHERE region=%s LIMIT 1", (region,))
            exists = cur.fetchone() is not None
        if exists: log.info("    ⏭  %-30s region=%-15s already done", table, region)
        return exists
    except Exception:
        return False

def insert(conn, table, rows):
    if not rows: return
    cols = get_table_cols(conn, table)
    if not cols: log.warning("    Table %s not found", table); return
    now  = datetime.now(timezone.utc)
    ts_cols = {"collected_at","scraped_at","created_at","updated_at"}
    filtered = []
    for row in rows:
        r = {}
        for c in cols:
            if c in row:
                v = row[c]
                r[c] = j(v) if isinstance(v,(dict,list)) else v
            elif c in ts_cols:
                r[c] = now
        if r: filtered.append(r)
    if not filtered: return
    used = list(filtered[0].keys())
    sql = "INSERT INTO {} ({}) VALUES ({}) ON CONFLICT DO NOTHING".format(
        table, ",".join(used), ",".join(["%s"]*len(used)))
    try:
        with conn.cursor() as cur:
            cur.executemany(sql, [tuple(r.get(c) for c in used) for r in filtered])
        conn.commit()
        log.info("    ✔ %-30s %d rows", table, len(filtered))
    except Exception as e:
        conn.rollback()
        log.error("    ✗ %-30s %s", table, e)

# ─── PER-REGION SCRAPERS ─────────────────────────────────────────────────────

def scrape_ec2_instance_types(ec2, conn, region):
    targets = ["aws_ec2_instances","ec2_inventory"]
    targets = [t for t in targets if not region_already_done(conn,t,region)]
    if not targets: return
    data = pages(ec2,"describe_instance_types","InstanceTypes")
    if not data: return
    rows = []
    for d in data:
        vcpu    = d.get("VCpuInfo",{}).get("DefaultVCpus")
        mem_mib = d.get("MemoryInfo",{}).get("SizeInMiB")
        net     = d.get("NetworkInfo",{})
        proc    = d.get("ProcessorInfo",{})
        ebs     = d.get("EbsInfo",{})
        stor    = d.get("InstanceStorageInfo") or {}
        gpu     = d.get("GpuInfo")
        rows.append({
            "instance_id":                 f"catalog-{region}-{d.get('InstanceType')}",
            "instance_type":               d.get("InstanceType"),
            "resource_id":                 f"{region}-{d.get('InstanceType')}",
            "name":                        f"Catalog-{d.get('InstanceType')}",
            "region":                      region,
            "availability_zone":           f"{region}a",
            "state":                       "catalog",
            "state_reason":                "AWS service catalog entry",
            "vcpu_count":                  vcpu,
            "memory_mib":                  mem_mib,
            "memory_gib":                  round(mem_mib/1024,2) if mem_mib else None,
            "network_performance":         net.get("NetworkPerformance"),
            "max_network_interfaces":      net.get("MaximumNetworkInterfaces"),
            "ipv6_supported":              net.get("Ipv6Supported"),
            "private_ip":                  None, "public_ip": None,
            "private_dns": None, "public_dns": None,
            "network_interfaces":          j([]),
            "vpc_id":                      None, "subnet_id": None,
            "security_groups":             j([]),
            "ebs_optimized":               ebs.get("EbsOptimizedSupport")=="supported",
            "ebs_optimized_support":       ebs.get("EbsOptimizedSupport"),
            "ebs_encryption_support":      ebs.get("EncryptionSupport"),
            "block_device_mappings":       j([]),
            "root_device_name":            "/dev/sda1",
            "root_device_type":            "ebs",
            "architecture":                (proc.get("SupportedArchitectures") or ["x86_64"])[0],
            "architectures":               j(proc.get("SupportedArchitectures",[])),
            "clock_speed_ghz":             proc.get("SustainedClockSpeedInGhz"),
            "instance_storage_supported":  d.get("InstanceStorageSupported"),
            "instance_storage_gb":         stor.get("TotalSizeInGB"),
            "nvme_support":                stor.get("NvmeSupport"),
            "gpu_info":                    j(gpu) if gpu else None,
            "hibernation_supported":       d.get("HibernationSupported"),
            "hibernation_options":         d.get("HibernationSupported"),
            "burstable_performance":       d.get("BurstablePerformanceSupported"),
            "current_generation":          d.get("CurrentGeneration"),
            "free_tier_eligible":          d.get("FreeTierEligible"),
            "ena_support":                 net.get("EnaSupport")=="required",
            "source_dest_check":           True,
            "hypervisor":                  "nitro" if d.get("Hypervisor")=="nitro" else "xen",
            "virtualization_type":         "hvm",
            "supported_virt_types":        j(d.get("SupportedVirtualizationTypes",[])),
            "tenancy":                     "default",
            "host_id": None, "capacity_reservation_id": None,
            "placement_group_strategies":  j(d.get("PlacementGroupInfo",{}).get("SupportedStrategies",[])),
            "ami_id": None, "ami_launch_index": None, "launch_time": None,
            "kernel_id": None, "ramdisk_id": None,
            "iam_instance_profile": None, "key_name": None, "platform": None,
            "monitoring_state":            "disabled",
            "metadata_options":            j({}),
            "enclave_options":             j({"Enabled":False}),
            "cpu_options":                 j({"CoreCount":vcpu,"ThreadsPerCore":2}) if vcpu else j({}),
            "spot_instance_req_id": None, "instance_lifecycle": None,
            "supported_usage_classes":     j(d.get("SupportedUsageClasses",[])),
            "supported_root_device_types": j(d.get("SupportedRootDeviceTypes",[])),
            "tags":                        j({"Source":"aws-catalog","Region":region}),
            "raw_config":                  j(d),
            "source":                      "aws-catalog",
        })
    for t in targets:
        insert(conn, t, rows)

def scrape_ebs_types(conn, region):
    targets = ["aws_ebs_volumes","ebs_inventory"]
    targets = [t for t in targets if not region_already_done(conn,t,region)]
    if not targets: return
    types = [
        {"volume_type":"gp3","category":"SSD","max_iops":16000,"default_iops":3000,
         "max_throughput_mbps":1000,"default_throughput_mbps":125,"min_size_gib":1,"max_size_gib":16384,
         "multi_attach":False,"current_gen":True,"encrypted":True,
         "use_case":"General purpose. Boot volumes, dev/test."},
        {"volume_type":"gp2","category":"SSD","max_iops":16000,"default_iops":None,
         "max_throughput_mbps":250,"default_throughput_mbps":None,"min_size_gib":1,"max_size_gib":16384,
         "multi_attach":False,"current_gen":False,"encrypted":True,
         "use_case":"General purpose legacy. 3 IOPS/GiB. Prefer gp3."},
        {"volume_type":"io2","category":"SSD","max_iops":256000,"default_iops":None,
         "max_throughput_mbps":4000,"default_throughput_mbps":None,"min_size_gib":4,"max_size_gib":16384,
         "multi_attach":True,"current_gen":True,"encrypted":True,
         "use_case":"High IOPS: SAP HANA, large DBs. 99.999% durability."},
        {"volume_type":"io1","category":"SSD","max_iops":64000,"default_iops":None,
         "max_throughput_mbps":1000,"default_throughput_mbps":None,"min_size_gib":4,"max_size_gib":16384,
         "multi_attach":True,"current_gen":False,"encrypted":True,
         "use_case":"High IOPS legacy. Prefer io2."},
        {"volume_type":"st1","category":"HDD","max_iops":500,"default_iops":None,
         "max_throughput_mbps":500,"default_throughput_mbps":None,"min_size_gib":125,"max_size_gib":16384,
         "multi_attach":False,"current_gen":True,"encrypted":True,
         "use_case":"Throughput: data warehouses, MapReduce, logs."},
        {"volume_type":"sc1","category":"HDD","max_iops":250,"default_iops":None,
         "max_throughput_mbps":250,"default_throughput_mbps":None,"min_size_gib":125,"max_size_gib":16384,
         "multi_attach":False,"current_gen":True,"encrypted":True,
         "use_case":"Cold storage, lowest cost HDD."},
        {"volume_type":"standard","category":"HDD","max_iops":200,"default_iops":None,
         "max_throughput_mbps":None,"default_throughput_mbps":None,"min_size_gib":1,"max_size_gib":1024,
         "multi_attach":False,"current_gen":False,"encrypted":False,
         "use_case":"Legacy magnetic. Not recommended."},
    ]
    rows = [dict(
        volume_id=f"catalog-{region}-{t['volume_type']}",
        resource_id=f"{region}-{t['volume_type']}",
        volume_type=t["volume_type"], category=t["category"],
        region=region, state="catalog",
        availability_zone=f"{region}a",
        instance_id=None, device_name=None,
        size_gb=t["max_size_gib"],
        min_size_gib=t["min_size_gib"], max_size_gib=t["max_size_gib"],
        iops=t["default_iops"] or t["max_iops"],
        max_iops=t["max_iops"], default_iops=t["default_iops"],
        throughput=t["default_throughput_mbps"],
        max_throughput_mbps=t["max_throughput_mbps"],
        default_throughput_mbps=t["default_throughput_mbps"],
        encrypted=t["encrypted"], kms_key_id=None,
        multi_attach=t["multi_attach"], multi_attach_enabled=t["multi_attach"],
        snapshot_id=None, create_time=None, attachments=j([]),
        current_generation=t["current_gen"], use_case=t["use_case"],
        tags=j({"Source":"aws-catalog","Region":region}),
        raw_config=j(t), source="aws-catalog"
    ) for t in types]
    for t in targets:
        insert(conn, t, rows)

def scrape_rds_catalog(rds, conn, region):
    table = "rds_inventory"
    if region_already_done(conn, table, region): return
    data = pages(rds,"describe_db_engine_versions","DBEngineVersions")
    if not data: return
    rows = []
    for d in data:
        rows.append({
            "db_instance_identifier": f"{region}-{d.get('Engine')}-{d.get('EngineVersion')}",
            "resource_id":            f"{region}-{d.get('Engine')}-{d.get('EngineVersion')}",
            "engine":                 d.get("Engine"),
            "engine_version":         d.get("EngineVersion"),
            "db_engine_description":  d.get("DBEngineDescription"),
            "db_parameter_group_family": d.get("DBParameterGroupFamily"),
            "status":                 d.get("Status"),
            "supports_log_exports":   j(d.get("ExportableLogTypes",[])),
            "supports_read_replica":  d.get("SupportsReadReplica"),
            "supports_global_db":     d.get("SupportsGlobalDatabases"),
            "supports_multi_az":      None,
            "valid_upgrade_targets":  j(d.get("ValidUpgradeTarget",[])),
            "raw_config":             j(d),
            "region":                 region,
            "source":                 "aws-catalog",
        })
    insert(conn, table, rows)

    # Orderable options — engine x instance class x storage type
    engine_pairs = list({(e["Engine"],e["EngineVersion"]) for e in data})
    seen = set(); batch = []
    for engine, version in engine_pairs:
        opts = pages(rds,"describe_orderable_db_instance_options",
                     "OrderableDBInstanceOptions", Engine=engine, EngineVersion=version)
        for opt in opts:
            key = f"{region}#{opt.get('Engine')}@{opt.get('EngineVersion')}#{opt.get('DBInstanceClass')}#{opt.get('StorageType')}"
            if key in seen: continue
            seen.add(key)
            batch.append({
                "db_instance_identifier": key,
                "resource_id":            key,
                "engine":                 opt.get("Engine"),
                "engine_version":         opt.get("EngineVersion"),
                "db_instance_class":      opt.get("DBInstanceClass"),
                "storage_type":           opt.get("StorageType"),
                "supports_iops":          opt.get("SupportsIops"),
                "supports_multi_az":      opt.get("MultiAZCapable"),
                "supports_read_replica":  opt.get("ReadReplicaCapable"),
                "supports_iam_auth":      opt.get("SupportsIAMDatabaseAuthentication"),
                "supports_performance_insights": opt.get("SupportsPerformanceInsights"),
                "availability_zones":     j(opt.get("AvailabilityZones",[])),
                "raw_config":             j(opt),
                "region":                 region,
                "source":                 "aws-catalog-orderable",
            })
        if len(batch) >= 500:
            insert(conn, table, batch); batch = []
    if batch: insert(conn, table, batch)

def scrape_alb_catalog(elb, conn, region):
    ssl_policies = pages(elb,"describe_ssl_policies","SslPolicies")
    limits       = pages(elb,"describe_account_limits","Limits")
    lb_types = [
        {"lb_type":"application","short":"ALB","layer":"7","protocols":"HTTP,HTTPS,gRPC",
         "use_case":"HTTP routing, path/host/header rules, WAF, Lambda targets"},
        {"lb_type":"network","short":"NLB","layer":"4","protocols":"TCP,UDP,TLS",
         "use_case":"Ultra-low latency, static IP, preserve source IP"},
        {"lb_type":"gateway","short":"GWLB","layer":"3","protocols":"IP(GENEVE)",
         "use_case":"Deploy/scale virtual appliances (firewalls, IDS/IPS)"},
    ]
    rows = [dict(
        alb_arn=f"arn:aws:elasticloadbalancing:{region}:catalog:loadbalancer/{lb['lb_type']}/catalog-{lb['short']}/catalog",
        load_balancer_arn=f"arn:aws:elasticloadbalancing:{region}:catalog:loadbalancer/{lb['lb_type']}/catalog-{lb['short']}/catalog",
        alb_name=f"catalog-{lb['short']}", load_balancer_name=lb["short"],
        resource_id=f"{region}-{lb['lb_type']}",
        type=lb["lb_type"], layer=lb["layer"], region=region,
        dns_name=f"catalog-{lb['short']}.elb.{region}.amazonaws.com",
        canonical_hosted_zone="Z1234567890ABC",
        scheme="internet-facing", state="catalog",
        state_reason="AWS service catalog entry",
        vpc_id=None, ip_address_type="ipv4",
        security_groups=j([]), availability_zones=j([]), created_time=None,
        customer_owned_ipv4_pool=None,
        protocols_supported=lb["protocols"], use_case=lb["use_case"],
        ssl_policies=j([p["Name"] for p in ssl_policies]),
        service_limits=j({l["Name"]:l["Max"] for l in limits}),
        attributes=j({}),
        tags=j({"Source":"aws-catalog","Region":region,"LBType":lb["lb_type"]}),
        raw_config=j(lb), source="aws-catalog"
    ) for lb in lb_types]
    for table in ("aws_albs","alb_inventory"):
        if not region_already_done(conn, table, region):
            insert(conn, table, rows)

def scrape_listener_catalog(elb, conn, region):
    ssl_policies = pages(elb,"describe_ssl_policies","SslPolicies")
    protocols = [
        {"protocol":"HTTP",    "lb_type":"application","ssl":False,"description":"Plain HTTP","default_port":80},
        {"protocol":"HTTPS",   "lb_type":"application","ssl":True, "description":"HTTPS with TLS termination","default_port":443},
        {"protocol":"TCP",     "lb_type":"network",    "ssl":False,"description":"Raw TCP passthrough","default_port":80},
        {"protocol":"UDP",     "lb_type":"network",    "ssl":False,"description":"UDP (NLB only)","default_port":53},
        {"protocol":"TLS",     "lb_type":"network",    "ssl":True, "description":"TLS termination at NLB","default_port":443},
        {"protocol":"TCP_UDP", "lb_type":"network",    "ssl":False,"description":"TCP+UDP same listener","default_port":53},
        {"protocol":"GENEVE",  "lb_type":"gateway",    "ssl":False,"description":"GWLB encapsulation port 6081","default_port":6081},
    ]
    rows = [dict(
        listener_arn=f"arn:aws:elasticloadbalancing:{region}:catalog:listener/catalog/{p['protocol']}/catalog",
        resource_id=f"{region}-catalog-{p['protocol']}",
        alb_arn=f"arn:aws:elasticloadbalancing:{region}:catalog:loadbalancer/{p['lb_type']}/catalog/catalog",
        region=region, port=p["default_port"], protocol=p["protocol"],
        lb_type=p["lb_type"], ssl_required=p["ssl"], description=p["description"],
        ssl_policy="ELBSecurityPolicy-TLS13-1-2-2021-06" if p["ssl"] else None,
        certificates=j([]) if not p["ssl"] else j([{"CertificateArn":"arn:aws:acm:region:account:certificate/catalog"}]),
        default_actions=j([{"Type":"forward","TargetGroupArn":"arn:aws:elasticloadbalancing:region:account:targetgroup/catalog/catalog"}]),
        alpn_policy=j([]) if not p["ssl"] else j(["HTTP2Preferred"]),
        available_ssl_policies=j([sp["Name"] for sp in ssl_policies] if p["ssl"] else []),
        tags=j({"Source":"aws-catalog","Region":region,"Protocol":p["protocol"]}),
        raw_config=j(p), source="aws-catalog"
    ) for p in protocols]
    for table in ("aws_alb_listeners","alb_listener_inventory"):
        if not region_already_done(conn, table, region):
            insert(conn, table, rows)

def scrape_listener_rules_catalog(conn, region):
    table = "aws_alb_listener_rules"
    if region_already_done(conn, table, region): return
    rules = [
        {"rule_type":"path-pattern",        "category":"condition","description":"Match URL path e.g. /api/*"},
        {"rule_type":"host-header",          "category":"condition","description":"Match hostname e.g. api.example.com"},
        {"rule_type":"http-header",          "category":"condition","description":"Match any HTTP header value"},
        {"rule_type":"http-request-method",  "category":"condition","description":"Match HTTP method GET/POST/etc"},
        {"rule_type":"query-string",         "category":"condition","description":"Match query param key=value"},
        {"rule_type":"source-ip",            "category":"condition","description":"Match client IP CIDR"},
        {"rule_type":"forward",              "category":"action",   "description":"Forward to target group(s) with weights"},
        {"rule_type":"redirect",             "category":"action",   "description":"HTTP 301/302 redirect"},
        {"rule_type":"fixed-response",       "category":"action",   "description":"Return fixed HTTP response"},
        {"rule_type":"authenticate-cognito", "category":"action",   "description":"Auth via Amazon Cognito"},
        {"rule_type":"authenticate-oidc",    "category":"action",   "description":"Auth via OIDC provider"},
    ]
    rows = [dict(
        rule_arn=f"arn:aws:elasticloadbalancing:{region}:catalog:listener-rule/catalog/{r['rule_type']}/catalog",
        resource_id=f"{region}-catalog-{r['rule_type']}",
        listener_arn=f"arn:aws:elasticloadbalancing:{region}:catalog:listener/catalog/catalog",
        alb_arn=f"arn:aws:elasticloadbalancing:{region}:catalog:loadbalancer/application/catalog/catalog",
        region=region,
        priority="100" if r["category"]=="condition" else "default",
        is_default=r["category"]=="action",
        rule_type=r["rule_type"], category=r["category"], description=r["description"],
        conditions=j([{"Field":r["rule_type"],"Values":["example"]}]) if r["category"]=="condition" else j([]),
        actions=j([{"Type":r["rule_type"]}]) if r["category"]=="action" else j([{"Type":"forward"}]),
        tags=j({"Source":"aws-catalog","Region":region}),
        raw_config=j(r), source="aws-catalog"
    ) for r in rules]
    insert(conn, table, rows)

def scrape_target_group_catalog(conn, region):
    target_types = [
        {"target_type":"instance","description":"EC2 instances by instance ID"},
        {"target_type":"ip",      "description":"Private IPs (EC2, on-prem via DX/VPN, ECS tasks)"},
        {"target_type":"lambda",  "description":"Lambda function (ALB only)"},
        {"target_type":"alb",     "description":"Another ALB (NLB only)"},
    ]
    algorithms = [
        {"algorithm":"round_robin",               "description":"Default. Even distribution."},
        {"algorithm":"least_outstanding_requests","description":"Fewest active requests."},
        {"algorithm":"weighted_random",           "description":"Random with anomaly mitigation."},
    ]
    stickiness = [
        {"type":"lb_cookie",        "lb":"ALB","duration":"1-604800s"},
        {"type":"app_cookie",       "lb":"ALB","duration":"1-604800s","cookie_name":"custom"},
        {"type":"source_ip",        "lb":"NLB","duration":"N/A"},
        {"type":"source_ip_dest_ip","lb":"GWLB","duration":"N/A"},
    ]
    rows = [dict(
        tg_arn=f"arn:aws:elasticloadbalancing:{region}:catalog:targetgroup/catalog-{tt['target_type']}/catalog",
        target_group_arn=f"arn:aws:elasticloadbalancing:{region}:catalog:targetgroup/catalog-{tt['target_type']}/catalog",
        tg_name=f"catalog-{tt['target_type']}",
        resource_id=f"{region}-catalog-{tt['target_type']}",
        region=region, protocol="HTTP", protocol_version="HTTP1", port=80,
        vpc_id=None, target_type=tt["target_type"], description=tt["description"],
        ip_address_type="ipv4",
        health_check_enabled=True, health_check_protocol="HTTP",
        health_check_port="traffic-port", health_check_path="/health",
        health_check_interval=30, health_check_timeout=5,
        healthy_threshold=5, unhealthy_threshold=2, matcher_http_codes="200",
        load_balancer_arns=j([]),
        lb_algorithms=j(algorithms), stickiness_types=j(stickiness),
        health_check_protocols=j(["HTTP","HTTPS","TCP","TLS"]),
        deregistration_delay_seconds=300, slow_start_duration_seconds=0,
        attributes=j({}),
        tags=j({"Source":"aws-catalog","Region":region}),
        raw_config=j(tt), source="aws-catalog"
    ) for tt in target_types]
    for table in ("aws_alb_target_groups","alb_target_group_inventory"):
        if not region_already_done(conn, table, region):
            insert(conn, table, rows)

def scrape_target_health_catalog(conn, region):
    table = "aws_alb_target_health"
    if region_already_done(conn, table, region): return
    states = [
        {"state":"initial",    "reason":"Elb.RegistrationInProgress or InitialHealthChecking"},
        {"state":"healthy",    "reason":"Target passed all health checks"},
        {"state":"unhealthy",  "reason":"Target.ResponseCodeMismatch / Timeout / FailedHealthChecks"},
        {"state":"unused",     "reason":"Target.NotRegistered / NotInUse / InvalidState"},
        {"state":"draining",   "reason":"Target.DeregistrationInProgress"},
        {"state":"unavailable","reason":"Elb.InternalError"},
    ]
    rows = [dict(
        tg_arn=f"arn:aws:elasticloadbalancing:{region}:catalog:targetgroup/catalog-{s['state']}/catalog",
        target_group_arn=f"arn:aws:elasticloadbalancing:{region}:catalog:targetgroup/catalog-{s['state']}/catalog",
        resource_id=f"{region}-catalog-{s['state']}",
        region=region,
        target_id=f"i-catalog{s['state']}", target_port=80, target_az=f"{region}a",
        health_state=s["state"],
        health_reason_code=s["reason"].split()[0] if " " in s["reason"] else s["reason"],
        health_description=s["reason"], reason_code=s["reason"], description=s["reason"],
        raw_config=j(s), source="aws-catalog"
    ) for s in states]
    insert(conn, table, rows)

def scrape_s3_catalog(conn, region):
    # S3 is global — only store once under region="global"
    effective = "global"
    for table in ("aws_s3_buckets","s3_inventory"):
        if region_already_done(conn, table, effective): continue
        classes = [
            {"storage_class":"STANDARD",           "tier":"standard",   "min_days":None, "retrieval":"ms"},
            {"storage_class":"INTELLIGENT_TIERING","tier":"tiered",     "min_days":None, "retrieval":"ms"},
            {"storage_class":"STANDARD_IA",        "tier":"infrequent", "min_days":30,   "retrieval":"ms"},
            {"storage_class":"ONEZONE_IA",         "tier":"infrequent", "min_days":30,   "retrieval":"ms"},
            {"storage_class":"GLACIER_INSTANT",    "tier":"archive",    "min_days":90,   "retrieval":"ms"},
            {"storage_class":"GLACIER_FLEXIBLE",   "tier":"archive",    "min_days":90,   "retrieval":"1min-12hr"},
            {"storage_class":"DEEP_ARCHIVE",       "tier":"archive",    "min_days":180,  "retrieval":"12-48hr"},
            {"storage_class":"EXPRESS_ONEZONE",    "tier":"performance","min_days":None, "retrieval":"single-digit ms"},
            {"storage_class":"REDUCED_REDUNDANCY", "tier":"legacy",     "min_days":None, "retrieval":"ms"},
        ]
        rows = [dict(
            bucket_name=f"catalog-{c['storage_class'].lower()}",
            resource_id=c["storage_class"], region=effective,
            creation_date=None, storage_class=c["storage_class"],
            tier=c["tier"], min_duration_days=c["min_days"], retrieval_time=c["retrieval"],
            versioning="Disabled", mfa_delete="Disabled",
            encryption_rules=j([{"SSEAlgorithm":"AES256"}]),
            public_access_block=j({"BlockPublicAcls":True,"IgnorePublicAcls":True,
                                    "BlockPublicPolicy":True,"RestrictPublicBuckets":True}),
            bucket_policy=j({}), acl_owner="catalog", acl_grants=j([]),
            lifecycle_rules=j([]), logging_target_bucket=None,
            website_config=j({}), replication_config=j({}),
            tags=j({"Source":"aws-catalog"}), raw_config=j(c), source="aws-catalog"
        ) for c in classes]
        insert(conn, table, rows)

def scrape_vpc_catalog(ec2, conn, region):
    endpoint_svcs = safe(lambda: ec2.describe_vpc_endpoint_services(
        Filters=[{"Name":"owner","Values":["amazon"]}]).get("ServiceDetails",[]), [])
    cidr_opts = ["/16 (65536 hosts)","/20 (4096 hosts)","/24 (256 hosts)","/28 (16 hosts min)"]
    rows = [dict(
        vpc_id=f"catalog-vpc-{region}-{t}",
        resource_id=f"{region}-catalog-vpc-{t}",
        region=region, state="catalog", cidr_block="10.0.0.0/16",
        ipv6_cidr_blocks=j([]), is_default=False, dhcp_options_id="dopt-default",
        instance_tenancy=t, description=f"{'Shared' if t=='default' else 'Dedicated'} hardware tenancy",
        owner_id="catalog", cidr_options=j(cidr_opts),
        endpoint_services_count=len(endpoint_svcs),
        endpoint_services=j([s["ServiceName"] for s in endpoint_svcs[:150]]),
        tags=j({"Source":"aws-catalog","Region":region}),
        raw_config=j({"tenancy":t,"region":region}), source="aws-catalog"
    ) for t in ("default","dedicated")]
    for table in ("aws_vpcs","vpc_inventory"):
        if not region_already_done(conn, table, region):
            insert(conn, table, rows)

def scrape_subnet_catalog(ec2, conn, region):
    azs = safe(lambda: ec2.describe_availability_zones(
        AllAvailabilityZones=True).get("AvailabilityZones",[]), [])
    if not azs: return
    rows = [dict(
        subnet_id=f"catalog-{region}-{az['ZoneId']}",
        resource_id=f"{region}-{az['ZoneId']}",
        vpc_id=None, region=region,
        availability_zone=az["ZoneName"], availability_zone_id=az["ZoneId"],
        zone_type=az.get("ZoneType","availability-zone"),
        cidr_block="10.0.0.0/24", ipv6_cidr_block=None,
        available_ip_address_count=251,
        map_public_ip_on_launch=False, assign_ipv6_address_on_creation=False,
        state=az.get("State","available"), is_default=False, owner_id="catalog",
        subnet_arn=f"arn:aws:ec2:{region}:catalog:subnet/catalog-{az['ZoneId']}",
        outpost_arn=None, region_name=az.get("RegionName"),
        supported_cidr_blocks=j(["/20","/22","/24","/26","/28"]),
        tags=j({"Source":"aws-catalog","Region":region,"AZ":az["ZoneName"]}),
        raw_config=j(az), source="aws-catalog"
    ) for az in azs]
    for table in ("aws_subnets","subnet_inventory"):
        if not region_already_done(conn, table, region):
            insert(conn, table, rows)

def scrape_igw_catalog(conn, region):
    states = ["attached","detached","attaching","detaching"]
    rows = [dict(
        igw_id=f"catalog-igw-{region}-{s}",
        internet_gateway_id=f"catalog-igw-{region}-{s}",
        resource_id=f"{region}-catalog-igw-{s}",
        region=region, owner_id="catalog", state=s,
        description=f"IGW state: {s}",
        vpc_id="vpc-catalog" if s=="attached" else None,
        tags=j({"Source":"aws-catalog","Region":region}),
        raw_config=j({"state":s,"region":region}), source="aws-catalog"
    ) for s in states]
    for table in ("aws_internet_gateways","igw_inventory"):
        if not region_already_done(conn, table, region):
            insert(conn, table, rows)

def scrape_route_table_catalog(conn, region):
    table = "route_table_inventory"
    if region_already_done(conn, table, region): return
    targets = [
        {"target":"igw",             "prefix":"igw-",  "description":"Internet Gateway — public internet"},
        {"target":"nat-gateway",     "prefix":"nat-",  "description":"NAT Gateway — private subnet internet access"},
        {"target":"vpc-peering",     "prefix":"pcx-",  "description":"VPC Peering connection"},
        {"target":"transit-gateway", "prefix":"tgw-",  "description":"Transit Gateway — hub and spoke"},
        {"target":"vpn-gateway",     "prefix":"vgw-",  "description":"Virtual Private Gateway — Site-to-Site VPN"},
        {"target":"vpc-endpoint",    "prefix":"vpce-", "description":"Gateway Endpoint (S3, DynamoDB)"},
        {"target":"local",           "prefix":"local", "description":"Local VPC CIDR — always present"},
        {"target":"network-interface","prefix":"eni-", "description":"Elastic Network Interface"},
        {"target":"egress-only-igw", "prefix":"eigw-", "description":"Egress-only IGW for IPv6 outbound"},
        {"target":"instance",        "prefix":"i-",    "description":"EC2 instance (for NAT instances)"},
    ]
    rows = [dict(
        route_table_id=f"catalog-rt-{region}-{t['target']}",
        resource_id=f"{region}-catalog-rt-{t['target']}",
        target_type=t["target"], target_prefix=t["prefix"], description=t["description"],
        region=region, raw_config=j(t), source="aws-catalog"
    ) for t in targets]
    insert(conn, table, rows)

def scrape_security_group_catalog(conn, region):
    table = "security_group_inventory"
    if region_already_done(conn, table, region): return
    protocols = [
        {"protocol":"tcp",   "number":6,   "common_ports":"22,80,443,3306,5432,6379,27017,8080,8443"},
        {"protocol":"udp",   "number":17,  "common_ports":"53,123,500,4500,1194"},
        {"protocol":"icmp",  "number":1,   "common_ports":"N/A (type/code)"},
        {"protocol":"icmpv6","number":58,  "common_ports":"N/A (type/code)"},
        {"protocol":"all",   "number":-1,  "common_ports":"All ports all protocols"},
        {"protocol":"esp",   "number":50,  "common_ports":"IPsec VPN"},
        {"protocol":"ah",    "number":51,  "common_ports":"IPsec VPN"},
        {"protocol":"sctp",  "number":132, "common_ports":"Telecom signaling"},
    ]
    rows = [dict(
        group_id=f"catalog-sg-{region}-{p['protocol']}",
        resource_id=f"{region}-catalog-sg-{p['protocol']}",
        protocol=p["protocol"], protocol_number=p["number"], common_ports=p["common_ports"],
        region=region, raw_config=j(p), source="aws-catalog"
    ) for p in protocols]
    insert(conn, table, rows)

def scrape_lambda_catalog(lam, conn, region):
    targets = ["lambda_inventory", "aws_lambda_functions"]
    targets = [t for t in targets if not region_already_done(conn, t, region)]
    if not targets: return
    limits = safe(lambda: lam.get_account_settings().get("AccountLimit",{}), {})
    runtimes = [
        {"runtime":"nodejs20.x",     "language":"Node.js 20","arch":["x86_64","arm64"],"status":"supported"},
        {"runtime":"nodejs18.x",     "language":"Node.js 18","arch":["x86_64","arm64"],"status":"supported"},
        {"runtime":"python3.12",     "language":"Python 3.12","arch":["x86_64","arm64"],"status":"supported"},
        {"runtime":"python3.11",     "language":"Python 3.11","arch":["x86_64","arm64"],"status":"supported"},
        {"runtime":"python3.10",     "language":"Python 3.10","arch":["x86_64","arm64"],"status":"supported"},
        {"runtime":"python3.9",      "language":"Python 3.9", "arch":["x86_64","arm64"],"status":"supported"},
        {"runtime":"java21",         "language":"Java 21",    "arch":["x86_64","arm64"],"status":"supported"},
        {"runtime":"java17",         "language":"Java 17",    "arch":["x86_64","arm64"],"status":"supported"},
        {"runtime":"java11",         "language":"Java 11",    "arch":["x86_64","arm64"],"status":"supported"},
        {"runtime":"dotnet8",        "language":".NET 8",     "arch":["x86_64","arm64"],"status":"supported"},
        {"runtime":"dotnet6",        "language":".NET 6",     "arch":["x86_64","arm64"],"status":"supported"},
        {"runtime":"ruby3.3",        "language":"Ruby 3.3",   "arch":["x86_64","arm64"],"status":"supported"},
        {"runtime":"ruby3.2",        "language":"Ruby 3.2",   "arch":["x86_64","arm64"],"status":"supported"},
        {"runtime":"go1.x",          "language":"Go 1.x",     "arch":["x86_64"],        "status":"deprecated"},
        {"runtime":"provided.al2023","language":"Custom AL2023","arch":["x86_64","arm64"],"status":"supported"},
        {"runtime":"provided.al2",   "language":"Custom AL2",  "arch":["x86_64","arm64"],"status":"supported"},
    ]
    rows = [dict(
        function_arn=f"arn:aws:lambda:{region}:catalog:function:{r['runtime']}",
        function_name=r["runtime"], resource_id=f"{region}-{r['runtime']}",
        runtime=r["runtime"], language=r["language"],
        supported_architectures=j(r["arch"]), status=r["status"],
        memory_min_mb=128, memory_max_mb=10240, timeout_max_seconds=900,
        ephemeral_storage_max_mb=10240,
        concurrency_limit=limits.get("ConcurrentExecutions"),
        raw_config=j({**r,"account_limits":limits}),
        region=region, source="aws-catalog"
    ) for r in runtimes]
    for t in targets:
        insert(conn, t, rows)

def scrape_dynamodb_catalog(conn, region):
    table = "aws_dynamodb_tables"
    if region_already_done(conn, table, region): return
    billing_modes = [
        {"billing_mode":"PROVISIONED", "table_class":"STANDARD",
         "description":"Fixed read/write capacity units (RCU/WCU). Best for predictable workloads.",
         "use_case":"Predictable traffic, consistent throughput requirements"},
        {"billing_mode":"PROVISIONED", "table_class":"STANDARD_INFREQUENT_ACCESS",
         "description":"Lower storage cost for infrequently accessed data with provisioned capacity.",
         "use_case":"Historical data, audit logs, infrequent reads"},
        {"billing_mode":"PAY_PER_REQUEST", "table_class":"STANDARD",
         "description":"On-demand capacity. Auto-scales. Pay per read/write request.",
         "use_case":"Unpredictable traffic, new applications, serverless architectures"},
        {"billing_mode":"PAY_PER_REQUEST", "table_class":"STANDARD_INFREQUENT_ACCESS",
         "description":"On-demand + lower storage cost for infrequent access.",
         "use_case":"Unpredictable traffic with infrequently accessed data"},
    ]
    rows = [dict(
        table_arn=f"arn:aws:dynamodb:{region}:catalog:table/catalog-{b['billing_mode']}-{b['table_class']}",
        table_name=f"catalog-{b['billing_mode'].lower()}-{b['table_class'].lower()}",
        region=region, table_status="ACTIVE",
        billing_mode=b["billing_mode"], table_class=b["table_class"],
        read_capacity_units=5 if b["billing_mode"]=="PROVISIONED" else None,
        write_capacity_units=5 if b["billing_mode"]=="PROVISIONED" else None,
        stream_enabled=False, encryption_type="DEFAULT",
        point_in_time_recovery=False, ttl_enabled=False,
        description=b["description"], use_case=b["use_case"],
        tags=j({"Source":"aws-catalog","Region":region}),
        raw_config=j(b), source="aws-catalog"
    ) for b in billing_modes]
    insert(conn, table, rows)

def scrape_api_gateway_catalog(conn, region):
    table = "aws_api_gateways"
    if region_already_done(conn, table, region): return
    api_types = [
        {"protocol_type":"HTTP",      "endpoint_type":"REGIONAL",
         "description":"HTTP API — low-latency, cost-effective RESTful APIs with native OIDC/OAuth2.",
         "use_case":"Serverless backends, Lambda proxy, low-cost REST APIs",
         "auth_types":["JWT","IAM","None"],
         "route_selection_expression":"${request.method} ${request.path}"},
        {"protocol_type":"REST",      "endpoint_type":"REGIONAL",
         "description":"REST API — full-featured API with request/response transformations, caching, WAF.",
         "use_case":"Enterprise APIs, request validation, API keys, usage plans",
         "auth_types":["IAM","Cognito","Lambda Authorizer","API Key","None"],
         "route_selection_expression":None},
        {"protocol_type":"REST",      "endpoint_type":"EDGE",
         "description":"REST API with CloudFront edge-optimized endpoint for global clients.",
         "use_case":"Global APIs with CloudFront caching, geographically distributed clients",
         "auth_types":["IAM","Cognito","Lambda Authorizer","API Key","None"],
         "route_selection_expression":None},
        {"protocol_type":"REST",      "endpoint_type":"PRIVATE",
         "description":"REST API accessible only within a VPC via VPC endpoint.",
         "use_case":"Internal microservices, VPC-only APIs",
         "auth_types":["IAM","Cognito","Lambda Authorizer","None"],
         "route_selection_expression":None},
        {"protocol_type":"WEBSOCKET", "endpoint_type":"REGIONAL",
         "description":"WebSocket API — persistent 2-way connections for real-time communication.",
         "use_case":"Chat apps, real-time dashboards, gaming, streaming",
         "auth_types":["IAM","Lambda Authorizer","None"],
         "route_selection_expression":"${request.body.action}"},
    ]
    rows = [dict(
        api_id=f"catalog-{region}-{a['protocol_type']}-{a['endpoint_type']}",
        api_name=f"catalog-{a['protocol_type']}-{a['endpoint_type']}",
        region=region, protocol_type=a["protocol_type"],
        endpoint_type=a["endpoint_type"],
        description=a["description"], use_case=a["use_case"],
        auth_types=j(a["auth_types"]),
        route_selection_expression=a["route_selection_expression"],
        status="catalog",
        tags=j({"Source":"aws-catalog","Region":region}),
        raw_config=j(a), source="aws-catalog"
    ) for a in api_types]
    insert(conn, table, rows)

def scrape_ecs_catalog(conn, region):
    table = "ecs_cluster_inventory"
    if region_already_done(conn, table, region): return
    fargate_configs = [
        {"vcpu":0.25,"vcpu_units":256, "memory_gib":[0.5,1,2]},
        {"vcpu":0.5, "vcpu_units":512, "memory_gib":[1,2,3,4]},
        {"vcpu":1,   "vcpu_units":1024,"memory_gib":list(range(2,9))},
        {"vcpu":2,   "vcpu_units":2048,"memory_gib":list(range(4,17))},
        {"vcpu":4,   "vcpu_units":4096,"memory_gib":list(range(8,31))},
        {"vcpu":8,   "vcpu_units":8192,"memory_gib":list(range(16,61))},
        {"vcpu":16,  "vcpu_units":16384,"memory_gib":list(range(32,121))},
    ]
    rows = [dict(
        cluster_arn=f"arn:aws:ecs:{region}:catalog:cluster/fargate-{fc['vcpu']}vcpu",
        cluster_name=f"fargate-{fc['vcpu']}vcpu",
        resource_id=f"{region}-fargate-{fc['vcpu']}vcpu",
        launch_type="FARGATE", vcpu=fc["vcpu"], vcpu_units=fc["vcpu_units"],
        memory_options_gib=j(fc["memory_gib"]),
        supported_network_modes=j(["awsvpc"]),
        raw_config=j(fc), status="ACTIVE", region=region, source="aws-catalog"
    ) for fc in fargate_configs]
    insert(conn, table, rows)

def scrape_eks_catalog(eks, conn, region):
    table = "eks_cluster_inventory"
    if region_already_done(conn, table, region): return
    addons = safe(lambda: eks.describe_addon_versions().get("Addons",[]), [])
    k8s_versions = ["1.30","1.29","1.28","1.27","1.26","1.25"]
    rows = [dict(
        cluster_arn=f"arn:aws:eks:{region}:catalog:cluster/eks-{v}",
        cluster_name=f"eks-k8s-{v}",
        resource_id=f"{region}-eks-{v}",
        kubernetes_version=v, status="ACTIVE",
        supported_addons=j([a.get("AddonName") for a in addons]),
        supported_ami_types=j(["AL2_x86_64","AL2_x86_64_GPU","AL2_ARM_64",
                                "BOTTLEROCKET_x86_64","BOTTLEROCKET_ARM_64",
                                "WINDOWS_CORE_2019_x86_64","WINDOWS_FULL_2022_x86_64"]),
        raw_config=j({"kubernetes_version":v,"addons":addons}),
        region=region, source="aws-catalog"
    ) for v in k8s_versions]
    insert(conn, table, rows)

def scrape_sns_catalog(conn, region):
    table = "sns_inventory"
    if region_already_done(conn, table, region): return
    protocols = [
        {"protocol":"http",       "description":"HTTP endpoint (POST JSON)"},
        {"protocol":"https",      "description":"HTTPS endpoint (POST JSON)"},
        {"protocol":"email",      "description":"Email (plain text)"},
        {"protocol":"email-json", "description":"Email (JSON formatted)"},
        {"protocol":"sms",        "description":"SMS text message"},
        {"protocol":"sqs",        "description":"SQS queue"},
        {"protocol":"lambda",     "description":"Lambda function"},
        {"protocol":"firehose",   "description":"Kinesis Data Firehose"},
        {"protocol":"application","description":"Mobile push (APNS, FCM, ADM, WNS)"},
    ]
    rows = [dict(
        topic_arn=f"arn:aws:sns:{region}:catalog:{p['protocol']}",
        resource_id=f"{region}-catalog-{p['protocol']}",
        protocol=p["protocol"], description=p["description"],
        region=region, raw_config=j(p), source="aws-catalog"
    ) for p in protocols]
    insert(conn, table, rows)

def scrape_elasticache_catalog(conn, region):
    table = "aws_elasticache_clusters"
    if region_already_done(conn, table, region): return
    engines = [
        {"engine":"redis", "versions":["7.0","6.2"],"ports":[6379]},
        {"engine":"memcached", "versions":["1.6"],"ports":[11211]},
    ]
    node_types = ["cache.t3.micro", "cache.t3.small", "cache.m5.large"]
    
    rows = []
    for eng in engines:
        for ver in eng["versions"]:
            for nt in node_types:
                cid = f"catalog-{region}-{eng['engine']}-{ver}-{nt}".replace(".","-")
                # Max cluster_id limit is 20 chars if replication group, but for catalog we can have more.
                # Usually ElastiCache limits id to 20-40 chars. Let's just use it as is for catalog.
                rows.append(dict(
                    cluster_id=f"catalog-{eng['engine'][:3]}-{nt.replace('cache.','')}",
                    cluster_name=f"Catalog {eng['engine'].title()} {nt}",
                    region=region, engine=eng["engine"], engine_version=ver,
                    node_type=nt, num_cache_nodes=1, status="available",
                    port=eng["ports"][0], subnet_group_name="default",
                    preferred_availability_zone=f"{region}a",
                    preferred_maintenance_window="sun:23:00-mon:01:30",
                    snapshot_retention_limit=0, snapshot_window="05:00-09:00",
                    tags=j({"Source":"aws-catalog","Region":region}),
                    raw_config=j({"engine":eng["engine"], "node_type":nt, "version":ver}),
                    source="aws-catalog"
                ))
    insert(conn, table, rows)

def scrape_eventbridge_catalog(conn, region):
    table = "aws_eventbridge_rules"
    if region_already_done(conn, table, region): return
    rule_templates = [
        {"name": "scheduled-daily", "schedule": "rate(1 day)", "desc": "Daily scheduled rule",
         "pattern": None, "bus": "default"},
        {"name": "scheduled-hourly", "schedule": "rate(1 hour)", "desc": "Hourly scheduled rule",
         "pattern": None, "bus": "default"},
        {"name": "ec2-state-change", "schedule": None, "desc": "EC2 instance state change",
         "pattern": '{"source":["aws.ec2"],"detail-type":["EC2 Instance State-change Notification"]}',
         "bus": "default"},
        {"name": "s3-object-created", "schedule": None, "desc": "S3 object creation events",
         "pattern": '{"source":["aws.s3"],"detail-type":["Object Created"]}',
         "bus": "default"},
        {"name": "ecs-task-state", "schedule": None, "desc": "ECS task state change",
         "pattern": '{"source":["aws.ecs"],"detail-type":["ECS Task State Change"]}',
         "bus": "default"},
        {"name": "custom-app-events", "schedule": None, "desc": "Custom application events",
         "pattern": '{"source":["com.myapp"],"detail-type":["OrderCreated","OrderUpdated"]}',
         "bus": "custom-app-bus"},
    ]

    rows = []
    for tmpl in rule_templates:
        arn = f"arn:aws:events:{region}:123456789012:rule/{tmpl['bus']}/{tmpl['name']}"
        rows.append(dict(
            rule_arn=arn,
            rule_name=tmpl["name"],
            region=region,
            event_bus_name=tmpl["bus"],
            description=tmpl["desc"],
            state="ENABLED",
            schedule_expression=tmpl["schedule"] or "",
            event_pattern=tmpl["pattern"] or j({}),
            role_arn=f"arn:aws:iam::123456789012:role/EventBridgeRole",
            managed_by=None,
            targets=j([{"Arn": f"arn:aws:lambda:{region}:123456789012:function:handler", "Id": "target-1"}]),
            tags=j({"Source": "aws-catalog", "Region": region}),
            raw_config=j(tmpl),
            source="aws-catalog"
        ))
    insert(conn, table, rows)

def scrape_sqs_catalog(conn, region):
    table = "sqs_inventory"
    if region_already_done(conn, table, region): return
    queue_types = [
        {"queue_type":"standard","fifo":False,"max_throughput":"Unlimited (nearly)",
         "ordering":"Best-effort","delivery":"At-least-once",
         "max_message_size_kb":256,"max_retention_seconds":1209600,
         "default_visibility_seconds":30,"max_visibility_seconds":43200,"max_delay_seconds":900,
         "use_case":"High throughput, loose ordering OK"},
        {"queue_type":"fifo","fifo":True,"max_throughput":"3000/s batched, 300/s unbatched",
         "ordering":"Strict FIFO per MessageGroupId","delivery":"Exactly-once",
         "max_message_size_kb":256,"max_retention_seconds":1209600,
         "default_visibility_seconds":30,"max_visibility_seconds":43200,"max_delay_seconds":900,
         "use_case":"Strict ordering required (financial, order processing)"},
    ]
    rows = [dict(
        queue_url=f"https://sqs.{region}.amazonaws.com/catalog/catalog-{q['queue_type']}",
        queue_name=f"catalog-{q['queue_type']}",
        resource_id=f"{region}-catalog-{q['queue_type']}",
        queue_type=q["queue_type"], fifo_queue=q["fifo"],
        max_message_size_kb=q["max_message_size_kb"],
        max_retention_seconds=q["max_retention_seconds"],
        default_visibility_timeout=q["default_visibility_seconds"],
        max_delay_seconds=q["max_delay_seconds"],
        max_throughput=q["max_throughput"], ordering_guarantee=q["ordering"],
        delivery_guarantee=q["delivery"], use_case=q["use_case"],
        region=region, raw_config=j(q), source="aws-catalog"
    ) for q in queue_types]
    insert(conn, table, rows)

# ─── GLOBAL-ONLY SCRAPERS (run once, not per region) ─────────────────────────

def scrape_iam_catalog(iam, conn):
    table = "iam_role_inventory"
    if not region_already_done(conn, table, "global"):
        log.info("  Scraping IAM managed policies (global)...")
        policies = pages(iam,"list_policies","Policies",Scope="AWS")
        rows = [dict(
            role_id=p.get("PolicyId"), role_name=p.get("PolicyName"),
            resource_id=p.get("PolicyId"), arn=p.get("Arn"),
            description=p.get("Description"), path=p.get("Path"),
            attachment_count=p.get("AttachmentCount"), is_attachable=p.get("IsAttachable"),
            create_date=p.get("CreateDate"), update_date=p.get("UpdateDate"),
            raw_config=j(p), region="global", source="aws-managed-policy"
        ) for p in policies]
        insert(conn, table, rows)

    table = "iam_user_inventory"
    if not region_already_done(conn, table, "global"):
        log.info("  Scraping IAM MFA device types (global)...")
        mfa_types = [
            {"type":"virtual",  "description":"TOTP virtual MFA (Google Authenticator, Authy)"},
            {"type":"hardware", "description":"Hardware MFA token (Gemalto)"},
            {"type":"u2f",      "description":"FIDO U2F security key (YubiKey)"},
            {"type":"passkey",  "description":"Passkey / FIDO2 biometric"},
        ]
        rows = [dict(
            user_id=f"catalog-mfa-{m['type']}", user_name=f"mfa-{m['type']}",
            resource_id=f"catalog-mfa-{m['type']}", mfa_type=m["type"],
            description=m["description"], raw_config=j(m), region="global", source="aws-catalog"
        ) for m in mfa_types]
        insert(conn, table, rows)

def scrape_aws_inventory_global(ec2_global, conn):
    table = "aws_inventory"
    if region_already_done(conn, table, "global"): return
    log.info("  Scraping regions + AZs + placement groups → aws_inventory...")
    regions = safe(lambda: ec2_global.describe_regions(AllRegions=True).get("Regions",[]),[])
    azs     = safe(lambda: ec2_global.describe_availability_zones(
        AllAvailabilityZones=True).get("AvailabilityZones",[]),[])
    rows = []
    for r in regions:
        rows.append({"resource_id":r["RegionName"],"resource_type":"REGION",
                     "region":r["RegionName"],"raw_config":j(r),"source":"aws-catalog"})
    for az in azs:
        rows.append({"resource_id":az["ZoneId"],"resource_type":"AVAILABILITY_ZONE",
                     "region":az.get("RegionName"),"raw_config":j(az),"source":"aws-catalog"})
    for strategy in ["cluster","spread","partition"]:
        rows.append({"resource_id":f"placement-{strategy}","resource_type":"PLACEMENT_GROUP_STRATEGY",
                     "region":"global","raw_config":j({"strategy":strategy}),"source":"aws-catalog"})
    insert(conn, table, rows)

# ─── MAIN ────────────────────────────────────────────────────────────────────

def main():
    t0 = datetime.now(timezone.utc)
    log.info("══════════════════════════════════════════════════════════")
    log.info("  AWS Meta Scraper — ALL REGIONS")
    log.info("══════════════════════════════════════════════════════════")
    if FORCE_TABLE:  log.info("  FORCE_TABLE  = %s", FORCE_TABLE)
    if FORCE_REGION: log.info("  FORCE_REGION = %s", FORCE_REGION)

    db_cfg = get_db_config()
    conn = psycopg2.connect(**db_cfg)
    log.info("✔ DB: %s@%s:%s/%s\n", db_cfg["user"], db_cfg["host"], db_cfg["port"], db_cfg["dbname"])

    base_session = boto3.Session()

    # ── Global scrapers (once only) ───────────────────────────────────────────
    log.info("── Global resources (IAM, regions, AZs) ──────────────────")
    iam        = base_session.client("iam", region_name="us-east-1")
    ec2_global = base_session.client("ec2", region_name="us-east-1")
    scrape_iam_catalog(iam, conn)
    scrape_aws_inventory_global(ec2_global, conn)
    scrape_s3_catalog(conn, "global")   # S3 storage classes are global

    # ── Per-region scrapers ───────────────────────────────────────────────────
    all_regions = get_all_regions(base_session)
    log.info("\n── Per-region scrape: %d regions ─────────────────────────", len(all_regions))

    for idx, reg in enumerate(all_regions, 1):
        region     = reg["RegionName"]
        reg_status = reg.get("OptInStatus","opt-in-not-required")

        log.info("\n[%d/%d] %-20s (%s)", idx, len(all_regions), region, reg_status)

        # Skip opted-out regions (they'll reject API calls anyway)
        if reg_status == "not-opted-in":
            log.info("  ⏭  Region not opted-in — skipping")
            continue

        try:
            session = boto3.Session(region_name=region)
            ec2 = session.client("ec2")
            rds = session.client("rds")
            elb = session.client("elbv2")
            lam = session.client("lambda")
            eks = session.client("eks")

            scrape_ec2_instance_types(ec2, conn, region)
            scrape_ebs_types(conn, region)
            # scrape_rds_catalog(rds, conn, region)
            scrape_alb_catalog(elb, conn, region)
            scrape_listener_catalog(elb, conn, region)
            scrape_listener_rules_catalog(conn, region)
            scrape_target_group_catalog(conn, region)
            scrape_target_health_catalog(conn, region)
            scrape_vpc_catalog(ec2, conn, region)
            scrape_subnet_catalog(ec2, conn, region)
            scrape_igw_catalog(conn, region)
            scrape_route_table_catalog(conn, region)
            scrape_security_group_catalog(conn, region)
            scrape_lambda_catalog(lam, conn, region)
            scrape_dynamodb_catalog(conn, region)
            scrape_api_gateway_catalog(conn, region)
            scrape_ecs_catalog(conn, region)
            scrape_eks_catalog(eks, conn, region)
            scrape_sns_catalog(conn, region)
            scrape_sqs_catalog(conn, region)
            scrape_elasticache_catalog(conn, region)
            scrape_eventbridge_catalog(conn, region)

        except Exception as e:
            log.error("  ✗ Region %s failed: %s — continuing", region, e)
            conn.rollback()
            continue

    elapsed = (datetime.now(timezone.utc) - t0).total_seconds()
    log.info("\n══════════════════════════════════════════════════════════")
    log.info("  Done in %.1fs (%.1f min)", elapsed, elapsed/60)
    log.info("══════════════════════════════════════════════════════════")
    conn.close()

if __name__ == "__main__":
    main()