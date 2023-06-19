# 2.4.17
Jonas Kalderstam (10):
  * [71835f81] Fixed possible crash during article parsing
  * [36f08f92] Fixed charset detection for sites not using UTF-8
  * [4b2c4bd3] Fixed lineheight not scaling with text size
  * [f3d1b0f5] Improved keyboard navigation through the app
  * [dbd9af16] Added suitable dimensions for TVs
  * [b7501f87] Changed how number of columns in grid layout is calculated
  * [032b77ae] Added some special UI handling for Foldable devices
  * [c0578304] Adjusted width of reader on large screens
  * [967797dd] Fixed some code deprecations and such
  * [7103a0fc] Temporarily disabled mark as read on scroll in Grid because
         it just doesn't work well enough

Patrik Daniel (1):
  * [bbd66edc] Updated Finnish translation using Weblate

S3aBreeze (1):
  * [ea412aa2] Updated Russian translation using Weblate

mm4c (1):
  * [16ce2cb1] Updated Dutch translation using Weblate

# 2.4.16
Jonas Kalderstam (11):
  * [3493e09b] Added global notifications setting as an alternative way to
         toggle feed notifications
  * [6d11e165] Changed block list setting to have dynamic size
  * [7d4e275e] Cleaned up some code
  * [cce0be9b] Added an entry in the nav drawer to easily access bookmarked
         articles
  * [fd0577d4] Fixed mark as read after/before
  * [502c16cd] Removed ability to pin articles
  * [a3630e7a] Renamed "bookmark" to "save article"
  * [b903a8f5] Ensured old pinned articles becomes saved articles when
         upgrading
  * [e390d229] Changed so Feeder will try and parse responses from sites
         even if the mimetype is wrong
  * [06179eab] Added option to mark as read while scrolling
  * [835329dc] Changed mark as read on scroll delay to 800ms down from
         1000ms

Agnieszka C (2):
  * [0368c1ed] Updated Polish translation using Weblate
  * [cec966ba] Updated Polish translation using Weblate

Alexthegib (1):
  * [052eeb59] Updated Portuguese (Portugal) translation using Weblate

Dan (2):
  * [8ac1d5f0] Updated Ukrainian translation using Weblate
  * [810d0766] Updated Ukrainian translation using Weblate

ERYpTION (2):
  * [4910290a] Updated Danish translation using Weblate
  * [50490d02] Updated Danish translation using Weblate

El Pirujo (2):
  * [b62198f8] Updated Spanish translation using Weblate
  * [ae153e5b] Updated Spanish translation using Weblate

Eric (2):
  * [f4bee166] Updated Chinese (Simplified) translation using Weblate
  * [b660084f] Updated Chinese (Simplified) translation using Weblate

J. Lavoie (1):
  * [5fa9186e] Updated Italian translation using Weblate

Nicola Masarone (1):
  * [7807be16] Updated Italian translation using Weblate

Oğuz Ersen (1):
  * [81db851a] Updated Turkish translation using Weblate

ROCK TAKEY (1):
  * [fd41e07c] Updated Japanese translation using Weblate

Retrial (1):
  * [a533ebf8] Updated Greek translation using Weblate

VfBFan (2):
  * [18901a75] Updated German translation using Weblate
  * [c56fb097] Updated German translation using Weblate

eevan78 (1):
  * [37648ec4] Translated using Weblate (Serbian)

pchelium (3):
  * [e64914ef] Updated Czech translation using Weblate
  * [6dafba63] Updated Czech translation using Weblate
  * [546751ab] Updated Czech translation using Weblate

zmni (2):
  * [936ee470] Updated Indonesian translation using Weblate
  * [647040b6] Updated Indonesian translation using Weblate

# 2.4.15
Jonas Kalderstam (15):
  * [6838e46a] Updated versions
  * [8cd2453a] Changed to official upsert
  * [aa4c7362] Changed to new and safer flow collection with lifecycle
         awareness
  * [233afed4] Updated so text should appear more balanced with line breaks
         and hyphenation
  * [5077ad25] Added tooltips to all icon buttons on long-press
  * [4c7ab111] Changed so zeros aren't shown in nav drawer
  * [ab30b452] Changed so New-indicator is only shown if read items would be
         shown
  * [48c8b1e0] Changed so pressing Back will close the nav drawer if it is
         open
  * [792bdd93] Changed placeholder images to be easier on the eyes
  * [3c83228e] Fixed too many image captions when image was wrapped in
         figure
  * [586d6735] Changed so image captions are not included in TextToSpeech
  * [6c4f6f14] Improved article layout with spacing and image captions
  * [5fd8c853] Improved table rendering in article view
  * [e23ecb47] Improved reader screen performance
  * [6bf00a09] Fixed display of nested figures in reader

# 2.4.14
Jonas Kalderstam (4):
  * [9251378d] Added TW title
  * [f6bd778e] Updated UserAgent to explain what the app is for server
         owners
  * [d13def0e] Fixed crash when searching for strange URLs
  * [63c0da7a] Fixed rare crash in reader

Subham Jena (1):
  * [5e89dfc2] Updated Odia translation using Weblate

yangyangdaji (1):
  * [f86db9ca] Translated using Weblate (Chinese (Traditional))

# 2.4.13
Hotarun (1):
  * [a25e1cbb] Updated Russian translation using Weblate

Jonas Kalderstam (9):
  * [9f8dd7e4] Updated versions and enabled gradle configuration cache
  * [34843b8e] Syncing will now scroll list to top so new items are
         immediately visible
  * [3016f72c] Fixed send bug report to open email client instead of GitLab
  * [10c661ad] Added check for notification permission before trying to
         notify
  * [d58e2955] Improved build performance
  * [bca3f845] Fixed screen getting offset when increasing display size on
         device
  * [cf81e5e5] Fixed so release script can generate config locales
  * [2854679f] Tweaked release script

# 2.4.12
Belmar Begić (1):
  * [9157af8f] Updated Bosnian translation using Weblate

