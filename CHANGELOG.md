# Changelog

<a id="v2.0.3"></a>
## [Notes (Privacy Friendly) v2.0.3](https://github.com/SecUSo/privacy-friendly-notes/releases/tag/v2.0.3) - 2024-12-03

## What's Changed
* Integrates translations via Weblate by [@coderPaddyS](https://github.com/coderPaddyS) in [#186](https://github.com/SecUSo/privacy-friendly-notes/pull/186)
* Improvement of UI and UX by [@coderPaddyS](https://github.com/coderPaddyS) in [#184](https://github.com/SecUSo/privacy-friendly-notes/pull/184)


**Full Changelog**: https://github.com/SecUSo/privacy-friendly-notes/compare/v2.0.2...v2.0.3

[Changes][v2.0.3]


<a id="v2.0.2"></a>
## [Notes (Privacy Friendly) v2.0.2](https://github.com/SecUSo/privacy-friendly-notes/releases/tag/v2.0.2) - 2024-10-25

## What's Changed
* [fix] checklist items now consistently contain their state. by [@coderPaddyS](https://github.com/coderPaddyS) in [#181](https://github.com/SecUSo/privacy-friendly-notes/pull/181)


**Full Changelog**: https://github.com/SecUSo/privacy-friendly-notes/compare/v2.0.1...v2.0.2

[Changes][v2.0.2]


<a id="v2.0.1"></a>
## [Notes (Privacy Friendly) v2.0.1](https://github.com/SecUSo/privacy-friendly-notes/releases/tag/v2.0.1) - 2024-09-20

## What's Changed
* Update changelog for v2.0.0 by [@github-actions](https://github.com/github-actions) in [#166](https://github.com/SecUSo/privacy-friendly-notes/pull/166)
* Fix Intent handling in BaseNoteActivity by [@udenr](https://github.com/udenr) in [#167](https://github.com/SecUSo/privacy-friendly-notes/pull/167)
* Fix current category not selected for new notes by [@udenr](https://github.com/udenr) in [#168](https://github.com/SecUSo/privacy-friendly-notes/pull/168)
* Update GitHub workflows by [@udenr](https://github.com/udenr) in [#169](https://github.com/SecUSo/privacy-friendly-notes/pull/169)
* Fix notes from default category not shown after update to v2.0 by [@udenr](https://github.com/udenr) in [#173](https://github.com/SecUSo/privacy-friendly-notes/pull/173)
* Fix filter not ignoring case by [@udenr](https://github.com/udenr) in [#171](https://github.com/SecUSo/privacy-friendly-notes/pull/171)
* Fix potential crash if preference contains invalid value by [@udenr](https://github.com/udenr) in [#172](https://github.com/SecUSo/privacy-friendly-notes/pull/172)
* Fix race condition in some edge cases by [@udenr](https://github.com/udenr) in [#174](https://github.com/SecUSo/privacy-friendly-notes/pull/174)
* Use Glide to load images in `NoteAdapter` by [@udenr](https://github.com/udenr) in [#170](https://github.com/SecUSo/privacy-friendly-notes/pull/170)
* V2.0.1 by [@coderPaddyS](https://github.com/coderPaddyS) in [#175](https://github.com/SecUSo/privacy-friendly-notes/pull/175)


**Full Changelog**: https://github.com/SecUSo/privacy-friendly-notes/compare/v2.0.0...v2.0.1

[Changes][v2.0.1]


<a id="v2.0.0"></a>
## [Notes (Privacy Friendly) v2.0.0](https://github.com/SecUSo/privacy-friendly-notes/releases/tag/v2.0.0) - 2024-09-03

## What's Changed
### UI
- Theming (Light and Dark mode)
- Colored categories: Identify categories and colors and quickly find your notes.
- Improved note preview
- Redesign of the checklist
- Confirmation Dialogs for deleting a note
- New FAB to create notes

### Behavior and Features
- More and better sorting options in the note overview. Also custom sorting possible.
- Convert text notes to checklists and vise versa
- Undo/Redo buttons for sketches
- Swipe to delete and drag-and-drop to reorder everywhere
- Opening a txt file automatically creates a new text note
- Sharing text with the notes app creates a new corresponding text note
- Button to clear recycling bin
- When deleting a category, all notes will be deleted if the corresponding setting is enabled.

### Others
- Bug fixes. More notably: Force-closing the app whilst having edited a sketch resulted in the corruption of said sketch.
- Closes issues [#143](https://github.com/SecUSo/privacy-friendly-notes/issues/143), [#74](https://github.com/SecUSo/privacy-friendly-notes/issues/74), [#131](https://github.com/SecUSo/privacy-friendly-notes/issues/131), [#150](https://github.com/SecUSo/privacy-friendly-notes/issues/150), [#59](https://github.com/SecUSo/privacy-friendly-notes/issues/59), [#43](https://github.com/SecUSo/privacy-friendly-notes/issues/43).

## New Contributors
* [@jahway603](https://github.com/jahway603) made their first contribution in [#155](https://github.com/SecUSo/privacy-friendly-notes/pull/155)

**Full Changelog**: https://github.com/SecUSo/privacy-friendly-notes/compare/v1.4.5...v2.0.0

[Changes][v2.0.0]


<a id="v1.4.5"></a>
## [Notes (Privacy Friendly) v1.4.5](https://github.com/SecUSo/privacy-friendly-notes/releases/tag/v1.4.5) - 2024-01-11

## What's Changed
* Fix export permissions for API >= 33 by [@udenr](https://github.com/udenr) in [#158](https://github.com/SecUSo/privacy-friendly-notes/pull/158)

**Full Changelog**: https://github.com/SecUSo/privacy-friendly-notes/compare/v1.4.4...v1.4.5

[Changes][v1.4.5]


<a id="v1.4.4"></a>
## [Notes (Privacy Friendly) v1.4.4](https://github.com/SecUSo/privacy-friendly-notes/releases/tag/v1.4.4) - 2023-10-31

## What's Changed
* Update to BackupAPI v1.2.0 by [@udenr](https://github.com/udenr) in [#147](https://github.com/SecUSo/privacy-friendly-notes/pull/147)
* Add missing preference key to BackupRestorer.java by [@udenr](https://github.com/udenr) in [#146](https://github.com/SecUSo/privacy-friendly-notes/pull/146)
* Fix notifications by [@udenr](https://github.com/udenr) in [#151](https://github.com/SecUSo/privacy-friendly-notes/pull/151)
* Update dependencies and target sdk by [@udenr](https://github.com/udenr) in [#152](https://github.com/SecUSo/privacy-friendly-notes/pull/152)


**Full Changelog**: https://github.com/SecUSo/privacy-friendly-notes/compare/v1.4.2...v1.4.4

[Changes][v1.4.4]


<a id="v1.4.2"></a>
## [Notes (Privacy Friendly) v1.4.2](https://github.com/SecUSo/privacy-friendly-notes/releases/tag/v1.4.2) - 2023-05-09

## What's Changed
* Fixes: [#132](https://github.com/SecUSo/privacy-friendly-notes/issues/132) by [@coderPaddyS](https://github.com/coderPaddyS) in [#135](https://github.com/SecUSo/privacy-friendly-notes/pull/135)
* Fixes: [#133](https://github.com/SecUSo/privacy-friendly-notes/issues/133) by [@coderPaddyS](https://github.com/coderPaddyS) in [#136](https://github.com/SecUSo/privacy-friendly-notes/pull/136)
* v1.4.2 Bugfix sharing notes by [@coderPaddyS](https://github.com/coderPaddyS) in [#145](https://github.com/SecUSo/privacy-friendly-notes/pull/145)


**Full Changelog**: https://github.com/SecUSo/privacy-friendly-notes/compare/v1.4.1...v1.4.2

[Changes][v1.4.2]


<a id="v1.4.1"></a>
## [Notes (Privacy Friendly) v1.4.1](https://github.com/SecUSo/privacy-friendly-notes/releases/tag/v1.4.1) - 2022-12-02

## What's Changed
* Fixed the save button to function the same as the back action by [@coderPaddyS](https://github.com/coderPaddyS) in [#128](https://github.com/SecUSo/privacy-friendly-notes/pull/128)
* Fixed the NPE in noteFromIntent caused by empty extras. by [@coderPaddyS](https://github.com/coderPaddyS) in [#129](https://github.com/SecUSo/privacy-friendly-notes/pull/129)


**Full Changelog**: https://github.com/SecUSo/privacy-friendly-notes/compare/v1.4.0...v1.4.1

[Changes][v1.4.1]


<a id="v1.4.0"></a>
## [Notes (Privacy Friendly) v1.4.0](https://github.com/SecUSo/privacy-friendly-notes/releases/tag/v1.4.0) - 2022-11-21

## What's Changed
* Refactored MainActivityViewModel to use Flows instead of LiveData. by [@coderPaddyS](https://github.com/coderPaddyS) in [#123](https://github.com/SecUSo/privacy-friendly-notes/pull/123)
* Overhaul Note UI and Bugfixes by [@coderPaddyS](https://github.com/coderPaddyS) in [#124](https://github.com/SecUSo/privacy-friendly-notes/pull/124)

## New Contributors
* [@coderPaddyS](https://github.com/coderPaddyS) made their first contribution in [#123](https://github.com/SecUSo/privacy-friendly-notes/pull/123)

**Full Changelog**: https://github.com/SecUSo/privacy-friendly-notes/compare/v1.3.0...v1.4.0

[Changes][v1.4.0]


<a id="v1.3.0"></a>
## [Notes (Privacy Friendly) v1.3.0](https://github.com/SecUSo/privacy-friendly-notes/releases/tag/v1.3.0) - 2022-08-23

## What's Changed
* fix sql constraint exception that would occur when a note had a null category by [@Kamuno](https://github.com/Kamuno) in [#114](https://github.com/SecUSo/privacy-friendly-notes/pull/114)
* fix backup import error by [@Kamuno](https://github.com/Kamuno) and [@udenr](https://github.com/udenr) in [#115](https://github.com/SecUSo/privacy-friendly-notes/pull/115)
* add utility methods to get the correct database version on backup creation by [@Kamuno](https://github.com/Kamuno) in [#116](https://github.com/SecUSo/privacy-friendly-notes/pull/116)
* Added a setting to enable/disable note previews by [@Kamuno](https://github.com/Kamuno) in [#117](https://github.com/SecUSo/privacy-friendly-notes/pull/117)


**Full Changelog**: https://github.com/SecUSo/privacy-friendly-notes/compare/v1.2.5...v1.3.0

[Changes][v1.3.0]


<a id="v1.2.5"></a>
## [Notes (Privacy Friendly) v1.2.5](https://github.com/SecUSo/privacy-friendly-notes/releases/tag/v1.2.5) - 2022-08-08

## What's Changed
* Allow main thread queries for now by [@Kamuno](https://github.com/Kamuno) in [#111](https://github.com/SecUSo/privacy-friendly-notes/pull/111)


**Full Changelog**: https://github.com/SecUSo/privacy-friendly-notes/compare/v1.2.4...v1.2.5

[Changes][v1.2.5]


<a id="v1.2.4"></a>
## [Notes (Privacy Friendly) v1.2.4](https://github.com/SecUSo/privacy-friendly-notes/releases/tag/v1.2.4) - 2022-08-08

## What's Changed
* Fix database access from main thread crash in BootReceiver by [@udenr](https://github.com/udenr) in [#110](https://github.com/SecUSo/privacy-friendly-notes/pull/110)


**Full Changelog**: https://github.com/SecUSo/privacy-friendly-notes/compare/v1.2.3...v1.2.4

[Changes][v1.2.4]


<a id="v1.2.3"></a>
## [Notes (Privacy Friendly) v1.2.3](https://github.com/SecUSo/privacy-friendly-notes/releases/tag/v1.2.3) - 2022-08-06

## What's Changed
* Fixed class cast exception, pending intent flags, missing permission by [@Kamuno](https://github.com/Kamuno) in [#103](https://github.com/SecUSo/privacy-friendly-notes/pull/103)
* Update Icons and fastlane app icon by [@Kamuno](https://github.com/Kamuno) in [#104](https://github.com/SecUSo/privacy-friendly-notes/pull/104)
* Fix version number in the about activity not being updated automatically by [@udenr](https://github.com/udenr) in [#107](https://github.com/SecUSo/privacy-friendly-notes/pull/107)
* Fix notifications by [@udenr](https://github.com/udenr) in [#106](https://github.com/SecUSo/privacy-friendly-notes/pull/106)

## New Contributors
* [@udenr](https://github.com/udenr) made their first contribution in [#107](https://github.com/SecUSo/privacy-friendly-notes/pull/107)

**Full Changelog**: https://github.com/SecUSo/privacy-friendly-notes/compare/v1.2.2...v1.2.3

[Changes][v1.2.3]


<a id="v1.2.2"></a>
## [Notes (Privacy Friendly) v1.2.2](https://github.com/SecUSo/privacy-friendly-notes/releases/tag/v1.2.2) - 2022-08-03

## What's Changed
* Fixed crash that would occur more than one text note was saved by [@Kamuno](https://github.com/Kamuno)


**Full Changelog**: https://github.com/SecUSo/privacy-friendly-notes/compare/v1.2.1...v1.2.2

[Changes][v1.2.2]


<a id="v1.2.1"></a>
## [Notes (Privacy Friendly) v1.2.1](https://github.com/SecUSo/privacy-friendly-notes/releases/tag/v1.2.1) - 2022-08-03

## What's Changed
* Fixed crash that would occur when no text notes are saved by [@Kamuno](https://github.com/Kamuno) in [#100](https://github.com/SecUSo/privacy-friendly-notes/pull/100)


**Full Changelog**: https://github.com/SecUSo/privacy-friendly-notes/compare/v1.2.0...v1.2.1

[Changes][v1.2.1]


<a id="v1.2.0"></a>
## [Notes (Privacy Friendly) v1.2.0](https://github.com/SecUSo/privacy-friendly-notes/releases/tag/v1.2.0) - 2022-08-03

Notes (Privacy Friendly) v1.2.0

## What's Changed
* Added Support for the Backup App from F-Droid [@Kamuno](https://github.com/Kamuno) 
* Updated Backup API to v1.0.0 [@Kamuno](https://github.com/Kamuno) 

**Full Changelog**: https://github.com/SecUSo/privacy-friendly-notes/compare/v1.1.1...v1.2.0

[Changes][v1.2.0]


<a id="v1.1.1"></a>
## [Notes (Privacy Friendly) v1.1.1](https://github.com/SecUSo/privacy-friendly-notes/releases/tag/v1.1.1) - 2022-07-31

Notes (Privacy Friendly) v1.1.1

## What's Changed
* Fixed migration for text notes [@Kamuno](https://github.com/Kamuno) 
* Fixed sharing of notes [@Kamuno](https://github.com/Kamuno) 

**Full Changelog**: https://github.com/SecUSo/privacy-friendly-notes/compare/v1.1.0...v1.1.1

[Changes][v1.1.1]


<a id="v1.1.0"></a>
## [Notes (Privacy Friendly) v1.1.0 ** BROKEN RELEASE **](https://github.com/SecUSo/privacy-friendly-notes/releases/tag/v1.1.0) - 2022-07-25

## What's Changed
* Lab WS22 + Backup API update by [@Kamuno](https://github.com/Kamuno) and [@maxmitz](https://github.com/maxmitz) in [#95](https://github.com/SecUSo/privacy-friendly-notes/pull/95)
* Update to correct backup api version. by [@Kamuno](https://github.com/Kamuno) in [#96](https://github.com/SecUSo/privacy-friendly-notes/pull/96)

- Database switched to Room
- Switched Note Overview to RecyclerView
- Added Backup API integration
- Added a search function
- Added color black to the color palette
- Added simple text format options
- Improved sharing of notes


**Full Changelog**: https://github.com/SecUSo/privacy-friendly-notes/compare/v1.0.2...v1.1.0

[Changes][v1.1.0]


<a id="v1.0.2"></a>
## [Notes (Privacy Friendly) v1.0.2](https://github.com/SecUSo/privacy-friendly-notes/releases/tag/v1.0.2) - 2021-01-27

Added api to backup application.
Added adaptive icon

[Changes][v1.0.2]


<a id="v1.0.1"></a>
## [Privacy Friendly Notes v1.0.1](https://github.com/SecUSo/privacy-friendly-notes/releases/tag/v1.0.1) - 2016-12-12

- Japanese Translation
- Export Icon Update


[Changes][v1.0.1]


<a id="v1.0"></a>
## [Privacy Friendly Notes v1.0](https://github.com/SecUSo/privacy-friendly-notes/releases/tag/v1.0) - 2016-11-24

Privacy Friendly Notes is an Android application for the creation and management of notes. It is also possible to can define categories and assign them to the notes. Notes can have one of these types: 
­- simple text notes 
­- check-list notes 
­- audio notes 
­- sketch notes 

This app belongs to the Privacy Friendly Apps group developed by the research group SECUSO at Technische Universität Darmstadt, Germany.


[Changes][v1.0]


[v2.0.3]: https://github.com/SecUSo/privacy-friendly-notes/compare/v2.0.2...v2.0.3
[v2.0.2]: https://github.com/SecUSo/privacy-friendly-notes/compare/v2.0.1...v2.0.2
[v2.0.1]: https://github.com/SecUSo/privacy-friendly-notes/compare/v2.0.0...v2.0.1
[v2.0.0]: https://github.com/SecUSo/privacy-friendly-notes/compare/v1.4.5...v2.0.0
[v1.4.5]: https://github.com/SecUSo/privacy-friendly-notes/compare/v1.4.4...v1.4.5
[v1.4.4]: https://github.com/SecUSo/privacy-friendly-notes/compare/v1.4.2...v1.4.4
[v1.4.2]: https://github.com/SecUSo/privacy-friendly-notes/compare/v1.4.1...v1.4.2
[v1.4.1]: https://github.com/SecUSo/privacy-friendly-notes/compare/v1.4.0...v1.4.1
[v1.4.0]: https://github.com/SecUSo/privacy-friendly-notes/compare/v1.3.0...v1.4.0
[v1.3.0]: https://github.com/SecUSo/privacy-friendly-notes/compare/v1.2.5...v1.3.0
[v1.2.5]: https://github.com/SecUSo/privacy-friendly-notes/compare/v1.2.4...v1.2.5
[v1.2.4]: https://github.com/SecUSo/privacy-friendly-notes/compare/v1.2.3...v1.2.4
[v1.2.3]: https://github.com/SecUSo/privacy-friendly-notes/compare/v1.2.2...v1.2.3
[v1.2.2]: https://github.com/SecUSo/privacy-friendly-notes/compare/v1.2.1...v1.2.2
[v1.2.1]: https://github.com/SecUSo/privacy-friendly-notes/compare/v1.2.0...v1.2.1
[v1.2.0]: https://github.com/SecUSo/privacy-friendly-notes/compare/v1.1.1...v1.2.0
[v1.1.1]: https://github.com/SecUSo/privacy-friendly-notes/compare/v1.1.0...v1.1.1
[v1.1.0]: https://github.com/SecUSo/privacy-friendly-notes/compare/v1.0.2...v1.1.0
[v1.0.2]: https://github.com/SecUSo/privacy-friendly-notes/compare/v1.0.1...v1.0.2
[v1.0.1]: https://github.com/SecUSo/privacy-friendly-notes/compare/v1.0...v1.0.1
[v1.0]: https://github.com/SecUSo/privacy-friendly-notes/tree/v1.0

<!-- Generated by https://github.com/rhysd/changelog-from-release v3.8.1 -->
