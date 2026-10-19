// Client component: it tracks submit state and posts with fetch.
"use client";

import { useId, useState } from "react";

type Status = "idle" | "sending" | "joined" | "invalid" | "limited" | "failed";

const messages: Record<Exclude<Status, "idle" | "sending">, string> = {
  joined: "You're on the list. One email when the beta opens, nothing else.",
  invalid: "That doesn't look like an email address.",
  limited: "Too many tries from this network. Try again in an hour.",
  failed: "Something went wrong. Try again in a minute.",
};

export function WaitlistForm() {
  const [status, setStatus] = useState<Status>("idle");
  const emailId = useId();

  async function submit(form: FormData) {
    setStatus("sending");
    const response = await fetch("/api/waitlist", {
      method: "POST",
      headers: { "content-type": "application/json" },
      body: JSON.stringify({ email: form.get("email"), website: form.get("website") }),
    }).catch(() => null);
    if (response?.ok) setStatus("joined");
    else if (response?.status === 400) setStatus("invalid");
    else if (response?.status === 429) setStatus("limited");
    else setStatus("failed");
  }

  if (status === "joined") {
    return <p className="text-latched-bright" role="status">{messages.joined}</p>;
  }

  return (
    <form action={submit} className="flex flex-col gap-3">
      <div className="flex flex-col gap-2 sm:flex-row">
        <label htmlFor={emailId} className="sr-only">Email</label>
        <input
          id={emailId}
          name="email"
          type="email"
          required
          autoComplete="email"
          placeholder="you@example.com"
          className="min-w-0 flex-1 rounded-md border border-key-pressed bg-key px-4 py-3 text-label placeholder:text-muted"
        />
        <input name="website" tabIndex={-1} autoComplete="off" aria-hidden="true" className="hidden" />
        <button
          type="submit"
          disabled={status === "sending"}
          className="rounded-md bg-latched px-5 py-3 font-medium text-label shadow-[0_1px_0_0_rgb(255_255_255/0.08)_inset,0_6px_16px_-6px_rgb(0_0_0/0.6)] transition-[background-color,transform] duration-150 ease-out hover:bg-latched-hover active:translate-y-px disabled:opacity-60"
        >
          {status === "sending" ? "Joining…" : "Join the waitlist"}
        </button>
      </div>
      {status !== "idle" && status !== "sending" && (
        <p className="text-sm text-muted" role="status">{messages[status]}</p>
      )}
    </form>
  );
}
