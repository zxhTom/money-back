# Ordinary contract user password recovery

The existing mini-program recovery route accepts username + identity number + new password. The former name-only request is no longer accepted. Recovery changes both login and payment passwords, retaining existing product behavior and session revocation.

Only an enabled user whose complete role set consists of enabled contract roles is eligible. Administrators, additional roles, missing roles, disabled users, mismatched identities, and ambiguous/cross-tenant usernames are rejected with the same response. The target comes from the submitted login username, not nickname/real name or caller supplied user ID.

Settings in Infrastructure → Configuration, category security:

- security.password-recovery.ip-window-hours = 3
- security.password-recovery.ip-max-resets = 5

A trusted client IP shares one quota across accounts and tenants in an environment. The first validated reservation starts a fixed N-hour expiry; later reservations do not extend it. Invalid identity/role requests do not reserve quota. Atomic Redis Lua reservations prevent concurrent requests exceeding M. Failed transactions release their reservation. IP allowlisting does not bypass this quota. Existing short-term rate/abuse checks remain enabled.

New config keys are stored in the shared configuration database but are unused by the old production recovery implementation. Application deployment and mini-program experience upload target staging only; production is not switched. No real user's password is reset during validation.

Verification covers valid contract-only accounts, mixed/admin roles, wrong identity, disabled/foreign-tenant accounts, runtime settings, quota rejection, write rollback, and concurrent reservations using a temporary Redis test key.
