import { useCallback } from 'react';
import { useReactFlow } from 'reactflow';
import { Trash2, Copy, Plus } from 'lucide-react';
import { ResourceType } from '../types/diagram';

export interface ContextMenuProps {
  id?: string;
  top?: number;
  left?: number;
  right?: number;
  bottom?: number;
  type?: 'node' | 'pane' | 'edge';
  onClose: () => void;
  onAddResource?: (type: ResourceType) => void;
}

export function ContextMenu({
  id,
  top,
  left,
  right,
  bottom,
  type,
  onClose,
  onAddResource,
}: ContextMenuProps) {
  const { setNodes, setEdges, getNode } = useReactFlow();

  const deleteNode = useCallback(() => {
    setNodes((nodes) => nodes.filter((node) => node.id !== id));
    setEdges((edges) => edges.filter((edge) => edge.source !== id && edge.target !== id));
    onClose();
  }, [id, setNodes, setEdges, onClose]);

  const deleteEdge = useCallback(() => {
    setEdges((edges) => edges.filter((edge) => edge.id !== id));
    onClose();
  }, [id, setEdges, onClose]);

  const duplicateNode = useCallback(() => {
    const node = getNode(id!);
    if (!node) return;

    const newNode = {
      ...node,
      id: `${node.data.type.toLowerCase()}-${Date.now()}`,
      position: {
        x: node.position.x + 50,
        y: node.position.y + 50,
      },
      selected: false,
    };

    setNodes((nodes) => [...nodes, newNode]);
    onClose();
  }, [id, getNode, setNodes, onClose]);

  const addResource = useCallback((resourceType: ResourceType) => {
    if (onAddResource) {
      onAddResource(resourceType);
    }
    onClose();
  }, [onAddResource, onClose]);

  return (
    <div
      style={{ top, left, right, bottom }}
      className="absolute z-50 min-w-[160px] bg-slate-900/95 backdrop-blur-xl border border-slate-700/50 rounded-xl shadow-2xl p-1.5 flex flex-col animate-in fade-in zoom-in duration-200"
      onClick={onClose}
    >
      {type === 'node' && (
        <>
          <button
            onClick={duplicateNode}
            className="flex items-center gap-2 px-3 py-2 rounded-lg text-sm text-slate-300 hover:bg-slate-800 hover:text-white transition-all text-left"
          >
            <Copy className="w-4 h-4 text-indigo-400" />
            Duplicate
          </button>
          <button
            onClick={deleteNode}
            className="flex items-center gap-2 px-3 py-2 rounded-lg text-sm text-rose-400 hover:bg-rose-500/10 transition-all text-left"
          >
            <Trash2 className="w-4 h-4" />
            Delete
          </button>
        </>
      )}

      {type === 'edge' && (
        <button
          onClick={deleteEdge}
          className="flex items-center gap-2 px-3 py-2 rounded-lg text-sm text-rose-400 hover:bg-rose-500/10 transition-all text-left"
        >
          <Trash2 className="w-4 h-4" />
          Delete Connection
        </button>
      )}

      {type === 'pane' && (
        <div className="flex flex-col gap-0.5">
          <div className="px-3 py-1.5 text-[10px] font-bold text-slate-500 uppercase tracking-wider">
            Add Resource
          </div>
          {(['VPC', 'SUBNET', 'EC2', 'S3', 'RDS', 'INTERNET_GATEWAY', 'LOAD_BALANCER'] as ResourceType[]).map((res) => (
            <button
              key={res}
              onClick={() => addResource(res)}
              className="flex items-center gap-2 px-3 py-2 rounded-lg text-sm text-slate-300 hover:bg-slate-800 hover:text-white transition-all text-left"
            >
              <Plus className="w-4 h-4 text-emerald-400" />
              {res.replace(/_/g, ' ')}
            </button>
          ))}
        </div>
      )}
    </div>
  );
}
