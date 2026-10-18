# NOVE package format — draft v1

## Purpose

NOVE is GPTM's proposed custom package format. A .nove file is a ZIP archive with a required manifest.json at the archive root. It is not an APK and is not compatible with Geode unless a separate, explicit converter is designed and validated.

## Required manifest fields

- format: exactly "nove".
- format_version: integer schema version.
- id: stable reverse-domain identifier.
- name: display name.
- version: mod version.
- target: game and build compatibility declarations.
- entrypoint: path to the runtime payload entry point.
- runtime: required loader/runtime identifier.
- status: implementation status.

## Safety requirements for a future installer

A loader should reject path traversal, absolute paths, duplicate manifest keys, unsupported schema versions, oversized archives, missing entry points, unsupported game builds and unsupported ABIs. It should verify any declared hashes before loading code and request only explicitly declared permissions. Installing or updating must preserve backups and allow rollback.

## Runtime contract

The extension does not itself make a package executable. The NOVE loader must define the runtime API, initialization lifecycle, dependency handling, permissions, error reporting and native ABI. GPTM's current payload is intentionally marked as not implemented. The archive build is therefore a packaging prototype only, not proof of game compatibility.
