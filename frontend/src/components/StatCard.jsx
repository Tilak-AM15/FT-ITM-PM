import React from 'react';

export const StatCard = ({ title, value, subtitle, icon: Icon, color = '#6366f1', trend, progress }) => {
  return (
    <div className="glass-card p-5 relative overflow-hidden flex flex-col justify-between group hover:translate-y-[-2px] transition-all">
      <div className="flex items-start justify-between">
        <div>
          <p className="text-xs font-semibold text-slate-400 uppercase tracking-wider">{title}</p>
          <h3 className="text-2xl lg:text-3xl font-extrabold text-white mt-1.5 tracking-tight">{value}</h3>
        </div>
        <div
          className="w-10 h-10 rounded-xl flex items-center justify-center border transition-all"
          style={{
            backgroundColor: `${color}15`,
            borderColor: `${color}30`,
            color: color,
          }}
        >
          {Icon && <Icon className="w-5 h-5" />}
        </div>
      </div>

      <div className="mt-4 pt-3 border-t border-white/5 flex items-center justify-between text-xs">
        <span className="text-slate-400">{subtitle}</span>
        {trend && (
          <span className="font-semibold text-emerald-400 bg-emerald-500/10 px-2 py-0.5 rounded border border-emerald-500/20">
            {trend}
          </span>
        )}
      </div>

      {progress !== undefined && (
        <div className="w-full bg-slate-800 h-1.5 rounded-full overflow-hidden mt-2">
          <div
            className="h-full rounded-full transition-all duration-500"
            style={{ width: `${Math.min(100, Math.max(0, progress))}%`, backgroundColor: color }}
          />
        </div>
      )}
    </div>
  );
};
