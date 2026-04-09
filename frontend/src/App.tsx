import { PointerEvent, useCallback, useState, useEffect, useRef } from 'react';
import {
  Node,
  Edge,
  NodeChange,
  EdgeChange,
  Connection,
  applyNodeChanges,
  applyEdgeChanges,
  addEdge,
} from 'reactflow';
import { Toolbar } from './components/Toolbar';
import { DiagramCanvas } from './components/DiagramCanvas';
import { PropertiesPanel } from './components/PropertiesPanel';
import { CodeDisplayPanel } from './components/CodeDisplayPanel';
import { TemplateSelector } from './components/TemplateSelector';
import { PanelResizeHandle } from './components/PanelResizeHandle';
import { Template } from './data/templates';
import { diagramService } from './services/diagramService';
import { ResourceType, DiagramDTO, NodeDTO, EdgeDTO, ResourceProperties } from './types/diagram';
import { DEFAULT_PROPERTIES } from './types/resources';

interface HistoryState {
  nodes: Node[];
  edges: Edge[];
}

const XL_BREAKPOINT = 1280;

const clamp = (value: number, min: number, max: number) => Math.min(max, Math.max(min, value));

const getViewportWidth = () => (typeof window === 'undefined' ? XL_BREAKPOINT : window.innerWidth);

const getViewportHeight = () => (typeof window === 'undefined' ? 900 : window.innerHeight);

const getTemplatePanelWidthMax = () => Math.min(560, Math.max(320, Math.floor(getViewportWidth() * 0.42)));
const getTemplatePanelHeightMax = () => Math.min(560, Math.max(280, Math.floor(getViewportHeight() * 0.5)));
const getPropertiesPanelWidthMax = () => Math.min(560, Math.max(320, Math.floor(getViewportWidth() * 0.4)));
const getPropertiesPanelHeightMax = () => Math.max(320, getViewportHeight() - 120);
const getCodePanelHeightMax = () => Math.min(640, Math.max(240, Math.floor(getViewportHeight() * 0.5)));

