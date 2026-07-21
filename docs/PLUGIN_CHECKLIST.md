# New or Edited Plugin Checklist

- Plugin name: `Umami`
- Slug: `umami`
- Repository: `carmelosantana/minecraft-umami`
- Owner: `Carmelo Santana`
- Target version: `1.1.1` (released; no new version is proposed by this file)
- Paper version: `26.1.2 build 74`
- Java version: `25`
- Updater destination: `umami.jar`
- External services: `Umami analytics HTTP API` — opt-in, `enabled: false` by default
- Status: `active`

Maven `groupId`/`artifactId`: `org.xpfarm` / `umami`. `plugin.yml` name: `Umami`
(main class `world.hv2.umami.UmamiPlugin`). Releasable JAR: `umami-<version>.jar`.
Latest tag in this clone: `v1.1.1`.

---

## READ THIS FIRST — this is a backfill, written 2026-07-21

This plugin **predates the checklist process**. It has never had a `docs/PLUGIN_CHECKLIST.md`,
and gates 1–6 and 8–12 were **never formally run or recorded** for it. This file is written after
the fact to record what is *actually known*, not to reconstruct a history that does not exist.

Accordingly:

- **Gate 7a carries real evidence**, produced by the docker-rig-consolidation effort's shared test
  rig, quoted verbatim below from `minecraft-plugin-docs/.superpowers/sdd/task-5-report.md`. That
  report does not state its own date; the sibling `task-4-report.md` dates the same effort
  **2026-07-20**.
- **Gate 7b cites a real recorded run** (the 2026-07-19 ecosystem matrix) in
  `minecraft-plugin-docs/CURRENT_STATE.md`. It was not re-run for this backfill.
- Everything else is either an **observed fact** (something readable in the repo or manifest today,
  with the place it was read stated) or is marked **NOT RECORDED / NOT RUN / UNKNOWN**.
- An **observed fact is not a passed gate**. A checkbox is ticked only where a gate criterion is
  genuinely satisfied by evidence quoted here.

**This repository's own test harness never loaded this plugin at all** — its old
`docker-compose.yml` had no JAR mount of any kind and booted stock Paper. See gate 7a.

---

## 1. Scope — NOT RECORDED

- [ ] Status is explicitly recorded as active, experimental, or excluded. **NOT RECORDED at the
      time.** `active` is asserted here from the plugin's presence in
      `minecraft-plugin-updater/plugins.json` and in the Active Plugin Releases table of
      `minecraft-plugin-docs/CURRENT_STATE.md`. No scoping decision was ever written down —
      notable for a plugin whose purpose is transmitting player activity to a third-party endpoint.
- [ ] Purpose, commands, events, permissions, configuration, persistence, and acceptance checks are
      defined. **NOT RECORDED — predates the checklist process.** No requirements interview was
      run; no acceptance checks were ever defined. In particular **no data-handling or retention
      decision was recorded** for a plugin that ships event tracking for chat, kills, deaths, and
      logins. The surface below is **read out of `src/main/resources/plugin.yml` and `config.yml`
      today**.
- [ ] Known limitations and any intentionally withheld gates are recorded. **NOT RECORDED** for any
      released version. Gaps known *as of this backfill* are under Known gaps.

### Commands and permissions — observed from `src/main/resources/plugin.yml`

| Command | Usage | Permission | Aliases |
| --- | --- | --- | --- |
| `/umami` | `/umami <reload\|status\|test\|online\|version>` | `umami.use` | `uma` |

Permissions declared: `umami.use` (`default: true` — **every player**) and `umami.admin`
(`default: op`). Whether the subcommands that matter (`reload`, `test`) are gated behind
`umami.admin` inside the command handler was **not checked** for this backfill; `plugin.yml` puts
only `umami.use` on the command itself.

`description:` reads "A powerful analytics plugin that tracks player activity through the Umami
API".

### Observed default tracking posture (from `src/main/resources/config.yml`)

The whole integration is gated by `umami.enabled: false`. **When it is turned on**, the shipped
defaults track `player_login`, `player_logout`, `player_chat`, `crafting`, `player_kills`,
`player_deaths`, `item_pickup`, `server_performance`, and `player_count` — all `true` — while
`privacy.anonymize_players` is `false` and `filters.ignore_ops` is `false`. That is recorded as an
observation of the shipped file, not as an endorsement, and **no privacy review of it exists**.

