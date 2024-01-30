# Changelog

<a name="v1.4.5"></a>
## [Notes (Privacy Friendly) v1.4.5](https://github.com/SecUSo/privacy-friendly-notes/releases/tag/v1.4.5) - 11 Jan 2024

## What's Changed
* Fix export permissions for API >= 33 by [@udenr](https://github.com/udenr) in https://github.com/SecUSo/privacy-friendly-notes/pull/158

**Full Changelog**: https://github.com/SecUSo/privacy-friendly-notes/compare/v1.4.4...v1.4.5

[Changes][v1.4.5]


<a name="v1.4.4"></a>
## [Notes (Privacy Friendly) v1.4.4](https://github.com/SecUSo/privacy-friendly-notes/releases/tag/v1.4.4) - 31 Oct 2023

## What's Changed
* Update to BackupAPI v1.2.0 by [@udenr](https://github.com/udenr) in https://github.com/SecUSo/privacy-friendly-notes/pull/147
* Add missing preference key to BackupRestorer.java by [@udenr](https://github.com/udenr) in https://github.com/SecUSo/privacy-friendly-notes/pull/146
* Fix notifications by [@udenr](https://github.com/udenr) in https://github.com/SecUSo/privacy-friendly-notes/pull/151
* Update dependencies and target sdk by [@udenr](https://github.com/udenr) in https://github.com/SecUSo/privacy-friendly-notes/pull/152


**Full Changelog**: https://github.com/SecUSo/privacy-friendly-notes/compare/v1.4.2...v1.4.4

[Changes][v1.4.4]


<a name="v1.4.2"></a>
## [Notes (Privacy Friendly) v1.4.2](https://github.com/SecUSo/privacy-friendly-notes/releases/tag/v1.4.2) - 09 May 2023

## What's Changed
* Fixes: [#132](https://github.com/SecUSo/privacy-friendly-notes/issues/132) by [@coderPaddyS](https://github.com/coderPaddyS) in https://github.com/SecUSo/privacy-friendly-notes/pull/135
* Fixes: [#133](https://github.com/SecUSo/privacy-friendly-notes/issues/133) by [@coderPaddyS](https://github.com/coderPaddyS) in https://github.com/SecUSo/privacy-friendly-notes/pull/136
* v1.4.2 Bugfix sharing notes by [@coderPaddyS](https://github.com/coderPaddyS) in https://github.com/SecUSo/privacy-friendly-notes/pull/145


**Full Changelog**: https://github.com/SecUSo/privacy-friendly-notes/compare/v1.4.1...v1.4.2

[Changes][v1.4.2]


<a name="v1.4.1"></a>
## [Notes (Privacy Friendly) v1.4.1](https://github.com/SecUSo/privacy-friendly-notes/releases/tag/v1.4.1) - 02 Dec 2022

## What's Changed
* Fixed the save button to function the same as the back action by [@coderPaddyS](https://github.com/coderPaddyS) in https://github.com/SecUSo/privacy-friendly-notes/pull/128
* Fixed the NPE in noteFromIntent caused by empty extras. by [@coderPaddyS](https://github.com/coderPaddyS) in https://github.com/SecUSo/privacy-friendly-notes/pull/129


**Full Changelog**: https://github.com/SecUSo/privacy-friendly-notes/compare/v1.4.0...v1.4.1

[Changes][v1.4.1]


<a name="v1.4.0"></a>
## [Notes (Privacy Friendly) v1.4.0](https://github.com/SecUSo/privacy-friendly-notes/releases/tag/v1.4.0) - 21 Nov 2022

## What's Changed
* Refactored MainActivityViewModel to use Flows instead of LiveData. by [@coderPaddyS](https://github.com/coderPaddyS) in https://github.com/SecUSo/privacy-friendly-notes/pull/123
* Overhaul Note UI and Bugfixes by [@coderPaddyS](https://github.com/coderPaddyS) in https://github.com/SecUSo/privacy-friendly-notes/pull/124

## New Contributors
* [@coderPaddyS](https://github.com/coderPaddyS) made their first contribution in https://github.com/SecUSo/privacy-friendly-notes/pull/123

**Full Changelog**: https://github.com/SecUSo/privacy-friendly-notes/compare/v1.3.0...v1.4.0

[Changes][v1.4.0]


<a name="v1.3.0"></a>
## [Notes (Privacy Friendly) v1.3.0](https://github.com/SecUSo/privacy-friendly-notes/releases/tag/v1.3.0) - 23 Aug 2022

## What's Changed
* fix sql constraint exception that would occur when a note had a null category by [@Kamuno](https://github.com/Kamuno) in https://github.com/SecUSo/privacy-friendly-notes/pull/114
* fix backup import error by [@Kamuno](https://github.com/Kamuno) and [@udenr](https://github.com/udenr) in https://github.com/SecUSo/privacy-friendly-notes/pull/115
* add utility methods to get the correct database version on backup creation by [@Kamuno](https://github.com/Kamuno) in https://github.com/SecUSo/privacy-friendly-notes/pull/116
* Added a setting to enable/disable note previews by [@Kamuno](https://github.com/Kamuno) in https://github.com/SecUSo/privacy-friendly-notes/pull/117


**Full Changelog**: https://github.com/SecUSo/privacy-friendly-notes/compare/v1.2.5...v1.3.0

[Changes][v1.3.0]


<a name="v1.2.5"></a>
## [Notes (Privacy Friendly) v1.2.5](https://github.com/SecUSo/privacy-friendly-notes/releases/tag/v1.2.5) - 08 Aug 2022

## What's Changed
* Allow main thread queries for now by [@Kamuno](https://github.com/Kamuno) in https://github.com/SecUSo/privacy-friendly-notes/pull/111


**Full Changelog**: https://github.com/SecUSo/privacy-friendly-notes/compare/v1.2.4...v1.2.5

[Changes][v1.2.5]


<a name="v1.2.4"></a>
## [Notes (Privacy Friendly) v1.2.4](https://github.com/SecUSo/privacy-friendly-notes/releases/tag/v1.2.4) - 08 Aug 2022

## What's Changed
* Fix database access from main thread crash in BootReceiver by [@udenr](https://github.com/udenr) in https://github.com/SecUSo/privacy-friendly-notes/pull/110


**Full Changelog**: https://github.com/SecUSo/privacy-friendly-notes/compare/v1.2.3...v1.2.4

[Changes][v1.2.4]


<a name="v1.2.3"></a>
## [Notes (Privacy Friendly) v1.2.3](https://github.com/SecUSo/privacy-friendly-notes/releases/tag/v1.2.3) - 06 Aug 2022

## What's Changed
* Fixed class cast exception, pending intent flags, missing permission by [@Kamuno](https://github.com/Kamuno) in https://github.com/SecUSo/privacy-friendly-notes/pull/103
* Update Icons and fastlane app icon by [@Kamuno](https://github.com/Kamuno) in https://github.com/SecUSo/privacy-friendly-notes/pull/104
* Fix version number in the about activity not being updated automatically by [@udenr](https://github.com/udenr) in https://github.com/SecUSo/privacy-friendly-notes/pull/107
* Fix notifications by [@udenr](https://github.com/udenr) in https://github.com/SecUSo/privacy-friendly-notes/pull/106

## New Contributors
* [@udenr](https://github.com/udenr) made their first contribution in https://github.com/SecUSo/privacy-friendly-notes/pull/107

**Full Changelog**: https://github.com/SecUSo/privacy-friendly-notes/compare/v1.2.2...v1.2.3

[Changes][v1.2.3]


<a name="v1.2.2"></a>
## [Notes (Privacy Friendly) v1.2.2](https://github.com/SecUSo/privacy-friendly-notes/releases/tag/v1.2.2) - 03 Aug 2022

## What's Changed
* Fixed crash that would occur more than one text note was saved by [@Kamuno](https://github.com/Kamuno)


**Full Changelog**: https://github.com/SecUSo/privacy-friendly-notes/compare/v1.2.1...v1.2.2

[Changes][v1.2.2]


<a name="v1.2.1"></a>
## [Notes (Privacy Friendly) v1.2.1](https://github.com/SecUSo/privacy-friendly-notes/releases/tag/v1.2.1) - 03 Aug 2022

## What's Changed
* Fixed crash that would occur when no text notes are saved by [@Kamuno](https://github.com/Kamuno) in https://github.com/SecUSo/privacy-friendly-notes/pull/100


**Full Changelog**: https://github.com/SecUSo/privacy-friendly-notes/compare/v1.2.0...v1.2.1

[Changes][v1.2.1]


<a name="v1.2.0"></a>
## [Notes (Privacy Friendly) v1.2.0](https://github.com/SecUSo/privacy-friendly-notes/releases/tag/v1.2.0) - 03 Aug 2022

Notes (Privacy Friendly) v1.2.0

## What's Changed
* Added Support for the Backup App from F-Droid [@Kamuno](https://github.com/Kamuno) 
* Updated Backup API to v1.0.0 [@Kamuno](https://github.com/Kamuno) 

**Full Changelog**: https://github.com/SecUSo/privacy-friendly-notes/compare/v1.1.1...v1.2.0

[Changes][v1.2.0]


<a name="v1.1.1"></a>
## [Notes (Privacy Friendly) v1.1.1](https://github.com/SecUSo/privacy-friendly-notes/releases/tag/v1.1.1) - 31 Jul 2022

Notes (Privacy Friendly) v1.1.1

## What's Changed
* Fixed migration for text notes [@Kamuno](https://github.com/Kamuno) 
* Fixed sharing of notes [@Kamuno](https://github.com/Kamuno) 

**Full Changelog**: https://github.com/SecUSo/privacy-friendly-notes/compare/v1.1.0...v1.1.1

[Changes][v1.1.1]


<a name="v1.1.0"></a>
## [Notes (Privacy Friendly) v1.1.0 ** BROKEN RELEASE **](https://github.com/SecUSo/privacy-friendly-notes/releases/tag/v1.1.0) - 25 Jul 2022

## What's Changed
* Lab WS22 + Backup API update by [@Kamuno](https://github.com/Kamuno) and [@maxmitz](https://github.com/maxmitz) in https://github.com/SecUSo/privacy-friendly-notes/pull/95
* Update to correct backup api version. by [@Kamuno](https://github.com/Kamuno) in https://github.com/SecUSo/privacy-friendly-notes/pull/96

- Database switched to Room
- Switched Note Overview to RecyclerView
- Added Backup API integration
- Added a search function
- Added color black to the color palette
- Added simple text format options
- Improved sharing of notes


**Full Changelog**: https://github.com/SecUSo/privacy-friendly-notes/compare/v1.0.2...v1.1.0

[Changes][v1.1.0]


<a name="v1.0.2"></a>
## [Notes (Privacy Friendly) v1.0.2](https://github.com/SecUSo/privacy-friendly-notes/releases/tag/v1.0.2) - 27 Jan 2021

Added api to backup application.
Added adaptive icon

[Changes][v1.0.2]


<a name="v1.0.1"></a>
## [Privacy Friendly Notes v1.0.1](https://github.com/SecUSo/privacy-friendly-notes/releases/tag/v1.0.1) - 12 Dec 2016

- Japanese Translation
- Export Icon Update


[Changes][v1.0.1]


<a name="v1.0"></a>
## [Privacy Friendly Notes v1.0](https://github.com/SecUSo/privacy-friendly-notes/releases/tag/v1.0) - 24 Nov 2016

Privacy Friendly Notes is an Android application for the creation and management of notes. It is also possible to can define categories and assign them to the notes. Notes can have one of these types: 
­- simple text notes 
­- check-list notes 
­- audio notes 
­- sketch notes 

This app belongs to the Privacy Friendly Apps group developed by the research group SECUSO at Technische Universität Darmstadt, Germany.


[Changes][v1.0]


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

<!-- Generated by https://github.com/rhysd/changelog-from-release v3.7.1 -->
