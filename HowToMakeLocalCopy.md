# How to get this work? #
Here's are details to run project on your machine.

# Details #

First You need to [checkout](http://code.google.com/p/chrome-rest-client/source/checkout) project into your IDE (I use eclipse).

Rest Client need reference to XMLHttpRequest2 project (included in trunk).
You don't actually need checkout XMLHttpRequest2 source code. You need an lib from XMLHttpRequest2/build/jar and include it into Your project.

After import set up references to GWT sdk.
This project also need 2 more library:
  * [GWT HTML5 database](http://code.google.com/p/gwt-mobile-webkit/)
  * [GWT Log](http://code.google.com/p/gwt-log/)

Update references and you are ready to go.

In latest version I've added extension to run chrome API in dev mode. In chrome open extensions tab and load unpacked extension from RestClient/war/extension.

You need to have Chrome version 23.