# Project Skill Guidance

## Scope

This directory contains repository-local OpenCode skills. Keep skills focused,
version-aware, and subordinate to the nearest subsystem `AGENTS.md`.

## Structure

- One skill lives at `<skill-name>/SKILL.md`.
- The directory name and frontmatter `name` must match and use lowercase
  hyphenated names.
- `description` is the discovery contract: name concrete paths, technologies,
  and task signals without claiming unrelated work.
- Keep the main skill focused on decisions and invariants. Put examples, source
  maps, long checklists, and operational recipes in `references/` and link them
  only where needed.
- Router skills load the smallest relevant set; they do not duplicate every
  specialist rule.

## Sources Of Truth

- Verify mutable facts against source, Gradle configuration, scripts, and the
  nearest `AGENTS.md` before changing a skill.
- Pin guidance to versions in this repository. Do not import current vendor
  advice that assumes a different major version or framework.
- Existing code is evidence, not authority to preserve a known bug. Document
  deliberate exceptions and current migration boundaries explicitly.
- Adapt useful external procedures; do not paste a broad upstream skill
  wholesale.

## Maintenance

- Update routing and the owning subsystem skill catalog when adding/removing a
  specialist.
- Search all local skills for stale names, commands, credentials, serials, and
  duplicate ownership after edits.
- Never put credentials, tokens, private URLs, personal data, or hard-coded
  emulator serial mappings in skills or examples.
- Keep migrations incremental. A skill may identify technical debt without
  requiring unrelated feature work to repair all of it.
- Do not add a new skill when one focused section or reference in an existing
  owner is sufficient.

## Validation

From the repository root:

```bash
opencode api get "/api/skill?location%5Bdirectory%5D=$PWD" | python3 -c \
  'import json,sys; print(sorted(s["id"] for s in json.load(sys.stdin)["data"] if "/.opencode/skills/" in s.get("path","")))'
git diff --check
```

Also verify that every skill named by a router exists, every linked local
reference resolves, and the commands match their current scripts. Restart
OpenCode from this repository root after changing skills so local discovery is
refreshed.
