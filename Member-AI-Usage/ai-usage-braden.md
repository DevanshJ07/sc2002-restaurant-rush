# Braden's AI Use Summary (SC2002 Restaurant Rush)

15 Nov 2026 · Braden

## Overview

I used Claude (claude.ai, inside a Project called "SC2002 OOP") across 7 project conversations between 17 Oct and 14 Nov 2026, mainly for design work: class diagrams, the sequence diagram, design documentation and presentation slides. I made the first class diagram myself in draw.io; AI was used to check it, critique it and keep the diagrams consistent with the brief and the code.

- **Tool:** Claude (Anthropic), claude.ai web/desktop app, one Project for all SC2002 chats.
- **Period:** 17 Oct 2026 to 14 Nov 2026 (plus this summary on 15 Nov).
- **Main uses:** reviewing designs against the brief, checking .drawio diagrams, writing supporting workflow docs, a draft Java implementation for testing(not the submitted product), a task plan, and presentation slides with a script.

**Important:** the briefing (slide 7) says *"Keep the full conversation in order. A summary alone is insufficient."* This file is a condensed index of my AI use. The full chat histories are in my branch under `ai-history/stage-N/chat-NN.md`.

## At a glance
Seven conversations, oldest first. "Stage" is the suggested `ai-history` folder; most design chats covered all three stages.

| # | Date | Conversation | Deliverable / stage | Main AI output |
| --- | --- | --- | --- | --- |
| 1 | 17 Oct | Verify sequence diagram in text format | Sequence + class diagram (Stage 3) | Mermaid sequence diagram; 6 fixes to my class diagram|
| 2 | 22 Oct | To do list generation | Planning (all stages) | Task list by stage, reordered into SDLC phases |
| 3 | 28 Oct | Review proposed changes to class diagram | Class spec + code (all stages) | 15 accepted design changes|
| 4 | 31 Oct | Verify draw.io sequence diagram | 3 stage class diagrams | .drawio sequence diagram walkthrough, design-rationale doc, Stage 1/2/3 split |
| 5 | 5 Nov | Class diagram discrepancies | Diagrams aligned to code (all stages) | Spec audit; 4 diagram updates; sequence diagram + walkthrough updated |
| 6 | 6 Nov | Host seating and class diagram | Host behaviour (Stage 3) | 3 new methods; sequence diagram changed to manager-chosen seating |
| 7 | 14 Nov | PowerPoint presentation request | Presentation (Parts 3 to 5) | 21-slide deck with speaker notes; script doc |

## Conversation by conversation

### 1. Sequence diagram in text format (17 Oct)

- **I asked:** to verify my sequence diagram order in plain text, based on my own class diagram (`SC2002_Class_Diagram.drawio`) and the project manual.
- **AI produced:** a sequence diagram covering arrival/order, Waiter/Chef assignment, READY→SERVED, eating, payment and result check. Scenario: VIP pricing plus Happy Hour, using the manual's $10.80 example ($10.80 vs $13.50 depending on the manager's turn-2 choice).
- **Follow-up:** AI found 6 mismatches between my class diagram and the required flow. I asked for specifics changes, `SC2002_Class_Diagram_v2.drawio` (added `billInCents`, an `EATING` state, `chooseItem()` with a Critic override, int cents for money, discount methods on PolicyManager, fixed connectors). Edits were made manually.

### 2. To do list generation (22 Oct)

- **I asked:** for a to-do list from the assignment brief, then to reorder it by SDLC phase.
- **AI produced:** tasks by stage (stage-1/2/3 tags), then 8 SDLC phases (planning, requirements, design, 3 implement-and-test iterations, documentation, deployment) with milestone dates before the 15 Nov deadline.


### 3. Review proposed changes to class diagram (28 Oct)

- **I asked:** to review the group's 15 proposed changes (`SC2002_Group_4_Refined.pdf`), keep only those that made sense, and apply them to the class list PDF (`SC2002_Group_4.pdf`) and the v3 annotated diagram.
- **AI produced:** accepted all 15 (e.g. `readyToPayTurn` + `collectPayment`, typed staff fields instead of a list, game loop moved to `GameCLI.run()`, `InvalidActionException`, money as int cents) and added its own fixes ("owns" wording, `getCustomers()` returns a copy, integer-division fix, missing getters). Rebuilt the PDF and diagram and cross-checked them.
- **Follow-up:** I asked for the game code. AI wrote a 23-file Java implementation (`RestaurantRush.zip`), compiled it and ran scripted test games. It flagged placeholder values to replace from the brief (prices, table count, VIP/Critic arrivals, victory thresholds) and split classes by stage.