Jonas Kalderstam (4):
  * [43518971] Fixed couldn't add a feed with unknown protocols in links
  * [c83e7054] Improved link handling
  * [ca3a977b] Some cleanup

Raman (1):
  * [043f5da1] Updated Malayalam translation using Weblate

Zayed Al-Saidi (1):
  * [95e0e8c0] Translated using Weblate (Arabic)

fincent (1):
  * [b502d1ff] Updated Dutch translation using Weblate

# 2.4.11
Juraj Liso (1):
  * [61a263f3] Added Slovak translation using Weblate

Parsa (1):
  * [1f07f778] Translated using Weblate (Persian)

Zayed Al-Saidi (1):
  * [3b48b9b8] Translated using Weblate (Arabic)

# 2.4.10
Jonas Kalderstam (3):
  * [c2010a4f] Disabled emulator tests on github
  * [24a024f5] Upgraded kotlin, compose compiler and compose BOM

S-H-Y-A (1):
  * [6a786c1f] Translated using Weblate (Japanese)

# 2.4.9
Aitor Salaberria (1):
  * [3d295d48] Updated Basque translation using Weblate

Jonas Kalderstam (8):
  * [cfe0511a] Fixed all lint errors

VfBFan (1):
  * [dbfa1c90] Translated using Weblate (German)

jc (1):
  * [76b24997] Translated using Weblate (Portuguese (Portugal))

zmni (1):
  * [6a822bb8] Updated Indonesian translation using Weblate

# 2.4.8
Carles Muñoz Gorriz (1):
  * [be63937d] Updated Catalan translation using Weblate

Felix Otto (1):
  * [b14eb758] adjust URLs of screenshots

Gabriel Camargo (1):
  * [da170316] Updated Portuguese (Brazil) translation using Weblate

Jonas Kalderstam (2):
  * [736935e5] Fixed articles marking themselves as unread when toggling
         view unread
  * [cac7d5e8] Fixed trailing commas

bowornsin (1):
  * [d070511f] Updated Thai translation using Weblate

gallegonovato (1):
  * [a3211c77] Updated Galician translation using Weblate

mm4c (1):
  * [c45efb88] Updated Dutch translation using Weblate

# 2.4.7
Agnieszka C (1):
  * [173a35ca] Updated Polish translation using Weblate

Dan (1):
  * [4c60bc5a] Updated Ukrainian translation using Weblate

ERYpTION (2):
  * [0bbf5ef0] Updated Danish translation using Weblate
  * [0881c3d0] Updated Danish translation using Weblate

El Pirujo (1):
  * [f0865ca1] Updated Spanish translation using Weblate

Eric (2):
  * [8e4583c0] Updated Chinese (Simplified) translation using Weblate
  * [3fe875ef] Updated Chinese (Simplified) translation using Weblate

J. Lavoie (1):
  * [50a1550c] Updated Italian translation using Weblate

Jonas Kalderstam (13):
  * [01cce9a0] Tweaked release script
  * [5fdb22d9] Added stricter ContentType restrictions on responses
  * [2a3e3df1] Changed so full text articles are are not retried
         automatically
  * [2caa0032] Moved all article data to cacheDir instead of some in
         filesDir
  * [b69b4a0e] Fixed some issues related to block list
  * [c4aa6f97] Fixed english translation
  * [08408ca0] Fixed english translation
  * [84d781db] Fixed bug where backstack would get stacked with multiple
         feeds
  * [8de0b6f2] Fixed feed navigation
  * [c24a2c77] Really fixed feed navigation
  * [d0cfbb42] Refactored to follow guidelines. Top to bottom reached empty
  * [ee3c027d] Fixed navigation properly
  * [fbd47e77] Updated Swedish translation using Weblate

Oğuz Ersen (2):
  * [5c04dc46] Updated Turkish translation using Weblate
  * [510a76aa] Updated Turkish translation using Weblate

Retrial (2):
  * [5b1b6ad4] Updated Greek translation using Weblate
  * [598cc6a3] Updated Greek translation using Weblate

Simona Iacob (1):
  * [ecdfb3f3] Updated Romanian translation using Weblate

Skrripy (1):
  * [db0fa1b2] Updated Ukrainian translation using Weblate

Space Cowboy (5):
  * [7a5074b4] Merge branch 'weblate-feeder-android-strings' into 'master'
  * [3e92bc4a] Merge branch 'fix-data-consumption' into 'master'
  * [d8c81995] Merge branch 'fix-glob-insert' into 'master'
  * [5b0ae748] Merge branch 'weblate-feeder-android-strings' into 'master'
  * [2464acaa] Merge branch 'guideliens' into 'master'

VfBFan (2):
  * [d3e91290] Updated German translation using Weblate
  * [c73a4c2a] Updated German translation using Weblate

gallegonovato (1):
  * [c16eac5a] Updated Galician translation using Weblate

zmni (1):
  * [163f27ae] Updated Indonesian translation using Weblate

# 2.4.6
Aitor (2):
  * [69989627] Translated using Weblate (Basque)
  * [21629a41] Updated Basque translation using Weblate

Hur Ezeiza Zaldua (1):
  * [af3ff07b] Updated Basque translation using Weblate

Jonas Kalderstam (2):
  * [a87da794] Try to ignore if conscrypt insertion fails
  * [8adb4114] Updated UserAgent to avoid some issues with anti-spam
  * [9ddc1a14] Fixed unit test

Retrial (1):
  * [e176df4d] Translated using Weblate (Greek)

Skrripy (1):
  * [05f2f077] Translated using Weblate
  

# 2.4.6
Aitor (2):
  * [69989627] Translated using Weblate (Basque)
  * [21629a41] Updated Basque translation using Weblate

Hur Ezeiza Zaldua (1):
  * [af3ff07b] Updated Basque translation using Weblate

Jonas Kalderstam (2):
  * [a87da794] Try to ignore if conscrypt insertion fails
  * [8adb4114] Updated UserAgent to avoid some issues with anti-spam
  * [9ddc1a14] Fixed unit test

Retrial (1):
  * [e176df4d] Translated using Weblate (Greek)

