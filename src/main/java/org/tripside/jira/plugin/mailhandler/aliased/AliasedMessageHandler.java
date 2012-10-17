/*

Copyright (c) 2012, Mike Eldridge <diz@tripside.org>
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:

  - Redistributions of source code must retain the above copyright
    notice, this list of conditions and the following disclaimer.

  - Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions and the following disclaimer in the
    documentation and/or other materials provided with the distribution.

  - Neither the name of the <organization> nor the names of its
    contributors may be used to endorse or promote products derived from
    this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT
HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

***********************************************************************/

package org.tripside.jira.plugin.mailhandler.aliased;

import com.atlassian.crowd.embedded.api.User;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.CreateException;

import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.user.UserUtils;

import com.atlassian.jira.service.util.handler.MessageHandler;
import com.atlassian.jira.service.util.handler.MessageHandlerContext;
import com.atlassian.jira.service.util.handler.MessageHandlerErrorCollector;
import com.atlassian.jira.service.util.handler.MessageUserProcessor;

import com.atlassian.mail.MailUtils;

import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Message;
import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;

public class AliasedMessageHandler implements MessageHandler
{
	static final Logger log = Logger.getLogger(AliasedMessageHandler.class);

	private final MessageUserProcessor messageUserProcessor;

	public Pattern handledEmailAddressPattern;

	public AliasedMessageHandler (MessageUserProcessor messageUserProcessor)
	{
		this.messageUserProcessor = messageUserProcessor;
	}

	protected Project getProjectFromRecipientList (Address[] addresses)
	{
		final ProjectManager projectManager = ComponentAccessor.getProjectManager();

		Project project = null;

		for (Address address : addresses) {
			if (address instanceof InternetAddress) {
				InternetAddress emailAddress = (InternetAddress) address;
				Matcher matcher = handledEmailAddressPattern.matcher(emailAddress.getAddress());

				if (matcher.matches()) {
					String s = matcher.group(1);

					log.debug("MATCH on \"" + emailAddress.getAddress() + "\"");

					if (s != null && s.length() > 0) {
						log.debug("submatch is not zero-length");
						log.debug("attempting to lookup project with key \"" + s.toUpperCase(Locale.getDefault()) + "\"");

						project = projectManager.getProjectObjByKey(s.toUpperCase(Locale.getDefault()));

						if (project == null)
							log.debug("project not found");
						else
							break;
					}
				} else
					log.debug("NO MATCH on \"" + emailAddress.getAddress() + "\"");
			}
		}

		return project;
	}

	@Override
	public void init (Map <String, String> params, MessageHandlerErrorCollector monitor)
	{
		log.debug("CALLED init");

		if (params.containsKey("pattern"))
			handledEmailAddressPattern = Pattern.compile(params.get("pattern"));
	}

	@Override
	public boolean handleMessage (Message message, MessageHandlerContext context) throws MessagingException
	{
		log.debug("CALLED handleMessage");

		final Project project = getProjectFromRecipientList(message.getAllRecipients());

		if (project == null) {
			log.debug("unable to determine project from message");
			return false;
		}

		final User reporter = messageUserProcessor.getAuthorFromSender(message);

		if (reporter == null) {
			log.debug("unable to determine reporter from message");
			return false;
		} else
			log.debug("issue reporter will be \"" + reporter.getDisplayName() + "\"");

		IssueType issueType = ComponentAccessor.getIssueTypeSchemeManager().getDefaultValue(project.getGenericValue());

		if (issueType == null) {
			final Collection <IssueType> issueTypes = project.getIssueTypes();
			Iterator <IssueType> issueTypeIterator = issueTypes.iterator();

			while (issueTypeIterator.hasNext()) {
				issueType = issueTypeIterator.next();
				if (issueType.getName().equals("Bug"))
					break;
			}
		} else
			log.debug("got default IssueType from IssueTypeSchemeManager");

		log.debug("using IssueType \"" + issueType.getName() + "\"");

		MutableIssue muIssue = ComponentAccessor.getIssueFactory().getIssue();

		muIssue.setProjectObject(project);
		muIssue.setReporter(reporter);
		muIssue.setSummary(message.getSubject());
		muIssue.setDescription(MailUtils.getBody(message));
		muIssue.setIssueTypeId(issueType.getId());

		boolean success;

		try {
			context.createIssue(reporter, muIssue);
			success = true;
		} catch (CreateException e) {
			log.error("caught CreateException when attempting to create an issue: " + e.toString());
			success = false;
		} catch (Exception e) {
			log.error("caught Exception when attempting to create an issue: " + e.toString());
			success = false;
		}

		return success;
	}
}
