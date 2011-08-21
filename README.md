
Special Note for SproutCore Competition Judges
==============================================

I'm in the process of setting up a demo site [http://demo.chililog.com](http://demo.chililog.com). You can login as admin/admin.

It will be done by (late) Sunday night ready for you to review Monday morning.

This will save you time from downloading and installing everything.

However, if you do wish to download it and have a go, the instructions are in src/main/docs/developer_guide/developer_guide.textile.


What?
=====
ChiliLog is a real time log aggregation, analysis and monitoring tool.

* ChiliLog aggregates your log entries using 'publishers'.  The goal is to create publishers that work on different platforms/frameworks/languages. Pubishers transmits your log entries to the Chililog Server for storage.

* ChiliLog allows you to view aggregated logs in real time and also to search for historical entries. Because your logs are parsed, you can filter and sort the log data.

* ChiliLog can also monitors your logs for patterns that you define. If your pattern is matched, you will be notified.


Why?
====
I had a big problem the other day when my data centre provider claimed that I had exceeded by bandwidth allowance.

To find out what was causing my unusually high bandwidth usage, I had to aggregate logs from the firewall, load balancer, web server and app servers. 

Wow - what a pain in the arse!

The logs were in different formats, the timestamps were in different timezones and I had to wait 1 week before I got access to the firewall logs.

It was at this moment that the idea for ChiliLog was born.  

I tried to find open source software but I could not find one that did everything I needed in one package.  Due to corporate policy, I could not use a cloud logging service.


When?
====

ChiliLog is in development and "un-released".

Unfortunately, ChiliLog is not my day job.

The current road map is:

* JAN 2011 - Integrate HornetQ and mongoDB, parse incoming logs and store to mongoDB. (Done)

* MAY 2011 - Chiliog Log server without UI. (Done)

* JUL 2011 - Original release date posposed because I am not happy with UI in Sproutcore 1.0. Tables for displaying log entries are not work working very well. (Milestone missed).

* AUG 2011 - UI re-write in Sproutcore 2.0.

* OCT 2011 - Tentative alpha release.

* DEC 2011 - Monitoring.


Links
=====

* [Documentation](https://github.com/chililog/server/wiki) 

* [Issues](https://github.com/chililog/server/issues)


