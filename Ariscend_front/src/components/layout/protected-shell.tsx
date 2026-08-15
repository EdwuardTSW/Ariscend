"use client";

import { useEffect } from "react";
import { usePathname, useRouter } from "next/navigation";
import { useSelectedUser } from "@/contexts/selected-user-context";
import { AppHeader } from "@/components/layout/app-header";
import { BottomNavigation } from "@/components/layout/bottom-navigation";
import { PageLoading } from "@/components/feedback/page-loading";

export function ProtectedShell({ children }: { children: React.ReactNode }) {
  const { selectedUser, loading } = useSelectedUser();
  const router = useRouter();
  const pathname = usePathname();

  useEffect(() => {
    if (!loading && !selectedUser) {
      router.replace(`/login?next=${encodeURIComponent(pathname)}`);
    }
  }, [loading, pathname, router, selectedUser]);

  if (loading || !selectedUser) return <PageLoading />;

  return (
    <div className="min-h-screen min-h-dvh">
      <a href="#main-content" className="focus-ring fixed left-4 top-3 z-[60] -translate-y-20 rounded-full bg-white px-4 py-2 text-sm font-semibold text-black transition focus:translate-y-0">Saltar al contenido</a>
      <AppHeader />
      <main id="main-content" className="mx-auto max-w-[1200px] px-4 pb-[calc(8rem+env(safe-area-inset-bottom))] pt-7 md:px-8 md:pt-9">{children}</main>
      <BottomNavigation />
    </div>
  );
}
