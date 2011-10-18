
What is Chililog?
=================
Chililog is a log aggregation, analysis and monitoring server.

* Chililog aggregates your log entries using 'publishers'.  The goal is to create publishers that work on different platforms/frameworks/languages. Publishers transmits your log entries to Chililog Server for storage.

* Chililog allows you to view your aggregated logs in real time. You can also search, filter and sort historical log entries.

* Chililog monitors your logs for patterns that you define. If your pattern is matched, you will be notified.



Live Demo
=========

Here are the [instructions](https://github.com/chililog/chililog-server/wiki/Live-Demo) on how to access our live demo.

You'll create log entries from your browser and view them in real time. 



How to get hold of Chililog?
============================

Chililog is open source software published under the Apache License Version 2.0. 

Download it [here](https://github.com/chililog/chililog-server/downloads).

Hope you find it as useful and we do.



Why build it?
=============
I had a big problem the other day (back in 2010) when my data centre provider claimed that I had exceeded by bandwidth allowance.

To find out what was causing my unusually high bandwidth usage, I had to aggregate logs from the firewall, load balancer, web server and app servers. 

Wow - what a pain in the arse!

The logs were in different formats, the timestamps were in different timezones and I had to wait 1 week before I got access to the firewall logs.

I tried to find open source software but I could not find one that did everything I needed in one package.

It was at this moment when the idea for Chililog was born.  



Technical Titbits
=================

I've used Chililog to find out about newer technologies (well ... new as of 2010).   

* Chililog has a browser based client coded in HTML5 and javascript. It uses Sproutcore2 and jQuery Ajax to communicate with the server.
* Server coded in Java and uses open source
    * [MongoDB](http://www.mongodb.org/) to store data
    * [HornetQ](http://www.jboss.org/hornetq) for pubsub, and
    * [Netty](http://www.jboss.org/netty) as a web server.
* There are also tools to help you publish (write) and subscribe (read) log entries: 
    * [JavaScirpt](https://github.com/chililog/chililog-javascript-pubsub)
    * [Java](https://github.com/chililog/chililog-java-pubsub)
    * [.Net](https://github.com/chililog/chililog-dotnet-pubsub)


Roadmap
=======

The current road map is:

* JAN 2011 - Integrate HornetQ and mongoDB, parse incoming logs and store to mongoDB. (Done)

* MAY 2011 - Chiliog Log server without UI. (Done)

* JUL 2011 - Original release date postponed because I am not happy with UI in Sproutcore 1.0. Tables for displaying log entries are not work working very well. (Milestone missed).

* AUG 2011 - UI re-write in Sproutcore 2.0. (Done)

* OCT 2011 - Tentative alpha release.

* DEC 2011 - Monitoring.


Links
=====

* [Web Site](http://www.chililog.org)

* [Documentation](https://github.com/chililog/chililog-server/wiki) 

* [Issues](https://github.com/chililog/chililog-server/issues)

* [Twitter](http://www.twitter.com/chililog)

* [Forum](http://groups.google.com/group/chililog)

* [Blog](http://blog.chililog.org)


Special Note for Sproutcore Coders
==================================

The Sproutcore 2 source files are in [src/main/sc2](https://github.com/chililog/chililog-server/tree/master/src/main/sc2).

Sproutcore functionality used in the code include:

* Templates
* Data Store
* State Charts
* DateTime
* Bindings and custom binding transforms
* Strings Localization
* jQuery UI integration

I'm slowly [blogging](http://blog.chililog.org) about my experiences with Sproutcore 2.
 
