import type { Metadata } from "next";
import { Geist, Hanken_Grotesk } from "next/font/google";
import { Toaster } from "sonner";
import { SelectedUserProvider } from "@/contexts/selected-user-context";
import "./globals.css";

const hanken = Hanken_Grotesk({
  variable: "--font-hanken",
  subsets: ["latin"],
});

const geist = Geist({
  variable: "--font-geist",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: "Ariscend",
  description: "Disciplina, organización y progreso personal.",
};

export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  return (
    <html lang="es" className={`${hanken.variable} ${geist.variable}`}>
      <body className="font-[var(--font-hanken)]">
        <SelectedUserProvider>{children}</SelectedUserProvider>
        <Toaster theme="dark" position="top-center" richColors />
      </body>
    </html>
  );
}
