# Minh – AI history (stage-3)

**Role:** documentation, SOLID review and the final refactor after Stage 3.
**Tool:** Claude (Anthropic), one long conversation, 6–15 Nov 2026.
**Full transcript:** [chat-01.md](chat-01.md), unedited and in order.
**This page:** my own index of that conversation, drafted with Claude's help and checked by me. The transcript is the record.

All of this happened after the stage-3 tag (6 Nov), so it is filed under stage-3.

## What I did, and where AI helped

| Date | What I did / decided | How AI helped |
|---|---|---|
| 6 Nov | Wanted to know whether our code really followed SOLID before writing the report; compared our repo with a past group's | Explained SOLID and reviewed the repo against the brief |
| 6–9 Nov | Planned the timeline and video with the group; agreed the work split | Suggested a schedule and video outline |
| 9 Nov | Created `cleanup/code-style`, applied the clean-up, ran every test and replay, committed under my account | Listed small fixes that would not change the diagrams |
| 9–10 Nov | Took the proposed SOLID changes to the group and asked Devansh and ZN for consent before touching their code. **Chose not to add a `PricingPolicy` interface** because it would mean redrawing every diagram, and explained it in the report as an alternative design | Drafted the group messages and explained each change's cost |
| 10–11 Nov | Applied the three refactors (`GameStats`, template method `calculateBill()`, `seat()`/`abandon()`/`pay()`) one commit at a time; ran `build-and-check.sh` after each (105/74/17/41/31 checks, replays identical); opened and merged PR #3 | Drafted the patches and the matching diagram edits |
| 11 Nov | Reviewed every diagram myself; **spotted the bent inheritance arrows into Customer** after the box grew and had them fixed (PR #6) | Re-anchored the arrows |
| 11 Nov | Decided which remaining issues to fix and which to keep as documented trade-offs (null checks, `losePatience` placement); removed the duplicated Happy Hour flag (PR #4) | Final OOP review of `main` |
| 11 Nov | Synced README and docs; explained to Braden in my own words why Stage 1/2 diagrams should not get `GameStats` (they must match their tags) | Explained `GameStats` so I could defend it |
| 9–14 Nov | Rebalanced speaking time, recorded my own clips, wrote my reflection | Drafted the video script and my slides |
| 15 Nov | Checked that the sequence diagram still matches the code; checked teammates' AI files against the brief and told the group what was missing; filled in my declaration | Rendered the diagrams as images and pointed out gaps |

## My own work vs AI-assisted work

| Area | Mine | AI-assisted |
|---|---|---|
| Decisions | What to change, what to leave as a trade-off, asking the team first, dropping `PricingPolicy` | Listed options and their costs |
| Code | Applied, tested and committed every change; reviewed each diff | First drafts of the patches |
| Diagrams | Reviewed the rendered result and caught the arrow error | Edited the `.drawio` XML |
| Docs, script, report | Final wording, my reflection, my recorded part | First drafts and grammar |

## How I checked AI output
- Every code change ran the full test suite and both replays (victory: 5 paid, $73.80, 84.80; defeat: 0 paid) before it was committed.
- I looked at the diagrams, not just the files, and sent back what looked wrong.
- I did not merge anything into teammates' code without their agreement.

## Declaration
My signed Declaration on Use of GAI Assistance (ticked i, ii and iii) is submitted with the group's zip file.
