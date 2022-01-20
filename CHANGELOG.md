# 2.0.13
Anne Onyme 017 (1):
  * [896b575f] Updated French translation using Weblate

Jonas Kalderstam (4):
  * [6ee7f869] Fixed open notification not marking it as read or notified
  * [683da3e0] Fixed images using srcset but no src not showing

Luna Jernberg (1):
  * [7dd75f18] Updated Swedish translation using Weblate

Nikita Epifanov (1):
  * [3b4d66fa] Updated Russian translation using Weblate

Simona Iacob (1):
  * [a69440bd] Updated Romanian translation using Weblate

Tadeáš Erban (1):
  * [9e4460b1] Translated using Weblate (Czech)

Vitor Henrique (1):
  * [11630420] Updated Portuguese (Brazil) translation using Weblate

bruh (1):
  * [60c71749] Updated Vietnamese translation using Weblate

mm4c (1):
  * [9c1befbd] Updated Dutch translation using Weblate

# 2.0.12
Agnieszka C (1):
  * [7daeddf9] Updated Polish translation using Weblate

Allan Nordhøy (1):
  * [ce11cb59] Updated Norwegian Bokmål translation using Weblate

Andrij Mizyk (1):
  * [750a0665] Updated Ukrainian translation using Weblate

El Pirujo (1):
  * [00a98173] Updated Spanish translation using Weblate

Eric (1):
  * [38dcf806] Updated Chinese (Simplified) translation using Weblate

I. Musthafa (1):
  * [a08922ba] Updated Indonesian translation using Weblate

J. Lavoie (1):
  * [3cba889d] Updated Italian translation using Weblate

Jonas Kalderstam (3):
  * [54d33a4f] Changed sync notification icon to a different icon than
         regular notifications
  * [72a3d842] Fixed clicking on notifications not opening article
  * [504d0339] Fixed feed responses being mangled sometimes

Oğuz Ersen (1):
  * [bbe1a9c0] Updated Turkish translation using Weblate

VfBFan (1):
  * [f768b9eb] Updated German translation using Weblate

Vitor Henrique (1):
  * [7d6eb347] Updated Portuguese (Brazil) translation using Weblate

mm4c (1):
  * [71228fb7] Updated Dutch translation using Weblate

# 2.0.11
Jonas Kalderstam (5):
  * [1e4ecf09] Fixed crash when opening app
  * [64b0c705] Fixed a reported crash (rare edge case)
  * [735190fd] Fixed rare crash in case no TextToSpeech engine was installed
  * [cedbb7ea] Fixed UI getting stuck in a weird empty state
  * [8a11ce87] Added some handling in case an open article is deleted

Nikita Epifanov (1):
  * [494e7a56] Updated Russian translation using Weblate

Vitor Henrique (1):
  * [0d968db5] Updated Portuguese (Brazil) translation using Weblate

gutierri (1):
  * [9ec9cf6a] Updated Portuguese (Brazil) translation using Weblate

mm4c (1):
  * [2f4ed127] Updated Dutch translation using Weblate

# 2.0.10
Jonas Kalderstam (2):
  * [6bbabe68] Fixed open in browser opening wrong link
  * [696203ea] Renamed folder to match Play store restrictions

Simona Iacob (1):
  * [b10efbc5] Updated Romanian translation using Weblate

mm4c (1):
  * [1ac2b331] Translated using Weblate (Dutch)

# 2.0.9
Jonas Kalderstam (2):
  * [5c9259cc] Fixed app not respecting what to open articles with
  * [28618bc9] Fixed notifications not dismissing when reading articles

mm4c (1):
  * [b85fd95c] Updated Dutch translation using Weblate

zmni (1):
  * [1600cb79] Updated Indonesian translation using Weblate

# 2.0.8
Agnieszka C (2):
  * [b8857312] Updated Polish translation using Weblate

Andrij Mizyk (2):
  * [7bbed7f1] Updated Ukrainian translation using Weblate

El Pirujo (2):
  * [3bbb9216] Updated Spanish translation using Weblate

Eric (2):
  * [e3a59bbe] Updated Chinese (Simplified) translation using Weblate

J. Lavoie (2):
  * [993a07b8] Updated Italian translation using Weblate

Jonas Kalderstam (7):
  * [f976af75] Fixed sync indicator getting stuck sometimes
  * [3d3da496] Added sync progress indicators on individual feeds in nav
         drawer
  * [bb505183] Reduced minimum feed age for sync to 5 minutes instead of 15
  * [c4228d47] Fixed hardware keyboard support: ENTER now works as expected
  * [df92ffab] Upgraded and added some dependencies
  * [50aa5e47] Improved error message when OPML import/export fails
  * [c993b876] Implemented Tablet only interface

Naveen (1):
  * [2dfed712] Updated Tamil translation using Weblate

Nikita Epifanov (1):
  * [818745b8] Updated Russian translation using Weblate

Oğuz Ersen (2):
  * [23354741] Updated Turkish translation using Weblate

Simona Iacob (1):
  * [1793a4a4] Updated Romanian translation using Weblate

VfBFan (1):
  * [55006182] Updated German translation using Weblate

bruh (2):
  * [200be0ce] Updated Vietnamese translation using Weblate

g (1):
  * [faf4d7d3] Updated Lithuanian translation using Weblate

# 2.0.7
Agnieszka C (1):
  * [eccc086f] Updated Polish translation using Weblate

Andrij Mizyk (1):
  * [5c30def8] Updated Ukrainian translation using Weblate

El Pirujo (1):
  * [17ae3a97] Updated Spanish translation using Weblate

Eric (1):
  * [0449b989] Updated Chinese (Simplified) translation using Weblate

J. Lavoie (1):
  * [87cf87f4] Updated Italian translation using Weblate

Jonas Kalderstam (1):
  * [1001815d] Fixed crash introduced in 2.0.5

Oğuz Ersen (1):
  * [450919e5] Updated Turkish translation using Weblate

VfBFan (1):
  * [4b67ae83] Updated German translation using Weblate

Vitor Henrique (1):
  * [a00177be] Updated Portuguese (Brazil) translation using Weblate

g (1):
  * [0e94edb6] Updated Lithuanian translation using Weblate

zmni (1):
  * [9eb6273d] Updated Indonesian translation using Weblate

