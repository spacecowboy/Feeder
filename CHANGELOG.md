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
