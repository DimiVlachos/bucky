import type { WaitlistStore } from "../waitlist.ts";

export class FailingWaitlistStore implements WaitlistStore {
  async add(): Promise<void> {
    throw new Error("store down");
  }

  async hit(): Promise<number> {
    throw new Error("store down");
  }
}
