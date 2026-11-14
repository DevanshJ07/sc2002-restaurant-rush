# AI chat history: ZN

- **Tool:** Claude (Anthropic), via claude.ai
- **Date:** 14 November 2026
- **Project phase:** after Stage 3 (group report)
- **Purpose:** ideas for what to write in the group report
- **How I used it:** as suggestions only; the team decided what went into the report

---

## Me

give me some ideas on what to write for the Group report

## Claude

**1. Class diagram (25 marks)**
- Put the Stage 3 diagram on its own page in landscape, so the attributes and multiplicities are readable.
- Add a short caption under each OOP annotation (E1–E4, P1–P3, C1–C2) pointing to the matching code, e.g. "E1 → `Order.markServed()`".
- Add one sentence on why some links are composition and others association: the controller owns its parts for the whole game, while a customer only points to an order.

**2. Sequence diagram (20 marks)**
- Split the long diagram into turn-sized figures so each is readable.
- Under each figure, write one line on what it shows, e.g. "Turn 2: Happy Hour alt fragment; c1 recovers to 100."
- Highlight the one manager decision in colour and end with the result: 247 vs 239 → VICTORY vs DEFEAT.

**3. Design considerations (15 marks)**
- **A second alternative design:** the original `List<Staff>` proposal, dropped because it would need `instanceof` casts. Markers like seeing that you compared options.
- **A future extension:** e.g. "If we added a second Chef, this would change, and this wouldn't." It shows you understand how extensible the design is.
- **Admit a weakness honestly:** adding the Host meant editing the controller, so the design isn't fully open to new staff roles.

**4. Implementation (20 marks)**
- Add one short code snippet per stage: the VIP's `applyPricing`, the Critic's `chooseItem`, `ComboMeal`'s constructor, `PolicyManager.applyDiscount`.
- Mention one bug or edge case the checks caught, if anyone remembers one. Real stories make this section stronger.

**5. Development process and reflection (20 marks)**

Group reflection ideas:
- **Teamwork:** how you split the work (code, diagrams, documentation) and how the handoffs worked.
- **Communication:** how you agreed on things like the Host change. Was it a group chat, a meeting, a pull request?
- **Tools:** GitHub branches and pull requests, draw.io, the check scripts, and AI assistants (keep this consistent with your GAI declarations).
- **Timeline:** the proposal, then the refined v3 design, then stages 1–3, the refactor and the diagrams. A simple timeline picture works well here.
- **Biggest lesson as a team:** for example, "Agree the behaviour first, then code", or "Keep docs and diagrams in the same pull request as the code."

**6. Functional evidence**
- Screenshots: the opening screen, an invalid input being retried, a dish going READY → SERVED → paid, the Happy Hour line, the VICTORY and DEFEAT endings.
- A one-line caption under each saying what it proves.

If you tell me which section you're adding to, I can write it into the report doc.
