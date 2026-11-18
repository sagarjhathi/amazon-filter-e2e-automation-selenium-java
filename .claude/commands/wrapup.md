---
description: Wrap up the session - update STATUS.md, DECISIONS.md and (if needed) CLAUDE.md, then show the diff
---

Wrap up this session. Do not commit anything.

1. Run `git status`, `git branch --show-current`, `git log -1 --format="%h %s"` and `git diff --stat`. Review what changed in this session.
2. Update `docs/STATUS.md`:
   - move finished items to Done, each with evidence (file or commit); only items actually run or verified, otherwise keep them out of Done or mark "unverified"
   - update In progress with the exact stopping point (files touched, what is half-finished)
   - refresh Next (priority order), Known issues and Open questions
   - refresh the "Last updated" line: today's date, current branch, last commit short hash and message
3. Append any new non-obvious technical decisions from this session to the bottom of `docs/DECISIONS.md` (never edit old entries; if one is reversed, add a new entry that says so). Use the entry format in that file.
4. Update `CLAUDE.md` only if conventions, commands or architecture actually changed.
5. Show me the full diff of the doc changes (`git diff --no-index` or the before/after text, since `docs/` may be gitignored) and wait for my approval. Do not commit.