Skrripy (1):
  * [05f2f077] Translated using Weblate (Ukrainian)

# 2.4.6
Aitor (2):
  * [69989627] Translated using Weblate (Basque)
  * [21629a41] Updated Basque translation using Weblate

Hur Ezeiza Zaldua (1):
  * [af3ff07b] Updated Basque translation using Weblate

Jonas Kalderstam (2):
  * [a87da794] Try to ignore if conscrypt insertion fails
  * [8adb4114] Updated UserAgent to avoid some issues with anti-spam

Retrial (1):
  * [e176df4d] Translated using Weblate (Greek)

Skrripy (1):
  * [05f2f077] Translated using Weblate (Ukrainian)

VfBFan (1):
  * [2b1382df] Translated using Weblate (German)

Vitor Henrique (1):
  * [b34335b1] Updated Portuguese (Brazil) translation using Weblate

slothtown (1):
  * [5e000306] Fixed typo

wackbyte (1):
  * [1e4572c7] Translated using Weblate (Toki Pona)

Ícar N. S (1):
  * [dabcdbd2] Updated Catalan translation using Weblate

# 2.4.5
Jonas Kalderstam (3):
  * [7a26e00d] Fixed crash when sharing link to Feeder
  * [1ce1f6b0] Upgraded some versions
  * [3ef1871a] Removed Large Top App Bar because of crash when rotating
         device

José Cabeda (1):
  * [30455aba] Updated Portuguese (Portugal) translation using Weblate

bowornsin (1):
  * [d9b58d61] Updated Thai translation using Weblate

# 2.4.4
Jonas Kalderstam (5):
  * [22996d2b] Removed all static functions with DI in them
  * [3dbb26b5] Fixed crash on startup if "Sync upon app start" was enabled
  * [8109a298] Consolidated all compose providers
  * [202dd81d] Fixed wrong colors for small top app bar
  * [d45711ea] Fixed new item count not respecting block list

Retrial (1):
  * [9518e7e8] Translated using Weblate (Greek)

# 2.4.3
Belmar Begić (1):
  * [b230216a] Updated Bosnian translation using Weblate

Jonas Kalderstam (4):
  * [0657fd32] Fixed crash when sharing link to app
  * [27a04033] Fixed a recursion bug with DI and some cleanup
  * [ccf438c7] Fixed crash on database upgrade
  * [855fe6d5] Show diff on release

bowornsin (1):
  * [69e93779] Updated Thai translation using Weblate

mm4c (1):
  * [591c92cf] Translated using Weblate (Dutch)

# 2.4.2
Agnieszka C (1):
  * [f4bb4abf] Updated Polish translation using Weblate

Axus Wizix (1):
  * [b4351577] Updated Russian translation using Weblate

Dan (2):
  * [d0dd8fa2] Translated using Weblate (Ukrainian)
  * [e9349b64] Updated Ukrainian translation using Weblate

ERYpTION (1):
  * [d1ea9ca3] Updated Danish translation using Weblate

Eric (1):
  * [bf17b3e0] Updated Chinese (Simplified) translation using Weblate

J. Lavoie (1):
  * [d7609fb2] Updated French translation using Weblate

Jonas Kalderstam (6):
  * [eaf28eff] Fixed spacing issue in settings
  * [d172d658] Changed so block list now works immediately instead of after
         sync
  * [f224bca4] Changed so more devices will won't use large top app bars

Oğuz Ersen (1):
  * [17d2f848] Updated Turkish translation using Weblate

Simone Dotto (1):
  * [b50bc9ad] Updated Italian translation using Weblate

gallegonovato (1):
  * [a0f49a1a] Updated Galician translation using Weblate

haidarah esmander (1):
  * [8bd20a6d] Added Arabic translation using Weblate

zmni (1):
  * [9c13bc84] Updated Indonesian translation using Weblate

# 2.4.1
Agnieszka C (1):
  * [86b33eb1] Updated Polish translation using Weblate

Andrij Mizyk (1):
  * [f5557397] Updated Ukrainian translation using Weblate

Dritan Taulla (1):
  * [301cdff5] Updated Albanian translation using Weblate

ERYpTION (1):
  * [4b2c8749] Updated Danish translation using Weblate

El Pirujo (1):
  * [7fff2056] Updated Spanish translation using Weblate

Eric (1):
  * [6e75150b] Updated Chinese (Simplified) translation using Weblate

Gediminas Murauskas (1):
  * [6e2d9edd] Updated Lithuanian translation using Weblate

J. Lavoie (1):
  * [72c5bca5] Updated Italian translation using Weblate

Jonas Kalderstam (6):
  * [2dd030b7] Fixed some text not scaling according to settings
  * [b83c5d38] Fixed bug where swiping was not possible in list because of
         grid
  * [807722c7] Added SwipeToDismiss to GridView
  * [5d0103a4] Fixed SwipeToDismiss so it works even with disabled
         animations

Oğuz Ersen (1):
  * [872e93e2] Updated Turkish translation using Weblate

VfBFan (1):
  * [17a8a29c] Updated German translation using Weblate

zmni (1):
  * [4aaaa296] Updated Indonesian translation using Weblate

# 2.4.0
Dritan Taulla (1):
  * [fa0a128e] Updated Albanian translation using Weblate

Jonas Kalderstam (18):
  * [e5ff3dab] Made TopAppBar larger on tall screens to make it easier for
         one-handed use
  * [582c7088] Fixed color of status bar and top app bar in Black theme
  * [f56169ee] Added setting for Font Size
  * [84648be4] Added support for app specific locale
  * [faa0e234] Changed animations from slide to fade

Minh P (1):
  * [0999d81e] Updated Vietnamese translation using Weblate

Nikita Epifanov (1):
  * [a39c4175] Updated Russian translation using Weblate

WB (1):
  * [e91fb923] Updated Galician translation using Weblate

bowornsin (1):
  * [b027d0c8] Updated Thai translation using Weblate

# 2.3.9
Aitor Salaberria (1):
  * [c7c6c271] Updated Basque translation using Weblate

