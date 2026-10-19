import { handleWaitlistRequest } from "@/lib/waitlist-handler.ts";
import { RedisWaitlistStore, redisFromEnv } from "@/lib/redis-waitlist-store.ts";

export async function POST(request: Request) {
  const redis = redisFromEnv();
  return handleWaitlistRequest(request, redis ? new RedisWaitlistStore(redis) : null, Date.now());
}
