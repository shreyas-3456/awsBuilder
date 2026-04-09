import { Prism as SyntaxHighlighter } from 'react-syntax-highlighter';
import { vscDarkPlus } from 'react-syntax-highlighter/dist/esm/styles/prism';
import { Copy, Check } from 'lucide-react';
import { useState } from 'react';

interface CodeDisplayPanelProps {
  terraformCode: string;
  cloudformationCode: string;
  isLoading: boolean;
  error: string | null;
}

export function CodeDisplayPanel({ terraformCode, cloudformationCode, isLoading, error }: CodeDisplayPanelProps) {
  const [copied, setCopied] = useState(false);
  const [activeTab, setActiveTab] = useState<'terraform' | 'cloudformation'>('terraform');

  const activeCode = activeTab === 'terraform' ? terraformCode : cloudformationCode;
  const language = activeTab === 'terraform' ? 'hcl' : 'yaml';

  const handleCopy = async () => {
    await navigator.clipboard.writeText(activeCode);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const handleTabChange = (tab: 'terraform' | 'cloudformation') => {
    if (!isLoading) {
      setActiveTab(tab);
    }
  };

  return (
    <div className="flex h-full w-full flex-col bg-gray-900">
      <div className="flex flex-col border-b border-gray-700 bg-gray-800">
        <div className="flex flex-col gap-3 px-4 py-3 sm:flex-row sm:items-center sm:justify-between">
          <div className="flex flex-wrap gap-2">
            <button
              onClick={() => handleTabChange('terraform')}
              disabled={isLoading}
              className={`px-3 py-1.5 text-sm font-medium rounded-md transition-colors ${
                activeTab === 'terraform'
                  ? 'bg-blue-600 text-white'
                  : 'bg-gray-700 text-gray-300 hover:bg-gray-600 disabled:opacity-50'
              } ${isLoading ? 'cursor-not-allowed' : ''}`}
            >
              Terraform
            </button>
            <button
              onClick={() => handleTabChange('cloudformation')}
              disabled={isLoading}
              className={`px-3 py-1.5 text-sm font-medium rounded-md transition-colors ${
                activeTab === 'cloudformation'
                  ? 'bg-blue-600 text-white'
                  : 'bg-gray-700 text-gray-300 hover:bg-gray-600 disabled:opacity-50'
              } ${isLoading ? 'cursor-not-allowed' : ''}`}
            >
              CloudFormation
            </button>
          </div>
          {activeCode && !isLoading && !error && (
            <button
              onClick={handleCopy}
              className="flex items-center justify-center gap-2 rounded-md bg-blue-600 px-3 py-1.5 text-sm text-white transition-colors hover:bg-blue-700 sm:justify-start"
            >
              {copied ? (
                <>
                  <Check className="w-4 h-4" />
                  Copied!
                </>
              ) : (
                <>
                  <Copy className="w-4 h-4" />
                  Copy
                </>
              )}
            </button>
          )}
        </div>
      </div>

      <div className="flex-1 overflow-auto">
        {isLoading ? (
          <div className="flex items-center justify-center h-full">
            <div className="text-gray-400">Generating code...</div>
          </div>
        ) : error ? (
          <div className="p-4">
            <div className="bg-red-900/20 border border-red-500 rounded-lg p-4">
              <h3 className="text-red-400 font-semibold mb-2">Error</h3>
              <p className="text-red-300 text-sm">{error}</p>
            </div>
          </div>
        ) : activeCode ? (
          <SyntaxHighlighter
            language={language}
            style={vscDarkPlus}
            customStyle={{
              margin: 0,
              padding: '1rem',
              background: 'transparent',
              fontSize: '0.875rem',
              minHeight: '100%',
            }}
            showLineNumbers
          >
            {activeCode}
          </SyntaxHighlighter>
        ) : (
          <div className="flex items-center justify-center h-full">
            <div className="text-center text-gray-400">
              <p className="mb-2">No code generated yet</p>
              <p className="text-sm">Add resources and click "Generate"</p>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
