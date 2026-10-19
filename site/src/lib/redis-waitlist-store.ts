import { Redis } from "@upstash/redis";
import type { WaitlistStore } from "./waitlist.ts";

export class RedisWaitlistStore implements WaitlistStore {
  constructor(private readonly redis: Redis) {}

  async add(email: string, joinedAtMs: number): Promise<void> {
    await this.redis.zadd("waitlist", { nx: true }, { score: joinedAtMs, member: email });
  }

  async hit(key: string, windowSeconds: number): Promise<number> {
    // One transaction, and NX sets the TTL only once, so a key can never be left without expiry.
    const [count] = await this.redis
      .multi()
      .incr(`ratelimit:${key}`)
      .expire(`ratelimit:${key}`, windowSeconds, "NX")
      .exec<[number, number]>();
    return count;
  }
}

// The Vercel Marketplace integration names these KV_*; a direct Upstash setup uses UPSTASH_*.
export function redisFromEnv(): Redis | null {
  const url = process.env.KV_REST_API_URL ?? process.env.UPSTASH_REDIS_REST_URL;
  const token = process.env.KV_REST_API_TOKEN ?? process.env.UPSTASH_REDIS_REST_TOKEN;
  return url && token ? new Redis({ url, token }) : null;
}
