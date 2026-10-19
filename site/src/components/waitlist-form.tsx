// Client component: it tracks submit state and posts with fetch.
"use client";

import { useId, useRef, useState } from "react";

type Status = "idle" | "sending" | "joined" | "invalid" | "limited" | "failed";

const messages: Record<Status, string> = {
  idle: "",
  sending: "",
  joined: "You're on the list. One email when the beta opens, nothing else.",
  invalid: "That doesn't look like an email address.",
  limited: "Too many tries from this network. Try again in an hour.",
  failed: "Something went wrong. Try again in a minute.",
};

export function WaitlistForm() {
  const [status, setStatus] = useState<Status>("idle");
  const id = useId();
  const statusRef = useRef<HTMLParagraphElement>(null);
  const emailId = `${id}-email`;
  const statusId = `${id}-status`;
  const joined = status === "joined";

  async function submit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    setStatus("sending");
    const response = await fetch("/api/waitlist", {
      method: "POST",
      headers: { "content-type": "application/json" },
      body: JSON.stringify({ email: form.get("email"), website: form.get("website") }),
    }).catch(() => null);
    const next: Status = response?.ok
      ? "joined"
      : response?.status === 400
        ? "invalid"
        : response?.status === 429
          ? "limited"
          : "failed";
    setStatus(next);
    if (next === "joined") requestAnimationFrame(() => statusRef.current?.focus());
  }

  return (
    <div className="flex flex-col gap-3">
      {!joined && (
        <form onSubmit={submit} className="flex flex-col gap-2 sm:flex-row">
          <label htmlFor={emailId} className="sr-only">Email</label>
          <input
            id={emailId}
            name="email"
            type="email"
            required
            autoComplete="email"
            placeholder="you@example.com"
            aria-invalid={status === "invalid"}
            aria-describedby={statusId}
            className="min-w-0 flex-1 rounded-md border border-key-edge bg-key px-4 py-3 text-label placeholder:text-muted"
          />
          <input name="website" tabIndex={-1} autoComplete="off" aria-hidden="true" className="hidden" />
          <button
            type="submit"
            disabled={status === "sending"}
            className="rounded-md bg-latched px-5 py-3 font-medium text-label shadow-[0_1px_0_0_rgb(255_255_255/0.08)_inset,0_6px_16px_-6px_rgb(0_0_0/0.6)] transition-[background-color,transform] duration-150 ease-out hover:bg-latched-hover active:translate-y-px disabled:opacity-60"
          >
            {status === "sending" ? "Joining…" : "Join the waitlist"}
          </button>
        </form>
      )}
      <p
        id={statusId}
        ref={statusRef}
        tabIndex={-1}
        role="status"
        aria-live="polite"
        className={joined ? "text-latched-bright outline-none" : "text-sm text-muted empty:hidden"}
      >
        {messages[status]}
      </p>
    </div>
  );
}
