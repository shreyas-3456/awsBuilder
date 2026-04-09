import { useState, useEffect } from 'react';
import { configService, CatalogItem, Ec2Instance, Vpc, Subnet, S3Bucket, Alb, InternetGateway } from '../services/configService';
import { ResourceType } from '../types/diagram';
import { Search, RefreshCw, AlertCircle, Package } from 'lucide-react';

interface CatalogSelectorProps {
  resourceType: ResourceType;
  onSelect: (catalogItem: CatalogItem) => void;
  filters?: {
    region?: string;
    vpcId?: string;
    albArn?: string;
    instanceId?: string;
  };
}

interface CatalogSelectorState {
  catalogItems: CatalogItem[];
  loading: boolean;
  error: string | null;
  selectedRegion: string | null;
  searchQuery: string;
}

/**
 * CatalogSelector component for displaying and selecting AWS catalog resources.
 * Fetches catalog data from backend API and displays items in a searchable list.
 */
export function CatalogSelector({ resourceType, onSelect, filters }: CatalogSelectorProps) {
  const [state, setState] = useState<CatalogSelectorState>({
    catalogItems: [],
    loading: true,
    error: null,
    selectedRegion: filters?.region || null,
    searchQuery: ''
  });

  // Fetch catalog data on mount and when filters change
  useEffect(() => {
    setState(prev => ({ ...prev, selectedRegion: filters?.region || null }));
    fetchCatalogData();
  }, [resourceType, filters?.region, filters?.vpcId]);

  const fetchCatalogData = async () => {
    setState(prev => ({ ...prev, loading: true, error: null }));
    try {
      let items: CatalogItem[] = [];

      switch (resourceType) {
        case 'EC2':
          items = await configService.getEc2Instances(filters?.region);
          break;
        case 'SUBNET':
          items = await configService.getSubnets(filters?.vpcId, filters?.region);
          break;
        case 'VPC':
          items = await configService.getVpcs(filters?.region);
          break;
        case 'S3':
          items = await configService.getS3Buckets(filters?.region);
          break;
        case 'INTERNET_GATEWAY':
          items = await configService.getInternetGateways(filters?.vpcId);
          break;
        case 'LOAD_BALANCER':
          items = await configService.getAlbs(filters?.vpcId, filters?.region);
          break;
        default:
          items = [];
      }

      setState(prev => ({
        ...prev,
        catalogItems: items,
        loading: false
      }));
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Failed to load catalog data';
      setState(prev => ({
        ...prev,
        error: errorMessage,
        loading: false,
        catalogItems: []
      }));
    }
  };

  const handleRefresh = () => {
    fetchCatalogData();
  };

  // Get unique regions from catalog items
  const getAvailableRegions = (): string[] => {
    const regions = new Set<string>();
    state.catalogItems.forEach(item => {
      if ('region' in item && item.region) {
        regions.add(item.region);
      }
    });
    return Array.from(regions).sort();
  };

  // Filter items based on search query and selected region
  const getFilteredItems = (): CatalogItem[] => {
    let filtered = state.catalogItems;

    // Apply region filter
    if (state.selectedRegion) {
      filtered = filtered.filter(item => {
        if ('region' in item) {
          return item.region === state.selectedRegion;
        }
        return true;
      });
    }

    // Apply search filter
    if (state.searchQuery) {
      const query = state.searchQuery.toLowerCase();
      filtered = filtered.filter(item => {
        const searchableFields = getSearchableFields(item);
        return searchableFields.some(field => 
          String(field).toLowerCase().includes(query)
        );
      });
    }

    return filtered;
  };

  // Get searchable fields for an item based on its type
  const getSearchableFields = (item: CatalogItem): string[] => {
    switch (resourceType) {
      case 'EC2':
        return [(item as Ec2Instance).instanceId, (item as Ec2Instance).name];
      case 'SUBNET':
        return [(item as Subnet).subnetId, (item as Subnet).cidrBlock];
      case 'VPC':
        return [(item as Vpc).vpcId, (item as Vpc).cidrBlock];
      case 'S3':
        return [(item as S3Bucket).bucketName];
      case 'LOAD_BALANCER':
        return [(item as Alb).albArn, (item as Alb).albName];
      case 'INTERNET_GATEWAY':
        return [(item as InternetGateway).igwId];
      default:
        return [];
    }
  };

  // Get display name for an item
  const getItemDisplayName = (item: CatalogItem): string => {
    switch (resourceType) {
      case 'EC2':
        return (item as Ec2Instance).name || (item as Ec2Instance).instanceId;
      case 'SUBNET':
        return (item as Subnet).subnetId;
      case 'VPC':
        return (item as Vpc).vpcId;
      case 'S3':
        return (item as S3Bucket).bucketName;
      case 'LOAD_BALANCER':
        return (item as Alb).albName;
      case 'INTERNET_GATEWAY':
        return (item as InternetGateway).igwId;
      default:
        return 'Unknown';
    }
  };

  // Get display details for an item
  const getItemDetails = (item: CatalogItem): string => {
    switch (resourceType) {
      case 'EC2': {
        const ec2 = item as Ec2Instance;
        return `${ec2.instanceType} • ${ec2.state} • ${ec2.region}`;
      }
      case 'SUBNET': {
        const subnet = item as Subnet;
        return `${subnet.cidrBlock} • ${subnet.availabilityZone}`;
      }
      case 'VPC': {
        const vpc = item as Vpc;
        return `${vpc.cidrBlock} • ${vpc.region}`;
      }
      case 'S3': {
        const s3 = item as S3Bucket;
        return `${s3.region}`;
      }
      case 'LOAD_BALANCER': {
        const alb = item as Alb;
        return `${alb.scheme} • ${alb.state} • ${alb.region}`;
      }
      case 'INTERNET_GATEWAY': {
        const igw = item as InternetGateway;
        return `${igw.state} • ${igw.region}`;
      }
      default:
        return '';
    }
  };

  const availableRegions = getAvailableRegions();
  const filteredItems = getFilteredItems();

  return (
    <div className="mb-6 p-4 rounded-xl bg-slate-800/40 border border-slate-700/40">
      <div className="flex items-center justify-between mb-3">
        <h3 className="text-sm font-semibold text-slate-300">Catalog Resources</h3>
        <button
          onClick={handleRefresh}
          disabled={state.loading}
          className="p-1.5 rounded-lg hover:bg-slate-700/50 text-slate-400 hover:text-slate-300 transition-all disabled:opacity-50"
          title="Refresh catalog data"
        >
          <RefreshCw className={`w-4 h-4 ${state.loading ? 'animate-spin' : ''}`} />
        </button>
      </div>

      {/* Region Filter */}
      {availableRegions.length > 1 && (
        <div className="mb-3">
          <label className="block text-xs font-semibold text-slate-400 mb-1.5">Region</label>
          <select
            value={state.selectedRegion || ''}
            onChange={(e) => setState(prev => ({ ...prev, selectedRegion: e.target.value || null }))}
            className="w-full px-2.5 py-1.5 rounded-lg bg-slate-700/50 border border-slate-600/50 text-slate-200 text-xs focus:outline-none focus:ring-1 focus:ring-indigo-500 transition-all"
          >
            <option value="">All Regions</option>
            {availableRegions.map(region => (
              <option key={region} value={region}>{region}</option>
            ))}
          </select>
        </div>
      )}

      {/* Search Input */}
      <div className="mb-3 relative">
        <Search className="absolute left-2.5 top-2.5 w-4 h-4 text-slate-500" />
        <input
          type="text"
          placeholder="Search resources..."
          value={state.searchQuery}
          onChange={(e) => setState(prev => ({ ...prev, searchQuery: e.target.value }))}
          className="w-full pl-8 pr-3 py-1.5 rounded-lg bg-slate-700/50 border border-slate-600/50 text-slate-200 text-xs placeholder-slate-500 focus:outline-none focus:ring-1 focus:ring-indigo-500 transition-all"
        />
      </div>

      {/* Loading State */}
      {state.loading && (
        <div className="flex items-center justify-center py-6">
          <div className="animate-spin rounded-full h-5 w-5 border-2 border-indigo-500 border-t-transparent"></div>
          <span className="ml-2 text-xs text-slate-400">Loading catalog data...</span>
        </div>
      )}

      {/* Error State */}
      {state.error && !state.loading && (
        <div className="p-3 rounded-lg bg-rose-500/10 border border-rose-500/20 mb-3">
          <div className="flex items-start gap-2">
            <AlertCircle className="w-4 h-4 text-rose-400 mt-0.5 flex-shrink-0" />
            <div>
              <p className="text-xs font-semibold text-rose-400">Failed to load catalog</p>
              <p className="text-xs text-rose-300/80 mt-0.5">{state.error}</p>
            </div>
          </div>
        </div>
      )}

      {/* Empty State */}
      {!state.loading && !state.error && filteredItems.length === 0 && (
        <div className="flex flex-col items-center justify-center py-6 text-center">
          <Package className="w-8 h-8 text-slate-600 mb-2" />
          <p className="text-xs text-slate-400">No catalog resources available</p>
          <p className="text-xs text-slate-500 mt-1">Run the inventory collector to populate data</p>
        </div>
      )}

      {/* Catalog Items List */}
      {!state.loading && !state.error && filteredItems.length > 0 && (
        <div className="space-y-1.5 max-h-64 overflow-y-auto">
          {filteredItems.map((item, index) => (
            <button
              key={index}
              onClick={() => onSelect(item)}
              className="w-full text-left p-2.5 rounded-lg bg-slate-700/30 hover:bg-slate-700/60 border border-slate-600/30 hover:border-slate-600/60 transition-all group"
            >
              <p className="text-xs font-semibold text-slate-200 group-hover:text-indigo-300 truncate">
                {getItemDisplayName(item)}
              </p>
              <p className="text-xs text-slate-400 mt-0.5 truncate">
                {getItemDetails(item)}
              </p>
            </button>
          ))}
        </div>
      )}

      {/* Item Count */}
      {!state.loading && !state.error && state.catalogItems.length > 0 && (
        <p className="text-xs text-slate-500 mt-2 text-center">
          {filteredItems.length} of {state.catalogItems.length} resources
        </p>
      )}
    </div>
  );
}
