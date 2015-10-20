![http://gwtx.googlecode.com/svn/wiki/gwtx.png](http://gwtx.googlecode.com/svn/wiki/gwtx.png)

The goal of GWTx is to provide drop in support for an extended set of the of the standard Java library classes on top of what is provided by the GWT distribution provided by Google.

---

**GWTx 1.5.2 released!**

Fix  [issue #18](https://code.google.com/p/gwtx/issues/detail?id=#18) and [issue  #20](https://code.google.com/p/gwtx/issues/detail?id=#20)


**GWTx 1.5.1 released!**

This release fix blocking [issue #16](https://code.google.com/p/gwtx/issues/detail?id=#16)

This release uses Maven naming convention and is deployed on central under com.google.code.gwt groupId.

Next GWTx release will focus on GWT 1.6 support.


---


Currently emulated JRE classes include:
  * `java.beans.PropertyChange*` and associated listeners
  * `java.util.StringTokenizer`

New features in 1.5.x :
  * `java.beans.Introspector` and `java.beans.BeanInfo` with (limited) support for `java.lang.reflect.Method`
  * javax.annotation, including javax.annotation.secutiry
  * javax.persistence
  * javax.validation
  * java.text
  * java.util.regexp (be aware Java regexp are not exactly same as JavaScript ones)

Deprecations

  * `java.util.logging` emulation has been removed. Logging has better support from the gwt-log project and it's contribution to GWT Logging API in gwt-incubator.

  * `java.io` and `java.net` emulation has also been removed. They were created to support `java.util.logging` but never became stable enough for large adoption. If you need them, please send your use case to nicolas.deloof\_at\_gmail.com to get them back in GWTx.

Usage:

  1. Download the GWTx.jar file from the downloads linked on the right of this page and add it to your project's classpath.
  1. Add the inherits to your .gwt.xml: 

&lt;inherits name='com.googlecode.gwtx.Java'/&gt;


  1. Use any of the above emulated classes like you normally would.

It's my hope that these classes get included in the official GWT distribution. I also hope that the GWTx project can become the unofficial de facto place to go for additional emulated JRE classes for use in client code that aren't provided by GWT.

If you wish to join the project and are willing to make sure your contributions are ASL 2 licensed then let me know.
