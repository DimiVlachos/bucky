import Image from "next/image";
import Link from "next/link";
import { WaitlistForm } from "@/components/waitlist-form.tsx";
import { REPO_URL } from "@/lib/site.ts";

const features = [
  {
    title: "Real keys, not text",
    body: "Ctrl+C interrupts, Esc leaves insert mode, arrows walk your shell history. Termux, vim and SSH clients get the key events they expect.",
  },
  {
    title: "The whole layout",
    body: "Esc, Tab, Ctrl, Alt, Home, End, Del, arrows, and every symbol where your fingers expect it — including ~ and |.",
  },
  {
    title: "Nothing leaves your phone",
    body: "No internet permission. The keyboard cannot send what you type anywhere, and the source is open (Apache-2.0) so you can check.",
  },
  {
    title: "Free where it counts",
    body: "Everything Hacker's Keyboard did stays free, forever. Optional extras later pay for the work.",
  },
];

export default function Home() {
  return (
    <main className="mx-auto flex max-w-2xl flex-col gap-14 px-6 py-16">
      <header className="flex flex-col gap-5">
        <p className="font-mono text-sm text-muted">Bucky Keys · Android</p>
        <h1 className="text-4xl font-semibold tracking-tight sm:text-5xl">
          A real PC keyboard for Android.
        </h1>
        <p className="text-lg text-muted">
          Ctrl, Esc, Tab, arrows and F-keys that Termux, vim and SSH actually understand. The
          maintained successor Hacker&apos;s Keyboard users have been asking for.
        </p>
        <WaitlistForm />
      </header>

      <figure className="flex flex-col gap-3">
        <Image
          src="/hero-vim.png"
          alt="vim editing a backup script in Termux, with Bucky Keys below and its Ctrl key latched"
          width={1440}
          height={2778}
          priority
          className="mx-auto w-full max-w-sm rounded-2xl border border-key-pressed"
        />
        <figcaption className="text-center text-sm text-muted">
          Real vim in Termux. Esc just left insert mode; Ctrl is latched for the next key.
        </figcaption>
      </figure>

      <section className="grid gap-8 sm:grid-cols-2">
        {features.map((feature) => (
          <div key={feature.title} className="flex flex-col gap-2">
            <h2 className="font-medium">{feature.title}</h2>
            <p className="text-sm leading-relaxed text-muted">{feature.body}</p>
          </div>
        ))}
      </section>

      <footer className="flex flex-wrap gap-x-6 gap-y-2 border-t border-key-pressed pt-6 text-sm text-muted">
        <a href={REPO_URL} className="hover:text-label">Source on GitHub</a>
        <Link href="/privacy" className="hover:text-label">Privacy</Link>
      </footer>
    </main>
  );
}
