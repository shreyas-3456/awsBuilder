import { PointerEventHandler } from 'react';

interface PanelResizeHandleProps {
  orientation: 'horizontal' | 'vertical';
  edge: 'top' | 'right' | 'bottom' | 'left';
  label: string;
  onPointerDown: PointerEventHandler<HTMLButtonElement>;
  className?: string;
}

export function PanelResizeHandle({
  orientation,
  edge,
  label,
  onPointerDown,
  className = '',
}: PanelResizeHandleProps) {
  const isHorizontal = orientation === 'horizontal';

  const edgeClassMap = {
    top: 'left-0 right-0 top-0 h-3 -translate-y-1/2 cursor-row-resize',
    right: 'bottom-0 right-0 top-0 w-3 translate-x-1/2 cursor-col-resize',
    bottom: 'bottom-0 left-0 right-0 h-3 translate-y-1/2 cursor-row-resize',
    left: 'bottom-0 left-0 top-0 w-3 -translate-x-1/2 cursor-col-resize',
  };

  const lineClassName = isHorizontal
    ? 'h-1 w-14 rounded-full'
    : 'h-14 w-1 rounded-full';

  return (
    <button
      type="button"
      aria-label={label}
      onPointerDown={onPointerDown}
      className={`absolute z-20 flex items-center justify-center touch-none ${edgeClassMap[edge]} ${className}`}
    >
      <span className={`bg-slate-500/60 transition-colors hover:bg-indigo-400 ${lineClassName}`} />
    </button>
  );
}
