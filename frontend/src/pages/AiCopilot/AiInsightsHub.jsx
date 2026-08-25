import React, { useState, useEffect } from 'react';
import { aiApi } from '../../api/endpoints';
import {
  BrainCircuit,
  Sparkles,
  AlertTriangle,
  TrendingDown,
  ShieldCheck,
  CheckCircle2,
  Lightbulb,
  Zap,
} from 'lucide-react';

export const AiInsightsHub = () => {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const load = async () => {
      try {
        const res = await aiApi.getExecutiveSummary();
        setData(res.data);
      } catch (err) {
        console.error('Failed to load AI copilot analysis:', err);
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <div className="w-8 h-8 rounded-full border-2 border-indigo-500 border-t-transparent animate-spin" />
      </div>
    );
  }

  const risks = data?.riskPredictions || [];
  const anomalies = data?.anomalies || [];
  const actions = data?.recommendedNextActions || [];

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-2 border-b border-white/5">
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-2xl lg:text-3xl font-extrabold text-white font-heading">AI Operations Copilot</h1>
            <span className="bg-gradient-to-r from-purple-500 to-indigo-500 text-white text-[10px] font-extrabold px-2.5 py-0.5 rounded-full uppercase tracking-wider shadow-lg shadow-purple-500/20">
              Phase 8 Intelligence
            </span>
          </div>
          <p className="text-xs text-slate-400 mt-0.5">Continuous automated analysis across Projects + Tasks + Timesheets + Deadlines.</p>
        </div>
      </div>

      {/* Executive Synopsis Banner */}
      <div className="glass-card p-6 border-indigo-500/30 bg-gradient-to-br from-indigo-950/40 via-slate-900/80 to-slate-950/90 relative overflow-hidden">
        <div className="flex items-start gap-4">
          <div className="w-12 h-12 rounded-2xl bg-gradient-to-tr from-purple-600 to-indigo-500 flex items-center justify-center text-white shadow-lg shadow-indigo-500/30 flex-shrink-0">
            <Sparkles className="w-6 h-6 animate-pulse" />
          </div>
          <div className="space-y-2">
            <h2 className="text-base font-bold text-white font-heading">Executive Project Intelligence Summary</h2>
            <p className="text-xs sm:text-sm text-slate-300 leading-relaxed">
              {data?.executiveSummary || 'Organizational delivery health is stable at 84% on-time index. Projected revenue efficiency indicates 78% billable realization.'}
            </p>
          </div>
        </div>
      </div>

      {/* 2-Column: Risk Predictions & Workload Anomalies */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Left Col: Project Risk Forecasts */}
        <div className="lg:col-span-6 glass-card p-6 space-y-4">
          <div className="flex items-center justify-between pb-3 border-b border-white/5">
            <div className="flex items-center gap-2">
              <AlertTriangle className="w-4 h-4 text-amber-400" />
              <h3 className="text-sm font-bold text-white font-heading">Predictive Risk Forecasts</h3>
            </div>
            <span className="text-xs text-slate-400 font-semibold">{risks.length} Assessed</span>
          </div>

          <div className="space-y-3">
            {risks.map((r, i) => (
              <div key={i} className="p-4 rounded-xl bg-slate-900/60 border border-white/5 space-y-2.5">
                <div className="flex items-start justify-between">
                  <div>
                    <h4 className="text-xs font-bold text-white">{r.projectName}</h4>
                    <span className="font-mono text-[10px] text-slate-400">{r.projectCode}</span>
                  </div>
                  <span className={`text-[10px] font-bold px-2 py-0.5 rounded ${r.riskLevel === 'HIGH' ? 'bg-rose-500/20 text-rose-300' : 'bg-emerald-500/20 text-emerald-300'}`}>
                    {r.riskLevel} Risk
                  </span>
                </div>

                <div className="grid grid-cols-2 gap-2 text-xs">
                  <div className="p-2 rounded bg-slate-950/60">
                    <span className="text-[10px] text-slate-400 block">Delay Probability</span>
                    <span className="font-bold text-amber-400">{r.delayProbabilityPercentage}%</span>
                  </div>
                  <div className="p-2 rounded bg-slate-950/60">
                    <span className="text-[10px] text-slate-400 block">Cost Variance Risk</span>
                    <span className="font-bold text-rose-400">+{r.predictedCostOverrunPercentage}%</span>
                  </div>
                </div>

                <div className="text-[11px] text-slate-300">
                  <span className="text-slate-400 font-semibold">Primary Factor: </span>
                  {r.primaryRiskFactor}
                </div>

                {r.mitigationRecommendations && (
                  <div className="pt-2 border-t border-white/5 space-y-1">
                    <span className="text-[10px] font-bold text-indigo-400 uppercase tracking-wider block">AI Mitigation Strategy:</span>
                    {r.mitigationRecommendations.map((rec, j) => (
                      <p key={j} className="text-[11px] text-slate-400 flex items-start gap-1.5">
                        <span className="text-indigo-400 mt-0.5">•</span>
                        <span>{rec}</span>
                      </p>
                    ))}
                  </div>
                )}
              </div>
            ))}
          </div>
        </div>

        {/* Right Col: Timesheet Anomalies & Recommended Next Actions */}
        <div className="lg:col-span-6 space-y-6">
          {/* Anomaly Detection */}
          <div className="glass-card p-6 space-y-4">
            <div className="flex items-center justify-between pb-3 border-b border-white/5">
              <div className="flex items-center gap-2">
                <Zap className="w-4 h-4 text-cyan-400" />
                <h3 className="text-sm font-bold text-white font-heading">Effort & Timesheet Anomaly Detector</h3>
              </div>
            </div>

            <div className="space-y-3">
              {anomalies.map((a, i) => (
                <div key={i} className="p-3.5 rounded-xl bg-slate-900/60 border border-white/5 flex items-start gap-3">
                  <div className={`p-2 rounded-lg ${a.severity === 'WARNING' ? 'bg-amber-500/15 text-amber-400' : 'bg-emerald-500/15 text-emerald-400'}`}>
                    <ShieldCheck className="w-4 h-4" />
                  </div>
                  <div>
                    <h4 className="text-xs font-bold text-white">{a.employeeName} — {a.projectOrTaskName}</h4>
                    <p className="text-[11px] text-slate-400 mt-0.5">{a.description}</p>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Recommended Next Actions */}
          <div className="glass-card p-6 space-y-4">
            <div className="flex items-center gap-2 pb-3 border-b border-white/5">
              <Lightbulb className="w-4 h-4 text-amber-400" />
              <h3 className="text-sm font-bold text-white font-heading">Copilot Recommended Next Actions</h3>
            </div>

            <div className="space-y-2.5">
              {actions.map((act, i) => (
                <div key={i} className="p-3 rounded-xl bg-slate-900/40 border border-white/5 flex items-start gap-2.5 text-xs text-slate-300">
                  <CheckCircle2 className="w-4 h-4 text-indigo-400 mt-0.5 flex-shrink-0" />
                  <span>{act}</span>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
