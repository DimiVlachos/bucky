import Link from "next/link";
import { Kbd } from "@/components/kbd.tsx";

export default function NotFound() {
  return (
    <main className="mx-auto flex min-h-[60vh] max-w-2xl flex-col justify-center gap-6 px-6 py-16">
      <p className="flex items-center gap-1.5 text-xl">
        <Kbd latched>Ctrl</Kbd>
        <Kbd>C</Kbd>
      </p>
      <h1 className="text-3xl font-semibold tracking-tight">This page doesn&apos;t exist.</h1>
      <Link href="/" className="text-latched-bright">Back to Bucky Keys</Link>
    </main>
  );
}
