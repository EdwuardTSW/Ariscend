import { redirect } from "next/navigation";

function safeNextPath(value: string | string[] | undefined) {
  const path = Array.isArray(value) ? value[0] : value;
  return path?.startsWith("/") && !path.startsWith("//") && !path.includes("\\") ? path : "/";
}

export default async function SelectUserPage({
  searchParams,
}: {
  searchParams: Promise<{ next?: string | string[] }>;
}) {
  const next = safeNextPath((await searchParams).next);
  redirect(`/login?next=${encodeURIComponent(next)}`);
}
