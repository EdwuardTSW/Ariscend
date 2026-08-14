import type { HTMLAttributes } from "react";
import { cn } from "@/lib/utils";

export function ObsidianCard({ className, ...props }: HTMLAttributes<HTMLDivElement>) {
  return <div className={cn("obsidian-card rounded-2xl", className)} {...props} />;
}
