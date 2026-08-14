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
      router.replace(`/seleccionar-usuario?next=${encodeURIComponent(pathname)}`);
    }
  }, [loading, pathname, router, selectedUser]);

  if (loading || !selectedUser) return <PageLoading />;

  return (
    <div className="min-h-screen">
      <AppHeader />
      <main className="mx-auto max-w-[1200px] px-4 pb-32 pt-7 md:px-8 md:pt-9">{children}</main>
      <BottomNavigation />
    </div>
  );
}
