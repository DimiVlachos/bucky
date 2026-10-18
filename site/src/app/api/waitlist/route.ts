import { joinWaitlist } from "@/lib/waitlist.ts";
import { RedisWaitlistStore, redisFromEnv } from "@/lib/redis-waitlist-store.ts";

export async function POST(request: Request) {
  const redis = redisFromEnv();
  if (!redis) return Response.json({ error: "unavailable" }, { status: 503 });

  const body: unknown = await request.json().catch(() => null);
  const fields = typeof body === "object" && body !== null ? (body as Record<string, unknown>) : {};
  const ip = request.headers.get("x-forwarded-for")?.split(",")[0]?.trim() || "unknown";

  const result = await joinWaitlist(
    new RedisWaitlistStore(redis),
    { email: fields.email, website: fields.website, ip },
    Date.now(),
  );
  if (result.ok) return Response.json({ ok: true });
  return Response.json(
    { error: result.error },
    { status: result.error === "rate_limited" ? 429 : 400 },
  );
}