# 2.0.6
I. Musthafa (1):
  * [f4801944] Translated using Weblate (Indonesian)

Jonas Kalderstam (10):
  * [60416443] Fixed scroll in Feed being cleared when going back from
         Reader
  * [bf7de231] Fixed translucent navigation bar on Android 23-26
  * [de683726] Fixed reconfiguration of sync when changing sync settings
  * [5af7f79b] Stopped requiring high battery for sync
  * [e7a2f364] Added code to ensure sync is configured on app start
  * [be5d7540] Improved support for background restrictions in Android 12
  * [4193fd74] Improved reliability of notifications
  * [5014172b] Fixed a possible collision with article IDs

Nikita Epifanov (1):
  * [0881dc4a] Updated Russian translation using Weblate

Simona Iacob (1):
  * [289553fa] Updated Romanian translation using Weblate

VfBFan (1):
  * [5b073871] Updated German translation using Weblate

# 2.0.5
Agnieszka C (1):
  * [7bc1efe6] Updated Polish translation using Weblate

Allan Nordhøy (1):
  * [9ce82f0e] Updated Norwegian Bokmål translation using Weblate

Andrij Mizyk (1):
  * [dfb5c4b6] Updated Ukrainian translation using Weblate

El Pirujo (1):
  * [97fde55f] Updated Spanish translation using Weblate

Eric (1):
  * [9bd20bd3] Updated Chinese (Simplified) translation using Weblate

J. Lavoie (1):
  * [2d4e241b] Updated Italian translation using Weblate

Jonas Kalderstam (2):
  * [e83489e2] Added missing string resource
  * [6fcd11d9] Updated Swedish translation using Weblate

Nikita Epifanov (1):
  * [68defb93] Updated Russian translation using Weblate

Oğuz Ersen (1):
  * [fb3b4b82] Updated Turkish translation using Weblate

VfBFan (1):
  * [5f7ae0c7] Updated German translation using Weblate

g (1):
  * [9bfaa162] Updated Lithuanian translation using Weblate

zmni (1):
  * [12554068] Updated Indonesian translation using Weblate

# 2.0.4
Agnieszka C (1):
  * [625596b8] Updated Polish translation using Weblate

Andrij Mizyk (1):
  * [d46b883b] Updated Ukrainian translation using Weblate

El Pirujo (1):
  * [cc83e248] Updated Spanish translation using Weblate

Eric (1):
  * [b83579fa] Updated Chinese (Simplified) translation using Weblate

Gediminas Murauskas (1):
  * [9d469462] Updated Lithuanian translation using Weblate

J. Lavoie (1):
  * [214dff39] Updated Italian translation using Weblate

Jonas Kalderstam (1):
  * [d0486040] Fixed crash with certain tag names

Oğuz Ersen (1):
  * [4bed2875] Updated Turkish translation using Weblate

VfBFan (1):
  * [481dbcd5] Updated German translation using Weblate

# 2.0.3
Allan Nordhøy (4):
  * [6f5418e3] Crowdin integration removed
  * [826a18df] Correct locale for Norwegian Bokmål
  * [871143e8] Reworded some strings
  * [1612b7c2] Updated Norwegian Bokmål translation using Weblate

Jonas Kalderstam (1):
  * [c4048f9f] Added a new feed option to fix feeds with bad ids

Simona Iacob (1):
  * [eaddfe7e] Updated Romanian translation using Weblate

THANOS SIOURDAKIS (1):
  * [99f8e333] Updated Greek translation using Weblate

harisai (1):
  * [28fcb903] Added Telugu translation using Weblate

# 2.0.2
Agnieszka C (1):
  * [2530f074] Updated Polish translation using Weblate

Andrij Mizyk (1):
  * [dc7b2c8f] Translated using Weblate (Ukrainian)

Eric (1):
  * [99457a1a] Updated Chinese (Simplified) translation using Weblate

Felipe Alvarez (2):
  * [aed89f36] Hide FAB when feed is empty
  * [3154cf7a] Swap booleans order

Gediminas Murauskas (1):
  * [0bd119ca] Updated Lithuanian translation using Weblate

J. Lavoie (1):
  * [02c90a84] Updated Italian translation using Weblate

Jonas Kalderstam (11):
  * [0dce0373] Fixed back button not exiting app
  * [47e48e54] Added Compose test for back button exiting the app
  * [bb113d33] Fixed some broken tests
  * [20517a02] Disabled broken sync test
  * [4b3e83f9] Fixed scrolling in Reader not working close to screen edges
  * [7eb2d0f8] Fixed crash if loading very large (50MB+) images
  * [941b3341] Fixed black theme not having true black as background
  * [31851c94] Fixed crash when adding feed with empty title
  * [96d13aef] Fixed Open Items By Default not defauling to system default
  * [a74c8664] Change RSS ID generation again to avoid some duplicates
  * [dca107a1] Fixed links always opening in custom tab

Nikita Epifanov (1):
  * [972a4a75] Updated Russian translation using Weblate

Oğuz Ersen (1):
  * [6b8ec48f] Updated Turkish translation using Weblate

bruh (1):
  * [02d0b027] Updated Vietnamese translation using Weblate

zmni (1):
  * [83b12cb6] Updated Indonesian translation using Weblate

# 2.0.1
Felipe Alvarez (7):
  * [656a276a] Added new dark theme

Jonas Kalderstam (16):
  * [204f854d] Fixed some reported crashes
  * [7324a25c] Fixed possible crashes in semantics
  * [3d21a700] Fixed possible crash reported in play store
  * [81b81aa1] Fixed tags with certain characters not working correctly
  * [63fa3b54] Added a search button to the search screen
  * [c896bba0] Fixed handling of ids in RSS feeds where guid is not unique
  * [8b860ae9] Added share action to long press menu in FeedScreen
  * [c95053ef] Updated swedish translation

Nuno Araújo (1):
  * [d4a8d9ff] Updated Portuguese (Portugal) translation using Weblate

THANOS SIOURDAKIS (1):
  * [e688797f] Updated Greek translation using Weblate

VfBFan (1):
  * [25966dc2] Updated German translation using Weblate

zmni (1):
  * [18fcb357] Updated Indonesian translation using Weblate

# 2.0.0

* UI layer of Feeder has been rewritten in Jetpack Compose
* Improved accessibility
* Added choice of style for articles in list
* Added a playback interface for TextToSpeech
* Translation updates by the community