### Known gaps (as of this backfill)

- Runtime evidence covers **load and enable** only. No `/umami` subcommand has ever been dispatched
  on a stack, and no event has ever been observed being transmitted to a real Umami endpoint.
- No acceptance criteria exist against which any release could be judged.
- The plugin ships dormant, so every green boot recorded so far proves dormancy, not analytics.

## 2. Repository — PARTIAL (observed)

- [x] Repository is `carmelosantana/minecraft-umami` with an SSH `origin` and `main` branch.
      **Observed** via `git remote -v`: `origin git@github.com:carmelosantana/minecraft-umami.git`.
      This file is committed on `test/docker-rig-consolidation`, branched off `main`.
- [ ] Existing user-owned worktree changes were identified and preserved. **NOT RECORDED as a
      gate.** Observed today: clean tree on `test/docker-rig-consolidation`. The rig-migration
      report states the repo was `## main...origin/main` with no local changes before branching.
- [ ] No `herobrinesystems` references remain in source, metadata, workflows, remotes, or
      documentation. **PARTIALLY CHECKED, not a formal audit.** A case-insensitive grep of the
      working tree run on 2026-07-21 returned **zero hits**. Scope limits: it excluded `target/`,
      `.git/`, `releases/`, and `server/`, and therefore says **nothing about git history**. Note
      separately that the Java package is `world.hv2.umami` — a *different* legacy namespace, still
      present throughout source and shade relocations; that was not part of this check and is not
      claimed clean.

## 3. Metadata — PARTIAL (observed)

- [x] AGPL-3.0-or-later `LICENSE` and Maven license metadata are present and consistent.
      **Observed:** `LICENSE` begins "GNU AFFERO GENERAL PUBLIC LICENSE / Version 3, 19 November
      2007"; `pom.xml` declares `<name>GNU Affero General Public License v3.0 or later</name>`.
- [ ] `https://xpfarm.org` metadata and Carmelo Santana author metadata are present. **HALF
      PRESENT.** Author metadata is there — `plugin.yml` `authors: [Carmelo Santana]`. The
      `xpfarm.org` URL is **not**: `pom.xml` `<url>` and `plugin.yml` `website:` both point at
      `https://github.com/carmelosantana/minecraft-umami`. Left unchecked; this backfill must not
      edit `pom.xml` or `plugin.yml`.
- [x] `play.xpfarm.org` is recorded as the public Minecraft server hostname where server identity
      is documented. **Observed:** `README.md:366` and `CONTRIBUTING.md:348` both name
      `play.xpfarm.org` as the public/test server.
- [x] New work uses the `org.xpfarm` Maven group, or an existing-coordinate compatibility decision
      is documented. **Observed:** `<groupId>org.xpfarm</groupId>`. But the Java package is
      `world.hv2.umami` and the shade relocations target `world.hv2.umami.libs.*` — group and
      package **do not match**, and the package carries a namespace unrelated to either xpfarm or
      the author. **No compatibility decision explaining this is documented anywhere**; it is
      recorded here as an open inconsistency, not a resolved choice.
- [x] Repository slug, artifact, releasable JAR, updater destination, and `plugin.yml` names are
      consistent. **Observed:** slug `umami`, artifact `umami`, JAR `umami-1.1.1.jar`, updater
      destination `umami.jar`, `plugin.yml` name `Umami`. The manifest `asset_regex`
      `^umami-[0-9].*\.jar$` matches the JAR name.
- [ ] No secrets committed in source, defaults, tests, logs, history, or documentation.
      **NOT AUDITED.** No secret scan was run for this backfill. Observed only: `config.yml` ships
      **placeholders**, not credentials — `endpoint: "https://your-umami-instance.com/api/send"`,
      `website_id: "your-website-id"`, `api_key: "your-api-key"`. That is one file read today, not
      a scan of source, tests, or history — and this plugin *does* have a real credential field, so
      the scan matters more here than in the no-service repos. Also present in the repo root:
      `generate-api-token.sh`, which was **not reviewed**.

