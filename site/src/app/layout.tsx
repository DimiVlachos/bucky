import type { Metadata, Viewport } from "next";
import { GeistSans } from "geist/font/sans";
import { GeistMono } from "geist/font/mono";
import { Analytics } from "@vercel/analytics/next";
import { SITE_URL } from "@/lib/site.ts";
import "./globals.css";

const title = "Bucky Keys — a real PC keyboard for Android";
const description =
  "Ctrl, Esc, Tab and arrow keys that Termux, vim and SSH actually understand. In development: open source, no internet permission.";

export const metadata: Metadata = {
  metadataBase: new URL(SITE_URL),
  title,
  description,
  alternates: { canonical: "/" },
  openGraph: { type: "website", siteName: "Bucky Keys", title, description, locale: "en_US" },
  twitter: { card: "summary_large_image", title, description },
};

export const viewport: Viewport = { themeColor: "#1e1f22", colorScheme: "dark" };

export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  return (
    <html lang="en" className={`${GeistSans.variable} ${GeistMono.variable}`}>
      <body className="bg-board text-label font-sans antialiased">
        {children}
        <Analytics />
      </body>
    </html>
  );
}