# 2.0.0-rc.4
Agnieszka C (1):
  * [89ba5aef] Updated Polish translation using Weblate

Andrij Mizyk (1):
  * [38b0167b] Updated Ukrainian translation using Weblate

El Pirujo (1):
  * [34edcbc3] Updated Spanish translation using Weblate

Eric (1):
  * [97085761] Updated Chinese (Simplified) translation using Weblate

Gediminas Murauskas (1):
  * [12b100a1] Updated Lithuanian translation using Weblate

J. Lavoie (2):
  * [f4e87145] Updated German translation using Weblate
  * [a032ba03] Updated Italian translation using Weblate

Jim (1):
  * [8a252911] Updated Chinese (Traditional) translation using Weblate

Jonas Kalderstam (9):
  * [b9a627d4] Use BoxWithConstraints instead of onLayout Callback
  * [f59c5dc5] Increased swipable thresholds to mitigate mistaken swipes
  * [d4138a7a] Suppress some warnings
  * [d0498ff0] Fixed so list scrolls to top after mark above as read
  * [bcfa2e7b] Updated Swedish translation using Weblate
  * [6c96cdc2] Fixed sharing article link
  * [777be597] Fixed customtab/browser not marking articles as read
  * [980af280] Moved SearchFeed to own package
  * [4e87becf] Made UI not so wide on tablets

Nikita Epifanov (1):
  * [9b3e4bbd] Updated Russian translation using Weblate

Oğuz Ersen (1):
  * [3157b54c] Updated Turkish translation using Weblate

Simona Iacob (1):
  * [991f1204] Updated Romanian translation using Weblate

bruh (1):
  * [1e442a3c] Updated Vietnamese translation using Weblate

Éfrit (1):
  * [8780fab4] Translated using Weblate (French)

# 2.0.0-rc.3
Andrij Mizyk (2):
  * [4615622f] Updated Ukrainian translation using Weblate

Jam Jam (2):
  * [e2cd1132] Translated using Weblate (Ukrainian)

Jonas Kalderstam (15):
  * [5b0892ff] Fixed infinite loop issue if for example a notification was
         clicked then back
  * [0b35343c] Fixed app shortcuts not being cleared after delete
  * [5b0badf8] Fixed TTS (and rest of app) not working on Android S
  * [afdca651] Fixed image placeholders
  * [95118fa6] Fixed images in reader view
  * [4669d4d3] Added a new setting: style of articles in list

Mixter (2):
  * [12ce17fd] Updated Chinese (Traditional) translation using Weblate

Simona Iacob (1):
  * [16520c88] Updated Romanian translation using Weblate

VfBFan (1):
  * [575e1dc0] Updated German translation using Weblate

# 2.0.0-rc.2
Agnieszka C (1):
  * [b36db45a] Updated Polish translation using Weblate

El Pirujo (1):
  * [5c5a3c91] Updated Spanish translation using Weblate

Eric (1):
  * [76e9eba5] Updated Chinese (Simplified) translation using Weblate

Gediminas Murauskas (1):
  * [0d802855] Updated Lithuanian translation using Weblate

J. Lavoie (1):
  * [0aa39250] Updated Italian translation using Weblate

Jonas Kalderstam (7):
  * [d94de6a7] Fixed so TextToSpeech is not initialized as part of App
         startup
  * [e717ab31] Added plural forms for n_unread_articles string
  * [fe8faac4] Changed so CI pipeline builds APK with R8 optimizations
  * [d4792743] Slightly increased size of title in list
  * [3e00725f] Fixed so read aloud player is not behind navigation bars
  * [302da01a] Fixed inconsistent behavior with different sort options
  * [214e6660] Updated Swedish translation using Weblate

Oğuz Ersen (1):
  * [03f2be20] Updated Turkish translation using Weblate

bruh (1):
  * [5d3998ec] Updated Vietnamese translation using Weblate

# 2.0.0-rc.1
Jonas Kalderstam (10):
  * [f0e87b8a] Fixed accessibility descriptions
  * [fa632512] Changed to Readability4JExtended for full text parsing
  * [908efc98] Made Feeder very TalkBack compatible
  * [3fda9092] Fixed so Feeder handles rotation gracefully

# 2.0.0-beta.6
Jonas Kalderstam (5):
  * [08760e63] Fixed sync indicator being rendered behind top app bar
  * [92e802b0] Added ability to toggle between full text and included
         article text
  * [e46f91ff] Fixed so scroll to refresh works on the empty screen again
  * [47c40796] Adding some fade in/out animations to empty screen
  * [d1275492] Tweaked some padding in list

# 2.0.0-beta.5
Jonas Kalderstam (6):
  * [0ad40b9e] Fixed incorrect decoding during parsing for some feeds
  * [d0b6ca47] Fixed color of icon in floating action bar to be white
         (again)
  * [fe52639e] Made 'Mark as read' from notification less interuptive
  * [7c24c763] Fixed broken test
  * [78da415a] Feeds are now sorted alphabetically in dialogs
  * [1e2fe647] Added dialog for editing feed when viewing a tag (like for
         delete)

# 2.0.0-beta.4
Jonas Kalderstam (10):
  * [fdb700fa] Reversed expansion icons in navigation drawer to match
         material design
  * [3e6ce929] Fixed youtube thumbnails and made images clickable
  * [56a6e464] Sync on startup if set
  * [b7e74a11] Feed Title clickable in Reader again
  * [f5ecd777] Fixed color of status bar and navigation bar
  * [f975fda2] Fixed toolbar color in custom tab

# 2.0.0-beta.3
Jonas Kalderstam (2):
  * [a97a0257] Fixed some notifications not being cleared when opened
  * [3f8240f3] Fixed feeds not being possible to add after enabling R8

# 2.0.0-beta.2
Jonas Kalderstam (5):
  * [60694836] Renamed Norwegian play store metadata
  * [9c8f5e07] Validate fastlane deployment on 2.0.0 branch
  * [aea58578] Enabled R8 - compose relies heavily on it for performance
  * [3d2aa645] Fixed mapping directive in Fastlane
  * [93b8f4c8] Added beta support to fastlane

# 2.0.0-beta.1

Complete rewrite of the UI in Jetpack Compose

Expect bugs, but all features should be present.

