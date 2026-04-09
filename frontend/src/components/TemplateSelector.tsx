import { CSSProperties, PointerEvent } from 'react';
import { Layout, ChevronLeft, LayoutGrid, ArrowRight } from 'lucide-react';
import { Template, TEMPLATES } from '../data/templates';
import { PanelResizeHandle } from './PanelResizeHandle';

interface TemplateSelectorProps {
  onSelect: (template: Template) => void;
  onClose: () => void;
  className?: string;
  style?: CSSProperties;
  onResizeStart?: (event: PointerEvent<HTMLButtonElement>, direction: 'horizontal' | 'vertical') => void;
}

export function TemplateSelector({
  onSelect,
  onClose,
  className = '',
  style,
  onResizeStart,
}: TemplateSelectorProps) {
  return (
    <div
      className={`relative flex h-full w-full min-h-0 shrink-0 flex-col overflow-hidden border-b border-slate-700/50 bg-slate-900/95 backdrop-blur-xl animate-in slide-in-from-left duration-300 xl:border-b-0 xl:border-r ${className}`}
      style={style}
    >
      {onResizeStart && (
        <>
          <PanelResizeHandle
            orientation="horizontal"
            edge="bottom"
            label="Resize template panel height"
            onPointerDown={(event) => onResizeStart(event, 'vertical')}
            className="xl:hidden"
          />
          <PanelResizeHandle
            orientation="vertical"
            edge="right"
            label="Resize template panel width"
            onPointerDown={(event) => onResizeStart(event, 'horizontal')}
            className="hidden xl:flex"
          />
        </>
      )}

      <div className="flex items-center justify-between border-b border-slate-700/50 p-4 sm:p-5">
        <div className="flex items-center gap-2">
          <LayoutGrid className="w-5 h-5 text-indigo-400" />
          <h2 className="text-base font-semibold text-slate-200">Architecture Templates</h2>
        </div>
        <button
          onClick={onClose}
          className="p-1.5 rounded-lg hover:bg-slate-800 text-slate-400 hover:text-slate-200 transition-all"
        >
          <ChevronLeft className="w-5 h-5" />
        </button>
      </div>

      <div className="flex-1 min-h-0 overflow-y-auto p-4 space-y-4">
        {TEMPLATES.map((template) => (
          <div
            key={template.id}
            className="group relative p-4 rounded-xl bg-slate-800/40 border border-slate-700/40 hover:border-indigo-500/50 hover:bg-slate-800/60 transition-all cursor-pointer overflow-hidden"
            onClick={() => onSelect(template)}
          >
            {/* Hover Decor */}
            <div className="absolute -right-4 -top-4 w-20 h-20 bg-indigo-500/10 rounded-full blur-2xl group-hover:bg-indigo-500/20 transition-all" />
            
            <div className="relative">
              <div className="flex items-start justify-between mb-2">
                <h3 className="text-sm font-bold text-slate-100 group-hover:text-indigo-300 transition-colors">
                  {template.name}
                </h3>
                <Layout className="w-4 h-4 text-slate-500 group-hover:text-indigo-400" />
              </div>
              <p className="text-xs text-slate-400 leading-relaxed mb-4">
                {template.description}
              </p>
              
              <div className="flex items-center gap-1.5 text-xs font-semibold text-indigo-400 group-hover:text-indigo-300">
                Apply Template
                <ArrowRight className="w-3.5 h-3.5 group-hover:translate-x-1 transition-transform" />
              </div>
            </div>
          </div>
        ))}
      </div>

      <div className="border-t border-slate-700/50 bg-slate-900/50 p-4 text-center text-[10px] font-bold uppercase tracking-widest text-slate-500">
        Quick-start common architectures
      </div>
    </div>
  );
}
