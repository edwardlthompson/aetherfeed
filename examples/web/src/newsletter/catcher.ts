export const NEWSLETTER_FALLBACK =
  "No local mail catcher is configured. Skip newsletter-as-RSS until a FOSS inbound mailbox exists.";

export function canCatchMail(env: { mailboxPath?: string } = {}): boolean {
  return Boolean(env.mailboxPath?.trim());
}

export function catchNewsletters(env: { mailboxPath?: string } = {}): string[] {
  if (!canCatchMail(env)) return [];
  return [];
}

export function skipReason(env: { mailboxPath?: string } = {}): string | null {
  return canCatchMail(env) ? null : NEWSLETTER_FALLBACK;
}
