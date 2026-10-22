import React, { useState, useEffect } from 'react';
import { 
  RefreshCw, 
  BrainCircuit, 
  Target 
} from 'lucide-react';
import { api } from '../services/api';
import { IssueNlpReport } from '../types';
import { useRepository } from '../context/RepositoryContext';

export const IssuesNlpPage: React.FC = () => {
  const { selectedRepo, selectedRepoId } = useRepository();
  const [report, setReport] = useState<IssueNlpReport | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadIssuesNlp();
  }, [selectedRepoId]);

  const loadIssuesNlp = async () => {
    if (!selectedRepoId) return;
    try {
      setLoading(true);
      setError(null);
      const data = await api.getIssuesNlp(selectedRepoId);
      setReport(data);
    } catch (err: any) {
      setError(err?.message || 'Failed to load Issue NLP analytics');
    } finally {
      setLoading(false);
    }
  };

  if (!selectedRepo) {
    return (
      <div className="dev-card p-12 text-center">
        <h2 className="text-lg font-semibold text-white">No Target Repository Selected</h2>
        <p className="text-slate-400 mt-2 text-sm max-w-md mx-auto">
          Please select or connect a repository to run weak-supervision NLP classification on its issues.
        </p>
      </div>
    );
  }

  const categoryNames = [
    'BUG_DEFECT',
    'FEATURE_ENHANCEMENT',
    'DOCUMENTATION',
    'TECH_DEBT_REFACTOR',
    'QUESTION_SUPPORT',
  ];

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between p-6 rounded-xl bg-gradient-to-r from-slate-900 via-surface to-slate-900 border border-border">
        <div>
          <div className="flex items-center space-x-2">
            <h1 className="text-xl font-bold text-white tracking-tight">Issue NLP Categorization & Themes</h1>
            <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-blue-500/10 text-blue-400 border border-blue-500/20 font-mono">
              Weak Supervision + TF-IDF
            </span>
          </div>
          <p className="text-slate-400 mt-1 text-sm max-w-2xl">
            Multi-class issue categorization combining GitHub label ground truth with TF-IDF lexicon classification, evaluated via confusion matrix and F1 metrics.
          </p>
        </div>
        <button
          onClick={loadIssuesNlp}
          className="mt-4 md:mt-0 flex items-center space-x-2 px-4 py-2 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-200 text-sm border border-slate-700 transition"
        >
          <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
          <span>Re-classify Issues</span>
        </button>
      </div>

      {error && (
        <div className="p-4 rounded-lg bg-rose-950/30 border border-rose-900/50 text-xs text-rose-300">
          {error}
        </div>
      )}

      {/* Model Overview Cards */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        <div className="dev-card p-4">
          <span className="text-xs text-slate-400 uppercase font-mono">Analyzed Issues</span>
          <div className="text-2xl font-bold text-white font-mono mt-1">
            {report?.totalAnalyzedIssues ?? 0}
          </div>
          <span className="text-xs text-slate-500 mt-1 block">Full dataset corpus</span>
        </div>

        <div className="dev-card p-4">
          <span className="text-xs text-slate-400 uppercase font-mono">Ground-Truth Labeled</span>
          <div className="text-2xl font-bold text-blue-400 font-mono mt-1">
            {report?.labeledIssuesCount ?? 0}
          </div>
          <span className="text-xs text-slate-500 mt-1 block">Explicit GitHub labels</span>
        </div>

        <div className="dev-card p-4">
          <span className="text-xs text-slate-400 uppercase font-mono">Model Agreement Rate</span>
          <div className="text-2xl font-bold text-emerald-400 font-mono mt-1">
            {report?.evaluation?.agreementRate !== undefined && report?.evaluation?.agreementRate !== null
              ? `${(report.evaluation.agreementRate > 1.0 ? report.evaluation.agreementRate : report.evaluation.agreementRate * 100).toFixed(1)}%`
              : 'N/A'}
          </div>
          <span className="text-xs text-slate-500 mt-1 block">Supervision consistency</span>
        </div>

        <div className="dev-card p-4">
          <span className="text-xs text-slate-400 uppercase font-mono">Unlabeled Predictions</span>
          <div className="text-2xl font-bold text-purple-400 font-mono mt-1">
            {report?.unlabeledIssuesCount ?? 0}
          </div>
          <span className="text-xs text-slate-500 mt-1 block">TF-IDF inferred labels</span>
        </div>
      </div>

      {/* Category Breakdown Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-5 gap-4">
        {report?.categories && report.categories.length > 0 ? (
          report.categories.map((cat) => (
            <div key={cat.categoryKey} className="dev-card p-4 flex flex-col justify-between">
              <div>
                <span className="text-xs font-semibold text-white block truncate" title={cat.categoryName}>
                  {cat.categoryName}
                </span>
                <div className="mt-2 text-2xl font-extrabold text-blue-400 font-mono">
                  {cat.totalIssues}
                </div>
                <span className="text-[11px] text-slate-400 mt-0.5 block">
                  {cat.percentage?.toFixed(1)}% of volume
                </span>

                <div className="mt-3 text-[11px] text-slate-400 space-y-1">
                  <div className="flex justify-between">
                    <span>Median Wait:</span>
                    <span className="text-slate-200 font-mono font-medium">{cat.medianResolutionHours?.toFixed(1)}h</span>
                  </div>
                  <div className="flex justify-between">
                    <span>P75 Wait:</span>
                    <span className="text-slate-300 font-mono">{cat.p75ResolutionHours?.toFixed(1)}h</span>
                  </div>
                </div>
              </div>

              {/* Keywords */}
              <div className="mt-3 pt-2.5 border-t border-border/60">
                <div className="flex flex-wrap gap-1">
                  {cat.topKeywords && cat.topKeywords.slice(0, 3).map((kw) => (
                    <span key={kw} className="text-[10px] px-1.5 py-0.5 rounded bg-slate-800 text-slate-400 font-mono">
                      {kw}
                    </span>
                  ))}
                </div>
              </div>
            </div>
          ))
        ) : (
          <div className="col-span-5 text-center py-6 text-slate-500 text-xs italic">
            No category distribution available yet.
          </div>
        )}
      </div>

      {/* Evaluation Metrics & Confusion Matrix */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Precision / Recall / F1 Table */}
        <div className="dev-card p-6">
          <div className="flex items-center space-x-2 mb-4">
            <Target className="w-5 h-5 text-emerald-400" />
            <h2 className="text-base font-semibold text-white">Category Precision, Recall & F1-Score</h2>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-slate-900/80 text-slate-400 border-b border-border uppercase font-mono">
                <tr>
                  <th className="py-2.5 px-3">Category</th>
                  <th className="py-2.5 px-3">Precision</th>
                  <th className="py-2.5 px-3">Recall</th>
                  <th className="py-2.5 px-3">F1-Score</th>
                  <th className="py-2.5 px-3">Support</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border">
                {report?.evaluation?.categoryMetrics && report.evaluation.categoryMetrics.length > 0 ? (
                  report.evaluation.categoryMetrics.map((m) => (
                    <tr key={m.category} className="hover:bg-slate-900/50">
                      <td className="py-2.5 px-3 font-mono font-medium text-white">{m.category}</td>
                      <td className="py-2.5 px-3 font-mono text-emerald-400">{(m.precision * 100).toFixed(1)}%</td>
                      <td className="py-2.5 px-3 font-mono text-blue-400">{(m.recall * 100).toFixed(1)}%</td>
                      <td className="py-2.5 px-3 font-mono text-purple-400 font-bold">{(m.f1Score * 100).toFixed(1)}%</td>
                      <td className="py-2.5 px-3 font-mono text-slate-400">{m.support}</td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan={5} className="py-6 text-center text-slate-500 italic">
                      Zero labeled samples available for metric evaluation.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>

        {/* 5x5 Confusion Matrix */}
        <div className="dev-card p-6">
          <div className="flex items-center space-x-2 mb-4">
            <BrainCircuit className="w-5 h-5 text-purple-400" />
            <h2 className="text-base font-semibold text-white">5x5 Classification Confusion Matrix</h2>
          </div>
          <p className="text-xs text-slate-400 mb-4">
            Row: Actual Ground Truth Label | Column: TF-IDF Predicted Category
          </p>

          <div className="overflow-x-auto">
            <table className="w-full text-center text-xs font-mono">
              <thead>
                <tr className="border-b border-border text-slate-400">
                  <th className="p-2 text-left text-[11px]">Actual \ Pred</th>
                  {categoryNames.map((c) => (
                    <th key={c} className="p-2 text-[10px]" title={c}>
                      {c.substring(0, 4)}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody className="divide-y divide-border">
                {categoryNames.map((actual) => (
                  <tr key={actual}>
                    <td className="p-2 text-left text-[10px] text-slate-400" title={actual}>
                      {actual.substring(0, 8)}
                    </td>
                    {categoryNames.map((predicted) => {
                      const count = report?.evaluation?.confusionMatrix?.[actual]?.[predicted] ?? 0;
                      const isDiagonal = actual === predicted;
                      return (
                        <td
                          key={predicted}
                          className={`p-2 font-bold ${
                            isDiagonal
                              ? count > 0 ? 'bg-emerald-500/20 text-emerald-400' : 'text-slate-600'
                              : count > 0 ? 'bg-rose-500/20 text-rose-400' : 'text-slate-700'
                          }`}
                        >
                          {count}
                        </td>
                      );
                    })}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
};
