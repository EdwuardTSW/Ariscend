"use client";

import { useEffect, useState, type FormEvent } from "react";
import { ChevronDown, ChevronUp, Flag, Plus, Target, Trash2 } from "lucide-react";
import { toast } from "sonner";
import { FinanceModal } from "@/components/finance/finance-modal";
import { Button } from "@/components/ui/button";
import { ObsidianCard } from "@/components/ui/obsidian-card";
import { PageLoading } from "@/components/feedback/page-loading";
import { PageError } from "@/components/feedback/page-error";
import { useSelectedUser } from "@/contexts/selected-user-context";
import { formatMoney, inputClass, labelClass, localDateKey } from "@/lib/finance-utils";
import { financeApi } from "@/services/finance-api";
import type { Card, FinanceSettings, FinancialGoal, GoalContribution } from "@/types/api";

export function GoalsPanel() {
  const { selectedUser } = useSelectedUser();
  const [goals, setGoals] = useState<FinancialGoal[]>([]);
  const [cards, setCards] = useState<Card[]>([]);
  const [settings, setSettings] = useState<FinanceSettings | null>(null);
  const [contributions, setContributions] = useState<Record<number, GoalContribution[]>>({});
  const [expandedGoal, setExpandedGoal] = useState<number | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [goalModal, setGoalModal] = useState(false);
  const [contributionGoal, setContributionGoal] = useState<FinancialGoal | null>(null);
  const [reloadKey, setReloadKey] = useState(0);
  const [goalName, setGoalName] = useState("");
  const [goalAmount, setGoalAmount] = useState("");
  const [goalCurrency, setGoalCurrency] = useState("MXN");
  const [goalDate, setGoalDate] = useState("");
  const [contributionAmount, setContributionAmount] = useState("");
  const [exchangeRate, setExchangeRate] = useState("");
  const [sourceCardId, setSourceCardId] = useState("");
  const [notes, setNotes] = useState("");
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (!selectedUser) return;
    let active = true;
    Promise.all([
      financeApi.goals(selectedUser.id, "ACTIVE"),
      financeApi.goals(selectedUser.id, "COMPLETED"),
      financeApi.goals(selectedUser.id, "CANCELLED"),
      financeApi.cards(selectedUser.id),
      financeApi.settings(selectedUser.id),
    ]).then(([activeGoals, completedGoals, cancelledGoals, cardList, financeSettings]) => {
      if (!active) return;
      setGoals([...activeGoals, ...completedGoals, ...cancelledGoals]);
      setCards(cardList);
      setSettings(financeSettings);
      setGoalCurrency((current) => current || financeSettings.baseCurrency);
    }).catch((requestError) => { if (active) setError(requestError instanceof Error ? requestError.message : "No se pudieron cargar las metas."); })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, [reloadKey, selectedUser]);

  async function createGoal(event: FormEvent) {
    event.preventDefault(); if (!selectedUser) return;
    setSubmitting(true);
    try {
      await financeApi.createGoal(selectedUser.id, { name: goalName, targetAmount: Number(goalAmount), currency: goalCurrency.toUpperCase(), targetDate: goalDate || null });
      setGoalName(""); setGoalAmount(""); setGoalDate(""); setGoalModal(false); setReloadKey((value) => value + 1);
      toast.success("Meta creada.");
    } catch (error) { toast.error(error instanceof Error ? error.message : "No se pudo crear la meta."); }
    finally { setSubmitting(false); }
  }

  async function addContribution(event: FormEvent) {
    event.preventDefault(); if (!selectedUser || !contributionGoal) return;
    setSubmitting(true);
    try {
      await financeApi.addContribution(selectedUser.id, contributionGoal.id, {
        amount: Number(contributionAmount),
        exchangeRate: settings && contributionGoal.currency !== settings.baseCurrency ? Number(exchangeRate) : null,
        contributionDate: localDateKey(),
        notes: notes || null,
        sourceDebitCardId: sourceCardId ? Number(sourceCardId) : null,
      });
      setContributionAmount(""); setExchangeRate(""); setSourceCardId(""); setNotes(""); setContributionGoal(null); setReloadKey((value) => value + 1);
      toast.success("Aporte registrado y gasto creado.");
    } catch (error) { toast.error(error instanceof Error ? error.message : "No se pudo registrar el aporte."); }
    finally { setSubmitting(false); }
  }

  async function toggleHistory(goal: FinancialGoal) {
    if (!selectedUser) return;
    if (expandedGoal === goal.id) { setExpandedGoal(null); return; }
    setExpandedGoal(goal.id);
    if (contributions[goal.id]) return;
    try {
      const result = await financeApi.contributions(selectedUser.id, goal.id);
      setContributions((current) => ({ ...current, [goal.id]: result }));
    } catch (error) { toast.error(error instanceof Error ? error.message : "No se cargaron los aportes."); }
  }

  async function cancelGoal(goal: FinancialGoal) {
    if (!selectedUser || !window.confirm(`¿Cancelar la meta “${goal.name}”?`)) return;
    try { await financeApi.cancelGoal(selectedUser.id, goal.id); setReloadKey((value) => value + 1); }
    catch (error) { toast.error(error instanceof Error ? error.message : "No se pudo cancelar la meta."); }
  }

  async function cancelContribution(goalId: number, contributionId: number) {
    if (!selectedUser || !window.confirm("¿Cancelar este aporte y su gasto relacionado?")) return;
    try {
      await financeApi.cancelContribution(selectedUser.id, goalId, contributionId);
      setContributions((current) => ({ ...current, [goalId]: (current[goalId] ?? []).filter((item) => item.id !== contributionId) }));
      setReloadKey((value) => value + 1);
    } catch (error) { toast.error(error instanceof Error ? error.message : "No se pudo cancelar el aporte."); }
  }

  if (loading && goals.length === 0) return <PageLoading label="Cargando tus metas" />;
  if (error && goals.length === 0) return <PageError message={error} onRetry={() => { setError(null); setLoading(true); setReloadKey((value) => value + 1); }} />;
  const usableDebitCards = cards.filter((card) => card.type === "DEBIT" && card.active && !card.cancelledAt && card.currency === contributionGoal?.currency);

  return (
    <div>
      <div className="mb-5 flex items-center justify-between gap-4"><div><h2 className="text-xl font-semibold">Metas financieras</h2><p className="mt-1 text-sm text-[#85878a]">Cada aporte crea un gasto para mantener tu balance real.</p></div><Button size="small" onClick={() => { setGoalCurrency(settings?.baseCurrency ?? "MXN"); setGoalModal(true); }}><Plus className="size-4" /> Nueva meta</Button></div>
      {goals.length === 0 ? <ObsidianCard className="flex min-h-64 flex-col items-center justify-center p-8 text-center"><Target className="size-8 text-[#77797c]" /><h3 className="mt-4 text-lg font-semibold">Define tu siguiente objetivo</h3><p className="mt-2 max-w-sm text-sm text-[#85878a]">Una moto, un viaje o tu fondo de emergencia.</p><Button className="mt-5" onClick={() => setGoalModal(true)}><Flag className="size-4" /> Crear meta</Button></ObsidianCard> : <div className="space-y-4">{goals.map((goal) => <ObsidianCard key={goal.id} className={goal.status === "CANCELLED" ? "p-5 opacity-45" : "p-5 md:p-6"}><div className="relative z-10"><div className="flex items-start justify-between gap-4"><div className="min-w-0"><div className="flex items-center gap-2"><h3 className="truncate text-lg font-semibold">{goal.name}</h3><span className="rounded-full border border-white/10 px-2 py-0.5 text-[10px] uppercase text-[#8c8e91]">{goal.status === "ACTIVE" ? "Activa" : goal.status === "COMPLETED" ? "Completada" : "Cancelada"}</span></div><p className="mt-1 text-sm text-[#85878a]">{formatMoney(goal.currentAmount, goal.currency)} de {formatMoney(goal.targetAmount, goal.currency)}</p></div><p className="font-[var(--font-geist)] text-xl font-semibold">{Math.round(goal.progressPercentage)}%</p></div><div className="mt-5 h-2 overflow-hidden rounded-full bg-white/[0.07]"><div className="h-full rounded-full bg-white transition-all" style={{ width: `${Math.min(100, goal.progressPercentage)}%` }} /></div><div className="mt-4 flex flex-wrap items-center justify-between gap-2"><p className="text-xs text-[#77797c]">Restante {formatMoney(goal.remainingAmount, goal.currency)}{goal.targetDate ? ` · Meta ${goal.targetDate}` : ""}</p><div className="flex gap-1">{goal.status === "ACTIVE" && <Button size="small" onClick={() => setContributionGoal(goal)}><Plus className="size-3.5" /> Aportar</Button>}<Button size="small" variant="ghost" onClick={() => void toggleHistory(goal)}>{expandedGoal === goal.id ? <ChevronUp className="size-3.5" /> : <ChevronDown className="size-3.5" />} Historial</Button>{goal.status === "ACTIVE" && <Button size="icon" variant="ghost" onClick={() => void cancelGoal(goal)} aria-label="Cancelar meta"><Trash2 className="size-4" /></Button>}</div></div>{expandedGoal === goal.id && <div className="mt-5 border-t border-white/[0.07] pt-4"><h4 className="text-sm font-semibold">Historial de aportes</h4>{!contributions[goal.id] ? <p className="mt-3 text-sm text-[#85878a]">Cargando...</p> : contributions[goal.id].length === 0 ? <p className="mt-3 text-sm text-[#85878a]">Sin aportes todavía.</p> : <div className="mt-3 space-y-2">{contributions[goal.id].map((item) => <div key={item.id} className="flex items-center justify-between rounded-xl border border-white/[0.06] p-3"><div><p className="text-sm font-semibold">{formatMoney(item.amount, item.currency)}</p><p className="mt-1 text-xs text-[#77797c]">{item.contributionDate}{item.notes ? ` · ${item.notes}` : ""}</p></div>{item.status === "ACTIVE" && <Button size="icon" variant="ghost" onClick={() => void cancelContribution(goal.id, item.id)} aria-label="Cancelar aporte"><Trash2 className="size-3.5" /></Button>}</div>)}</div>}</div>}</div></ObsidianCard>)}</div>}

      <FinanceModal open={goalModal} onOpenChange={setGoalModal} title="Nueva meta financiera">
        <form onSubmit={createGoal} className="grid gap-4 sm:grid-cols-2"><label className={`${labelClass} sm:col-span-2`}>Nombre<input required maxLength={120} value={goalName} onChange={(event) => setGoalName(event.target.value)} placeholder="Comprar una moto" className={`${inputClass} mt-2`} /></label><label className={labelClass}>Monto objetivo<input required min="0.01" step="0.01" type="number" value={goalAmount} onChange={(event) => setGoalAmount(event.target.value)} className={`${inputClass} mt-2`} /></label><label className={labelClass}>Moneda<input required maxLength={3} value={goalCurrency} onChange={(event) => setGoalCurrency(event.target.value.toUpperCase())} className={`${inputClass} mt-2 uppercase`} /></label><label className={`${labelClass} sm:col-span-2`}>Fecha objetivo opcional<input type="date" value={goalDate} onChange={(event) => setGoalDate(event.target.value)} className={`${inputClass} mt-2`} /></label><div className="flex justify-end gap-3 pt-2 sm:col-span-2"><Button type="button" variant="ghost" onClick={() => setGoalModal(false)}>Cancelar</Button><Button type="submit" disabled={submitting}>{submitting ? "Creando..." : "Crear meta"}</Button></div></form>
      </FinanceModal>

      <FinanceModal open={Boolean(contributionGoal)} onOpenChange={(open) => !open && setContributionGoal(null)} title={`Aportar a ${contributionGoal?.name ?? "meta"}`} description="El aporte se registrará también como gasto en Finanzas.">
        <form onSubmit={addContribution} className="grid gap-4 sm:grid-cols-2"><label className={labelClass}>Monto<input required min="0.01" step="0.01" type="number" value={contributionAmount} onChange={(event) => setContributionAmount(event.target.value)} className={`${inputClass} mt-2`} /></label>{settings && contributionGoal && contributionGoal.currency !== settings.baseCurrency && <label className={labelClass}>Cambio a {settings.baseCurrency}<input required min="0.000001" step="0.000001" type="number" value={exchangeRate} onChange={(event) => setExchangeRate(event.target.value)} className={`${inputClass} mt-2`} /></label>}<label className={`${labelClass} sm:col-span-2`}>Tarjeta de débito opcional<select value={sourceCardId} onChange={(event) => setSourceCardId(event.target.value)} className={`${inputClass} mt-2`}><option value="">Sin tarjeta origen</option>{usableDebitCards.map((card) => <option key={card.id} value={card.id}>{card.alias} ·•• {card.lastFourDigits}</option>)}</select></label><label className={`${labelClass} sm:col-span-2`}>Nota opcional<input maxLength={500} value={notes} onChange={(event) => setNotes(event.target.value)} className={`${inputClass} mt-2`} /></label><div className="flex justify-end gap-3 pt-2 sm:col-span-2"><Button type="button" variant="ghost" onClick={() => setContributionGoal(null)}>Cancelar</Button><Button type="submit" disabled={submitting}>{submitting ? "Registrando..." : "Registrar aporte"}</Button></div></form>
      </FinanceModal>
    </div>
  );
}
