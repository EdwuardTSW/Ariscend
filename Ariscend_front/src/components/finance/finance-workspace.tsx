"use client";

import { useState } from "react";
import dynamic from "next/dynamic";
import { ChartNoAxesCombined, CreditCard, Flag, ReceiptText } from "lucide-react";
import { FinanceOverview } from "@/components/finance/finance-overview";
import { cn } from "@/lib/utils";

const TransactionsPanel = dynamic(() => import("@/components/finance/transactions-panel").then((module) => module.TransactionsPanel));
const CardsPanel = dynamic(() => import("@/components/finance/cards-panel").then((module) => module.CardsPanel));
const GoalsPanel = dynamic(() => import("@/components/finance/goals-panel").then((module) => module.GoalsPanel));

type FinanceTab = "overview" | "transactions" | "cards" | "goals";

const tabs = [
  { id: "overview" as const, label: "Resumen", icon: ChartNoAxesCombined },
  { id: "transactions" as const, label: "Movimientos", icon: ReceiptText },
  { id: "cards" as const, label: "Tarjetas", icon: CreditCard },
  { id: "goals" as const, label: "Metas", icon: Flag },
];

export function FinanceWorkspace() {
  const [tab, setTab] = useState<FinanceTab>("overview");

  return (
    <div>
      <header className="mb-7 md:mb-9">
        <p className="mb-2 text-xs font-semibold uppercase tracking-[0.16em] text-[#8c8e91]">Control financiero</p>
        <h1 className="text-3xl font-semibold tracking-[-0.04em] md:text-5xl">Finanzas</h1>
        <p className="mt-2 text-[#a9abad]">Tu dinero, tarjetas y objetivos en un mismo lugar.</p>
      </header>

      <div role="tablist" aria-label="Secciones de finanzas" className="mb-6 grid grid-cols-4 gap-1 rounded-2xl border border-white/[0.08] bg-[#0d0d0d] p-1.5">
        {tabs.map((item) => {
          const Icon = item.icon;
          return <button key={item.id} role="tab" aria-selected={tab === item.id} aria-label={item.label} onClick={() => setTab(item.id)} className={cn("focus-ring flex min-h-11 min-w-0 items-center justify-center gap-2 rounded-xl px-2 py-2.5 text-xs font-semibold transition sm:text-sm", tab === item.id ? "bg-white text-black" : "text-[#a9abad] hover:bg-white/[0.05] hover:text-white")}><Icon className="size-4 shrink-0" /><span className="hidden sm:inline">{item.label}</span></button>;
        })}
      </div>

      <div key={tab} role="tabpanel" className="animate-enter">
        {tab === "overview" && <FinanceOverview />}
        {tab === "transactions" && <TransactionsPanel />}
        {tab === "cards" && <CardsPanel />}
        {tab === "goals" && <GoalsPanel />}
      </div>
    </div>
  );
}
