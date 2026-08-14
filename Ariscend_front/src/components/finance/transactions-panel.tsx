"use client";

import { useEffect, useState, type FormEvent } from "react";
import { ArrowDownRight, ArrowUpRight, CreditCard, Pencil, Plus, Trash2 } from "lucide-react";
import { toast } from "sonner";
import { FinanceModal } from "@/components/finance/finance-modal";
import { Button } from "@/components/ui/button";
import { ObsidianCard } from "@/components/ui/obsidian-card";
import { PageLoading } from "@/components/feedback/page-loading";
import { PageError } from "@/components/feedback/page-error";
import { useSelectedUser } from "@/contexts/selected-user-context";
import { currentMonthRange, formatMoney, inputClass, labelClass, localDateKey } from "@/lib/finance-utils";
import { financeApi } from "@/services/finance-api";
import type { Card, FinanceSettings, FinancialTransaction, FinancialTransactionInput, TransactionCategory, TransactionType } from "@/types/api";

interface TransactionFormState {
  type: TransactionType;
  amount: string;
  currency: string;
  exchangeRate: string;
  categoryId: string;
  cardId: string;
  paidCreditCardId: string;
  description: string;
  transactionDate: string;
}

function emptyForm(currency = "MXN"): TransactionFormState {
  return { type: "EXPENSE", amount: "", currency, exchangeRate: "", categoryId: "", cardId: "", paidCreditCardId: "", description: "", transactionDate: localDateKey() };
}

