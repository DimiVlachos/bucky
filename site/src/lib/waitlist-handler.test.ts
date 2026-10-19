import assert from "node:assert/strict";
import { test } from "node:test";
import { FailingWaitlistStore } from "./fake/failing-waitlist-store.ts";
import { FakeWaitlistStore } from "./fake/fake-waitlist-store.ts";
import { handleWaitlistRequest } from "./waitlist-handler.ts";

const NOW = 1_760_000_000_000;

function post(body: unknown, headers: Record<string, string> = {}): Request {
  return new Request("https://buckykeys.app/api/waitlist", {
    method: "POST",
    headers: {
      "content-type": "application/json",
      origin: "https://buckykeys.app",
      host: "buckykeys.app",
      "x-forwarded-for": "1.1.1.1",
      ...headers,
    },
    body: typeof body === "string" ? body : JSON.stringify(body),
  });
}

test("a same-origin JSON signup is accepted", async () => {
  const store = new FakeWaitlistStore();
  const response = await handleWaitlistRequest(post({ email: "dev@example.com" }), store, NOW);
  assert.equal(response.status, 200);
  assert.equal(store.emails.has("dev@example.com"), true);
});

test("a request from another site is refused and nothing is stored", async () => {
  const store = new FakeWaitlistStore();
  const response = await handleWaitlistRequest(
    post({ email: "dev@example.com" }, { origin: "https://evil.example" }),
    store,
    NOW,
  );
  assert.equal(response.status, 403);
  assert.equal(store.emails.size, 0);
});

test("a request without an Origin header is accepted", async () => {
  const store = new FakeWaitlistStore();
  const request = post({ email: "dev@example.com" });
  request.headers.delete("origin");
  const response = await handleWaitlistRequest(request, store, NOW);
  assert.equal(response.status, 200);
});

test("a non-JSON body is refused with 415", async () => {
  const store = new FakeWaitlistStore();
  const response = await handleWaitlistRequest(
    post("email=dev@example.com", { "content-type": "text/plain" }),
    store,
    NOW,
  );
  assert.equal(response.status, 415);
  assert.equal(store.emails.size, 0);
});

test("a JSON content type with a charset is accepted", async () => {
  const store = new FakeWaitlistStore();
  const response = await handleWaitlistRequest(
    post({ email: "dev@example.com" }, { "content-type": "application/json; charset=utf-8" }),
    store,
    NOW,
  );
  assert.equal(response.status, 200);
});

test("a missing store answers 503", async () => {
  const response = await handleWaitlistRequest(post({ email: "dev@example.com" }), null, NOW);
  assert.equal(response.status, 503);
});

test("a failing store answers 503 instead of crashing", async () => {
  const response = await handleWaitlistRequest(
    post({ email: "dev@example.com" }),
    new FailingWaitlistStore(),
    NOW,
  );
  assert.equal(response.status, 503);
  assert.deepEqual(await response.json(), { error: "unavailable" });
});

test("malformed JSON is an invalid email, not a crash", async () => {
  const response = await handleWaitlistRequest(post("{nope"), new FakeWaitlistStore(), NOW);
  assert.equal(response.status, 400);
});
