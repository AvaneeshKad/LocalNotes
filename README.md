# LocalNotes v2

> A fast, local-first hybrid note-taking application combining Markdown text editing and a freehand drawing canvas.

---

## Showcase

| Main Dashboard | Features & Handwriting | Combined Markdown + Drawings |
| :---: | :---: | :---: |
| <img src="showcase_1.jpeg" width="250" alt="Dashboard View"/> | <img src="showcase_2.jpeg" width="250" alt="Features Note View"/> | <img src="showcase_3.jpeg" width="250" alt="Combined View"/> |

---

## What's New in v2 🎉

* **Redesigned User Interface:** Modern layout with grid dashboard view, custom search bar, and clean top bar controls.
* **Eraser Tool:** Precision eraser functionality alongside pen controls on your drawing canvas.
* **Hybrid Editing:** Type full Markdown text and sketch freehand directly on the same canvas page.
* **Multiple Pages Support:** Create and manage multi-page documents seamlessly within individual notes.
* **Zoom & Canvas Controls:** Built-in scale controls ($100\%$ reset button, step zoom-in/out buttons) for visual adjustment.
* **PDF Export & Native Sharing:** Instant PDF rendering and system-level sharing capabilities.
* **Local Storage First:** Offline-first architecture ensuring zero reliance on remote servers.
* **Dark Theme UI:** Native dark mode palette for late-night session readability.

---

## Features Breakdown

* **Markdown Support:** Full support for standard syntax such as headings (`#`), bold (`**text**`), italics (`*text*`), and bullet points (`-`).
* **Interactive Canvas Tools:**
  * Freehand Pen Tool with color options.
  * Eraser Tool for line and stroke cleanup.
  * Lock Canvas toggle to prevent unintentional drawing strokes.
  * Fast page navigation and creation (`+ Page`).
* **Search Archive:** High-speed search filtering across note titles directly from the main view.
* **Document Export:** High-fidelity PDF rendering preserving both handwriting vectors and styled Markdown content.

---

## PDF Export Sample

An example of a document generated directly from LocalNotes v2 can be found in the repository:

* **Sample PDF File:** [`LocalNotes_Export.pdf`](LocalNotes_Export.pdf)

---

## Tech Stack & Requirements

* **Platform:** Android
* **UI Framework:** Android SDK / Jetpack Compose
* **Storage:** Local device file system storage

---

## Getting Started

### Prerequisites

* Android Studio (latest stable version)
* JDK 17 or higher
* Android SDK API Level 24+

### Installation & Build

```bash
git clone [https://github.com/AvaneeshKad/LocalNotes.git](https://github.com/AvaneeshKad/LocalNotes.git)
cd LocalNotes
