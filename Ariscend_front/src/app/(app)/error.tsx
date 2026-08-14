"use client";

import { RefreshCw } from "lucide-react";
import { Button } from "@/components/ui/button";
import { ObsidianCard } from "@/components/ui/obsidian-card";

export default function AppError({ reset }: { error: Error & { digest?: string }; reset: () => void }) {
  return (
    <ObsidianCard className="flex min-h-64 flex-col items-center justify-center gap-5 p-8 text-center">
      <div>
        <h1 className="text-2xl font-semibold">No pudimos mostrar esta sección</h1>
        <p className="mt-2 text-[#a9abad]">Tus datos no se modificaron. Intenta cargarla nuevamente.</p>
      </div>
      <Button variant="secondary" onClick={reset}>
        <RefreshCw className="size-4" /> Reintentar
      </Button>
    </ObsidianCard>
  );
}
