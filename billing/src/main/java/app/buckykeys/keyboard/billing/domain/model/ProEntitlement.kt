package app.buckykeys.keyboard.billing.domain.model

public sealed interface ProEntitlement {
    public data object Unknown : ProEntitlement

    public data object Free : ProEntitlement

    public data object Unlocked : ProEntitlement
}
