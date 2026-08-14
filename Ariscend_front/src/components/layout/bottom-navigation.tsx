"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { CircleCheckBig, FileText, House, ListChecks, WalletCards } from "lucide-react";
import { cn } from "@/lib/utils";

const items = [
  { href: "/", label: "Inicio", icon: House },
  { href: "/habitos", label: "Hábitos", icon: CircleCheckBig },
  { href: "/pendientes", label: "Pendientes", icon: ListChecks },
  { href: "/finanzas", label: "Finanzas", icon: WalletCards },
  { href: "/notas", label: "Notas", icon: FileText },
];

export function BottomNavigation() {
  const pathname = usePathname();

  return (
    <nav aria-label="Navegación principal" className="glass-panel fixed bottom-[calc(1rem+env(safe-area-inset-bottom))] left-1/2 z-50 flex w-[calc(100%-32px)] max-w-[560px] -translate-x-1/2 items-center justify-around rounded-full px-3 py-2.5 md:bottom-6 md:w-auto md:gap-7 md:px-9">
      {items.map((item) => {
        const active = item.href === "/" ? pathname === "/" : pathname.startsWith(item.href);
        const Icon = item.icon;
        return (
          <Link
            key={item.href}
            href={item.href}
            aria-current={active ? "page" : undefined}
            prefetch={item.href === "/finanzas" ? false : undefined}
            className={cn(
              "focus-ring relative flex min-w-12 flex-col items-center gap-1 rounded-lg px-1.5 py-0.5 text-[10px] font-medium transition md:min-w-16 md:text-[11px]",
              active ? "text-white" : "text-[#a9abad] hover:text-white",
            )}
          >
            <Icon className="size-5 md:size-6" strokeWidth={active ? 2.4 : 1.8} />
            <span>{item.label}</span>
            {active && <span className="absolute -bottom-1.5 size-1 rounded-full bg-white" />}
          </Link>
        );
      })}
    </nav>
  );
}