ERYpTION (1):
  * [bce0dae3] Updated Danish translation using Weblate

Gediminas Murauskas (1):
  * [e52e814a] Updated Lithuanian translation using Weblate

Jonas Kalderstam (5):
  * [ae04fa71] Implemented StaggeredGrid for tablets
  * [866363b1] Improved reliability of device sync
  * [8ead3242] Implemented predictive back

Mehmet (1):
  * [4a56f294] Updated Turkish translation using Weblate

bowornsin (1):
  * [cfe4f559] Updated Thai translation using Weblate

ssantos (1):
  * [cfedc4d4] Updated Portuguese (Portugal) translation using Weblate

# 2.3.8
ERYpTION (1):
  * [9e04ef88] Updated Danish translation using Weblate

Francesco Saltori (1):
  * [2106104e] Translated using Weblate (Italian)

J. Lavoie (1):
  * [d9af5801] Translated using Weblate (Italian)

Jonas Kalderstam (2):
  * [2f281017] Removed new-indicator from Compact and SuperCompact view
         styles
  * [5a43aaa0] Added app title for Thai

Mehmet (1):
  * [8a4304e4] Updated Turkish translation using Weblate

Sergi Font (1):
  * [26b07252] Updated Catalan translation using Weblate

Simona Iacob (1):
  * [45e30113] Updated Romanian translation using Weblate

bowornsin (1):
  * [8aa363b7] Translated using Weblate (Thai)

# 2.3.7
Ady (1):
  * [30fab5dd] Updated French translation using Weblate

Agnieszka C (1):
  * [65a039ef] Updated Polish translation using Weblate

Allan Nordhøy (1):
  * [ee6fe99d] Updated Norwegian Bokmål translation using Weblate

Andrij Mizyk (1):
  * [d457772f] Updated Ukrainian translation using Weblate

Dhruv Sangvikar (1):
  * [81e46e5b] Add support for showing favicons in nav drawer

ERYpTION (1):
  * [f057e3fb] Updated Danish translation using Weblate

El Pirujo (1):
  * [11bc8b68] Updated Spanish translation using Weblate

Eric (1):
  * [d8882a29] Updated Chinese (Simplified) translation using Weblate

Gediminas Murauskas (1):
  * [9328f4c8] Updated Lithuanian translation using Weblate

Jacob Highfield (2):
  * [f88ba589] Bold text on unread items
  * [dd6dbcdb] Don't bold the snippet

Jiri Grönroos (1):
  * [267a1d83] Translated using Weblate (Finnish)

Jonas Kalderstam (4):
  * [a0b960c1] Reduced title font weight to Bold from ExtraBold
  * [2020a70b] Removed unused parameter
  * [63b8cb53] Fixed text alignment in navdrawer after feed icons added
  * [3a93eb6b] Added divider in navdrawer so text can be aligned even with
         icons

Nikita Epifanov (1):
  * [2863af80] Updated Russian translation using Weblate

Oğuz Ersen (1):
  * [e8df291a] Updated Turkish translation using Weblate

VfBFan (1):
  * [c29b1c08] Updated German translation using Weblate

Zacharias Efraimidis (1):
  * [22fc67c0] Updated Greek translation using Weblate

bruh (1):
  * [671d7af7] Translated using Weblate (Vietnamese)

zmni (1):
  * [1dc28299] Updated Indonesian translation using Weblate

# 2.3.6
Belmar Begić (1):
  * [9b6b5a66] Updated Bosnian translation using Weblate

Jonas Kalderstam (1):
  * [8e662118] Fixed parsing of srcset images in Politico's feed

Miraficus (1):
  * [37a170df] Translated using Weblate (Czech)

Simona Iacob (1):
  * [db9b825d] Updated Romanian translation using Weblate

# 2.3.5
Ady (1):
  * [4cdc88ba] Translated using Weblate (French)

Jonas Kalderstam (2):
  * [ff23128b] Changed user-agent to match Chrome's
  * [6002d91d] Fixed user-agent test

Vitor Henrique (1):
  * [073ccb1d] Updated Portuguese (Brazil) translation using Weblate

liimee (1):
  * [e4090e15] Updated Indonesian translation using Weblate

zmni (1):
  * [9c1acc5b] Translated using Weblate (Indonesian)

# 2.3.4
Jonas Kalderstam (5):
  * [7036d422] Fixed parsing of additional types of thumbnails
  * [a22aa525] Further improved thumbnail parsing
  * [943695c5] Fixed decoding where smileys would not get rendered correctly

# 2.3.3
Agnieszka C (1):
  * [6145fb2a] Updated Polish translation using Weblate

Andrij Mizyk (1):
  * [9d72357f] Updated Ukrainian translation using Weblate

ERYpTION (1):
  * [38f8e2fd] Updated Danish translation using Weblate

El Pirujo (1):
  * [88b815b0] Updated Spanish translation using Weblate

Eric (1):
  * [4039f577] Updated Chinese (Simplified) translation using Weblate

J. Lavoie (1):
  * [819789ec] Updated French translation using Weblate

Jonas Kalderstam (3):
  * [88391f35] Added monochrome app icon
  * [52a2e030] Added a debug-only app icon
  * [b003d5ec] Fixed crash when sync on when charging was true

Oğuz Ersen (1):
  * [2d229786] Updated Turkish translation using Weblate

atilluF (1):
  * [e5b6191e] Updated Italian translation using Weblate

zmni (1):
  * [1a9efa68] Updated Indonesian translation using Weblate

# 2.3.2
Allan Nordhøy (1):
  * [9c947b4c] Updated Norwegian Bokmål translation using Weblate

Andrij Mizyk (1):
  * [e50de95c] Updated Ukrainian translation using Weblate

ERYpTION (1):
  * [6162c9e7] Translated using Weblate (Danish)

