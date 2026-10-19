import type { Metadata } from "next";
import Link from "next/link";
import { CONTACT_EMAIL } from "@/lib/site.ts";

export const metadata: Metadata = {
  title: "Privacy — Bucky Keys",
  description: "What the Bucky Keys waitlist stores, why, where, and how to have it deleted.",
  alternates: { canonical: "/privacy" },
  openGraph: { title: "Privacy — Bucky Keys", url: "/privacy" },
};

export default function Privacy() {
  const mail = (
    <a href={`mailto:${CONTACT_EMAIL}`} className="text-latched-bright">
      {CONTACT_EMAIL}
    </a>
  );
  return (
    <main className="mx-auto flex max-w-2xl flex-col gap-6 px-6 py-16 leading-relaxed">
      <Link href="/" className="font-mono text-sm text-muted hover:text-label">← Bucky Keys</Link>
      <h1 className="text-3xl font-semibold tracking-tight">Privacy</h1>
      <p>
        Bucky Keys is run by Dimitris Vlachos, an individual developer. Contact: {mail}.
      </p>
      <h2 className="text-xl font-semibold">The waitlist</h2>
      <p>
        When you join, the waitlist stores your email address and the time you joined. That is all.
        It is used to send you one email when the beta opens, nothing else, and it is never shared
        or sold. The legal basis is your consent, which you can withdraw at any time by emailing{" "}
        {mail}.
      </p>
      <p>
        To stop abuse, sign-up attempts are counted per network address for one hour. The count is
        then deleted automatically.
      </p>
      <h2 className="text-xl font-semibold">Where the data lives</h2>
      <p>
        The site is hosted by Vercel Inc. and the waitlist is stored with Upstash Inc. Both process
        the data on my behalf. The sign-up form is handled in Vercel&apos;s Frankfurt region and the
        list is stored in Upstash&apos;s Frankfurt region, in Germany. Both companies are based in
        the United States; transfers are covered by the EU Standard Contractual Clauses in their
        data processing agreements. Vercel also keeps standard request logs, including IP
        addresses, for a short time under its own policy.
      </p>
      <h2 className="text-xl font-semibold">Analytics</h2>
      <p>
        The site uses Vercel Web Analytics, which counts page views without cookies and without
        identifying you.
      </p>
      <h2 className="text-xl font-semibold">How long, and your rights</h2>
      <p>
        The list is deleted six months after launch at the latest. You can ask to see, correct or
        delete what is stored about you at any time by emailing {mail}. You can also complain to
        your data protection authority; in Greece that is the Hellenic Data Protection Authority.
      </p>
      <p className="text-muted">
        The keyboard app itself has no internet permission and collects nothing.
      </p>
    </main>
  );
}
