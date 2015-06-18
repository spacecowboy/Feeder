# -*- coding: utf-8 -*-


testfeed_link = "http://cowboyprogrammer.org/"

testfeed = """
<?xml version="1.0" encoding="UTF-8"?>
<rss xmlns:dc="http://purl.org/dc/elements/1.1/"
     xmlns:content="http://purl.org/rss/1.0/modules/content/"
     xmlns:atom="http://www.w3.org/2005/Atom" version="2.0"
     xmlns:media="http://search.yahoo.com/mrss/"><channel>
  <title>Cowboy Programmer</title>
  <description>Ramblings about stuff.</description>
  <link>http://cowboyprogrammer.org/</link>
  <generator>Ghost 0.6</generator>
  <lastBuildDate>Tue, 19 May 2015 10:30:24 GMT</lastBuildDate>
  <atom:link href="http://cowboyprogrammer.org/rss/" rel="self"
             type="application/rss+xml"/>
  <ttl>60</ttl>
  <item>
    <title>Title1</title>
    <description>This is the text in the first item.</description>
    <link>http://cowboyprogrammer.org/encrypt-a-btrfs-raid5-array-in-place/</link>
    <guid isPermaLink="false">3e8eef45-56e1-4fad-8998-7811a7d4eac8</guid>
    <category><![CDATA[linux]]></category>
    <category><![CDATA[btrfs]]></category>
    <category><![CDATA[encryption]]></category>
    <category><![CDATA[security]]></category>
    <dc:creator><![CDATA[Jonas Kalderstam]]></dc:creator>
    <pubDate>Thu, 01 Jan 2015 17:40:28 GMT</pubDate>
  </item>
  <item>
    <title><![CDATA[Making an RSS reader app with no GUID]]></title>
    <description><![CDATA[This is the text in the second item.]]></description>
    <link>http://cowboyprogrammer.org/making-an-rss-reader-app/</link>
    <category><![CDATA[android]]></category>
    <category><![CDATA[programming]]></category>
    <category><![CDATA[tutorials]]></category>
    <dc:creator><![CDATA[Jonas Kalderstam]]></dc:creator>
    <pubDate>Thu, 28 Aug 2014 13:56:30 GMT</pubDate>
  </item>
</channel></rss>
"""


testfeed_updated = """
<?xml version="1.0" encoding="UTF-8"?>
<rss xmlns:dc="http://purl.org/dc/elements/1.1/"
     xmlns:content="http://purl.org/rss/1.0/modules/content/"
     xmlns:atom="http://www.w3.org/2005/Atom" version="2.0"
     xmlns:media="http://search.yahoo.com/mrss/"><channel>
  <title>Cowboy Programmer</title>
  <description>Ramblings about stuff.</description>
  <link>http://cowboyprogrammer.org/</link>
  <generator>Ghost 0.6</generator>
  <lastBuildDate>Tue, 19 May 2015 10:30:24 GMT</lastBuildDate>
  <atom:link href="http://cowboyprogrammer.org/rss/" rel="self"
             type="application/rss+xml"/>
  <ttl>60</ttl>
  <item>
    <title>Title new</title>
    <description>This is the text in the new item.</description>
    <link>http://cowboyprogrammer.org/new-item/</link>
    <guid isPermaLink="false">abc123</guid>
    <pubDate>Thu, 06 Jan 2015 17:40:28 GMT</pubDate>
  </item>
  <item>
    <title>Title1 Updated</title>
    <description>This is the text in the first item.</description>
    <link>http://cowboyprogrammer.org/updated-link-for-first</link>
    <guid isPermaLink="false">3e8eef45-56e1-4fad-8998-7811a7d4eac8</guid>
    <category><![CDATA[linux]]></category>
    <category><![CDATA[btrfs]]></category>
    <category><![CDATA[encryption]]></category>
    <category><![CDATA[security]]></category>
    <dc:creator><![CDATA[Jonas Kalderstam]]></dc:creator>
    <pubDate>Thu, 03 Jan 2015 17:40:28 GMT</pubDate>
  </item>
  <item>
    <title><![CDATA[Making an RSS reader app with no GUID]]></title>
    <description><![CDATA[This is the text in the second item.]]></description>
    <link>http://cowboyprogrammer.org/making-an-rss-reader-app/</link>
    <category><![CDATA[android]]></category>
    <category><![CDATA[programming]]></category>
    <category><![CDATA[tutorials]]></category>
    <dc:creator><![CDATA[Jonas Kalderstam]]></dc:creator>
    <pubDate>Thu, 28 Aug 2014 13:56:30 GMT</pubDate>
  </item>
</channel></rss>
"""
