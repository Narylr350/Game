# server.net Branding Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Brand the `server.net` package comments and user-facing strings with `ICERainbow666` without changing package paths.

**Architecture:** Keep the existing package structure intact and make only text-level changes in the package. Centralize the runtime brand prefix in `Server` so prompt text stays consistent while preserving the user's in-progress logic edits.

**Tech Stack:** Java 21, Maven, JUnit 5

---

### Task 1: Add the failing prompt-branding test

**Files:**
- Create: `doudizhu/src/test/java/server/net/ServerMessageBrandingTest.java`
- Test: `doudizhu/src/main/java/server/net/Server.java`

**Step 1: Write the failing test**

```java
@Test
void getMessageIncludesGithubBrandPrefix() {
    assertTrue(Server.getMessage(MessageType.CALL_LANDLORD).contains("ICERainbow666"));
}
```

**Step 2: Run test to verify it fails**

Run: `mvn -Dtest=server.net.ServerMessageBrandingTest test`
Expected: FAIL because current prompt text does not contain `ICERainbow666`

**Step 3: Write minimal implementation**

```java
private static final String SERVER_BRAND = "ICERainbow666";
```

Use the brand in `Server.getMessage(...)` and other package-emitted strings that should visibly carry the new identity.

**Step 4: Run test to verify it passes**

Run: `mvn -Dtest=server.net.ServerMessageBrandingTest test`
Expected: PASS

**Step 5: Commit**

```bash
git add docs/plans/2026-04-07-server-net-branding-design.md docs/plans/2026-04-07-server-net-branding.md doudizhu/src/test/java/server/net/ServerMessageBrandingTest.java doudizhu/src/main/java/server/net/*.java
git commit -m "chore: brand server.net package as ICERainbow666"
```

### Task 2: Update server.net package documentation and strings

**Files:**
- Modify: `doudizhu/src/main/java/server/net/Server.java`
- Modify: `doudizhu/src/main/java/server/net/PlayerConnection.java`
- Modify: `doudizhu/src/main/java/server/net/Message.java`
- Modify: `doudizhu/src/main/java/server/net/MessageType.java`
- Modify: `doudizhu/src/main/java/server/net/Result.java`

**Step 1: Update comments**

Add `ICERainbow666` as the maintainer/branding reference in each class-level Javadoc.

**Step 2: Update runtime strings**

Brand the prompts and server identity strings emitted from `Server`.

**Step 3: Run verification**

Run: `mvn test`
Expected: PASS

**Step 4: Review git diff**

Run: `git diff --stat`
Expected: Only the planned docs, test file, and `server.net` source changes are present.

**Step 5: Commit**

```bash
git add docs/plans/2026-04-07-server-net-branding-design.md docs/plans/2026-04-07-server-net-branding.md doudizhu/src/test/java/server/net/ServerMessageBrandingTest.java doudizhu/src/main/java/server/net/*.java
git commit -m "chore: brand server.net package as ICERainbow666"
```
