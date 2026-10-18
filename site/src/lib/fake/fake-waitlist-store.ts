import type { WaitlistStore } from "../waitlist.ts";

export class FakeWaitlistStore implements WaitlistStore {
  readonly emails = new Map<string, number>();
  private readonly hits = new Map<string, number>();

  async add(email: string, joinedAtMs: number): Promise<void> {
    if (!this.emails.has(email)) this.emails.set(email, joinedAtMs);
  }

  async hit(key: string): Promise<number> {
    const count = (this.hits.get(key) ?? 0) + 1;
    this.hits.set(key, count);
    return count;
  }
}