# 1.13.5
Jonas Kalderstam (2):
  * [b9c97797] Changed so Feeder no longer changes the URL of feeds to
         canonical selflink
  * [a01dafc7] Updated Swedish translation using Weblate

Luna Jernberg (1):
  * [850f411d] Updated Swedish translation using Weblate

# 1.13.4
Agnieszka C (2):
  * [098172c1] Updated Polish translation using Weblate
  * [d8455440] Updated Polish translation using Weblate

Gediminas Murauskas (1):
  * [8a3df04f] Translated using Weblate (Lithuanian)

Jonas Kalderstam (1):
  * [b164dece] Added missing title for language lt

PPNplus (1):
  * [18a39e63] Added Thai translation using Weblate

Thien Bui (1):
  * [3d4cd189] Updated Vietnamese translation using Weblate

VfBFan (2):
  * [2ae60832] Translated using Weblate (German)
  * [368fa8ac] Updated German translation using Weblate

Weblate (1):
  * [35327cea] Added Slovenian translation using Weblate

daywalk3r666 (2):
  * [7a22bd67] Updated German translation using Weblate
  * [1ba9b59f] Updated German translation using Weblate

# 1.13.3
Agnieszka C (1):
  * [029f7af4] Updated Polish translation using Weblate

Jonas Kalderstam (5):
  * [199e8bf6] Improved formatting - should be less empty space and newlines

Naveen (1):
  * [eaae183b] Translated using Weblate (Tamil)

Nikhil Kadiyan (1):
  * [7bed6c84] Translated using Weblate (Hindi)

# 1.13.2
Drhaal (1):
  * [c4545c2b] Use different colors when swiping to mark article as
         read/unread

J. Lavoie (1):
  * [9d750135] Updated German translation using Weblate

Jonas Kalderstam (8):
  * [cadaef03] Raised minimum supported version of Android to M (6.0 -
         API23)
  * [df11985f] Added support for TLSv1.3 on older versions of Android
  * [40549eea] Update README.md with ko-fi link

Naveen (1):
  * [b2422d25] Added Tamil translation using Weblate

gutierri (1):
  * [7426f9d0] Updated Portuguese (Brazil) translation using Weblate

# 1.13.1
Axus Wizix (1):
  * [2f4d770f] Updated Russian translation using Weblate

Belmar Begić (1):
  * [c8af81d8] Updated Bosnian translation using Weblate

Jonas Kalderstam (4):
  * [533e92d4] Specified the region of bare Portuguese to Portugal
  * [73e6cddb] Translated using Weblate (Romanian)
  * [2d173196] Fixed dc:creator not showing up as author in RSS feeds
  * [0a2452c5] Updated Czech translation using Weblate

Simona Iacob (1):
  * [ec364392] Updated Romanian translation using Weblate

bruh (1):
  * [8652f087] Translated using Weblate (Vietnamese)

zmni (1):
  * [2b3e17bb] Updated Indonesian translation using Weblate

# 1.13.0
Drhaal (1):
  * [64512d3a] Added option to set article reader on a per feed basis

El Pirujo (1):
  * [966376eb] Updated Spanish translation using Weblate

Eric (1):
  * [fd48664d] Updated Chinese (Simplified) translation using Weblate

J. Lavoie (1):
  * [4995ea75] Updated Italian translation using Weblate

Oğuz Ersen (1):
  * [add0ef71] Updated Turkish translation using Weblate

Simona Iacob (1):
  * [47baaad1] Added Romanian translation using Weblate

VfBFan (1):
  * [e03eabb2] Updated German translation using Weblate

WaldiS (1):
  * [a375332b] Updated Polish translation using Weblate

phlostically (1):
  * [0293dec5] Updated Esperanto translation using Weblate

ssantos (1):
  * [46890329] Translated using Weblate (Portuguese)

zmni (1):
  * [b351658f] Updated Indonesian translation using Weblate

# 1.12.1
Belmar Begić (1):
  * [666f0e3c] Updated Bosnian translation using Weblate

J. Lavoie (1):
  * [cccbf8a7] Updated German translation using Weblate

Jonas Kalderstam (1):
  * [67f53ebc] Prevent fastlane from conflicting on releases

Tomáš Tihlařík (1):
  * [40adb64f] Updated czech strings

VfBFan (1):
  * [92e2a263] Updated German translation using Weblate

cld4h (1):
  * [95e8f6df] Translated using Weblate (Chinese (Simplified))

# 1.12.0
El Pirujo (1):
  * [ddf06c3a] Updated Spanish translation using Weblate

Eric (1):
  * [34e65ed7] Updated Chinese (Simplified) translation using Weblate

Francesco Bonazzi (2):
  * [6d51fd8c] Add support for reading feeds aloud with Android's
         TextToSpeech engine
  * [770ce381] moved text-to-speech code to model-view class

Hierax Swiftwing (1):
  * [c1e336fe] Translated using Weblate (Serbian)

J. Lavoie (1):
  * [d491cd62] Updated Italian translation using Weblate

Jonas Kalderstam (5):
  * [65a1d9b4] Handle dynamic shortcuts for deleted feeds
  * [dccdfa02] Cleaned up TextToSpeech slightly
  * [187d22de] Removed unused imports
  * [af708e46] Updated Swedish translation using Weblate
  * [f67bb187] Added esperanto to list of languages unsupported by play
         store

Nikita Epifanov (1):
  * [e2ffcab3] Updated Russian translation using Weblate

Oğuz Ersen (1):
  * [01d03869] Updated Turkish translation using Weblate

gnu-ewm (2):
  * [1bb598ce] Updated Polish translation using Weblate
  * [94b80f52] Updated Polish translation using Weblate

phlostically (1):
  * [a3302d9f] Updated Esperanto translation using Weblate

vachan-maker (1):
  * [8260c442] Updated Malayalam translation using Weblate

zmni (1):
  * [ddf53f41] Updated Indonesian translation using Weblate

# 1.11.3
Eric (1):
  * [b5a50dfb] Updated Chinese (Simplified) translation using Weblate

Jonas Kalderstam (9):
  * [aa9ebbd3] Maybe fixed a nullpointer error
  * [6023a40c] Fixed sporadic error while loading images
  * [56b7c946] Fixed reader going blank after opening webview and going back
  * [da0d2a9f] Updated view models with correct nullability
  * [078a486c] Fixed additional fragment view lifecycle issues