Jonas Kalderstam (14):
  * [4ee8790e] Fixed padding in tag list
  * [8cce8c0d] Fixed images rendering too large causing crashes
  * [c660a688] Added fallback to feed icon in compact views
  * [2b91e13f] Added blacklist for twitter icon as article icon
  * [d4817632] Added dividers in list for compact and superCompact styles
  * [605f9be2] Fixed so list stays at top when updating if already at top
  * [a7b032a1] Added padding in list so FAB doesn't cover last article
  * [54a1168f] Adjusted TopAppBar scroll behavior
  * [3613b758] Fixed syncclient re-initializing unnecessarily
  * [a20f65e6] Fixed reliability of read status sync
  * [5a45015d] Fixed image size in Compact item layouts
  * [7ba2253b] Fixed HTML not getting stripped from alt texts
  * [738495a2] Increased text size of block quotes in reader view
  * [925824de] Upgraded dependency versions and insets handling

Meiru (1):
  * [383bd23c] Updated Japanese translation using Weblate

MkQtS (1):
  * [1d6ea396] Updated Chinese (Simplified) translation using Weblate

Vitor Henrique (1):
  * [863c8327] Updated Portuguese (Brazil) translation using Weblate

WB (1):
  * [257dfed6] Translated using Weblate (Galician)

kak mi (1):
  * [89cb8b85] Updated Chinese (Simplified) translation using Weblate

Егор Ермаков (1):
  * [11505e3b] Updated Russian translation using Weblate

# 2.3.1
Ady (1):
  * [65b669ec] Updated French translation using Weblate

Jonas Kalderstam (5):
  * [130c2319] Fix Right to Left languages in headers
  * [35eb7660] Changed back to single screen on tablets in portrait mode
  * [2ccf7d00] Fixed content alignment in search screen
  * [8a68f2a5] Added parsing support for additional thumbnails
  * [fd46ece5] Fixed list not centered in landscape on phones

linsui (1):
  * [c07e95ce] Fix feed indicator localization

zmni (1):
  * [e37aba0c] Updated Indonesian translation using Weblate

# 2.3.0

* Upgraded to follow Material3 guidelines including dynamic colors
* Big improvements to TTS with the help of Kevin Jiang
* App is now tablet friendly on all screens

Kevin Jiang (7):
  * [65eb5d73] feat(settings): added detect language to settings
  * [0444b16a] feat(readaloud): detect language of the article for readaloud
  * [7295d5c7] test: test newly added detect language setting
  * [a4a3f843] Use separate description if language detect feature is not
         available
  * [45a63db4] Minor formatting change
  * [c047a0dd] Fix typo
  * [b8b73ab1] Updating test

Agnieszka C (2):
  * [0ec50961] Updated Polish translation using Weblate

Andrij Mizyk (2):
  * [464661bf] Updated Ukrainian translation using Weblate

D221 (1):
  * [13777023] Updated Lithuanian translation using Weblate

ERYpTION (2):
  * [8d2291d2] Updated Danish translation using Weblate

El Pirujo (3):
  * [addd743a] Updated Spanish translation using Weblate

Eric (2):
  * [ca0895a4] Updated Chinese (Simplified) translation using Weblate

J. Lavoie (1):
  * [7dbc8c85] Updated French translation using Weblate

Meiru (1):
  * [f6c5e8e7] Updated Japanese translation using Weblate

Oğuz Ersen (2):
  * [fd60a78e] Updated Turkish translation using Weblate

Simona Iacob (1):
  * [a664e174] Translated using Weblate (Romanian)

Tadeáš Erban (1):
  * [ace40142] Updated Czech translation using Weblate

VfBFan (2):
  * [1105f0da] Updated German translation using Weblate

atilluF (4):
  * [37e9840b] Translated using Weblate (Italian)

bruh (1):
  * [2a89025b] Updated Vietnamese translation using Weblate

zmni (1):
  * [991fce3e] Updated Indonesian translation using Weblate

Егор Ермаков (1):
  * [4f4d9a4c] Updated Russian translation using Weblate

Jonas Kalderstam (74):
  * [bb2f8226] Fixed all restartable but not skippable functions
  * [517efd70] Fixed some code formatting
  * [d2828981] Modified strings to be shorter
  * [fca6176f] Changed detect language settings to default to true
  * [942c3dd0] Created new read aloud settings group
  * [b892765e] Made detectLanguage slightly more robust for TTS failures
  * [bc36a663] Bumped some dependencies
  * [a442e159] Upgraded Android Gradle Plugin and such
  * [c343a4b7] Builds tools version doesn't need to be specified anymore
  * [28022f25] Bump rome version
  * [360b1d12] Fixed deprecation warnings and api changes
  * [eff41771] Removed accompanist insets - no longer needed
  * [e7c9af4f] Implemented runtime permission for notifications
  * [25d649fe] Some test fixes
  * [0677cf1c] Fixed regression in edit feed view
  * [5ab974b7] Removed all uses of live data since it seems buggy
  * [f49cf9c6] Bumped rome version
  * [361617d5] Theme
  * [ada5c099] Upgraded to Material3 - seems to work even
  * [9860a501] Implemented dynamic color scheme
  * [a10b77e1] Theme respects black again
  * [aaee2b64] Fixed color of status bar and padding
  * [5799cc1a] More sensible size
  * [f07a65a3] Fixed nav bar coloring on all versions
  * [7dbf7f26] Handle navigation bar padding in horizontal
  * [8598f2cd] Try giving foldables more usable space
  * [a9392955] NavBar button becomes toggleable now on tablets
  * [497c387c] Fixed more menu icons
  * [5a7c7850] More foldable use
  * [7aba6892] Forgot an activity
  * [ef0958d1] Pre split
  * [b3067123] Edit feed is dual now
  * [9f9dc5e1] Search feed is dual now
  * [8c7e8dee] Fixed color of cards and spec says image should be rounded
  * [1a0f6c93] Fixed link color to something more pleasing
  * [89f7cdf2] Updated indicators on feed items
  * [92339e7d] Removed unused overload and fixed FeedListScrollbehavior
  * [3ee4296f] Some animated navigation
  * [3f875378] Fixed animated transitions in sync screens
  * [73b71df2] Fixed scroll behavior for sync screens
  * [d1d4761b] Fixed a bug in animation
  * [75fe24b0] All screens animated and bottom bar fixed
  * [12c1468e] Fixed lint check
  * [5b87b130] Fixed bottom bar padding for navigation bar
  * [6000b1f6] Fix text lines in app bar
  * [365e902c] Fixed actions in top app bar to follow guidelines number
  * [feed23d4] Fixed incorrect default value in edit feed screen
  * [9b489168] Removed buggy sync indicators in navigation drawer
  * [489f3bde] Updated fastlane metadata with new screenshots
  * [4ad56535] Fixed color of indicator
  * [a6419890] Changed to canonical text to speech icon
  * [bbf0104b] Cleaned up custom icons
  * [2adfbac7] Changed to original done all icon for FAB
  * [af4a2dd1] Adjusted middle margin on tablets
  * [405d680b] Updated to official URL annotation
  * [a81a1b3c] Text given to TTS engine now has annotations
  * [433a8bb1] Fixed device list screen not showing devices
  * [87a068a5] Added a skip next button for TTS
  * [4b461392] TTS now detect language for each paragraph
  * [25449bb3] Moved stop icon to start
  * [85d7ddf3] Added ability to force Locale on TTS
  * [2458d2f5] Respect user locale order if set
  * [2ecad20d] Revert "Updated to official URL annotation"
  * [61486f13] Changed FAB to scaleIn animation as per guidelines
  * [fb431fa2] Added string for Text to speech
  * [2e67ac5b] Ignore RT tags in Ruby tags for now
  * [9a391a1b] Improved handling of RTL text
  * [2666f294] Improved RTL support in title bar
  * [1f0e2da9] Translated using Weblate (Swedish)
  * [8ae209c3] Further RTL improvements - now don't strip ZWNJ chars
  * [5a9ab235] Split pipeline so timeout is not hit
  * [e6b9aba6] Updated screenshots in README
  * [af05221c] Fixed pipeline deps
  * [38401846] Moved Mark All As Read to top of the menu

