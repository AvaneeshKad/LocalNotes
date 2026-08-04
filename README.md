# LocalNotes

## Summary
LocalNotes is a secure, privacy-focused, local-first Android application designed for fast, frictionless note-taking. Built with performance in mind, it stores all data directly on your device storage, ensuring complete offline functionality and zero cloud tracking.

## Technologies Used
* **Kotlin**: The primary modern, statically typed programming language used for logic and architecture.
* **Jetpack Compose**: Android’s native declarative UI framework for building responsive layouts.
* **Material 3 (M3)**: Google's design system providing modern components, typography, and dynamic theming.
* **Android SDK**: Built for Compile/Target SDK 35 with a minimum SDK support of 26.
* **KSP (Kotlin Symbol Processing)**: Efficient compiler plugin for annotation processing.

## Tools Used
* **Zed Editor**: High-performance code editor used for development.
* **Gradle**: Advanced build toolkit used for dependency resolution and compilation (`assembleRelease` / `bundleRelease`).
* **Git & GitHub CLI (`gh`)**: Version control, source code tracking, and automated release deployment.
* **Pop!_OS & COSMIC Terminal**: Linux-based development environment.

## Physics & Animations
LocalNotes incorporates real-world **physics-based animations** via the Jetpack Compose animation engine:
* **Spring Simulations**: UI elements and interactive states leverage mass-spring-damper physics models instead of rigid, fixed-duration keyframes. This calculates natural momentum, velocity, and bounce for layout transitions.
* **Kinetic Responsiveness**: Gestures and state changes react to touch velocity, mimicking real-world inertia and friction to provide tactile, fluid feedback.

## Language
* **Kotlin**: 100%

## How to Install from Releases
1. Open your web browser and go to your **LocalNotes** GitHub repository page.
2. Click on the **Releases** tab on the right sidebar.
3. Locate the latest version release (e.g., `v1.0.0`).
4. Expand the **Assets** section and download the `app-release.apk` file.
5. Move the APK file to your Android smartphone (via USB, cloud storage, or direct download).
6. Tap the APK file on your phone to install it. *(Note: If prompted, enable installation from unknown sources in your device security settings).*
