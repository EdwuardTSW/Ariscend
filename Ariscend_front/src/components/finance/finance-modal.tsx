"use client";

import * as Dialog from "@radix-ui/react-dialog";
import { X } from "lucide-react";

interface FinanceModalProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  title: string;
  description?: string;
  children: React.ReactNode;
}

export function FinanceModal({
  open,
  onOpenChange,
  title,
  description,
  children,
}: FinanceModalProps) {
  return (
    <Dialog.Root open={open} onOpenChange={onOpenChange}>
      <Dialog.Portal>
        <Dialog.Overlay className="fixed inset-0 z-[70] bg-black/80 sm:backdrop-blur-sm" />
        <Dialog.Content aria-describedby={description ? "finance-modal-description" : undefined} className="fixed bottom-0 left-0 z-[80] max-h-[92dvh] w-full overflow-y-auto rounded-t-3xl border border-white/10 bg-[#111111] p-5 pb-[calc(1.25rem+env(safe-area-inset-bottom))] outline-none sm:bottom-auto sm:left-1/2 sm:top-1/2 sm:max-w-xl sm:-translate-x-1/2 sm:-translate-y-1/2 sm:rounded-3xl sm:p-7">
          <div className="mb-6 flex items-start justify-between gap-4">
            <div>
              <Dialog.Title className="text-2xl font-semibold tracking-[-0.025em]">
                {title}
              </Dialog.Title>
              {description && (
                <Dialog.Description id="finance-modal-description" className="mt-2 text-sm leading-6 text-[#a9abad]">
                  {description}
                </Dialog.Description>
              )}
            </div>
            <Dialog.Close aria-label="Cerrar" className="focus-ring flex size-11 items-center justify-center rounded-full text-[#a9abad] transition hover:bg-white/[0.06] hover:text-white">
              <X className="size-5" />
            </Dialog.Close>
          </div>
          {children}
        </Dialog.Content>
      </Dialog.Portal>
    </Dialog.Root>
  );
}
