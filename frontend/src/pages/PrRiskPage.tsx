import React, { useState } from 'react';
import { 
  ShieldAlert,
  Search, 
  CheckCircle2, 
  AlertTriangle, 
  Zap
} from 'lucide-react';
import { api } from '../services/api';
import { PullRequestRiskAssessment, PullRequestSummary } from '../types';
import { useRepository } from '../context/RepositoryContext';

export const PrRiskPage: React.FC = () => {
  const { selectedRepoId } = useRepository();
  const [prIdInput, setPrIdInput] = useState<string>('');
  const [assessment, setAssessment] = useState<PullRequestRiskAssessment | null>(null);
  const [repoPrs, setRepoPrs] = useState<PullRequestSummary[]>([]);
  const [loadingPrs, setLoadingPrs] = useState<boolean>(false);
  const [loading, setLoading] = useState<boolean>(false);
  const [evaluating, setEvaluating] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  // Load repository PRs for quick selection
  React.useEffect(() => {
    if (!selectedRepoId) {
      setRepoPrs([]);
      return;
    }
    const loadPrs = async () => {
      try {
        setLoadingPrs(true);
        const res = await api.getPullRequests(selectedRepoId, 0, 25);
        setRepoPrs(res.content || []);
      } catch (e) {
        console.warn('Failed to load PRs for repository', e);
      } finally {
        setLoadingPrs(false);
      }
    };
    loadPrs();
  }, [selectedRepoId]);

  const selectAndInspectPr = async (prId: string) => {
    setPrIdInput(prId);
    try {
      setLoading(true);
      setError(null);
      const data = await api.getPrRisk(prId);
      setAssessment(data);
    } catch (err: any) {
      setError(err?.response?.data?.message || err?.message || 'Failed to fetch PR risk assessment');
      setAssessment(null);
    } finally {
      setLoading(false);
    }
  };

  const handleFetchRisk = async (e?: React.FormEvent) => {
    if (e) e.preventDefault();
    const query = prIdInput.trim();
    if (!query) return;

    let targetId = query;
    // Check if user entered a PR number like "1" or "#1"
    const numMatch = query.replace(/^#/, '');
    if (/^\d+$/.test(numMatch)) {
      const found = repoPrs.find(p => p.prNumber === parseInt(numMatch, 10));
      if (found) {
        targetId = found.id;
      }
    }

    try {
      setLoading(true);
      setError(null);
      const data = await api.getPrRisk(targetId);
      setAssessment(data);
    } catch (err: any) {
      setError(err?.response?.data?.message || err?.message || 'Failed to fetch PR risk assessment');
      setAssessment(null);
    } finally {
      setLoading(false);
    }
  };

  const handleForceEvaluate = async () => {
    const query = prIdInput.trim();
    if (!query) return;

    let targetId = query;
    const numMatch = query.replace(/^#/, '');
    if (/^\d+$/.test(numMatch)) {
      const found = repoPrs.find(p => p.prNumber === parseInt(numMatch, 10));
      if (found) {
        targetId = found.id;
      }
    }

    try {
      setEvaluating(true);
      setError(null);
      const data = await api.evaluatePrRisk(targetId);
      setAssessment(data);
    } catch (err: any) {
      setError(err?.response?.data?.message || err?.message || 'Failed to force evaluate PR risk');
    } finally {
      setEvaluating(false);
    }
  };

  const getRiskBadgeColor = (level: string) => {
    switch (level) {
      case 'CRITICAL':
        return 'text-rose-400 bg-rose-500/10 border-rose-500/30';
      case 'HIGH':
        return 'text-amber-400 bg-amber-500/10 border-amber-500/30';
      case 'MEDIUM':
        return 'text-blue-400 bg-blue-500/10 border-blue-500/30';
      default:
        return 'text-emerald-400 bg-emerald-500/10 border-emerald-500/30';
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between p-6 rounded-xl bg-gradient-to-r from-slate-900 via-surface to-slate-900 border border-border">
        <div>
          <div className="flex items-center space-x-2">
            <h1 className="text-xl font-bold text-white tracking-tight">Pull Request Risk Estimation & Guardrails</h1>
            <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-rose-500/10 text-rose-400 border border-rose-500/20 font-mono">
              Explainable Risk Guardrails
            </span>
          </div>
          <p className="text-slate-400 mt-1 text-sm max-w-2xl">
            Calculates an explainable risk score (0-100) based on changeset size, author tenure, hotspot overlap, review coverage, and off-hours submission timing.
          </p>
        </div>
      </div>

      {/* PR Search and Evaluation Input */}
      <div className="dev-card p-6">
        {repoPrs.length > 0 && (
          <div className="mb-4">
            <label className="text-xs text-slate-400 font-medium block mb-1.5">
              Select Pull Request from Dropdown: {loadingPrs && <span className="text-blue-400 font-normal ml-2">Loading...</span>}
            </label>
            <select
              value={prIdInput}
              onChange={(e) => {
                if (e.target.value) {
                  selectAndInspectPr(e.target.value);
                }
              }}
              className="w-full bg-slate-900 border border-border rounded-lg px-3 py-2 text-sm text-slate-200 outline-none focus:border-blue-500 transition font-mono"
            >
              <option value="">-- Choose a Pull Request from this repository --</option>
              {repoPrs.map((pr) => (
                <option key={pr.id} value={pr.id}>
                  #{pr.prNumber} - {pr.title} ({pr.state}) by @{pr.authorLogin}
                </option>
              ))}
            </select>
          </div>
        )}

        <form onSubmit={handleFetchRisk} className="flex flex-col sm:flex-row gap-3">
          <div className="relative flex-1">
            <Search className="w-4 h-4 text-slate-500 absolute left-3 top-3" />
            <input
              type="text"
              value={prIdInput}
              onChange={(e) => setPrIdInput(e.target.value)}
              placeholder="Enter Pull Request ID (UUID), PR # (e.g. 1 or #1), or select above..."
              className="w-full bg-slate-900 border border-border rounded-lg pl-9 pr-4 py-2 text-sm text-slate-200 outline-none focus:border-blue-500 transition"
            />
          </div>
          <button
            type="submit"
            disabled={loading || !prIdInput.trim()}
            className="px-5 py-2 rounded-lg bg-blue-600 hover:bg-blue-500 text-white text-sm font-medium transition disabled:opacity-50"
          >
            {loading ? 'Evaluating...' : 'Inspect Risk'}
          </button>
          <button
            type="button"
            onClick={handleForceEvaluate}
            disabled={evaluating || !prIdInput.trim()}
            className="px-4 py-2 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-200 text-sm border border-slate-700 transition flex items-center space-x-2 disabled:opacity-50"
          >
            <Zap className={`w-4 h-4 ${evaluating ? 'animate-spin' : ''}`} />
            <span>Re-evaluate</span>
          </button>
        </form>

        {repoPrs.length > 0 && (
          <div className="mt-4 pt-4 border-t border-border">
            <span className="text-xs text-slate-400 font-medium block mb-2">
              Recent Pull Requests in this Repository: {loadingPrs && <span className="text-blue-400 font-normal ml-2">Loading...</span>}
            </span>
            <div className="flex flex-wrap gap-2">
              {repoPrs.map((pr) => (
                <button
                  key={pr.id}
                  type="button"
                  onClick={() => selectAndInspectPr(pr.id)}
                  className={`text-xs px-2.5 py-1 rounded-md border transition flex items-center space-x-1.5 ${
                    prIdInput === pr.id
                      ? 'bg-blue-600/20 text-blue-300 border-blue-500/50'
                      : 'bg-slate-900/60 text-slate-300 border-slate-800 hover:border-slate-700 hover:bg-slate-800'
                  }`}
                >
                  <span className="font-mono text-blue-400">#{pr.prNumber}</span>
                  <span className="max-w-[200px] truncate">{pr.title}</span>
                </button>
              ))}
            </div>
          </div>
        )}

        {error && (
          <div className="mt-4 p-3 rounded-lg bg-rose-950/30 border border-rose-900/50 text-xs text-rose-300">
            {error}
          </div>
        )}
      </div>

      {/* Assessment Display */}
      {assessment ? (
        <div className="space-y-6">
          {/* Main Risk Score Card */}
          <div className="dev-card p-6 grid grid-cols-1 md:grid-cols-3 gap-6">
            <div className="space-y-2">
              <span className="text-xs text-slate-400 uppercase font-mono">Assessed Pull Request</span>
              <h2 className="text-lg font-bold text-white font-mono">
                #{assessment.prNumber} {assessment.title}
              </h2>
              <div className="text-xs text-slate-400">
                Author: <strong className="text-slate-200 font-mono">{assessment.authorLogin}</strong>
              </div>
              <div className="text-[11px] text-slate-500 font-mono">
                Model: {assessment.modelVersion} ({assessment.isHeuristic ? 'Explainable Heuristic' : 'ML Model'})
              </div>
            </div>

            <div className="flex flex-col justify-center items-center p-4 bg-slate-900/80 rounded-xl border border-border">
              <span className="text-xs text-slate-400 uppercase font-mono">Composite Risk Score</span>
              <div className="text-5xl font-extrabold text-white font-mono mt-2">
                {(assessment.riskScore * 100).toFixed(0)}
                <span className="text-sm text-slate-400 font-normal">/100</span>
              </div>
              <span className={`mt-2 px-2.5 py-0.5 rounded-full text-xs font-bold border uppercase font-mono ${getRiskBadgeColor(assessment.riskLevel)}`}>
                {assessment.riskLevel} RISK
              </span>
            </div>

            <div className="flex flex-col justify-center space-y-3">
              <div className="flex items-center justify-between text-xs">
                <span className="text-slate-400">Model Confidence:</span>
                <span className="text-emerald-400 font-mono font-bold">{(assessment.confidence * 100).toFixed(0)}%</span>
              </div>
              <div className="w-full bg-slate-800 rounded-full h-1.5 overflow-hidden">
                <div className="bg-emerald-500 h-1.5 rounded-full" style={{ width: `${(assessment.confidence * 100).toFixed(0)}%` }}></div>
              </div>

              <div className="flex items-center justify-between text-xs">
                <span className="text-slate-400">Data Sufficiency:</span>
                <span className={`font-mono text-xs ${assessment.isDataSufficient ? 'text-emerald-400' : 'text-amber-400'}`}>
                  {assessment.isDataSufficient ? 'SUFFICIENT' : 'LIMITED SAMPLES'}
                </span>
              </div>
            </div>
          </div>

          {/* Data Sufficiency Warning */}
          {assessment.dataSufficiencyWarning && (
            <div className="p-4 rounded-xl bg-amber-950/20 border border-amber-900/40 text-xs text-amber-300 flex items-start space-x-2">
              <AlertTriangle className="w-4 h-4 text-amber-400 shrink-0 mt-0.5" />
              <div>
                <strong>Data Sufficiency Notice:</strong> {assessment.dataSufficiencyWarning}
              </div>
            </div>
          )}

          {/* Explainable Risk Factors Breakdown */}
          <div className="dev-card p-6">
            <h3 className="text-base font-semibold text-white mb-4">Explainable Factor Attribution</h3>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {assessment.riskFactors && assessment.riskFactors.length > 0 ? (
                assessment.riskFactors.map((factor, idx) => (
                  <div key={idx} className="p-4 rounded-xl bg-slate-900 border border-border flex flex-col justify-between">
                    <div>
                      <div className="flex items-center justify-between mb-2">
                        <span className="text-xs font-mono font-bold text-slate-200">{factor.factorName}</span>
                        <span className="text-xs font-mono font-bold text-amber-400">
                          {(factor.riskScore * 100).toFixed(0)}% Risk (Weight: {((factor.weight || 0) * 100).toFixed(0)}%)
                        </span>
                      </div>
                      <p className="text-xs text-slate-300">{factor.description}</p>
                    </div>
                    {factor.recommendation && (
                      <div className="mt-3 pt-2 border-t border-border/50 text-[11px] text-slate-400">
                        {factor.recommendation}
                      </div>
                    )}
                  </div>
                ))
              ) : (
                <div className="col-span-2 text-center py-6 text-slate-500 text-xs italic">
                  No risk factors recorded.
                </div>
              )}
            </div>
          </div>

          {/* Quality Guardrails & Recommendations */}
          <div className="dev-card p-6">
            <h3 className="text-base font-semibold text-white mb-4">Prescriptive Quality Guardrails</h3>
            <div className="space-y-2">
              {assessment.guardrailRecommendations && assessment.guardrailRecommendations.length > 0 ? (
                assessment.guardrailRecommendations.map((rec, idx) => (
                  <div key={idx} className="p-3 bg-slate-900 rounded-lg border border-border text-xs text-slate-200 flex items-start space-x-3">
                    <CheckCircle2 className="w-4 h-4 text-blue-400 shrink-0 mt-0.5" />
                    <span>{rec}</span>
                  </div>
                ))
              ) : (
                <div className="p-3 bg-emerald-950/20 border border-emerald-900/40 text-xs text-emerald-300 rounded-lg flex items-center space-x-2">
                  <CheckCircle2 className="w-4 h-4 text-emerald-400" />
                  <span>Standard quality checklist applies. No high-risk guardrails triggered.</span>
                </div>
              )}
            </div>
          </div>
        </div>
      ) : (
        <div className="dev-card p-12 text-center">
          <div className="w-12 h-12 rounded-xl bg-blue-500/10 border border-blue-500/20 text-blue-400 flex items-center justify-center mx-auto mb-4">
            <ShieldAlert className="w-6 h-6" />
          </div>
          <h2 className="text-base font-semibold text-white">Inspect or Evaluate a Pull Request</h2>
          <p className="text-slate-400 mt-2 text-sm max-w-md mx-auto">
            Input a pull request ID from the ingested repository to review its explainable risk score, factor breakdown, and quality guardrails.
          </p>
        </div>
      )}
    </div>
  );
};