# 2.2.7
Jonas Kalderstam (1):
  * [c333c453] Fixed mark above/below as read with pinned items

Luna Jernberg (1):
  * [27173e02] Updated Swedish translation using Weblate

MkQtS (1):
  * [379c4329] Updated Chinese (Simplified) translation using Weblate

Simona Iacob (1):
  * [60817d7b] Updated Romanian translation using Weblate

мачко (1):
  * [317bdcd5] Added Bulgarian translation using Weblate

# 2.2.6
D221 (1):
  * [f6445c30] Updated Lithuanian translation using Weblate

Freddy Morán Jr (1):
  * [272e4800] Updated Spanish translation using Weblate

H Tamás (1):
  * [b39803b6] Updated Hungarian translation using Weblate

Vitor Henrique (1):
  * [00e0ae07] Updated Portuguese (Brazil) translation using Weblate

WB (1):
  * [62efdde3] Updated Galician translation using Weblate

zmni (1):
  * [b9c15802] Updated Indonesian translation using Weblate

# 2.2.5
Agnieszka C (1):
  * [59c9f5d9] Updated Polish translation using Weblate

Andrij Mizyk (1):
  * [c51289a7] Updated Ukrainian translation using Weblate

ERYpTION (1):
  * [bf971897] Updated Danish translation using Weblate

El Pirujo (1):
  * [2b5a0bc7] Updated Spanish translation using Weblate

Eric (1):
  * [c410021f] Updated Chinese (Simplified) translation using Weblate

H Tamás (1):
  * [3b2a72d9] Added Hungarian translation using Weblate

J. Lavoie (1):
  * [2161a18a] Updated Italian translation using Weblate

Meiru (1):
  * [9a3f100a] Updated Japanese translation using Weblate

Oğuz Ersen (1):
  * [648e9d5d] Updated Turkish translation using Weblate

VfBFan (1):
  * [14c66874] Updated German translation using Weblate

mm4c (1):
  * [48862b19] Updated Dutch translation using Weblate

Егор Ермаков (1):
  * [7dbee822] Translated using Weblate (Russian)

# 2.2.4-1
Jonas Kalderstam (2):
  * [b8f4f64e] Fixed content provider preventing installs

# 2.2.4
Artem (1):
  * [3f1e94b0] Translated using Weblate (Ukrainian)

Belmar Begić (1):
  * [62f9c28f] Updated Bosnian translation using Weblate

Jonas Kalderstam (1):
  * [1ae8eb07] Improved speed and reliability of swipe
  * [dea765b9] Implemented content provider so other apps can access data
         with permission

# 2.2.3
Athanasios Plastiras (1):
  * [d3ea05e1] Updated Greek translation using Weblate

Jonas Kalderstam (3):
  * [57528ef1] Releasing 2.2.3
  * [4889a5dc] Removed duplicate portugese language
  * [6ca4df09] Removed duplicate portugese play store translation

Simona Iacob (1):
  * [b7cce278] Updated Romanian translation using Weblate

Vítor Fernandes Almado (1):
  * [8c81a0ed] Updated Portuguese translation using Weblate

WB (1):
  * [4c03fe85] Translated using Weblate (Galician)

Weblate (1):
  * [6c49bc96] Added Portuguese translation using Weblate

# 2.2.3
Athanasios Plastiras (1):
  * [d3ea05e1] Updated Greek translation using Weblate

Vítor Fernandes Almado (1):
  * [8c81a0ed] Updated Portuguese translation using Weblate

WB (1):
  * [4c03fe85] Translated using Weblate (Galician)

Weblate (1):
  * [6c49bc96] Added Portuguese translation using Weblate

# 2.2.2
Andrij Mizyk (1):
  * [5f9dfafd] Updated Ukrainian translation using Weblate

Jonas Kalderstam (1):
  * [2bf95ec2] Fixed list incorrectly scrolling up when marking as read

Meiru (1):
  * [7e74a6d9] Updated Japanese translation using Weblate

VfBFan (1):
  * [1d9c7dcd] Updated German translation using Weblate

zmni (1):
  * [40ec8315] Updated Indonesian translation using Weblate

# 2.2.1
Ady (1):
  * [41c33ece] Updated French translation using Weblate

