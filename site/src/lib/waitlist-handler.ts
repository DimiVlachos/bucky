import { joinWaitlist, type WaitlistStore } from "./waitlist.ts";

export async function handleWaitlistRequest(
  request: Request,
  store: WaitlistStore | null,
  nowMs: number,
): Promise<Response> {
  // Browsers always send Origin on a cross-site POST; non-browser clients cannot forge a visitor's.
  const origin = request.headers.get("origin");
  if (origin !== null && origin !== new URL(request.url).origin) {
    return Response.json({ error: "forbidden" }, { status: 403 });
  }

  const contentType = request.headers.get("content-type") ?? "";
  if (!contentType.toLowerCase().startsWith("application/json")) {
    return Response.json({ error: "unsupported_media_type" }, { status: 415 });
  }

  if (!store) return Response.json({ error: "unavailable" }, { status: 503 });

  const body: unknown = await request.json().catch(() => null);
  const fields = typeof body === "object" && body !== null ? (body as Record<string, unknown>) : {};
  const ip = request.headers.get("x-forwarded-for")?.split(",")[0]?.trim() || "unknown";

  try {
    const result = await joinWaitlist(store, { email: fields.email, website: fields.website, ip }, nowMs);
    if (result.ok) return Response.json({ ok: true });
    return Response.json(
      { error: result.error },
      { status: result.error === "rate_limited" ? 429 : 400 },
    );
  } catch {
    return Response.json({ error: "unavailable" }, { status: 503 });
  }
}
