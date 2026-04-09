import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import ReactFlow, {
  Node,
  Edge,
  NodeChange,
  EdgeChange,
  Connection,
  Controls,
  Background,
  BackgroundVariant,
  MiniMap,
  NodeTypes,
  ReactFlowProvider,
  useReactFlow,
} from 'reactflow';
import 'reactflow/dist/style.css';
import { ResourceNode } from './ResourceNode';
import { ContextMenu, ContextMenuProps } from './ContextMenu';
import { ResourceType } from '../types/diagram';

interface DiagramCanvasProps {
  nodes: Node[];
  edges: Edge[];
  onNodesChange: (changes: NodeChange[]) => void;
  onEdgesChange: (changes: EdgeChange[]) => void;
  onNodesDelete: (nodes: Node[]) => void;
  onEdgesDelete: (edges: Edge[]) => void;
  onConnect: (connection: Connection) => void;
  onNodeSelect: (nodeId: string | null) => void;
  onNodeResize: (nodeId: string, width: number, height: number) => void;
  onAddResource?: (type: ResourceType) => void;
  fitViewTrigger?: number;
}

const defaultEdgeOptions = {
  type: 'smoothstep',
  animated: true,
  style: {
    stroke: '#6366f1',
    strokeWidth: 2,
  },
};

export function DiagramCanvas(props: DiagramCanvasProps) {
  return (
    <ReactFlowProvider>
      <DiagramCanvasInternal {...props} />
    </ReactFlowProvider>
  );
}

function DiagramCanvasInternal({
  nodes,
  edges,
  onNodesChange,
  onEdgesChange,
  onNodesDelete,
  onEdgesDelete,
  onConnect,
  onNodeSelect,
  onNodeResize,
  onAddResource,
  fitViewTrigger = 0,
}: DiagramCanvasProps) {
  const [menu, setMenu] = useState<ContextMenuProps | null>(null);
  const ref = useRef<HTMLDivElement>(null);
  const { fitView } = useReactFlow();

  const nodeTypes: NodeTypes = useMemo(() => ({
    resourceNode: (nodeProps: any) => (
      <ResourceNode {...nodeProps} onResize={onNodeResize} />
    ),
  }), [onNodeResize]);

  const onNodeContextMenu = useCallback(
    (event: React.MouseEvent, node: Node) => {
      event.preventDefault();

      const pane = ref.current?.getBoundingClientRect();
      if (!pane) return;

      setMenu({
        id: node.id,
        top: event.clientY - pane.top,
        left: event.clientX - pane.left,
        type: 'node',
        onClose: () => setMenu(null),
      });
    },
    [setMenu]
  );

  const onEdgeContextMenu = useCallback(
    (event: React.MouseEvent, edge: Edge) => {
      event.preventDefault();

      const pane = ref.current?.getBoundingClientRect();
      if (!pane) return;

      setMenu({
        id: edge.id,
        top: event.clientY - pane.top,
        left: event.clientX - pane.left,
        type: 'edge',
        onClose: () => setMenu(null),
      });
    },
    [setMenu]
  );

  const onPaneContextMenu = useCallback(
    (event: React.MouseEvent) => {
      event.preventDefault();

      const pane = ref.current?.getBoundingClientRect();
      if (!pane) return;

      setMenu({
        top: event.clientY - pane.top,
        left: event.clientX - pane.left,
        type: 'pane',
        onClose: () => setMenu(null),
        onAddResource,
      });
    },
    [setMenu, onAddResource]
  );

  const onPaneClick = useCallback(() => {
    setMenu(null);
    onNodeSelect(null);
  }, [onNodeSelect]);

  const handleNodeClick = useCallback(
    (_event: React.MouseEvent, node: Node) => {
      onNodeSelect(node.id);
    },
    [onNodeSelect]
  );

  useEffect(() => {
    if (fitViewTrigger === 0 || nodes.length === 0) {
      return;
    }

    const timeoutId = window.setTimeout(() => {
      fitView({
        padding: 0.18,
        duration: 350,
      });
    }, 120);

    return () => window.clearTimeout(timeoutId);
  }, [fitView, fitViewTrigger, nodes.length]);

  return (
    <div className="w-full h-full diagram-canvas" ref={ref}>
      <ReactFlow
        nodes={nodes}
        edges={edges}
        onNodesChange={onNodesChange}
        onEdgesChange={onEdgesChange}
        onNodesDelete={onNodesDelete}
        onEdgesDelete={onEdgesDelete}
        onConnect={onConnect}
        onNodeClick={handleNodeClick}
        onPaneClick={onPaneClick}
        onNodeContextMenu={onNodeContextMenu}
        onEdgeContextMenu={onEdgeContextMenu}
        onPaneContextMenu={onPaneContextMenu}
        nodeTypes={nodeTypes}
        defaultEdgeOptions={defaultEdgeOptions}
        fitView
        snapToGrid
        snapGrid={[16, 16]}
        className="react-flow-dark"
        deleteKeyCode={['Backspace', 'Delete']}
      >
        <Controls
          className="react-flow-controls"
          showInteractive={true}
        />
        <Background
          variant={BackgroundVariant.Dots}
          gap={20}
          size={1}
          color="rgba(99, 102, 241, 0.15)"
        />
        <MiniMap
          nodeColor={(node) => {
            const colorMap: Record<string, string> = {
              VPC: '#3b82f6',
              SUBNET: '#22c55e',
              EC2: '#f97316',
              S3: '#eab308',
              RDS: '#a855f7',
              INTERNET_GATEWAY: '#06b6d4',
              LOAD_BALANCER: '#ec4899',
            };
            return colorMap[node.data?.type] || '#6366f1';
          }}
          maskColor="rgba(15, 23, 42, 0.8)"
          style={{
            backgroundColor: 'rgba(30, 41, 59, 0.8)',
            borderRadius: '12px',
            border: '1px solid rgba(99, 102, 241, 0.2)',
          }}
        />
        {menu && <ContextMenu {...menu} />}
      </ReactFlow>
    </div>
  );
}
