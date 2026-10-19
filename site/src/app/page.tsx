import Image from "next/image";
import Link from "next/link";
import { Kbd } from "@/components/kbd.tsx";
import { WaitlistForm } from "@/components/waitlist-form.tsx";
import { REPO_URL } from "@/lib/site.ts";

const features = [
  {
    title: "Real keys, not text",
    body: "Ctrl+C interrupts, Esc leaves insert mode, arrows walk your shell history. Termux, vim and SSH clients get the key events they expect.",
    evidence: (
      <span className="flex items-center gap-1.5 text-xl">
        <Kbd latched>Ctrl</Kbd>
        <Kbd>C</Kbd>
      </span>
    ),
  },
  {
    title: "The whole layout",
    body: "Esc, Tab, Ctrl, Alt, Home, End, Del, arrows, and every symbol where your fingers expect it — including ~ and |.",
    evidence: (
      <span className="flex items-center gap-1.5 text-xl">
        <Kbd>~</Kbd>
        <Kbd>|</Kbd>
        <Kbd>\</Kbd>
      </span>
    ),
  },
  {
    title: "No internet permission",
    body: "The keyboard has no permission to open network connections, so it cannot upload what you type. The source is open (Apache-2.0) so you can check.",
    evidence: (
      <code className="font-mono text-sm text-label/70 line-through decoration-muted decoration-[1.5px]">
        android.permission.INTERNET
      </code>
    ),
  },
  {
    title: "Free where it counts",
    body: "Every key, layout and modifier Hacker's Keyboard had stays free, forever. Optional extras later, such as a layout editor, pay for the work.",
    evidence: <code className="font-mono text-sm text-latched-bright">$0, forever</code>,
  },
];

export default function Home() {
  return (
    <div className="mx-auto flex max-w-5xl flex-col px-6">
      <header className="flex items-center justify-between py-6">
        <span className="flex items-center gap-2.5 font-semibold tracking-tight">
          <Kbd className="text-base">B</Kbd>
          Bucky Keys
        </span>
        <a href={REPO_URL} className="text-sm text-muted transition-colors hover:text-label">
          GitHub
        </a>
      </header>

      <main className="flex flex-col">
        <section className="grid items-center gap-14 pt-10 pb-20 lg:grid-cols-[minmax(0,1fr)_340px] lg:gap-20 lg:pt-16 lg:pb-28">
          <div className="flex max-w-xl flex-col gap-7">
            <h1 className="text-[clamp(2.6rem,6vw,4.25rem)] leading-[1.02] font-semibold tracking-[-0.035em] text-balance">
              A real PC keyboard for Android.
            </h1>
            <p className="text-lg leading-relaxed text-pretty text-muted">
              <Kbd latched>Ctrl</Kbd>, <Kbd>Esc</Kbd>, <Kbd>Tab</Kbd> and the arrows, as real keys
              that Termux, vim and SSH actually understand. A from-scratch successor to
              Hacker&apos;s Keyboard, in development now.
            </p>
            <div className="flex flex-col gap-3">
              <WaitlistForm />
              <p className="flex flex-wrap gap-x-2 font-mono text-xs text-muted">
                <span className="whitespace-nowrap">Open source · Apache-2.0</span>
                <span className="whitespace-nowrap">· No internet permission</span>
                <span className="whitespace-nowrap">
                  · <Link href="/privacy" className="underline hover:text-label">Privacy</Link>
                </span>
              </p>
            </div>
          </div>

          <figure className="flex flex-col items-center gap-4">
            <Image
              src="/hero-vim.png"
              alt="vim editing a backup script in Termux, with Bucky Keys below and its Ctrl key latched"
              width={1440}
              height={2778}
              priority
              sizes="(min-width: 1024px) 340px, 300px"
              className="w-[300px] rounded-[1.75rem] border border-key-pressed shadow-[0_40px_80px_-30px_rgb(0_0_0/0.8)] lg:w-[340px]"
            />
            <figcaption className="max-w-[300px] text-center text-sm text-muted">
              Real vim in Termux. Esc just left insert mode; Ctrl is latched for the next key.
            </figcaption>
          </figure>
        </section>

        <section aria-label="Features" className="border-t border-key-pressed">
          {features.map((feature) => (
            <div
              key={feature.title}
              className="grid gap-4 border-b border-key-pressed py-9 sm:grid-cols-[14rem_minmax(0,1fr)] sm:gap-10"
            >
              <div className="flex items-start sm:pt-0.5">{feature.evidence}</div>
              <div className="flex max-w-xl flex-col gap-2">
                <h2 className="text-xl font-semibold tracking-tight">{feature.title}</h2>
                <p className="leading-relaxed text-muted">{feature.body}</p>
              </div>
            </div>
          ))}
        </section>

        <section className="flex flex-col items-start gap-6 py-24 sm:items-center sm:text-center">
          <h2 className="text-3xl font-semibold tracking-[-0.03em] text-balance sm:text-4xl">
            Want it on your phone first?
          </h2>
          <div className="w-full max-w-lg">
            <WaitlistForm />
          </div>
        </section>
      </main>

      <footer className="flex flex-wrap gap-x-6 gap-y-2 border-t border-key-pressed py-8 text-sm text-muted">
        <a href={REPO_URL} className="transition-colors hover:text-label">Source on GitHub</a>
        <Link href="/privacy" className="transition-colors hover:text-label">Privacy</Link>
      </footer>
    </div>
  );
}
