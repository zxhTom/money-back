# Contract privacy hardening

This change is validated in staging only. Production and staging share MySQL/ClickHouse: no role assignment deletion, historical log mutation, or bulk account/session changes are performed as part of staging validation.

- User directory: allow only super_admin/tenant_admin/contract-manager, scope results to the authenticated tenant, return encrypted identity values instead of plaintext.
- Archive read/run operations: require both the existing permission and an administrator role, enforce the default platform tenant, cap page size. Historical nested JSON is redacted when returned.
- Administrator contract search/detail: require both the existing permission and an administrator/contract-manager role; SQL always explicitly binds the current tenant, including when the legacy query bypasses the ORM tenant interceptor. Deleted-contract review remains supported.
- Contract Excel exports: use the current user's authorized own-list scope, so supplying someone else's identity filter cannot expand ordinary-user exports. Existing administrator/contract-manager own-list privileges remain.
- API access/error logs: redact sensitive field variants recursively, including encoded JSON within JSON. Redact reads/exports of historical records as well as new writes. Invalid JSON is omitted rather than logged unredacted.
- Contract uploads: force the private contract directory. Authorized upload/detail/list responses issue 10-minute HMAC download links, backed by a random Redis-held signing key. The download interceptor rejects missing/expired/modified signatures; public brand/image files retain public access. No existing active contracts had attachment fields during the audit.

The name-plus-identity password recovery endpoint is deliberately unchanged at the user's instruction. This remains an account recovery risk even after user-directory disclosure is closed.

Historical source records and backups still require a separately scheduled production redaction operation. Read-time redaction prevents exposure through the patched application but does not rewrite the shared physical archive. Previously exposed credentials may remain compromised; do not assume closing the read path invalidates them.

Validation: compile affected modules, security regression tests (ordinary-user permission boundaries, valid administrator access, tenant SQL binding, recursive redaction, malformed JSON, signature tampering/expiry/public assets), then live staging checks. Release packaging preserves every other deployed module and the staging frontend. Never switch production groups during validation.
