Aliased Mail Handler
====================

Background
----------

The stock mail handler classes included in JIRA aren't capable of issue
creation across the set of all projects using a single service config.
The commercial JEMH plugin is certainly capable, but is overkill for the
simple case.

Description
-----------

The Aliased Mail Handler supported use case is issue creation across all
projects.  The handler enables "routing" to specific projects by way of
a regular expression that is matched against the recipient's address.
This plugin works well for anyone using the relatively common aliasing
pattern "username+mailbox@domain.com" supported by providers like gmail
as well as SMTP servers like postfix.

The plugin doesn't do much more than that.  It's a very simple use case
and a very simple implementation.  Feel free to send me pull requests
for any enhancements here:

 * https://github.com/tripside/jira-mailhandler-aliased