Nikita Epifanov (1):
  * [d09adafe] Updated Russian translation using Weblate

# 1.11.2
Eric (1):
  * [b5a50dfb] Updated Chinese (Simplified) translation using Weblate

Jonas Kalderstam (5):
  * [130137d3] Fixed database test
  * [aa9ebbd3] Maybe fixed a nullpointer error
  * [6023a40c] Fixed sporadic error while loading images

Nikita Epifanov (1):
  * [d09adafe] Updated Russian translation using Weblate

# 1.11.1
Jonas Kalderstam (2):
  * [130137d3] Fixed database test
  * [aa9ebbd3] Maybe fixed a nullpointer error

Nikita Epifanov (1):
  * [d09adafe] Updated Russian translation using Weblate

# 1.11.0
Allan Nordhøy (2):
  * [4e05cb55] Updated Norwegian Bokmål translation using Weblate

Eduardo (1):
  * [7d23f022] Updated Portuguese (Brazil) translation using Weblate

El Pirujo (2):
  * [4af3761e] Updated Spanish translation using Weblate

J. Lavoie (2):
  * [65079f61] Updated Italian translation using Weblate

Jakub Fabijan (1):
  * [d49aa9e6] Updated Esperanto translation using Weblate

Jonas Kalderstam (7):
  * [074e85ac] Fixed links not opening after screen rotation
  * [2bd413a7] Fixed a leaking service connection
  * [72b26b59] Updated Japanese translation using Weblate
  * [13b0b601] Added full text parsing option using Readability4J
  * [e6a632d9] Updated Swedish translation using Weblate

Oğuz Ersen (2):
  * [e420ef80] Updated Turkish translation using Weblate

Reza Almanda (1):
  * [90cd9031] Updated Indonesian translation using Weblate

Tomáš Tihlařík (1):
  * [ab41bf6e] Update Czech strings.xml

WaldiS (1):
  * [b6ffe8bf] Updated Polish translation using Weblate

zmni (1):
  * [5d763077] Updated Indonesian translation using Weblate

Ícar N. S (1):
  * [d36e6d67] Updated Catalan translation using Weblate

# 1.10.14
Jonas Kalderstam (2):
  * [3777ebfd] Added error reporting when trying to add a feed fails

Meiru (1):
  * [ab59bc7a] Updated Japanese translation using Weblate

kak mi (1):
  * [0215e61c] Updated Chinese (Simplified) translation using Weblate

vachan-maker (1):
  * [1221c6f3] Updated Malayalam translation using Weblate

# 1.10.13
Eduardo Rodrigues (1):
  * [7d01b89f] Translated using Weblate (Portuguese (Brazil))

Jonas Kalderstam (3):
  * [9f191f73] Implemented parallel load of images in Reader view

Meiru (2):
  * [a92e14d8] Updated Japanese translation using Weblate
  * [607a0947] Translated using Weblate (Japanese)

Reza Almanda (1):
  * [253fdd6d] Translated using Weblate (Indonesian)

daywalk3r666 (1):
  * [017de69c] Translated using Weblate (German)

vachan-maker (2):
  * [c71b9210] Updated Malayalam translation using Weblate
  * [9a5a8f2e] Updated Malayalam translation using Weblate

zmni (1):
  * [b59e249b] Update Indonesian translation

Ícar N. S (1):
  * [c67896b7] Updated Catalan translation using Weblate

# 1.10.12
Belmar Begić (1):
  * [07443bf7] Updated Bosnian translation using Weblate

Jakub Fabijan (1):
  * [7b225d25] Updated Esperanto translation using Weblate

Jonas Kalderstam (9):
  * [365bd45c] Removed empty translations

Reza Almanda (1):
  * [b088b923] Updated Indonesian translation using Weblate

# 1.10.11
Allan Nordhøy (2):
  * [c64b4a57] Updated Norwegian Bokmål translation using Weblate
  * [853b7f0a] Translated using Weblate (Norwegian Bokmål)

El Pirujo (1):
  * [18a10d55] Translated using Weblate (Spanish)

George (1):
  * [f317a3ee] Translated using Weblate (Greek)

J. Lavoie (1):
  * [53b67002] Translated using Weblate (Italian)

Jakub Fabijan (1):
  * [a084f837] Added Esperanto translation using Weblate

Jonas Kalderstam (15):
  * [9e9c46f5] Replaced Crowdin widget with Weblate widget
  * [f5739850] Added contribution notes in README
  * [bc00fba3] Updated Russian translation using Weblate
  * [80eca008] Updated Norwegian Bokmål translation using Weblate
  * [fb557d58] Updated Malayalam translation using Weblate

Nikita Epifanov (1):
  * [eb3bbeff] Translated using Weblate (Russian)

Oğuz Ersen (1):
  * [eafe6fb8] Translated using Weblate (Turkish)

Riku Viitanen (2):
  * [78112ea3] Translated using Weblate (Finnish)
  * [6ccbdaa3] Translated using Weblate (Finnish)

WaldiS (1):
  * [d3a99c11] Translated using Weblate (Polish)

vachan-maker (1):
  * [0abb9096] Updated Malayalam translation using Weblate

Ícar N. S (1):
  * [65b7bc99] Translated using Weblate (Catalan)

# 1.10.10
Space Cowboy (3):
  * [b316df06] New translations from Crowdin

# 1.10.9
Jonas Kalderstam (3):
  * [5ac2bc2c] Disabled minification due to crash on old Android
  * [0add4d20] Added comments to some strings

Space Cowboy (5):
  * [7a715fa4] Updated translations from Crowdin

# 1.10.8
Jonas Kalderstam (2):
  * [472dc314] Fixed reader going blank after opening a web view

# 1.10.7
Jonas Kalderstam (22):
  * [27cd9114] Updated translations
  * [5fa9116f] Enabled minification for play and release builds

Muha Aliss (2):
  * [45ac6e09] Turkish translation updated
  * [387db7cd] Turkish translate checked and updated.

mezysinc (4):
  * [6dd67f76] description in ptbr
  * [3de13944] full desc. ptbr
  * [4044ee48] Delete .gitkeep
  * [589ac044] updated strings ptbr