**Observed drift note:** `pom.xml` sets `<maven.compiler.release>25</maven.compiler.release>` while
the `maven-compiler-plugin` block also carries `<source>21</source>`. Which wins was **not
investigated**.

## 4. Compatibility — PARTIAL

- [x] Java 25/Paper 26.1.2 build 74 compile succeeds and `plugin.yml` uses `api-version: '1.21'`.
      **Real evidence** from `task-5-report.md`:

      ```
      [INFO] Building jar: /home/carmelo/Projects/Minecraft/Plugins/umami/target/umami-1.1.1.jar
      [INFO] BUILD SUCCESS
      ```

      `pom.xml` depends on `io.papermc.paper:paper-api:${paper.version}` (`provided`), with
      `<paper.version>26.1.2.build.74-stable</paper.version>` in the POM properties.
      `plugin.yml` declares `api-version: '1.21'`.
- [x] Hard dependencies, soft dependencies, optional APIs, and load ordering were reviewed and
      declared. **Observed:** `plugin.yml` declares no `depend`, `softdepend`, `loadbefore`, or
      `libraries`. Runtime-scoped POM dependencies are `com.squareup.okhttp3:okhttp:4.12.0` and
      `com.google.code.gson:gson:2.10.1`, both **shaded with relocations** — `okhttp3`, `okio`,
      `com.google.gson`, `kotlin`, and `org.jetbrains.kotlin` all relocated under
      `world.hv2.umami.libs.*` — so none can collide with another plugin's copy. Note this pulls
      the **Kotlin runtime** in transitively via OkHttp; it is relocated but still bundled.
- [ ] Geyser/Floodgate/ViaVersion review covers Bedrock-safe input, UI, inventory, identity, and
      protocol behavior. **NEVER PERFORMED — NOT RECORDED.** The relevant surface here is
      *identity*: the plugin records player names/UUIDs and offers `privacy.anonymize_players`.
      How a Floodgate Bedrock player's `.`-prefixed Java-side username is recorded — as the
      prefixed name, the Bedrock gamertag, or a Floodgate UUID — is **UNKNOWN and unexamined**.
      The gate 7a boot shows coexistence with Geyser/Floodgate/ViaVersion; that is not a review.

## 5. External services — PARTIAL (defaults observed; failure path evidenced elsewhere)

- [x] External integrations are disabled by default or require explicit configuration and have
      bounded timeouts. **Observed** in `src/main/resources/config.yml`:

      ```yaml
      umami:
        # Analytics are opt-in. The plugin stays dormant when disabled.
        enabled: false
        api:
          endpoint: "https://your-umami-instance.com/api/send"
          website_id: "your-website-id"
          api_key: "your-api-key"
          timeout: 5000 # Request timeout in milliseconds
          retry_attempts: 3
          retry_delay: 1000
          ignore_ssl_certificates: false
      ```

      Off by default; the endpoint and credentials are unusable placeholders, so it cannot reach
      anything without explicit configuration; the timeout is bounded at 5000 ms with 3 retries and
      a 1000 ms delay. `ignore_ssl_certificates` correctly defaults to `false`. **These are the
      shipped values, read today. That the code honours them was not verified here.**
- [x] Ollama/Umami-style external endpoints are optional and failure-tolerant when applicable.
      **Evidence exists, but it is the matrix run's, not this gate's.**
      `minecraft-plugin-docs/CURRENT_STATE.md` records that on 2026-07-19 this plugin was
      deliberately pointed at TEST-NET-2 `198.51.100.9` to exercise the real failure path: "Umami
      enabled with a warning about its unconfigured website ID. Server stayed available." Cited
      from that document; **not re-run** here.
- [x] Endpoint failure cannot fail server/plugin startup, and diagnostics redact secrets. Same
      2026-07-19 evidence: the server stayed available and "No credential-shaped strings in logs."
      Caveat worth stating plainly: in that run the credential was still the **placeholder**
      `your-api-key`, so redaction of a *real* API key remains untested.

## 6. Tests and build — PARTIAL

- [x] Unit tests cover separable logic, configuration, serialization, permissions, and failure
      paths where applicable. **PARTIAL but real.** Two test classes exist under
      `src/test/java/world/hv2/umami/`, and the 2026-07-20 build recorded "unit tests: 8 run, 0
      failures/errors". What those eight cover was **not examined**; nothing is claimed about
      coverage of the privacy filters, the retry path, or the permission split.