Agnieszka C (1):
  * [76baedde] Updated Polish translation using Weblate

Andrij Mizyk (1):
  * [88ebe3cd] Updated Ukrainian translation using Weblate

ERYpTION (1):
  * [f99360d4] Updated Danish translation using Weblate

El Pirujo (1):
  * [8355e4b1] Updated Spanish translation using Weblate

Eric (1):
  * [7247ce08] Updated Chinese (Simplified) translation using Weblate

Jonas Kalderstam (1):
  * [ed0f3c33] Fixed crash when feeds have items with bad links

Nikita Epifanov (1):
  * [575fc3b3] Updated Russian translation using Weblate

Oğuz Ersen (1):
  * [5b1c4f60] Updated Turkish translation using Weblate

Vitor Henrique (1):
  * [576be0f3] Updated Portuguese (Brazil) translation using Weblate

mm4c (1):
  * [7980253d] Updated Dutch translation using Weblate

# 2.2.0
Ady (6):
  * [6f4bcab2] Update schema to add bookmarked status
  * [0ab5b1bb] Toggle bookmarks
  * [a90b04ce] Allow to filter lists per bookmarks
  * [2ad907ef] Align bookmark icon to the right
  * [a610cfab] Remove unnecessary code for bookmark's alignement
  * [132ced2a] Handle bookmarks sorting by date, feed and tag

Jonas Kalderstam (4):
  * [b8b414a6] Fixed DB test 21->22
  * [6e79da27] Added DB migration test 22->23
  * [3ffe84f1] Removed Galician language when deploying to play store; not
         supported by Google
  * [fba8c0db] Removed Galician from App; not supported by Android

Simone Dotto (1):
  * [35eac7c8] Updated Italian translation using Weblate

antonpaidoslalin (1):
  * [356d1c26] Translated using Weblate (Galician)

# 2.1.8
Ben Beaver (2):
  * [e0d24b91] Added Toki Pona translation using Weblate
  * [5be9e915] Translated using Weblate (Toki Pona)

ERYpTION (1):
  * [72d5b3fb] Translated using Weblate (Danish)

Jonas Kalderstam (2):
  * [bc4c314c] Toki Pona is not supported by Play Store
  * [5b46fa0a] Toki Pona not supported by Android

Vitor Henrique (1):
  * [bef7d13c] Updated Portuguese (Brazil) translation using Weblate

# 2.1.7
Alan (1):
  * [93606b38] Updated Portuguese (Brazil) translation using Weblate

ERYpTION (1):
  * [40bb39a8] Updated Danish translation using Weblate

Meiru (1):
  * [26e075aa] Updated Japanese translation using Weblate

Nikita Epifanov (1):
  * [aab536a0] Updated Russian translation using Weblate

WB (1):
  * [09af6ab5] Updated Galician translation using Weblate

bruh (1):
  * [10e8db60] Updated Vietnamese translation using Weblate

zmni (1):
  * [3ff53b65] Updated Indonesian translation using Weblate

# 2.1.6
Agnieszka C (1):
  * [0864c9f5] Updated Polish translation using Weblate

Andrij Mizyk (1):
  * [b2a24f8a] Updated Ukrainian translation using Weblate

ERYpTION (1):
  * [a045d6bd] Updated Danish translation using Weblate

El Pirujo (1):
  * [bcaeb600] Updated Spanish translation using Weblate

Eric (1):
  * [377e36c4] Updated Chinese (Simplified) translation using Weblate

J. Lavoie (1):
  * [53c9a700] Updated Italian translation using Weblate

Jonas Kalderstam (1):
  * [afa77107] Updated Swedish translation using Weblate

Manapart (1):
  * [e0288297] Create Block List that filters out feed items with a blocked
         word

Oğuz Ersen (1):
  * [47fc2a0a] Updated Turkish translation using Weblate

VfBFan (1):
  * [ca5e5fce] Updated German translation using Weblate

Vitor Henrique (1):
  * [17dfc130] Updated Portuguese (Brazil) translation using Weblate

eevan78 (1):
  * [760e6da0] Updated Serbian translation using Weblate

mm4c (1):
  * [9376ad3c] Updated Dutch translation using Weblate

zmni (1):
  * [ce5d1a0d] Updated Indonesian translation using Weblate

# 2.1.5
Andrij Mizyk (1):
  * [a3a24eb2] Updated Ukrainian translation using Weblate

Belmar Begić (1):
  * [1f14b005] Updated Bosnian translation using Weblate

El Pirujo (1):
  * [abb2ed1c] Updated Spanish translation using Weblate

J. Lavoie (1):
  * [106499ed] Updated Italian translation using Weblate

Jonas Kalderstam (8):
  * [0692737a] Disabled Sync API request when not configured
  * [559c5e50] Fixed crash when removing already removed device
  * [78791758] Moved all syncing of read status to regular sync job

Meiru (1):
  * [a87e1b91] Updated Japanese translation using Weblate

Oğuz Ersen (1):
  * [8f5c9838] Updated Turkish translation using Weblate

mm4c (1):
  * [99518473] Updated Dutch translation using Weblate

# 2.1.4
Agnieszka C (1):
  * [066ab713] Updated Polish translation using Weblate

ERYpTION (1):
  * [a7c0746b] Updated Danish translation using Weblate

Eric (1):
  * [369136d5] Updated Chinese (Simplified) translation using Weblate

Jonas Kalderstam (2):
  * [ce2f7648] Ktlint format
  * [b218326e] Fixed a crash introduced in 2.1.3

# 2.1.3
Andrij Mizyk (1):
  * [059041bc] Updated Ukrainian translation using Weblate

J. Lavoie (1):
  * [474462cb] Updated French translation using Weblate

Jonas Kalderstam (1):
  * [4bc528b7] Added ability to pin an article to top of the feed

Julian Chu (1):
  * [d69a0490] Updated Chinese (Traditional) translation using Weblate

mm4c (1):
  * [9a91d313] Updated Dutch translation using Weblate

