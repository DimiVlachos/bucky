import type { Metadata } from "next";
import Link from "next/link";
import { CONTACT_EMAIL } from "@/lib/site.ts";

export const metadata: Metadata = { title: "Privacy — Bucky Keys" };

export default function Privacy() {
  return (
    <main className="mx-auto flex max-w-2xl flex-col gap-6 px-6 py-16 leading-relaxed">
      <Link href="/" className="font-mono text-sm text-muted hover:text-label">← Bucky Keys</Link>
      <h1 className="text-3xl font-semibold tracking-tight">Privacy</h1>
      <p>
        The waitlist stores your email address and the time you joined. That is all. It is used to
        send you one email when the beta opens and one when the app launches, nothing else, and it
        is never shared or sold.
      </p>
      <p>
        To stop abuse, the waitlist counts sign-up attempts per network address for one hour. The
        count is then deleted.
      </p>
      <p>
        The site uses Vercel Web Analytics, which counts page views without cookies and without
        identifying you.
      </p>
      <p>
        The list is deleted six months after launch at the latest. To be removed sooner, or to ask
        what is stored about you, email{" "}
        <a href={`mailto:${CONTACT_EMAIL}`} className="text-latched-bright">{CONTACT_EMAIL}</a>.
      </p>
      <p className="text-muted">
        The keyboard app itself has no internet permission and collects nothing.
      </p>
    </main>
  );
}
