"use client";

import { useEffect, useState, type FormEvent } from "react";
import { BellRing, CreditCard, Eye, EyeOff, Plus, Power, Trash2, WalletCards } from "lucide-react";
import { toast } from "sonner";
import { FinanceModal } from "@/components/finance/finance-modal";
import { Button } from "@/components/ui/button";
import { ObsidianCard } from "@/components/ui/obsidian-card";
import { PageLoading } from "@/components/feedback/page-loading";
import { PageError } from "@/components/feedback/page-error";
import { useSelectedUser } from "@/contexts/selected-user-context";
import { formatMoney, inputClass, labelClass } from "@/lib/finance-utils";
import { financeApi } from "@/services/finance-api";
import type { Card, CardInput, CardSummary, CardType } from "@/types/api";

interface CardFormState {
  alias: string;
  issuer: string;
  type: CardType;
  lastFourDigits: string;
  currency: string;
  creditLimit: string;
  openingBalance: string;
  closingDay: string;
  paymentDueDay: string;
}

const initialForm: CardFormState = { alias: "", issuer: "", type: "DEBIT", lastFourDigits: "", currency: "MXN", creditLimit: "", openingBalance: "", closingDay: "", paymentDueDay: "" };

export function CardsPanel() {
  const { selectedUser } = useSelectedUser();
  const [cards, setCards] = useState<Card[]>([]);
  const [summaries, setSummaries] = useState<Record<number, CardSummary>>({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [modalOpen, setModalOpen] = useState(false);
  const [form, setForm] = useState<CardFormState>(initialForm);
  const [submitting, setSubmitting] = useState(false);
  const [showAmounts, setShowAmounts] = useState(true);
  const [reloadKey, setReloadKey] = useState(0);

  useEffect(() => {
    if (!selectedUser) return;
    let active = true;
    financeApi.cards(selectedUser.id).then(async (result) => {
      const summaryResults = await Promise.all(
        result.filter((card) => !card.cancelledAt).map(async (card) => [card.id, await financeApi.cardSummary(selectedUser.id, card.id)] as const),
      );
      if (!active) return;
      setCards(result);
      setSummaries(Object.fromEntries(summaryResults));
    }).catch((requestError) => { if (active) setError(requestError instanceof Error ? requestError.message : "No se pudieron cargar las tarjetas."); })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, [reloadKey, selectedUser]);

  function updateForm<K extends keyof CardFormState>(key: K, value: CardFormState[K]) {
    setForm((current) => ({ ...current, [key]: value }));
  }

  async function submit(event: FormEvent) {
    event.preventDefault(); if (!selectedUser) return;
    const data: CardInput = {
      alias: form.alias,
      issuer: form.issuer,
      type: form.type,
      lastFourDigits: form.lastFourDigits,
      currency: form.currency.toUpperCase(),
      creditLimit: form.type === "CREDIT" ? Number(form.creditLimit) : null,
      openingBalance: form.type === "DEBIT" ? Number(form.openingBalance) : null,
      closingDay: form.type === "CREDIT" ? Number(form.closingDay) : null,
      paymentDueDay: form.type === "CREDIT" ? Number(form.paymentDueDay) : null,
    };
    setSubmitting(true);
    try {
      await financeApi.createCard(selectedUser.id, data);
      setForm(initialForm); setModalOpen(false); setReloadKey((value) => value + 1);
      toast.success("Tarjeta registrada.");
    } catch (error) { toast.error(error instanceof Error ? error.message : "No se pudo registrar la tarjeta."); }
    finally { setSubmitting(false); }
  }

  async function toggleActive(card: Card) {
    if (!selectedUser) return;
    try {
      const updated = await financeApi.updateCardActive(selectedUser.id, card.id, !card.active);
      setCards((current) => current.map((item) => item.id === card.id ? updated : item));
    } catch (error) { toast.error(error instanceof Error ? error.message : "No se pudo cambiar el estado."); }
  }

  async function cancel(card: Card) {
    if (!selectedUser || !window.confirm(`¿Cancelar la tarjeta “${card.alias}”?`)) return;
    try {
      await financeApi.cancelCard(selectedUser.id, card.id);
      setReloadKey((value) => value + 1);
      toast.success("Tarjeta cancelada.");
    } catch (error) { toast.error(error instanceof Error ? error.message : "No se pudo cancelar la tarjeta."); }
  }

  if (loading && cards.length === 0) return <PageLoading label="Consultando tus tarjetas" />;
  if (error && cards.length === 0) return <PageError message={error} onRetry={() => { setError(null); setLoading(true); setReloadKey((value) => value + 1); }} />;
  const activeCards = cards.filter((card) => !card.cancelledAt);

  return (
    <div>
      <div className="mb-5 flex items-center justify-between gap-4"><div><h2 className="text-xl font-semibold">Tus tarjetas</h2><p className="mt-1 text-sm text-[#85878a]">Sólo guardamos alias y últimos cuatro dígitos.</p></div><div className="flex gap-2"><Button variant="ghost" size="icon" onClick={() => setShowAmounts((value) => !value)} aria-label="Mostrar u ocultar montos">{showAmounts ? <Eye className="size-4" /> : <EyeOff className="size-4" />}</Button><Button size="small" onClick={() => setModalOpen(true)}><Plus className="size-4" /> Agregar</Button></div></div>
      {activeCards.length === 0 ? <ObsidianCard className="flex min-h-64 flex-col items-center justify-center p-8 text-center"><WalletCards className="size-8 text-[#77797c]" /><h3 className="mt-4 text-lg font-semibold">Sin tarjetas registradas</h3><p className="mt-2 max-w-sm text-sm text-[#85878a]">Agrega una tarjeta de débito o crédito para calcular saldo, deuda y fechas.</p><Button className="mt-5" onClick={() => setModalOpen(true)}><Plus className="size-4" /> Agregar tarjeta</Button></ObsidianCard> : <div className="grid gap-4 lg:grid-cols-2">{activeCards.map((card) => {
        const summary = summaries[card.id];
        const amount = card.type === "CREDIT" ? summary?.currentDebt : summary?.currentBalance;
        return <ObsidianCard key={card.id} className={`min-h-64 p-6 ${!card.active ? "opacity-55" : ""}`}><div className="relative z-10 flex h-full flex-col"><div className="flex items-start justify-between"><div><p className="text-xs font-semibold uppercase tracking-[0.14em] text-[#8c8e91]">{card.issuer}</p><h3 className="mt-2 text-xl font-semibold">{card.alias}</h3></div><CreditCard className="size-7 text-[#b4b6b8]" /></div><p className="mt-8 font-[var(--font-geist)] text-sm tracking-[0.24em] text-[#b7b9bb]">•••• {card.lastFourDigits}</p><div className="mt-auto pt-7"><p className="text-xs text-[#85878a]">{card.type === "CREDIT" ? "Deuda actual" : "Saldo disponible"}</p><p className="mt-1 text-3xl font-semibold tracking-[-0.04em]">{showAmounts ? formatMoney(amount, card.currency) : "••••••"}</p>{card.type === "CREDIT" && summary && <div className="mt-4 grid grid-cols-2 gap-3 border-t border-white/[0.07] pt-4 text-xs"><span className="text-[#85878a]">Disponible<br /><b className="mt-1 block text-sm text-white">{showAmounts ? formatMoney(summary.availableCredit, card.currency) : "••••"}</b></span><span className="text-[#85878a]">Próximo pago<br /><b className="mt-1 block text-sm text-white">{summary.nextPaymentDate ?? "—"}</b></span></div>}{summary?.paymentAlertStatus !== "NONE" && <p className="mt-3 flex items-center gap-2 text-xs text-[#d7d7d8]"><BellRing className="size-3.5" /> {summary.paymentAlertStatus === "DUE_TODAY" ? "Pago programado para hoy" : `Pago próximo en ${summary.daysUntilPayment} días`}</p>}<div className="mt-4 flex justify-end gap-1"><Button size="small" variant="ghost" onClick={() => void toggleActive(card)}><Power className="size-3.5" /> {card.active ? "Desactivar" : "Activar"}</Button><Button size="icon" variant="ghost" onClick={() => void cancel(card)} aria-label="Cancelar tarjeta"><Trash2 className="size-4" /></Button></div></div></div></ObsidianCard>;
      })}</div>}

      <FinanceModal open={modalOpen} onOpenChange={setModalOpen} title="Registrar tarjeta" description="Ariscend nunca solicitará el número completo, CVV o PIN.">
        <form onSubmit={submit} className="grid gap-4 sm:grid-cols-2">
          <label className={labelClass}>Alias<input required maxLength={80} value={form.alias} onChange={(event) => updateForm("alias", event.target.value)} placeholder="Nu principal" className={`${inputClass} mt-2`} /></label>
          <label className={labelClass}>Emisor<input required maxLength={80} value={form.issuer} onChange={(event) => updateForm("issuer", event.target.value)} placeholder="Nu, Plata..." className={`${inputClass} mt-2`} /></label>
          <label className={labelClass}>Tipo<select value={form.type} onChange={(event) => updateForm("type", event.target.value as CardType)} className={`${inputClass} mt-2`}><option value="DEBIT">Débito</option><option value="CREDIT">Crédito</option></select></label>
          <label className={labelClass}>Últimos 4 dígitos<input required inputMode="numeric" pattern="\d{4}" maxLength={4} value={form.lastFourDigits} onChange={(event) => updateForm("lastFourDigits", event.target.value.replace(/\D/g, ""))} placeholder="1234" className={`${inputClass} mt-2`} /></label>
          <label className={labelClass}>Moneda<input required maxLength={3} value={form.currency} onChange={(event) => updateForm("currency", event.target.value.toUpperCase())} className={`${inputClass} mt-2 uppercase`} /></label>
          {form.type === "DEBIT" ? <label className={labelClass}>Saldo inicial<input required min="0" step="0.01" type="number" value={form.openingBalance} onChange={(event) => updateForm("openingBalance", event.target.value)} className={`${inputClass} mt-2`} /></label> : <><label className={labelClass}>Límite de crédito<input required min="0.01" step="0.01" type="number" value={form.creditLimit} onChange={(event) => updateForm("creditLimit", event.target.value)} className={`${inputClass} mt-2`} /></label><label className={labelClass}>Día de corte<input required min="1" max="31" type="number" value={form.closingDay} onChange={(event) => updateForm("closingDay", event.target.value)} className={`${inputClass} mt-2`} /></label><label className={labelClass}>Día de pago<input required min="1" max="31" type="number" value={form.paymentDueDay} onChange={(event) => updateForm("paymentDueDay", event.target.value)} className={`${inputClass} mt-2`} /></label></>}
          <div className="flex justify-end gap-3 pt-2 sm:col-span-2"><Button type="button" variant="ghost" onClick={() => setModalOpen(false)}>Cancelar</Button><Button type="submit" disabled={submitting}>{submitting ? "Guardando..." : "Registrar tarjeta"}</Button></div>
        </form>
      </FinanceModal>
    </div>
  );
}
