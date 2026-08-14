export function ProgressRing({ value }: { value: number }) {
  const radius = 42;
  const circumference = 2 * Math.PI * radius;
  const offset = circumference - (Math.min(100, Math.max(0, value)) / 100) * circumference;

  return (
    <div className="relative size-40 shrink-0 md:size-60">
      <svg className="size-full -rotate-90" viewBox="0 0 100 100" aria-label={`${value}% completado`}>
        <circle cx="50" cy="50" r={radius} fill="none" stroke="#29292b" strokeWidth="7" />
        <circle
          cx="50"
          cy="50"
          r={radius}
          fill="none"
          stroke="white"
          strokeWidth="7"
          strokeLinecap="round"
          strokeDasharray={circumference}
          strokeDashoffset={offset}
          className="transition-[stroke-dashoffset] duration-700"
        />
      </svg>
      <div className="absolute inset-0 flex flex-col items-center justify-center">
        <span className="text-4xl font-bold tracking-[-0.05em] md:text-6xl">{value}%</span>
        <span className="mt-1 font-[var(--font-geist)] text-[10px] font-semibold uppercase tracking-[0.16em] text-[#a9abad] md:text-xs">
          completado
        </span>
      </div>
    </div>
  );
}