- [x] `mvn --batch-mode --no-transfer-progress clean verify` succeeds. **Real evidence**, quoted
      above from `task-5-report.md`: `BUILD SUCCESS`, jar `target/umami-1.1.1.jar`, 8 tests, 0
      failures/errors.
- [ ] The releasable JAR and embedded `plugin.yml` were inspected; `original-*` JARs are excluded.
      **NOT RECORDED.** The shaded JAR has never been unzipped and inspected, so the five
      relocations and the `META-INF` filters above are **unverified at the bytecode level** — which
      matters here because the shade config also strips `META-INF/*.SF`/`.DSA`/`.RSA` and rewrites
      the manifest. Observed instead: `.github/workflows/build.yml` filters `! -name 'original-*'`
      on the checksum, artifact-upload, and release-upload steps, so an `original-*` JAR cannot
      reach a release.

      **Observed inconsistency:** the shade config sets
      `<createDependencyReducedPom>false</createDependencyReducedPom>`, yet a
      `dependency-reduced-pom.xml` is **tracked in git** at the repo root (unlike `ollama`, where
      the same file is git-ignored). It is therefore a stale committed artifact from before that
      setting. Not fixed here — out of scope for a documentation backfill.

## 7. Matrix

### 7a — single-plugin runtime verification — PARTIAL (real evidence, narrow scope)

Evidence source: **this effort's shared test rig** (`minecraft-plugin-docs/bin/xpfarm-test-stack`)
on a disposable fresh-volume Legendary stack, recorded verbatim in
`minecraft-plugin-docs/.superpowers/sdd/task-5-report.md`.

#### This repository's own harness never loaded the plugin

Confirmed during the 2026-07-20 migration and corroborated by
`minecraft-plugin-docs/CURRENT_STATE.md`: the deleted `docker-compose.yml` **had no JAR mount and
no copy mechanism at all** — "it only ever booted stock Paper". It was not a stale path or a typo,
as in the sibling repos; there was simply nothing there. Every local stack anyone ever brought up
from this repository was a plain Paper server that had never heard of this plugin, and it came up
perfectly healthy.

To be precise about firsts, since it is easy to overstate this: the **earliest** observation of
this plugin running is the **2026-07-19 ecosystem matrix run**, where the updater installed the
published `1.1.1` release asset and Paper enabled it (gate 7b). The **2026-07-20 rig boot below is
the first time it was observed running from a locally built JAR**, and the first single-plugin
verification this repository has ever had. No claim is made that it never ran before 2026-07-19 —
only that nothing in this repository could have shown it.

#### What was actually observed on 2026-07-20

- [x] Paper, Geyser, Floodgate, and ViaVersion start successfully together, with the plugin loaded
      **and enabled**. **VERIFIED.** Three independent observations, quoted exactly:

      Paper's own completion line:

      ```
      minecraft-1  | >....[K[16:50:40 INFO]: Done (18.876s)! For help, type "help"
      ```

      A **real Minecraft protocol handshake** against the Java port — not a bare TCP connect:

      ```
      MOTD: "A Minecraft Server"
      VERSION: Paper 26.1.2 | protocol 775
      PLAYERS: 0 / 20
      ```

      RCON `plugins`, captured raw with `cat -v` so the `§` colour bytes are visible as `M-BM-'`:

      ```
      AUTH OK
      $ plugins
      M-BM-'xM-BM-'3M-BM-'4M-BM-'9M-BM-'fM-BM-'dM-BM-'aM-bM-^DM-9 M-BM-'fServer Plugins (4):
      M-BM-'xM-BM-'eM-BM-'dM-BM-'8M-BM-'1M-BM-'0M-BM-'6Bukkit Plugins:
       M-BM-'8- M-BM-'afloodgateM-BM-'r, M-BM-'aGeyser-SpigotM-BM-'r, M-BM-'aUmamiM-BM-'r, M-BM-'aViaVersion
      ```

      `Umami` is prefixed `M-BM-'a` = `§a` = **green = enabled**, not merely listed. The header
      count `(4)` matches the four names listed. The rig's own `verify_plugin_present` assertion
      passed ("expecting 'Umami'"), and the slot was released cleanly on teardown. No
      Geyser/Floodgate/ViaVersion **version strings** were recorded for this run — only presence
      and green state.

