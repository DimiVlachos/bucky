import assert from "node:assert/strict";
import { test } from "node:test";
import { FakeWaitlistStore } from "./fake/fake-waitlist-store.ts";
import { joinWaitlist } from "./waitlist.ts";

const NOW = 1_760_000_000_000;

test("a valid email is stored trimmed and lowercased", async () => {
  const store = new FakeWaitlistStore();
  const result = await joinWaitlist(store, { email: "  Dev@Example.COM ", website: "", ip: "1.1.1.1" }, NOW);
  assert.deepEqual(result, { ok: true });
  assert.deepEqual([...store.emails], [["dev@example.com", NOW]]);
});

test("an invalid email is rejected and nothing is stored", async () => {
  const store = new FakeWaitlistStore();
  const result = await joinWaitlist(store, { email: "not-an-email", website: "", ip: "1.1.1.1" }, NOW);
  assert.deepEqual(result, { ok: false, error: "invalid_email" });
  assert.equal(store.emails.size, 0);
});

test("a missing or non-string email is rejected", async () => {
  const store = new FakeWaitlistStore();
  const result = await joinWaitlist(store, { email: 42, website: "", ip: "1.1.1.1" }, NOW);
  assert.deepEqual(result, { ok: false, error: "invalid_email" });
});

test("a filled honeypot looks successful but stores nothing", async () => {
  const store = new FakeWaitlistStore();
  const result = await joinWaitlist(store, { email: "bot@example.com", website: "spam", ip: "1.1.1.1" }, NOW);
  assert.deepEqual(result, { ok: true });
  assert.equal(store.emails.size, 0);
});

test("joining twice keeps the first join time", async () => {
  const store = new FakeWaitlistStore();
  await joinWaitlist(store, { email: "dev@example.com", website: "", ip: "1.1.1.1" }, NOW);
  await joinWaitlist(store, { email: "dev@example.com", website: "", ip: "2.2.2.2" }, NOW + 5000);
  assert.equal(store.emails.get("dev@example.com"), NOW);
});

test("the sixth attempt from one address within the window is rate limited", async () => {
  const store = new FakeWaitlistStore();
  for (let i = 0; i < 5; i++) {
    await joinWaitlist(store, { email: `dev${i}@example.com`, website: "", ip: "1.1.1.1" }, NOW);
  }
  const sixth = await joinWaitlist(store, { email: "dev6@example.com", website: "", ip: "1.1.1.1" }, NOW);
  assert.deepEqual(sixth, { ok: false, error: "rate_limited" });
  assert.equal(store.emails.has("dev6@example.com"), false);
});