# 1.10.6
Jonas Kalderstam (5):
  * [06f7fb81] Added a scrollbar to the Reader
  * [05078389] Fixed atom feed html content being unescaped twice
  * [e8c5470d] Fixed some additional html escaping cases

# 1.10.5
Armand Lynch (2):
  * [08b6aa7f] Adds 'mark above as read' option
  * [14c07701] Remove code duplication

Enrico Lovisotto (1):
  * [487a250c] Improved Italian translation and added missing items

Jonas Kalderstam (5):
  * [a622d655] Added minification to the app to make it faster to install
  * [3ebd53c3] Fixed scroll position being reset in Reader

Khar Khamal (1):
  * [6663bdf2] Update Spanish strings.xml to add one new string and correct
         other string

# 1.10.4
Jonas Kalderstam (1):
  * [51ef23e6] App is now compiled against Android 11 (SDK-30, R)

# 1.10.3
Jonas Kalderstam (1):
  * [4827e41c] Fixed crash when base64 encoded images were present in feeds

# 1.10.2
Fëdor T (1):
  * [e9787dee] Updated Russian translation

Muha Aliss (1):
  * [2885b218] Turkish translations added

# 1.10.1
Jonas Kalderstam (2):
  * [b9b3bd76] Reworded tooltip to reduce confusion
  * [a4d8dd3b] Increased synchronization speed

Khar Khamal (2):
  * [756cb108] Update strings.xml for Spanish language
  * [ffff95dd] Update strings.xml for Spanish language

aevw (1):
  * [1fd20db4] Updated Portuguese translation

linsui (1):
  * [19b64a13] Update Simplified Chinese translation

# 1.10.0
Jonas Kalderstam (5):
  * [3a3d3689] Added preference for battery optimization
  * [0d17d374] Fixed custom tab not showing as default option for opening
         links
  * [c8b57882] Added option to preload links in custom tab

Khar Khamal (1):
  * [0fdac915] Update Spanish strings.xml for Custom Tab

Sudeep Duggal (1):
  * [2c211b53] Feeder now opts out of sending usage metrics of WebView to
         Google

Tomáš Tihlařík (1):
  * [1709c2fd] Update Czech strings.xml for Custom Tab & Battery options

emersion (1):
  * [e0a9d261] Added support for custom tabs

linsui (1):
  * [751f8665] Update Simplified Chinese translation

zmni (2):
  * [8be9508a] Update Indonesian translation
  * [8bc4f4e6] Update Indonesian translation

# 1.9.9
Jonas Kalderstam (4):
  * [b4827aaa] Fixed text formatting not updating with System night mode

Khar Khamal (1):
  * [37646e21] Fixed typo in Spanish translation

Tomáš Tihlařík (1):
  * [b2cb4a11] Updated Czech translation

aevw (1):
  * [a2ac9334] Added Portuguese (Brazil) translation

# 1.9.8
Jonas Kalderstam (2):
  * [e3245b9c] Added 'mark as unread' to the webview menu

Khar Khamal (1):
  * [d8b09639] Updated Spanish translation

Michael Hynes (1):
  * [7d1e419b] Added an option to disable floating action button.

Sam Clie (1):
  * [bdedadd5] Fixed typo in Chinese translation

# 1.9.7
Tomáš Tihlařík (2):
  * [251ffe8d] Updated Czech translation
  * [8d92b9ac] Updated Czech translation

linsui (1):
  * [a7b65165] Updated Simplified Chinese Translation

# 1.9.6
Jonas Kalderstam (2):
  * [47bc0a5c] Fixed possible crash in case you pressed two feed items at
         once

Khar Khamal (2):
  * [8aa6649e] Updated Spanish translation
  * [2b9180c3] Updated Spanish translation

Ramzan Sheikh (5):
  * [fd7570e7] Modified FeedItemsViewModel to use LiveData for sorting
         preference
  * [d130f0d0] Added reverse sort option to settings menu
  * [cf70edfe] Added sorting option utilities to PrefUtils.kt
  * [61128e4f] Modified FeedItemDao and FeedItemsViewModel to allow listing
         feeds in reverse order
  * [6f002883] Fixed current feed order not changing when sorting setting
         changed

linsui (1):
  * [f9fdb071] Updated Simplified Chinese translation

zmni (1):
  * [4b67ce48] Updated Indonesian translation

# 1.9.5
Philipp Hutterer (1):
  * [9222bf71] Bugfix: decode encoded credentials before request

zmni (1):
  * [60007125] Update Indonesian translation

# 1.9.4
Jonas Kalderstam (9):
  * [6003e84c] Fixed monospacing of pre-tags
  * [fd87f04b] Removed html formatting from titles
  * [6f6ed5ca] Added share option for feeds
  * [bfa1a293] Fixed parsing some feeds with bad server responses

# 1.9.3
Jonas Kalderstam (4):
  * [614597d] Fixed sort order to be the minimum of syncing time and publish date
  * [5596669] Updated share menu to use modern chooser
  * [c59b2f5] Fixed items with no links showing "show in browser" buttons

Tomáš Tihlařík (1):
  * [ada5da8] Updated Czech translation

# 1.9.2
Jonas Kalderstam (2):
  * [d7eeb89] Fixed incorrect titles shown in delete dialog

Karol Kosek (1):
  * [1d64c7f] Updated Polish translation

Khar Khamal (1):
  * [3a3df99] Updated Spanish translation

Vadik Sirekanyan (1):
  * [85fc6d1] Added option for hiding thumbnails

# 1.9.1
  * [72f6d12] Disabled R9 minification to avoid crash on Android Kitkat

# 1.9.0
Jonas Kalderstam (3):
  * [ae1338e] Added additional accessibility strings
  * [25e9602] Added dialog for deleting multiple feeds
  * [da3089e] Changed sort order to include synchronization time

Khar Khamal (1):
  * [ff491f1] Updated Spanish translations

# 1.8.30
  * [49e7f76] Replaced day-night theme with manual control over themes
  * [d54ccbc] Fixed scrollbar ghosting
  * [3614f8a] Added another theme which follows system night mode
  * [4022f62] Save navigation state and restore it
  * [d72d7fa] Increased speed of app and implemented system-follow theme
  * [7df3dc2] Removed conscrypt
  * [e9a6dc3] Updated versions of libraries used

# 1.8.29
*   Added a UserAgent to fix sites blocking requests
    See !214 #248