export function TransactionsPanel() {
  const { selectedUser } = useSelectedUser();
  const range = currentMonthRange();
  const [transactions, setTransactions] = useState<FinancialTransaction[]>([]);
  const [categories, setCategories] = useState<TransactionCategory[]>([]);
  const [cards, setCards] = useState<Card[]>([]);
  const [settings, setSettings] = useState<FinanceSettings | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<FinancialTransaction | null>(null);
  const [form, setForm] = useState<TransactionFormState>(emptyForm());
  const [submitting, setSubmitting] = useState(false);
  const [typeFilter, setTypeFilter] = useState<"" | TransactionType>("");
  const [dateFrom, setDateFrom] = useState(range.from);
  const [dateTo, setDateTo] = useState(range.to);
  const [reloadKey, setReloadKey] = useState(0);

  useEffect(() => {
    if (!selectedUser) return;
    let active = true;
    Promise.all([
      financeApi.transactions(selectedUser.id, { type: typeFilter || undefined, dateFrom, dateTo }),
      financeApi.categories(selectedUser.id),
      financeApi.cards(selectedUser.id),
      financeApi.settings(selectedUser.id),
    ]).then(([movementPage, categoryList, cardList, financeSettings]) => {
      if (!active) return;
      setTransactions(movementPage.content);
      setCategories(categoryList);
      setCards(cardList.filter((card) => !card.cancelledAt));
      setSettings(financeSettings);
    }).catch((requestError) => {
      if (active) setError(requestError instanceof Error ? requestError.message : "No se pudieron cargar los movimientos.");
    }).finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, [dateFrom, dateTo, reloadKey, selectedUser, typeFilter]);

  function openCreate(type: TransactionType = "EXPENSE") {
    setEditing(null);
    setForm({ ...emptyForm(settings?.baseCurrency), type });
    setModalOpen(true);
  }

  function openEdit(transaction: FinancialTransaction) {
    setEditing(transaction);
    setForm({
      type: transaction.type,
      amount: String(transaction.amount),
      currency: transaction.currency,
      exchangeRate: String(transaction.exchangeRate),
      categoryId: transaction.categoryId ? String(transaction.categoryId) : "",
      cardId: transaction.cardId ? String(transaction.cardId) : "",
      paidCreditCardId: transaction.paidCreditCardId ? String(transaction.paidCreditCardId) : "",
      description: transaction.description ?? "",
      transactionDate: transaction.transactionDate,
    });
    setModalOpen(true);
  }

  function updateForm<K extends keyof TransactionFormState>(key: K, value: TransactionFormState[K]) {
    setForm((current) => ({ ...current, [key]: value }));
  }

  async function submit(event: FormEvent) {
    event.preventDefault();
    if (!selectedUser || !settings) return;
    const data: FinancialTransactionInput = {
      type: form.type,
      amount: Number(form.amount),
      currency: form.currency.toUpperCase(),
      exchangeRate: form.currency.toUpperCase() === settings.baseCurrency ? null : Number(form.exchangeRate),
      categoryId: form.type === "CREDIT_CARD_PAYMENT" ? null : Number(form.categoryId),
      cardId: form.cardId ? Number(form.cardId) : null,
      paidCreditCardId: form.type === "CREDIT_CARD_PAYMENT" ? Number(form.paidCreditCardId) : null,
      description: form.description || null,
      transactionDate: form.transactionDate,
    };
    setSubmitting(true);
    try {
      if (editing) await financeApi.updateTransaction(selectedUser.id, editing.id, data);
      else await financeApi.createTransaction(selectedUser.id, data);
      toast.success(editing ? "Movimiento actualizado." : "Movimiento registrado.");
      setModalOpen(false);
      setReloadKey((value) => value + 1);
    } catch (error) {
      toast.error(error instanceof Error ? error.message : "No se pudo guardar el movimiento.");
    } finally { setSubmitting(false); }
  }

  async function cancel(transaction: FinancialTransaction) {
    if (!selectedUser || !window.confirm("¿Cancelar este movimiento? Se conservará en el historial.")) return;
    try {
      await financeApi.cancelTransaction(selectedUser.id, transaction.id);
      setTransactions((current) => current.filter((item) => item.id !== transaction.id));
      toast.success("Movimiento cancelado.");
    } catch (error) { toast.error(error instanceof Error ? error.message : "No se pudo cancelar."); }
  }

  if (loading && transactions.length === 0) return <PageLoading label="Cargando movimientos" />;
  if (error && transactions.length === 0) return <PageError message={error} onRetry={() => { setError(null); setLoading(true); setReloadKey((value) => value + 1); }} />;

  const categoryOptions = categories.filter((category) => category.type === (form.type === "INCOME" ? "INCOME" : "EXPENSE"));
  const debitCards = cards.filter((card) => card.type === "DEBIT" && card.active);
  const creditCards = cards.filter((card) => card.type === "CREDIT");
  const usableCards = form.type === "INCOME" ? debitCards : cards.filter((card) => card.active);

  return (
    <div>
      <div className="mb-5 flex flex-col gap-3 lg:flex-row lg:items-end lg:justify-between">
        <div className="grid grid-cols-3 gap-2 sm:flex">
          <Button size="small" onClick={() => openCreate("EXPENSE")}><ArrowDownRight className="size-4" /> Gasto</Button>
          <Button size="small" variant="secondary" onClick={() => openCreate("INCOME")}><ArrowUpRight className="size-4" /> Ingreso</Button>
          <Button size="small" variant="secondary" onClick={() => openCreate("CREDIT_CARD_PAYMENT")}><CreditCard className="size-4" /> Pago</Button>
        </div>
        <div className="grid grid-cols-3 gap-2">
          <select value={typeFilter} onChange={(event) => setTypeFilter(event.target.value as "" | TransactionType)} className={inputClass}><option value="">Todos</option><option value="INCOME">Ingresos</option><option value="EXPENSE">Gastos</option><option value="CREDIT_CARD_PAYMENT">Pagos</option></select>
          <input type="date" value={dateFrom} onChange={(event) => setDateFrom(event.target.value)} className={inputClass} aria-label="Fecha inicial" />
          <input type="date" value={dateTo} onChange={(event) => setDateTo(event.target.value)} className={inputClass} aria-label="Fecha final" />
        </div>
      </div>

      {transactions.length === 0 ? <ObsidianCard className="flex min-h-56 flex-col items-center justify-center p-8 text-center"><ReceiptEmpty /><h3 className="mt-4 text-lg font-semibold">Sin movimientos en este periodo</h3><p className="mt-2 text-sm text-[#85878a]">Registra un ingreso o gasto para comenzar.</p><Button className="mt-5" onClick={() => openCreate()}><Plus className="size-4" /> Nuevo movimiento</Button></ObsidianCard> : <div className="space-y-2">{transactions.map((transaction) => {
        const isIncome = transaction.type === "INCOME";
        const isPayment = transaction.type === "CREDIT_CARD_PAYMENT";
        return <ObsidianCard key={transaction.id} className="flex items-center gap-4 p-4"><span className={`relative z-10 flex size-10 shrink-0 items-center justify-center rounded-full border ${isIncome ? "border-white/20 bg-white text-black" : "border-white/10 bg-white/[0.04]"}`}>{isIncome ? <ArrowUpRight className="size-4" /> : isPayment ? <CreditCard className="size-4" /> : <ArrowDownRight className="size-4" />}</span><div className="relative z-10 min-w-0 flex-1"><p className="truncate font-semibold">{transaction.description || transaction.categoryName || (isPayment ? "Pago de tarjeta" : "Movimiento")}</p><p className="mt-1 text-xs text-[#7f8184]">{transaction.transactionDate} · {transaction.currency}{transaction.goalGenerated ? " · Aporte a meta" : ""}</p></div><div className="relative z-10 text-right"><p className={`font-[var(--font-geist)] font-semibold ${isIncome ? "text-white" : "text-[#d0d0d2]"}`}>{isIncome ? "+" : "−"}{formatMoney(transaction.amount, transaction.currency)}</p><div className="mt-1 flex justify-end gap-1">{!transaction.goalGenerated && <button onClick={() => openEdit(transaction)} className="focus-ring rounded-md p-1.5 text-[#737579] hover:text-white" aria-label="Editar"><Pencil className="size-3.5" /></button>}<button onClick={() => void cancel(transaction)} className="focus-ring rounded-md p-1.5 text-[#737579] hover:text-red-200" aria-label="Cancelar"><Trash2 className="size-3.5" /></button></div></div></ObsidianCard>;
      })}</div>}

      <FinanceModal open={modalOpen} onOpenChange={setModalOpen} title={editing ? "Editar movimiento" : form.type === "INCOME" ? "Registrar ingreso" : form.type === "EXPENSE" ? "Registrar gasto" : "Pagar tarjeta"} description={form.type === "CREDIT_CARD_PAYMENT" ? "El pago reduce la deuda y no vuelve a contarse como gasto." : undefined}>
        <form onSubmit={submit} className="grid gap-4 sm:grid-cols-2">
          <label className={labelClass}>Tipo<select value={form.type} disabled={Boolean(editing?.goalGenerated)} onChange={(event) => updateForm("type", event.target.value as TransactionType)} className={`${inputClass} mt-2`}><option value="EXPENSE">Gasto</option><option value="INCOME">Ingreso</option><option value="CREDIT_CARD_PAYMENT">Pago de tarjeta</option></select></label>
          <label className={labelClass}>Monto<input required min="0.01" step="0.01" type="number" value={form.amount} onChange={(event) => updateForm("amount", event.target.value)} className={`${inputClass} mt-2`} /></label>
          <label className={labelClass}>Moneda<input required maxLength={3} value={form.currency} onChange={(event) => updateForm("currency", event.target.value.toUpperCase())} className={`${inputClass} mt-2 uppercase`} /></label>
          {settings && form.currency.toUpperCase() !== settings.baseCurrency && <label className={labelClass}>Tipo de cambio a {settings.baseCurrency}<input required min="0.000001" step="0.000001" type="number" value={form.exchangeRate} onChange={(event) => updateForm("exchangeRate", event.target.value)} className={`${inputClass} mt-2`} /></label>}
          {form.type !== "CREDIT_CARD_PAYMENT" && <label className={labelClass}>Categoría<select required value={form.categoryId} onChange={(event) => updateForm("categoryId", event.target.value)} className={`${inputClass} mt-2`}><option value="">Selecciona</option>{categoryOptions.map((category) => <option key={category.id} value={category.id}>{category.name}</option>)}</select></label>}
          {form.type !== "CREDIT_CARD_PAYMENT" ? <label className={labelClass}>Tarjeta opcional<select value={form.cardId} onChange={(event) => updateForm("cardId", event.target.value)} className={`${inputClass} mt-2`}><option value="">Sin tarjeta</option>{usableCards.map((card) => <option key={card.id} value={card.id}>{card.alias} ·•• {card.lastFourDigits}</option>)}</select></label> : <><label className={labelClass}>Tarjeta de crédito destino<select required value={form.paidCreditCardId} onChange={(event) => updateForm("paidCreditCardId", event.target.value)} className={`${inputClass} mt-2`}><option value="">Selecciona</option>{creditCards.map((card) => <option key={card.id} value={card.id}>{card.alias} ·•• {card.lastFourDigits}</option>)}</select></label><label className={labelClass}>Débito origen opcional<select value={form.cardId} onChange={(event) => updateForm("cardId", event.target.value)} className={`${inputClass} mt-2`}><option value="">Sin tarjeta origen</option>{debitCards.map((card) => <option key={card.id} value={card.id}>{card.alias} ·•• {card.lastFourDigits}</option>)}</select></label></>}
          <label className={labelClass}>Fecha<input required type="date" value={form.transactionDate} onChange={(event) => updateForm("transactionDate", event.target.value)} className={`${inputClass} mt-2`} /></label>
          <label className={`${labelClass} sm:col-span-2`}>Descripción<input maxLength={500} value={form.description} onChange={(event) => updateForm("description", event.target.value)} placeholder="Opcional" className={`${inputClass} mt-2`} /></label>
          <div className="flex justify-end gap-3 pt-2 sm:col-span-2"><Button type="button" variant="ghost" onClick={() => setModalOpen(false)}>Cancelar</Button><Button type="submit" disabled={submitting}>{submitting ? "Guardando..." : editing ? "Guardar cambios" : "Registrar"}</Button></div>
        </form>
      </FinanceModal>
    </div>
  );
}

function ReceiptEmpty() {
  return <div className="flex size-11 items-center justify-center rounded-full border border-white/10 text-[#85878a]"><ArrowDownRight className="size-5" /></div>;
}