- [ ] Java and Bedrock smoke tests cover joins plus commands, events, permissions, persistence, and
      reloads. **NOT DONE — neither side.** No client joined, so **not one tracked event
      (login, chat, kill, death, pickup) was ever generated**; no `/umami` subcommand was
      dispatched. The plugin ships `enabled: false`, so on this boot it started **dormant**.
      Load-and-enable is the entire behavioral claim, and for an analytics plugin that is a
      particularly thin one — the boot proves it does not crash a server, and nothing more.
- [ ] Public deployment smoke tests verify `play.xpfarm.org` reaches the intended entry points.
      Belongs to gate 11; **NOT DONE**.
- [x] Ollama and Umami unavailable-endpoint tests keep the server and plugins available.
      **Evidence exists but belongs to the 2026-07-19 matrix run** — see gate 5. Not exercised on
      the 2026-07-20 rig boot.

### 7b — ten-plugin ecosystem matrix — PASSED, but recorded elsewhere and not re-run here

Not run by this backfill. `minecraft-plugin-docs/CURRENT_STATE.md` records an
**Ecosystem Matrix Run (2026-07-19) — PASSED 11/11** on a fresh-volume Legendary stack, installing
every plugin through the one-shot updater from published release assets. Its row for this plugin:

| Plugin | Installed | Enabled in log | Result |
|---|---|---|---|
| Umami | 1.1.1 | Umami | PASS |

That run reported `Done (18.076s)`, zero SEVERE or exception lines, and that each installed JAR's
SHA-256 matched its published `SHA256SUMS.txt` digest. It also carried the negative-path evidence
quoted under gate 5, and it explicitly notes that with no endpoint configured the default run
"only proved dormancy". Cited, not reproduced. No client join was performed in that run either.

## 8. CI/CD — PARTIAL (observed)

- [x] Identical standard plugin Actions workflow is installed. **Observed:**
      `.github/workflows/build.yml` is **byte-identical** to the workflow in `copper-kingdom`,
      `death-depot`, `ollama`, and `curse` — md5 `df37a4e433a45b4cc999e14bb5997184` on all five,
      checked 2026-07-21. It triggers on `push` to `main`, `push` of `v*` tags, `pull_request` to
      `main`, and `workflow_dispatch`; builds with `temurin` Java `25`; runs
      `mvn --batch-mode --no-transfer-progress clean verify`; writes bare-filename `SHA256SUMS.txt`;
      and uploads release assets only for `refs/tags/v`.
- [ ] Successful main Actions run is recorded before tagging. **NOT RECORDED per release in this
      repository.** `CURRENT_STATE.md` states the tag and `main`-branch runs observed on
      `2026-07-19` were successful for all ten repositories, covering this repo at `v1.1.1` — but
      that is an ecosystem-wide observation of *outcome*, not a record that a green `main` run
      *preceded* each tag.
- [x] Workflow permissions contain no broader access than the documented contract. **Observed:**
      exactly `permissions: contents: write` at the top level, no job-level escalation, and the
      only token used is `GH_TOKEN: ${{ github.token }}` for `gh release`.

## 9. Release — `v1.1.1` published; asset verification NOT RE-DONE here

- [x] Semantic version matches the POM, plugin metadata, and `v<version>` tag. **Observed:**
      `pom.xml` `<version>1.1.1</version>`; newest tag in this clone is `v1.1.1`; `plugin.yml` uses
      `version: '${project.version}'`, so it cannot drift from the POM. Tags present: `v1.1.0`,
      `v1.1.1`.
- [x] Successful tag Actions run and GitHub release are recorded. **Cited, not re-verified.**
      `CURRENT_STATE.md` lists Umami at release `v1.1.1` and records successful tag and `main` runs
      observed on 2026-07-19. GitHub was not queried for this backfill.
- [ ] Release contains exactly one updater-matching JAR plus `SHA256SUMS.txt` and no `original-*`
      JAR. **NOT VERIFIED here.** Published assets were not downloaded or listed. Indirect support
      only: the workflow's `! -name 'original-*'` filters, and the matrix run's checksum match.
