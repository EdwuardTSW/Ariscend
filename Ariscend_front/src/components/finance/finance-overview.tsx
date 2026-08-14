"use client";

import { useEffect, useState } from "react";
import { ArrowDownRight, ArrowUpRight, Landmark } from "lucide-react";
import { toast } from "sonner";
import { ObsidianCard } from "@/components/ui/obsidian-card";
import { PageLoading } from "@/components/feedback/page-loading";
import { useSelectedUser } from "@/contexts/selected-user-context";
import { financeApi } from "@/services/finance-api";
import type { FinanceSummary } from "@/types/api";

function dateKey(date: Date) { return date.toISOString().slice(0, 10); }

export function FinanceOverview() {
  const { selectedUser } = useSelectedUser();
  const [summary, setSummary] = useState<FinanceSummary | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!selectedUser) return;
    const now = new Date();
    const first = new Date(now.getFullYear(), now.getMonth(), 1);
    const last = new Date(now.getFullYear(), now.getMonth() + 1, 0);
    void financeApi.summary(selectedUser.id, dateKey(first), dateKey(last))
      .then(setSummary)
      .catch((error) => toast.error(error instanceof Error ? error.message : "No se pudo cargar el resumen."))
      .finally(() => setLoading(false));
  }, [selectedUser]);

  if (loading || !summary) return <PageLoading label="Calculando tu balance" />;
  const money = new Intl.NumberFormat("es-MX", { style: "currency", currency: summary.baseCurrency });
  return <div><div className="mb-8"><p className="mb-2 text-xs font-semibold uppercase tracking-[0.16em] text-[#8c8e91]">Control financiero</p><h1 className="text-3xl font-semibold tracking-[-0.04em] md:text-5xl">Finanzas</h1><p className="mt-2 text-[#a9abad]">Resumen del mes actual en {summary.baseCurrency}.</p></div><div className="grid gap-4 md:grid-cols-3"><ObsidianCard className="p-6 md:col-span-3"><div className="relative z-10"><p className="text-xs uppercase tracking-[0.14em] text-[#888a8d]">Balance del mes</p><p className="mt-3 text-4xl font-semibold tracking-[-0.04em] md:text-6xl">{money.format(summary.balance)}</p></div></ObsidianCard><ObsidianCard className="p-5"><ArrowUpRight className="size-5 text-[#a9abad]" /><p className="mt-6 text-sm text-[#8c8e91]">Ingresos</p><p className="mt-1 text-2xl font-semibold">{money.format(summary.totalIncome)}</p></ObsidianCard><ObsidianCard className="p-5"><ArrowDownRight className="size-5 text-[#a9abad]" /><p className="mt-6 text-sm text-[#8c8e91]">Gastos</p><p className="mt-1 text-2xl font-semibold">{money.format(summary.totalExpenses)}</p></ObsidianCard><ObsidianCard className="p-5"><Landmark className="size-5 text-[#a9abad]" /><p className="mt-6 text-sm text-[#8c8e91]">Siguiente etapa</p><p className="mt-1 font-semibold">Movimientos, tarjetas y metas</p></ObsidianCard></div><p className="mt-6 text-sm leading-6 text-[#77797c]">La vista completa de movimientos, tarjetas y metas se construirá sobre esta conexión ya funcional.</p></div>;
}