### 4. Draw.io sequence diagram (31 Oct)

- **I asked:** to verify the sequence diagram using `SC2002_Class_Diagram_v4.drawio`, then the class diagram split into 3 stage diagrams (rubric requirement).
- **AI produced:**  Its consistency check found 3 missing dependency arrows, fixed in v5 (v4 kept). Also "Restaurant Rush: Design Choices and Rationale" (exported to .docx for Google Docs) and Stage 1/2/3 class diagrams, checked so Stages 2 and 3 only add to Stage 1.
- **My input:** asked that invented values and assumptions be clearly labelled (tagged A1 to A6) rather than presented as facts.

### 5. Class diagram discrepancies (5 Nov)

- **I asked:** first, an audit of the class diagram against the spec without changes; then 4 updates so diagrams match our actual code (not the other way round).
- **AI produced:** audit (missing `InvalidActionException` dependency, inconsistent static/final markings, composition vs aggregation question). Then added `finishWaiterPhase()`, made `happyHourActive` derived, marked `MAX_TURNS` `public static final`, and added display getters across all 3 stage files, same filenames, with a change table.
- **Follow-up:** matching sequence diagram edits, and the walkthrough PDF rebuilt (now 122 steps) plus a Google-Docs-friendly HTML version.

### 6. Host seating and class diagram (6 Nov)

- **I asked:** how the Stage 3 Host seats customers. **I decided** the Host should not seat automatically; seating should be the Manager's (player's) choice, with no class diagram changes unless necessary.
- **AI produced:** checked the brief, which supported my decision, and suggested 3 methods: `Host.seatCustomer(queue, customer, table)`, `GameController.hostSeat(customerId, tableId)`, `GameCLI.promptHostAction()`.
- **Follow-up:** I pushed back when AI first said no class diagram change was needed; it later agreed it was wrong. I asked for the Host to act before the Waiter and only when someone can be seated. Confirmed no Stage 1/2 changes were needed.

### 7. PowerPoint presentation request (14 Nov)

- **I asked:** for a PowerPoint for my parts of the video: Part 3 (class diagram), Part 4 (sequence diagram) and a short Part 5 slot, using my existing script.
- **AI produced:** a 21-slide deck with diagram crops and highlights, and speaker notes extending my script to fit the slides (satisfaction 247 vs 239 against target 240 in the outcome slide). Also a script doc for printing. AI flagged the Part 5 text as a draft to check against my own reflection.

## My own work vs AI-assisted work

| Area | Mine | AI-assisted |
| --- | --- | --- |
| Class diagram | First version drawn myself in draw.io; chose which changes to accept | Gap checks against the brief, edits to v2 to v5, Stage 1/2/3 split, OOP annotations |
| Design decisions | Manager-chosen Host seating; Host acts before Waiter; diagrams follow the code, not the reverse; no formatting changes | Checked decisions against the brief; proposed method signatures |
| Sequence diagram | Initial sequence diagram; Chose to keep it consistent with the class diagram and code; requested each change | sequence diagram walkthrough docs, updates |
| Documentation | Directed content; asked for assumptions to be labelled | Design-rationale doc, connector tables, walkthrough |
| Presentation | Original (shorter) script and part split with teammates | Slide deck, extended speaker notes, script doc |

## How I verified AI output

I did not accept changes without a check against the brief, the code or the original file.

- **Audit before editing:** asked for discrepancy reports first (chats 1, 5), then chose which fixes to apply.
- **Change tables:** required a before/after table for every diagram edit (chats 5, 6).
- **Versioned files:** kept earlier versions (v1 to v5) untouched so changes could be compared.
- **Cross-checks:** every sequence-diagram message was matched to a class-diagram method; stage diagrams were checked so Stages 2 and 3 only add to Stage 1.
- **Pushed back on errors:** e.g. in chat 6, corrected AI's claim that the class diagram needed no change for Host seating.
- **Labelled assumptions:** invented example values (prices, totals) marked as such, not presented as facts from the brief.

## Declaration

I, Braden, used Claude (Anthropic) during the SC2002 Restaurant Rush project from 17 Oct to 14 Nov 2026, as listed above. I used it to review and edit UML diagrams, draft supporting documentation, plan tasks and prepare presentation slides. I drew the original class diagram and sequence diagrams myself and made the design decisions recorded here. I checked AI output against the brief and our code and understand the work I am submitting. My full AI conversation histories are in my branch under `ai-history/`.
