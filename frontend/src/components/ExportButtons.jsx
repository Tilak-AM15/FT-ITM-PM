import React from 'react';
import { Download, Printer } from 'lucide-react';

export const ExportButtons = ({ onExportCsv, onPrint, csvDisabled = false }) => {
  const handlePrint = () => {
    if (onPrint) {
      onPrint();
    } else {
      window.print();
    }
  };

  return (
    <div className="flex items-center gap-2">
      {onExportCsv && (
        <button
          onClick={onExportCsv}
          disabled={csvDisabled}
          className="btn btn-secondary btn-sm"
          title="Export table data to CSV format"
        >
          <Download className="w-3.5 h-3.5 text-cyan-400" />
          <span>Export CSV</span>
        </button>
      )}
      <button
        onClick={handlePrint}
        className="btn btn-secondary btn-sm"
        title="Print or Save report as PDF"
      >
        <Printer className="w-3.5 h-3.5 text-indigo-400" />
        <span>Print / PDF</span>
      </button>
    </div>
  );
};
