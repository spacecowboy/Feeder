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
