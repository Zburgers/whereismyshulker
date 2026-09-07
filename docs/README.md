# Where Is My Shulker — Port Documentation

This directory contains the implementation-ready documentation for porting **Where Is My Shulker** from Minecraft **1.21.11** to **Minecraft 26.2** on Fabric.

**Research refresh:** 2026-09-08  
**Repository:** `Zburgers/whereismyshulker`  
**Production code baseline:** `29ec55b5d8c6da614764d33da95378ec42a0d99b`  
**Current status:** planning/research complete; implementation not yet performed by these docs commits.

## Read order

### 1. `26.2-migration-spec.md`

The architecture and compatibility contract.

Read this for:

- what the mod currently does;
- why the port remains client-only;
- the 1.21.11 → 26.2 migration boundary;
- exact dependency lock;
- target architecture;
- persistence/command/Mixin contracts;
- correctness fixes included in scope;
- invariants and out-of-scope work;
- acceptance criteria.

### 2. `26.2-research-audit.md`

The evidence ledger.

Read this for:

- which claims were verified from official Fabric docs/source/Maven;
- exact Fabric callback contracts;
- what was found in this repository's source;
- what remains a compiler or runtime gate;
- source URLs and confidence levels;
- why the break Mixin is removed but the placement Mixin remains.

### 3. `26.2-implementation-plan.md`

The executable work plan.

Read this for:

- phases A through M;
- exact files to edit/delete;
- mapping-migration sequencing;
- build script target shape;
- byte-sized code changes;
- compile checkpoints;
- suggested commit boundaries;
- static verification commands;
- full runtime smoke matrix;
- definition of done.

### 4. `26.2-shipyard-handoff.md`

The single-agent execution handoff.

Read this for:

- START / BUILD / GATE / SHIP profile;
- protected invariants;
- forbidden shortcuts;
- runtime debugging lenses;
- review lenses;
- evidence package required at completion;
- a copy-paste implementation-agent prompt.

## Frozen target lock

As of the 2026-09-08 research refresh:

```text
Minecraft       26.2
Java            25
Gradle          9.5.1
Fabric Loom     1.17.20
Fabric Loader   0.19.5
Fabric API      0.160.0+26.2
Mixin level     JAVA_25
```

## Target architecture in one screen

```text
WhereismyshulkerClient
  ├─ ClientPlayConnectionEvents.JOIN       -> load active CSV
  ├─ ClientPlayConnectionEvents.DISCONNECT -> clear session state
  ├─ ClientPlayerBlockBreakEvents.AFTER    -> remove dimension+pos row
  └─ ClientCommands /shulker               -> list/page/attended clear

BlockItemMixin (only remaining Mixin)
  ├─ HEAD   -> capture source custom name
  └─ RETURN -> require successful final shulker placement -> record

ShulkerBoxTracker
  ├─ existing SP/MP path semantics
  ├─ existing CSV format
  ├─ dimension-safe identity
  └─ direct shulker color API

ShulkerBoxData
  └─ 6/7-column compatibility + split(",", 7)
```

## Non-negotiable product contract

The port remains **client-only**. A multiplayer server must not need Where Is My Shulker installed.

The port must not move existing history files, replace the CSV schema, or turn the mod into a server-authoritative tracker.

## Implementation state

The docs are committed directly to `main`. They do **not** mean the 26.2 Java implementation has already been completed. The next action is to create an implementation branch from the docs-complete `main` and execute `26.2-implementation-plan.md` end-to-end.
