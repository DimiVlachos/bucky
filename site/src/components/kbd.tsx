type KbdProps = {
  children: React.ReactNode;
  latched?: boolean;
  className?: string;
};

// Drawn like Bucky's own keys: key_background fill, 6dp corners, latched modifiers in key_modifier_active.
export function Kbd({ children, latched = false, className = "" }: KbdProps) {
  return (
    <kbd
      className={`inline-flex min-w-[1.9em] items-center justify-center rounded-md px-[0.45em] py-[0.15em] font-mono text-[0.82em] leading-none font-medium shadow-[0_1px_0_0_rgb(255_255_255/0.06)_inset,0_2px_5px_-1px_rgb(0_0_0/0.55)] ${
        latched ? "keycap-latch bg-latched text-label" : "bg-key text-label"
      } ${className}`}
    >
      {children}
    </kbd>
  );
}