# 1.8.28

* Fixed a crash when clicking on notification

# 1.8.27

Minor bug fixes and tweaks.

# 1.8.26
*   Stores feed content primarily in files instead of database
    See !209 #227
*   More feeds should now have thumbnails displayed
    See !210 #231

# 1.8.24
*   Fixed alignment issue in RTL layout
    See !208 #224
*   Update Italian (it) translation
    Thanks to Emanuele Petriglia
    See !207

# 1.8.23
*   List should once again remember where you were when you come back
    See !206 #219
*   Spanish translation
    Thanks to Khar Khamal
    See !205

# 1.8.22
*   Update Spanish translations
    Thanks to Khar Khamal
    See !203

# 1.8.21
*   Long press items to open a context menu with various actions
    See !202
*   List will now auto scroll to top when new items are downloaded if list is already at the top
    See !202

# 1.8.20
*   Changed 'Report bug' to open the Gitlab issues page instead of an email
    See !201
*   Updated Czech translation
    Thanks to Tomáš Tihlařík
    See !200 !199

# 1.8.19
*   Added czech translation
    Thanks to Tomas
    See !198
*   Added option to toggle Javascript in Webview
    See !197

# 1.8.18
*   Indonesian translation
    Thanks to zmni
    See !196
*   Fixed back button handling in web view
    See !195

# 1.8.17
*   Made feed title clickable in Reader
    See !194 #205
*   Fixed crash when notification contained items to be marked as read
    See !193 #204

# 1.8.16
*   Fixed a null pointer crash if bare <li> tag was encountered
    See !192

# 1.8.15
*   Improved webview: cookie dialogs should no longer be off screen
    See !190

# 1.8.14
*   Fixed crash on tablets
    See !189 #191