function App() {
  const [nodes, setNodes] = useState<Node[]>([]);
  const [edges, setEdges] = useState<Edge[]>([]);
  const [selectedNodeId, setSelectedNodeId] = useState<string | null>(null);
  const [terraformCode, setTerraformCode] = useState<string>('');
  const [cloudformationCode, setCloudformationCode] = useState<string>('');
  const [selectedRegion, setSelectedRegion] = useState<string>('us-east-1');
  const [isGenerating, setIsGenerating] = useState(false);
  const [showTemplates, setShowTemplates] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [fitViewTrigger, setFitViewTrigger] = useState(0);
  const [viewportWidth, setViewportWidth] = useState<number>(getViewportWidth);
  const [templatePanelWidth, setTemplatePanelWidth] = useState(320);
  const [templatePanelHeight, setTemplatePanelHeight] = useState(340);
  const [propertiesPanelWidth, setPropertiesPanelWidth] = useState(352);
  const [propertiesPanelHeight, setPropertiesPanelHeight] = useState(() =>
    clamp(Math.round(getViewportHeight() * 0.58), 320, getPropertiesPanelHeightMax())
  );
  const [codePanelHeight, setCodePanelHeight] = useState(() =>
    clamp(Math.round(getViewportHeight() * 0.34), 220, getCodePanelHeightMax())
  );
  
  // History for Undo/Redo
  const history = useRef<HistoryState[]>([]);
  const future = useRef<HistoryState[]>([]);

  const selectedNode = nodes.find(n => n.id === selectedNodeId) || null;
  const isDesktopLayout = viewportWidth >= XL_BREAKPOINT;
  const handleCloseProperties = useCallback(() => {
    setSelectedNodeId(null);
  }, []);

  const startPanelResize = useCallback((
    event: PointerEvent<HTMLButtonElement>,
    cursor: 'col-resize' | 'row-resize',
    onDrag: (deltaX: number, deltaY: number) => void,
  ) => {
    event.preventDefault();

    const startX = event.clientX;
    const startY = event.clientY;
    const previousCursor = document.body.style.cursor;
    const previousUserSelect = document.body.style.userSelect;

    document.body.style.cursor = cursor;
    document.body.style.userSelect = 'none';

    const handlePointerMove = (moveEvent: globalThis.PointerEvent) => {
      onDrag(moveEvent.clientX - startX, moveEvent.clientY - startY);
    };

    const stopResize = () => {
      document.body.style.cursor = previousCursor;
      document.body.style.userSelect = previousUserSelect;
      document.removeEventListener('pointermove', handlePointerMove);
      document.removeEventListener('pointerup', stopResize);
      document.removeEventListener('pointercancel', stopResize);
    };

    document.addEventListener('pointermove', handlePointerMove);
    document.addEventListener('pointerup', stopResize);
    document.addEventListener('pointercancel', stopResize);
  }, []);

  const handleTemplateResizeStart = useCallback((
    event: PointerEvent<HTMLButtonElement>,
    direction: 'horizontal' | 'vertical',
  ) => {
    if (direction === 'horizontal') {
      const startWidth = templatePanelWidth;
      startPanelResize(event, 'col-resize', (deltaX) => {
        setTemplatePanelWidth(clamp(startWidth + deltaX, 260, getTemplatePanelWidthMax()));
      });
      return;
    }

    const startHeight = templatePanelHeight;
    startPanelResize(event, 'row-resize', (_deltaX, deltaY) => {
      setTemplatePanelHeight(clamp(startHeight + deltaY, 220, getTemplatePanelHeightMax()));
    });
  }, [startPanelResize, templatePanelHeight, templatePanelWidth]);

  const handlePropertiesResizeStart = useCallback((
    event: PointerEvent<HTMLButtonElement>,
    direction: 'horizontal' | 'vertical',
  ) => {
    if (direction === 'horizontal') {
      const startWidth = propertiesPanelWidth;
      startPanelResize(event, 'col-resize', (deltaX) => {
        setPropertiesPanelWidth(clamp(startWidth - deltaX, 280, getPropertiesPanelWidthMax()));
      });
      return;
    }

    const startHeight = propertiesPanelHeight;
    startPanelResize(event, 'row-resize', (_deltaX, deltaY) => {
      setPropertiesPanelHeight(clamp(startHeight - deltaY, 300, getPropertiesPanelHeightMax()));
    });
  }, [propertiesPanelHeight, propertiesPanelWidth, startPanelResize]);

  const handleCodeResizeStart = useCallback((event: PointerEvent<HTMLButtonElement>) => {
    const startHeight = codePanelHeight;
    startPanelResize(event, 'row-resize', (_deltaX, deltaY) => {
      setCodePanelHeight(clamp(startHeight - deltaY, 220, getCodePanelHeightMax()));
    });
  }, [codePanelHeight, startPanelResize]);

  const saveToHistory = useCallback(() => {
    history.current.push({ nodes, edges });
    future.current = [];
    // Keep history at a reasonable size
    if (history.current.length > 50) {
      history.current.shift();
    }
  }, [nodes, edges]);

  const undo = useCallback(() => {
    if (history.current.length === 0) return;
    const previous = history.current.pop()!;
    future.current.push({ nodes, edges });
    setNodes(previous.nodes);
    setEdges(previous.edges);
  }, [nodes, edges]);

  const redo = useCallback(() => {
    if (future.current.length === 0) return;
    const next = future.current.pop()!;
    history.current.push({ nodes, edges });
    setNodes(next.nodes);
    setEdges(next.edges);
  }, [nodes, edges]);

  // Keyboard shortcut listener
  useEffect(() => {
    const handleKeyDown = (event: KeyboardEvent) => {
      if ((event.ctrlKey || event.metaKey) && event.key === 'z') {
        if (event.shiftKey) {
          redo();
        } else {
          undo();
        }
      } else if ((event.ctrlKey || event.metaKey) && event.key === 'y') {
        redo();
      } else if (event.key === 'Escape' && selectedNodeId) {
        setSelectedNodeId(null);
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [redo, selectedNodeId, undo]);

  useEffect(() => {
    const handleResize = () => {
      setViewportWidth(window.innerWidth);
      setTemplatePanelWidth((current) => clamp(current, 260, getTemplatePanelWidthMax()));
      setTemplatePanelHeight((current) => clamp(current, 220, getTemplatePanelHeightMax()));
      setPropertiesPanelWidth((current) => clamp(current, 280, getPropertiesPanelWidthMax()));
      setPropertiesPanelHeight((current) => clamp(current, 300, getPropertiesPanelHeightMax()));
      setCodePanelHeight((current) => clamp(current, 220, getCodePanelHeightMax()));
    };

    handleResize();
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  const handleAddResource = useCallback((resourceType: ResourceType) => {
    saveToHistory();
    const id = `${resourceType.toLowerCase()}-${Date.now()}`;
    const newNode: Node = {
      id,
      type: 'resourceNode',
      position: { x: Math.random() * 400 + 100, y: Math.random() * 300 + 100 },
      data: {
        label: id,
        type: resourceType,
        properties: { ...DEFAULT_PROPERTIES[resourceType] }
      },
    };
    setNodes(prev => [...prev, newNode]);
  }, [saveToHistory]);

  const handleNodesChange = useCallback((changes: NodeChange[]) => {
    // We don't save to history on every minor change (like dragging)
    // to avoid flooding the history stack. We'll save on certain events instead.
    setNodes(nds => applyNodeChanges(changes, nds));
  }, []);

  const handleEdgesChange = useCallback((changes: EdgeChange[]) => {
    setEdges(eds => applyEdgeChanges(changes, eds));
  }, []);

  const handleNodesDelete = useCallback((deletedNodes: Node[]) => {
    saveToHistory();
    const deletedIds = deletedNodes.map(n => n.id);
    setNodes(nds => nds.filter(n => !deletedIds.includes(n.id)));
    if (selectedNodeId && deletedIds.includes(selectedNodeId)) {
      setSelectedNodeId(null);
    }
  }, [selectedNodeId, saveToHistory]);

  const handleEdgesDelete = useCallback((deletedEdges: Edge[]) => {
    saveToHistory();
    const deletedIds = deletedEdges.map(e => e.id);
    setEdges(eds => eds.filter(e => !deletedIds.includes(e.id)));
  }, [saveToHistory]);

  const handleConnect = useCallback((connection: Connection) => {
    saveToHistory();
    setEdges(eds => addEdge({ ...connection, type: 'smoothstep', animated: true }, eds));
  }, [saveToHistory]);

  const handleNodeSelect = useCallback((nodeId: string | null) => {
    setSelectedNodeId(nodeId);
  }, []);

  const handlePropertyChange = useCallback((nodeId: string, properties: ResourceProperties) => {
    saveToHistory();
    setNodes(prev => prev.map(node =>
      node.id === nodeId
        ? { ...node, data: { ...node.data, properties } }
        : node
    ));
  }, [saveToHistory]);

  const handleNodeResize = useCallback((nodeId: string, width: number, height: number) => {
    setNodes(prev => prev.map(node =>
      node.id === nodeId
        ? { ...node, style: { ...node.style, width, height } }
        : node
    ));
  }, []);

  const handleDeleteNode = useCallback((nodeId: string) => {
    saveToHistory();
    setNodes(nds => nds.filter(n => n.id !== nodeId));
    setEdges(eds => eds.filter(e => e.source !== nodeId && e.target !== nodeId));
    setSelectedNodeId(null);
  }, [saveToHistory]);

  const handleApplyTemplate = useCallback((template: Template) => {
    saveToHistory();

    const nextNodes = template.nodes.map((node) => ({
      ...node,
      position: { ...node.position },
      data: {
        ...node.data,
        properties: { ...(node.data.properties || {}) },
      },
    }));

    const nextEdges = template.edges.map((edge) => ({
      ...edge,
    }));

    setNodes(nextNodes);
    setEdges(nextEdges);
    setSelectedNodeId(null);
    setShowTemplates(false);
    setFitViewTrigger((value) => value + 1);
  }, [saveToHistory]);

  const handleClearDiagrams = useCallback(() => {
    if (nodes.length === 0 && edges.length === 0) {
      return;
    }

    saveToHistory();
    setNodes([]);
    setEdges([]);
    setSelectedNodeId(null);
    setTerraformCode('');
    setCloudformationCode('');
    setError(null);
  }, [edges.length, nodes.length, saveToHistory]);

  const handleGenerate = useCallback(async () => {
    setIsGenerating(true);
    setError(null);

    try {
      const diagramDTO: DiagramDTO = {
        nodes: nodes.map(node => ({
          id: node.id,
          type: node.data.type,
          properties: Object.entries(node.data.properties || {}).reduce((acc, [key, value]) => {
            acc[key] = String(value);
            return acc;
          }, {} as { [key: string]: string })
        } as NodeDTO)),
        edges: edges.map(edge => ({
          id: edge.id,
          source: edge.source,
          target: edge.target,
          type: edge.type || 'dependency'
        } as EdgeDTO)),
        region: selectedRegion
      };

      const response = await diagramService.generateTerraform(diagramDTO);
      setTerraformCode(response.terraform);
      setCloudformationCode(response.cloudformation);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'An unknown error occurred');
    } finally {
      setIsGenerating(false);
    }
  }, [nodes, edges, selectedRegion]);

  return (
    <div className="flex h-screen overflow-hidden bg-slate-950">
      <div className="flex min-h-0 flex-1 flex-col">
      <Toolbar
        onAddResource={handleAddResource}
        onGenerate={handleGenerate}
        onClearDiagrams={handleClearDiagrams}
        isGenerating={isGenerating}
        hasDiagrams={nodes.length > 0 || edges.length > 0}
        selectedRegion={selectedRegion}
        onRegionChange={setSelectedRegion}
        onToggleTemplates={() => setShowTemplates(!showTemplates)}
        showTemplates={showTemplates}
      />

      <div className="flex flex-1 flex-col overflow-hidden xl:flex-row">
        {showTemplates && (
          <div
            className="min-h-0 shrink-0"
            style={isDesktopLayout ? { width: templatePanelWidth } : { height: templatePanelHeight }}
          >
            <TemplateSelector
              onSelect={handleApplyTemplate}
              onClose={() => setShowTemplates(false)}
              onResizeStart={handleTemplateResizeStart}
            />
          </div>
        )}
        <div className="flex min-h-0 flex-1 flex-col 2xl:flex-row">
          <div className="flex min-h-0 flex-1 flex-col">
            <div className="relative min-h-[360px] flex-1 xl:min-h-0">
              <DiagramCanvas
                nodes={nodes}
                edges={edges}
                onNodesChange={handleNodesChange}
                onEdgesChange={handleEdgesChange}
                onNodesDelete={handleNodesDelete}
                onEdgesDelete={handleEdgesDelete}
                onConnect={handleConnect}
                onNodeSelect={handleNodeSelect}
                onNodeResize={handleNodeResize}
                onAddResource={handleAddResource}
                fitViewTrigger={fitViewTrigger}
              />
            </div>
            <div
              className="relative min-h-[220px] shrink-0 border-t border-slate-700/50"
              style={{ height: codePanelHeight }}
            >
              <PanelResizeHandle
                orientation="horizontal"
                edge="top"
                label="Resize code panel height"
                onPointerDown={handleCodeResizeStart}
              />
              <CodeDisplayPanel
                terraformCode={terraformCode}
                cloudformationCode={cloudformationCode}
                isLoading={isGenerating}
                error={error}
              />
            </div>
          </div>

          <div
            className="hidden xl:flex xl:min-h-0 xl:shrink-0"
            style={{ width: propertiesPanelWidth }}
          >
            <PropertiesPanel
              selectedNode={selectedNode}
              onPropertyChange={handlePropertyChange}
              onDeleteNode={handleDeleteNode}
              selectedRegion={selectedRegion}
              onClose={selectedNode ? handleCloseProperties : undefined}
              onResizeStart={handlePropertiesResizeStart}
            />
          </div>
        </div>
      </div>
      </div>

      {selectedNode && (
        <>
          <button
            type="button"
            className="fixed inset-0 z-40 bg-slate-950/70 backdrop-blur-sm xl:hidden"
            onClick={handleCloseProperties}
            aria-label="Close properties panel"
          />
          <div className="xl:hidden">
            <PropertiesPanel
              selectedNode={selectedNode}
              onPropertyChange={handlePropertyChange}
              onDeleteNode={handleDeleteNode}
              selectedRegion={selectedRegion}
              onClose={handleCloseProperties}
              variant="sheet"
              style={{ height: propertiesPanelHeight }}
              onResizeStart={handlePropertiesResizeStart}
            />
          </div>
        </>
      )}
    </div>
  );
}

export default App;