# 2.1.2
Jonas Kalderstam (1):
  * [d06407a5] Fixed broken test

mm4c (1):
  * [c7a09365] Updated Dutch translation using Weblate

# 2.1.1
Julian Chu (1):
  * [95ea703a] Updated Chinese (Traditional) translation using Weblate

Meiru (1):
  * [69437712] Updated Japanese translation using Weblate

Simona Iacob (1):
  * [99636a46] Updated Romanian translation using Weblate

Vitor Henrique (1):
  * [9a14699f] Updated Portuguese (Brazil) translation using Weblate

bruh (1):
  * [f2c70991] Updated Vietnamese translation using Weblate

mm4c (1):
  * [6fe26dc0] Updated Dutch translation using Weblate

# 2.1.0-1
Jonas Kalderstam (19):
  * [24c0c8fd] Implemented multi device sync
  * [211b1281] Fixed spaces getting replaced by + in feed titles
  * [4cfd9e2e] Fixed incorrect bundling of notifications and sync
         notification
  * [96cd9754] Added setting for swiping to mark as read
  * [154ad356] Fixed summary notification not getting cleared
  * [416bb580] Changed all header sizes inside articles to be the same size
  * [16ef3dc5] Fixed OPML export file lacking .opml suffix
  * [211bcf43] Fixed Play store locale code for Danish
  * [231293f4] Updated Swedish translation using Weblate
  * [1f6a35cc] Added confirmation dialog for leaving sync chain
  * [b084f454] Added message if barcode scanner could not be opened
  * [c7e01269] Updated Swedish translation using Weblate
  * [6364a49b] Fixed navigation bar obscuring UI in landscape
  * [be6f312d] Improved read aloud by splitting text on more punctuation
  * [f2f5fcfe] Fixed crash in case Play button in Read Aloud was double
         clicked
  * [cbe64e8b] Updated sync code to match server side updates

Agnieszka C (3):
  * [c68321b8] Updated Polish translation using Weblate

Andrij Mizyk (3):
  * [c32113b2] Updated Ukrainian translation using Weblate

ERYpTION (3):
  * [06039102] Updated Danish translation using Weblate

El Pirujo (3):
  * [2f615a19] Updated Spanish translation using Weblate

Eric (3):
  * [a5277f50] Updated Chinese (Simplified) translation using Weblate

J. Lavoie (3):
  * [81011919] Updated Italian translation using Weblate

Meiru (3):
  * [c3a00180] Updated Japanese translation using Weblate

Nikita Epifanov (2):
  * [f0fb62af] Updated Russian translation using Weblate

Oğuz Ersen (3):
  * [84a47963] Updated Turkish translation using Weblate

Simona Iacob (2):
  * [9b00f1d3] Updated Romanian translation using Weblate

THANOS SIOURDAKIS (1):
  * [004c6444] Updated Greek translation using Weblate

Tadeáš Erban (2):
  * [dc307594] Updated Czech translation using Weblate

VfBFan (4):
  * [6541d7c1] Updated German translation using Weblate

Vitor Henrique (2):
  * [5aca1d6e] Updated Portuguese (Brazil) translation using Weblate

mm4c (3):
  * [292e58fa] Updated Dutch translation using Weblate

zmni (1):
  * [496f8abc] Updated Indonesian translation using Weblate

Éfrit (1):
  * [5959c512] Updated French translation using Weblate

# 2.1.0
Agnieszka C (3):
  * [c68321b8] Updated Polish translation using Weblate

Andrij Mizyk (3):
  * [c32113b2] Updated Ukrainian translation using Weblate

ERYpTION (3):
  * [06039102] Updated Danish translation using Weblate

El Pirujo (3):
  * [2f615a19] Updated Spanish translation using Weblate

Eric (3):
  * [a5277f50] Updated Chinese (Simplified) translation using Weblate

J. Lavoie (3):
  * [81011919] Updated Italian translation using Weblate

Jonas Kalderstam (19):
  * [24c0c8fd] Implemented multi device sync
  * [211b1281] Fixed spaces getting replaced by + in feed titles
  * [4cfd9e2e] Fixed incorrect bundling of notifications and sync
         notification
  * [96cd9754] Added setting for swiping to mark as read
  * [154ad356] Fixed summary notification not getting cleared
  * [416bb580] Changed all header sizes inside articles to be the same size
  * [16ef3dc5] Fixed OPML export file lacking .opml suffix
  * [211bcf43] Fixed Play store locale code for Danish
  * [231293f4] Updated Swedish translation using Weblate
  * [1f6a35cc] Added confirmation dialog for leaving sync chain
  * [b084f454] Added message if barcode scanner could not be opened
  * [c7e01269] Updated Swedish translation using Weblate
  * [6364a49b] Fixed navigation bar obscuring UI in landscape
  * [be6f312d] Improved read aloud by splitting text on more punctuation
  * [f2f5fcfe] Fixed crash in case Play button in Read Aloud was double
         clicked
  * [cbe64e8b] Updated sync code to match server side updates

Meiru (3):
  * [c3a00180] Updated Japanese translation using Weblate

Nikita Epifanov (2):
  * [f0fb62af] Updated Russian translation using Weblate

Oğuz Ersen (3):
  * [84a47963] Updated Turkish translation using Weblate

Simona Iacob (2):
  * [9b00f1d3] Updated Romanian translation using Weblate

THANOS SIOURDAKIS (1):
  * [004c6444] Updated Greek translation using Weblate

Tadeáš Erban (2):
  * [dc307594] Updated Czech translation using Weblate

VfBFan (4):
  * [6541d7c1] Updated German translation using Weblate

Vitor Henrique (2):
  * [5aca1d6e] Updated Portuguese (Brazil) translation using Weblate

mm4c (3):
  * [292e58fa] Updated Dutch translation using Weblate

zmni (1):
  * [496f8abc] Updated Indonesian translation using Weblate

Éfrit (1):
  * [5959c512] Updated French translation using Weblate

# 2.0.14
Jonas Kalderstam (1):
  * [90e8048c] Fixed spaces getting replaced by + in feed titles

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
