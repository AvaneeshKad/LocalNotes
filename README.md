# LocalNotes

## Summary
LocalNotes: Fast, fluid, and fiercely private. Your thoughts, stored only where they belong—on your device. 

LocalNotes is a secure, local-first Android application designed for frictionless note-taking. It bypasses cloud syncing and telemetry entirely to guarantee zero latency in read/write operations by relying purely on local storage. 

## V1.0.0 Showcase
Below is a visual overview of the application in its initial release state:

*   **Dashboard & Grid View**: The main interface displays a clean, readable grid layout of all saved notes and a top search bar, as seen in `Screenshot_20260802_181850_LocalNotes.jpg`.
*   **Inking & Drawing Tools**: 
    *   `Screenshot_20260802_181856_LocalNotes.jpg` demonstrates the core editing interface, featuring a color palette, size slider, and a blue geometric doodle alongside typed text.
    *   `Screenshot_20260802_181903_LocalNotes.jpg` highlights multi-colored inking capabilities with natural stroke variations.
    *   `Screenshot_20260802_181908_LocalNotes.jpg` shows high-sensitivity touch tracking for cursive handwriting.
    *   `Screenshot_20260802_181913_LocalNotes.jpg` showcases the fluid rendering of different stroke colors and natural handwriting curves.
    *   `Screenshot_20260802_181917_LocalNotes.jpg` illustrates in-app observations noted during testing directly onto the canvas.
*   **Search & Filtering**: 
    *   `Screenshot_20260802_181934_LocalNotes.jpg` shows the search bar actively filtering the grid down to a specific note instantly.
    *   `Screenshot_20260802_182001_LocalNotes.jpg` displays the dynamic UI adjusting smoothly for the virtual keyboard while searching.
    *   `Screenshot_20260802_182130_LocalNotes.jpg` demonstrates the search engine successfully returning multiple related results.

## Technologies Used
*   **Kotlin**: The primary modern, statically typed programming language used for logic and architecture.
*   **Jetpack Compose**: Android’s declarative UI framework for building responsive, state-driven layouts.
*   **Material 3 (M3)**: Design system providing modern components, typography, and dynamic theming.
*   **Android SDK**: Built for Compile/Target SDK 35, with a minimum SDK support of 26.
*   **KSP (Kotlin Symbol Processing)**: Efficient compiler plugin for annotation processing.

## Tools Used
*   **Zed Editor**: High-performance code editor used for all primary code authoring.
*   **Pop!_OS & COSMIC**: Linux-based development environment utilizing the COSMIC terminal emulator customized with Oh My Posh for version control tracking.
*   **Gradle**: Advanced build toolkit used for dependency resolution and compilation.
*   **Git & GitHub CLI (`gh`)**: Version control, source code tracking, and automated release deployment.

## Physics & Animations
LocalNotes incorporates real-world physics-based animations via the Jetpack Compose animation engine:
*   **Spring Simulations**: UI elements and interactive states leverage mass-spring-damper physics models instead of rigid, fixed-duration keyframes. This calculates natural momentum, velocity, and bounce for layout transitions.
*   **Kinetic Responsiveness**: Gestures and state changes react to touch velocity, mimicking real-world inertia and friction to provide tactile, fluid feedback.

## Language
*   **Kotlin**: 100%

## How to Install from Releases
1.  Navigate to the **LocalNotes** GitHub repository in your browser.
2.  Click on the **Releases** tab on the right sidebar.
3.  Locate the latest version release (e.g., `v1.0.0`).
4.  Expand the **Assets** section and download the `app-release.apk` file.
5.  Transfer the APK file to your Android device via USB, cloud storage, or local network.
6.  Open the file on your device and follow the prompt to install (ensure "Install from unknown sources" is enabled in your Android security settings).