*   Fixed handling of URLs with only user (such as http://user@...)
    See !188

# 1.8.13
*   Fixed edit dialog starting with the wrong theme
    See !187
*   Fixed spelling error in Spanish
    See !185
*   Fixed webview resetting night mode
    See !185 #172
*   Migrated to single activity; app should feel faster
    See !185
*   Fixed thumbnails not showing in Engadget feed
    See !183 #186

# 1.8.12
*   Fixed webview being obscured by the action bar
    See !182 #179 #173
*   Added Spanish translation
    Thanks to Khar Khamal
    See !180

# 1.8.11

Removed "mark as read when scrolling". It had a bug when toggling display of read items, and it was very "surprising" to some users.

Will be back when bug free and off by default.

# 1.8.10
*   Update Simplified Chinese Translation
    Thanks to linsui
    See !179
*   Added option to mark items as read as you scroll (defaults to true)

# 1.8.9
*   Increased http timeouts to 30 seconds from 5 seconds
    See !175
*   Changed so time of publication (and not just date) is shown in Article
    See !174 #61

# 1.8.8
*   Changed plaintext conversion to stop formatting as markdown
    See !172
*   Fixed not being able to parse dates in certain feeds
    See !170
*   Fixed so feeds without publication dates gets some when synced
    See !169 #178

# 1.8.7
*   Added support for RTL
    Some devices might still not render perfectly though
    See !165 #176
*   Fixed youtube previews not showing
    See !168
*   Changed plaintext rendering to not include '[image alt text]' in text
    See !167
*   Changed so that notification actions do not open the app after pressing Back
    See !166

# 1.8.6
*   Fixed notification "Open in"-actions not working
    See !164

# 1.8.5
*   Fixed parsing of feeds without unique guids or links (NixOS)
    See !162
*   Changed so feed search finds alternate links in body of documents
    See !162
*   Fixed feed results not showing error message on *second* search
    See !162
*   Feeder can now be used to *open* links, not just accept *shared* ones
    See !161 #174
*   Fixed notifications so that all actions will mark item as read also
    See !160
*   Fixed app losing state if in reader and switching to another app and back again
    See !159
*   Fixed action bar overlaying web view
    See !157 #173
*   Fixed custom feed titles not being displayed
    See !154 #168 #167
*   Updated Simplified Chinese Translation
    Thanks to linsui
    See !153
*   Fixed feeds with no link not working
    See !150 #165
*   Fixed some parsing errors on feeds with slash-comments
    See #166

# 1.8.4
*   Fixed long blog title overlapping date
    See !149 #164
*   Fixed crash when loading certain videos
    See !148 #163
*   Fixed opening in browser from notification not marking as read or dismissing
    See !146 #155

# 1.8.3
*   Tweaked colors in themes
    See !144 #159
*   Fixed crash when loading bad images
*   Fixed scrolling position getting reset during sync in Reader
    See !142 #160
*   Fixed crash when loading bad images
    See !140
*   Fixed theme-specific place holder image for articles
    See !139

# 1.8.2
*   Fixed crash when image could not be loaded on pre Lollipop
    See !138 #156
*   Added menu item for sending a bug report via email
    See !137

# 1.8.1
*   Fixed crash when clearing notifications
    See !136 #153
*   Update Simplified Chinese
    Thanks to linsui
    See !134
*   Fixed screenshots in README
    Thanks to DJCrashdummy
    See !135

# 1.8.0
*   Removed option to sync on Hotspots
    Fixed automatic synchronization never running on mobile data
    Added option to sync when app is opened
    Improved caching so less data traffic will be used during sync
    Improved sync speed by only parsing feeds with new content
    See !131
*   Improved error handling in Add Feed dialog
    See !132
*   Simplified Chinese Translation
    Thanks to linsui
    See !128

# 1.7.1
*   Fixed possible crash when marking all items as read
    See !127 #145
*   Fixed text for show unread toggle
    See !125

# 1.7.0
*   Moved notification toggle to options menu
    See !123 #125 #66
*   Added a light theme
    See !122 #38
*   Fixed size of FAB icon on high density screens
    See !119
*   Fixed crash for certain feeds with slash comment meta-data
    See !117 #140
*   Added additional sync frequency options (15min and 30min)
    Also removed the need for an account and related system permission
    See #49
*   Added menu option in reader to mark item as unread
    See !111 #134

# 1.6.8
*   Fixed crash when supplying bad URL to add feed dialog
    See !110 #137
*   Fix typo in German translation
    Thanks to Swen Krüger
    See !109

# 1.6.7
*   Fixed crash on older Android versions when opening a web view
    See !108
*   Fixed update of views when pressing 'mark all as read' button
    See !107
*   Improved network caching
    See !105
*   German translations updated and added
    Thanks to Chris
    See !106

# 1.6.6

- Fixed a crash in Reader

# 1.6.5
*   Added support for username/password in URLs
    See !100 #128
*   Fixed https compatibility on older versions of Android
    See !102 #113
*   Fixed crash for HorribleSubs.info
    See !103 #131

# 1.6.4
*   Added paging to lists
    See !99
*   Added option for maximum number of items per feed
    See !98 #126

# 1.6.3
*   Now all links are explicitly opened in new browser tabs
    See !97 #117
*   Fixed buggy back stack
    See !96

# 1.6.2
*   Block cookies from webview  
    See !95

# 1.6.1
*   Fixed parsing of some OPML formats  
    See !94 #111

# 1.6.0
*   Added option of how to open articles.
    One of Reader, WebView or Browser.
    See !93 #39 #102
*   Fixed resolution of relative links
    See !92 #101

# 1.5.0
*   Fixed notifications  
    See !91 #10 #88
*   Changed to allow installation on internal storage  
    This has always been implied by the limitations of Android but now
    it is explicit to avoid issues for people who try to move it to
    external storage.
    See !78 #79
*   Added special handling for finding Youtube feeds  
    See !90 #100
*   Fixed HTML encoded titles not being decoded in list
    See !89 #91
*   Changed so more feeds display thumbnail images  
    See !88 #96
*   Fixed various crashes

# 1.4.3
*   Fixed crash for missing video urls  
    See !84 #90
*   Improved UI responsiveness but throttling database loaders
    See !81
*   Fixed existing tag not being shown in edit feed dialog  
    See !80 #82
*   Improved rendering of <pre> tags  
    See !77
*   Added newline between table columns  
    See !77
*   Handle ENTER press in add feed dialog  
    See !77

# 1.4.2
*   Stopped rendering script tags
    See !75 #85

# 1.4.1
*   Fixed some translation issues which could cause crashes  
    See !74
*   Added French translation  
    Thanks to Jef Roelandt  
    See !73
*   Added Polish translation  
    Thanks to Grzegorz Szymaszek  
    See !72

# 1.4.0

This version changes the database tables slighly which means your
read-status will be gone. Apologies for the inconvenience.

*   Feeds are now sorted case-insensitively  
    See !71 #77
*   Feeds are now displayed using correct encoding  
    See !68 #76
*   Articles are parsed to find cover images  
    See !67
*   Relative links are now resolved  
    See !67
*   Adding feeds will now parse the page in case it's not a feed and try
    to find alternate links to feeds. All results are displayed in the
    dialog.  
    See !67
*   Maintain scroll position in articles when switching between apps  
    See !66 #71
*   Images with relative URLs are now displayed  
    See !66 #37 #54
*   Added app shortcuts for the latest 3 feeds  
    See !65 #60
*   Added option to sync once per day  
    See !64
*   New icons  
    See !63
*   Added support for JSONFeed  
    See !41

# 1.3.15
*   Fixed an installation crash on Android 5  
    See !62 #69

# 1.3.14
*   Fixed loss of scroll position on redraw in left drawer menu  
    See !61 #57

# 1.3.13

*   Add new feed now finds feed links in web pages

    Makes it possible to input a url to a site, such as
    `cowboyprogrammer.org`, when adding a new feed.

    Previously, the direct address to the RSS/Atom feed was required
    (`cowboyprogrammer.org/atom.xml`). This was not ideal because

    - not all sites advertise a link to their feeds

    - the location of the feed is not standardized so it's not easily
      guessable

    - viewing the source of a web site to find the alternate link is
      very hard to do on mobile

    Now, the site you enter is parsed and if it contains alternate
    links to feeds, one of those links are loaded parsed
    instead. Currently RSS and Atom feeds are identified and Atom is
    preferred over RSS.

    See !60

*   Target Android 26  
    See !60

# 1.3.12
*   Changed so that an empty feed can be dragged to be refreshed  
    See !57 #40

# 1.3.11
*   Added Italian translation  
    Thanks to Marco  
    See !56

# 1.3.10
*   Fixed crash when toggling 'Notify for new items' on All Feeds  
    See !55 #56

# 1.3.9
*   Fixed visibility of notify icon on certain devices  
    See !53 #55

# 1.3.8
*   Fixed crash on older versions of Android  
    See !51 #53

# 1.3.7
*   Added a show all option in the sidebar  
    See !50 #50

# 1.3.6
*   Fixed crash when importing/exporting OPML on Android18  
    See !49 #51
*   Updated russian translation  
    Thanks to Anton Shestakov  
    See !48

# 1.3.5
*   Add tests for contributed OMPL files  
    See !47 #36
*   Move OMPL test to correct package  
    See !47
*   Handle case when cursor is null  
    See !47
*   Changed to 'Updated feeds' instead of 'New RSS-items'  
    See !46
*   Fixed OPML importing  
    See !46
*   Fixed OPML exporting  
    See !46
*   Improved performance of list by not loading full text of items  
    See !46 #48
*   Fixed crash if item had too much text  
    See !46 #48
*   Reduced size of some text to contain german translation  
    See !45 #46
*   New german translations courtesy of @dehnhard  
    See !45

# 1.3.4
*   Removed translations of dummy strings  
    See !43 #44
*   Added russian translation  
    Thanks to Anton Shestakov  
    See !42
*   Fixed sorting of feeds to be alphabetical  
    See !38 #41

# 1.3.3
*   Update feed items if they exist instead of effectively ignoring them  
    See !36 #33
*   Fallback to feed author if entry author is empty  
    See !36 #31
*   Update UI after each feed is synced instead of all at the end  
    See !36
*   Don't crash when column doesn't exist  
    See !35
*   Catch no such activity exceptions  
    See !34 #35
*   Don't print style tags in articles  
    See !33 #32
*   Don't print so many newlines in preview snippets  
    See !33
*   Don't render markdown links in plaintext snippets  
    See !32 #30

# 1.3.2
*   Fix OPML export  
    See !27
*   Add missing permission for SDK23 and below  
    See !30 #28
*   Make read story title even more readable

# 1.3.1
*   Make read story title even more readable  
    See !28
