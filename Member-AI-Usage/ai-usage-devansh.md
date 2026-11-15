## Devansh’s AI Brainstorming Summary — SC2002 Restaurant Rush

### Overview

My role in Restaurant Rush was to work on the game controller, the command-line interface, and the integration of the three stages. I used **OpenAI Codex** to discuss how to split the work with ZN, compare different ways the Stage 3 Host could work, and plan what to show in my part of the demo. The dates below are based on the related Git commits and demo file.

### At a glance

- **25 October 2026 · OpenAI Codex · Project planning:** Compared a parallel workflow with a sequential handoff and helped outline the division of work.
- **6 November 2026 · OpenAI Codex · Stage 3 Host design:** Compared automatic seating with giving the player a separate Host action.
- **14 November 2026 · OpenAI Codex · Demo preparation:** Helped identify what to point out during my five-turn gameplay segment.

### Conversation by conversation

**1. Splitting the coding work with ZN — 25 October**

I asked how ZN and I could split the work fairly without waiting on each other for every change. Codex suggested dividing it by what the classes were responsible for, rather than trying to give us the same number of files. We also discussed agreeing on method signatures first so that we could develop our parts in parallel. I chose to take the controller, CLI, and integration, while ZN took the customer, staff, and other classes that handle the game rules.

**2. Choosing how the Host acts — 6 November**

When I saw the first customer already seated on turn 1, I asked Codex to check whether that was required by the assignment or just a choice in our design. We compared the automatic Host with a version where the player chooses `seat` or `wait`. I chose the second option because I wanted the manager to decide when the Host should act. We kept the Host before the Waiter so a customer could still be seated and have their order taken in the same turn.

**3. Planning what to explain in my demo — 14 November**

I used Codex to go through my speaker block and connect the commands to the rules they demonstrate. The empty-table `serve 2` attempt shows that a mistake doesn’t cost the Waiter an action. The VIP’s Burger shows the 10% discount, and activating Happy Hour on the following turn shows that satisfaction can recover while the earlier **$13.50** bill stays unchanged.

### My decisions and other AI assistance

I chose my responsibilities, directed the change to make the Host player-controlled, and rehearsed the gameplay segment. This summary covers the **brainstorming discussions**