- [ ] Downloaded release assets pass `sha256sum --check SHA256SUMS.txt`. **NOT RUN here.**

## 10. Updater — enrolled (observed); behaviors NOT RUN

- [x] Updater manifest covers repository, destination, anchored asset regex, legacy globs, enabled
      state, and optional pin. **Observed** in `minecraft-plugin-updater/plugins.json`:

      ```json
      {"name": "Umami", "repo": "carmelosantana/minecraft-umami", "destination": "umami.jar", "asset_regex": "^umami-[0-9].*\\.jar$", "legacy_globs": ["umami-[0-9]*.jar"]}
      ```

      The regex is anchored at both ends. `enabled` is **absent, which means true**. There is **no
      version pin**. So this plugin **does install and enable on every fresh volume**, even though
      it is an external-service integration that ships dormant — `CURRENT_STATE.md` makes the same
      point ("nothing ships disabled"). No manifest change is proposed by this backfill.
- [ ] Fresh install, upgrade, no-op, legacy archival, endpoint failure, and checksum failure
      behaviors pass. **NOT RUN for this plugin.** The 2026-07-19 matrix exercised *fresh install*
      of this entry as a side effect; the other five behaviors were never tested per-plugin.
- [ ] Updater dry-run uses a disposable directory and never a production plugin directory.
      **NOT RUN.**
- [ ] Failure retains the installed JAR and default fail-open behavior permits Minecraft startup.
      **NOT RUN for this plugin.**

## 11. Deployment — NOT RECORDED

- [ ] Dokploy redeployment notes identify the full recreation used to rerun the one-shot updater.
      **NOT RECORDED.**
- [ ] Updater completion, Minecraft startup, destination JAR, and stack/plugin logs were inspected.
      **NOT RECORDED.**
- [ ] No production plugin hot reload was used. **UNKNOWN** — no deployment record exists for this
      plugin at any version.

No deployment was performed by this backfill, and this workstation has no Dokploy access, so no
production log could be inspected even in principle. **Whether a real Umami endpoint, website ID,
and API key are configured on `play.xpfarm.org` — and therefore whether player analytics are
actually being collected in production — is completely unknown from this repository.** That is the
single most important open question on this page.

**Rollback:** untested. The prior tag is `v1.1.0`. As with `ollama`, the cheapest mitigation for
this plugin is not a rollback — setting `umami.enabled: false` returns it to dormancy — but that
has not been rehearsed in production either.

## 12. Handoff — PARTIAL

- [ ] Current-state documentation refreshed with release, CI, updater, deployment, and local
      pending state. **NOT DONE by this backfill** — `minecraft-plugin-docs/CURRENT_STATE.md` was
      deliberately left untouched. It already flags this repo as one of four carrying no gate 7a
      checklist record; that flag is now stale in this repo's favour.
- [x] Known limitations, skipped checks, migration notes, rollback guidance, and follow-up owner
      are recorded. This file is that record. Owner: Carmelo Santana.
- [x] Evidence distinguishes source commit, published tag/release, updater state, and deployed
      state without exposing secrets. Source: `test/docker-rig-consolidation`, **local and
      unpushed**. Published: `v1.1.1`. Updater: enrolled, unpinned, enabled. Deployed: **unknown**.

**Follow-ups, in priority order:**

1. Determine whether analytics are actually enabled in production and, if so, record what is being
   collected. Nothing in this repository answers that (gate 11).
2. Exercise `/umami` and the event pipeline end to end against a real Umami instance with
   `enabled: true` — login, chat, death events, and the `reload` path. Gate 7a currently proves
   only that a **dormant** plugin's `onEnable()` does not throw.
3. Record a privacy decision for the shipped tracking defaults (chat, kills, deaths all on;
   `anonymize_players: false`), and confirm `umami.admin` actually gates `reload`/`test`.
4. Review how Floodgate Bedrock identities are recorded by the tracker (gate 4).
5. Run a secrets scan covering source, tests, and history, and review `generate-api-token.sh`
   (gate 3).
6. Inspect the shaded release JAR and confirm the five relocations and `META-INF` filters landed
   (gate 6); remove the stale tracked `dependency-reduced-pom.xml`.
7. Resolve or document the `org.xpfarm` group vs `world.hv2.umami` package split (gate 3).
