export function PageLoading({ label = "Preparando tu espacio" }: { label?: string }) {
  return (
    <div className="flex min-h-[55vh] flex-col items-center justify-center gap-4 text-[#a9abad]">
      <div className="size-9 animate-spin rounded-full border-2 border-white/15 border-t-white" />
      <p className="text-sm">{label}</p>
    </div>
  );
}
