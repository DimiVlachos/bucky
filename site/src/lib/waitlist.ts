export interface WaitlistStore {
  add(email: string, joinedAtMs: number): Promise<void>;
  hit(key: string, windowSeconds: number): Promise<number>;
}

export type JoinResult = { ok: true } | { ok: false; error: "invalid_email" | "rate_limited" };

const RATE_LIMIT = 5;
const RATE_WINDOW_SECONDS = 60 * 60;
const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export async function joinWaitlist(
  store: WaitlistStore,
  input: { email: unknown; website: unknown; ip: string },
  nowMs: number,
): Promise<JoinResult> {
  // Bots fill the hidden field; telling them they failed only invites retries.
  if (typeof input.website === "string" && input.website !== "") return { ok: true };

  const email = typeof input.email === "string" ? input.email.trim().toLowerCase() : "";
  if (email.length > 254 || !EMAIL_PATTERN.test(email)) return { ok: false, error: "invalid_email" };

  if ((await store.hit(`ip:${input.ip}`, RATE_WINDOW_SECONDS)) > RATE_LIMIT) {
    return { ok: false, error: "rate_limited" };
  }

  await store.add(email, nowMs);
  return { ok: true };
}
