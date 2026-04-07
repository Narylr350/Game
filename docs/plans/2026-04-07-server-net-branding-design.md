# server.net Branding Design

**Goal:** Keep the `server.net` package path unchanged while branding the package's source comments and user-facing strings with `ICERainbow666`.

**Scope:**
- Update source comments in `doudizhu/src/main/java/server/net`.
- Update package-local runtime strings that are emitted from `server.net`.
- Do not rename packages, classes, methods, or move files.
- Do not touch docs outside the package or compiled output under `target/`.

**Design:**
- Add a single brand constant in `Server` so branded prompts stay consistent.
- Prefix package-emitted prompts with the GitHub name where the package already talks to players or logs service identity.
- Mark each class in `server.net` as maintained by `ICERainbow666` through class-level documentation.
- Add a narrow regression test around `Server.getMessage(...)` so at least one externally visible prompt is covered by red/green verification.

**Verification:**
- Run the new focused test first to prove the branding does not already exist.
- Run the same focused test again after implementation.
- Run the full Maven test suite before committing.
