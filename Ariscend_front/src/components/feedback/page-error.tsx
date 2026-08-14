import { RefreshCw } from "lucide-react";
import { Button } from "@/components/ui/button";
import { ObsidianCard } from "@/components/ui/obsidian-card";

export function PageError({ message, onRetry }: { message: string; onRetry: () => void }) {
  return (
    <ObsidianCard role="alert" className="flex min-h-52 flex-col items-center justify-center gap-4 p-7 text-center">
      <p className="max-w-md text-[#c4c7c8]">{message}</p>
      <Button variant="secondary" onClick={onRetry}>
        <RefreshCw className="size-4" /> Reintentar
      </Button>
    </ObsidianCard>
  );
}
